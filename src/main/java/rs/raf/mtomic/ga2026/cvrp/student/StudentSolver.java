package rs.raf.mtomic.ga2026.cvrp.student;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rs.raf.mtomic.ga2026.cvrp.model.Customer;
import rs.raf.mtomic.ga2026.cvrp.model.Instance;
import rs.raf.mtomic.ga2026.cvrp.model.Route;
import rs.raf.mtomic.ga2026.cvrp.model.RouteBuilder;
import rs.raf.mtomic.ga2026.cvrp.model.Solution;
import rs.raf.mtomic.ga2026.cvrp.solver.Solver;

public class StudentSolver implements Solver {

    private double[][] d;     // matrica distanci, 0 = skladište, 1..n = kupci
    private int[] dem;        // potražnja, indeks 1..n
    private int cap;          // kapacitet vozila
    private int n;            // broj kupaca
    private int maxVehicles;  // maksimalan broj ruta
    private long deadline;
    private final Random rnd = new Random(123456789L);
        //
        // Dostupni podaci:
        //   instance.getCustomers()      - lista svih kupaca koje treba posetiti
        //   instance.getDepot()          - lokacija skladišta
        //   instance.getVehicleCapacity() - kapacitet vozila (isti za sva vozila)
        //   instance.getVehicleType()    - tip vozila (za izgradnju ruta)
        //   instance.getDistanceMatrix() - matrica distance, unapred izračunata [0=skladište, 1..N=kupci]
        //
        // Izgradnja rešenja:
        //   RouteBuilder rb = new RouteBuilder(instance.getVehicleType());
        //   rb.add(customer);             - dodaj kupca na rutu (na kraj)
        //   rb.canAdd(customer);          - proveri da li kupac odgovara trenutno dostupnom kapacitetu vozila
        //   rb.getRemainingCapacity();    - koliki je preostali kapacitet vozila na ovoj ruti
        //   Route route = rb.build();     - dovrši rutu
        //   return Solution.of(routes);   - kreiraj rešenje od liste ruta
        //
        // Praćenje vremena:
        //   long deadline = System.currentTimeMillis() + timeLimitMs;
        //   while (System.currentTimeMillis() < deadline) { ... }

    @Override
    public Solution solve(Instance instance, long timeLimitMs) {
        long start = System.currentTimeMillis();
        long deadline = start + timeLimitMs - 500; // rezerva za sastavljanje rešenja

        List<Customer> custs = instance.getCustomers();
        n = custs.size();
        cap = instance.getVehicleCapacity();
        maxVehicles = instance.getVehicleType().getCount();

        // Matrica distanci preko distanceTo() — nema zabune oko indeksiranja
        var depot = instance.getDepot();
        d = new double[n + 1][n + 1];
        dem = new int[n + 1];
        for (int i = 0; i < n; i++) {
            Customer ci = custs.get(i);
            dem[i + 1] = ci.getDemand();
            double dd = ci.distanceTo(depot);
            d[0][i + 1] = dd;
            d[i + 1][0] = dd;
            for (int j = i + 1; j < n; j++) {
                double dij = ci.distanceTo(custs.get(j));
                d[i + 1][j + 1] = dij;
                d[j + 1][i + 1] = dij;
            }
        }

        //Početno rešenje: najbliži sused 
        List<List<Integer>> routes = nearestNeighbor();
        enforceVehicleLimit(routes);

        List<List<Integer>> best = copy(routes);
        double bestCost = totalDist(best);

        //  Simulirano kaljenje (3 "restarta" radi stabilnosti) 
        int segments = 3;
        long totalTime = Math.max(1, deadline - System.currentTimeMillis());
        long segLen = totalTime / segments;

        for (int seg = 0; seg < segments; seg++) {
            long segStart = System.currentTimeMillis();
            long segEnd = Math.min(deadline, segStart + segLen);
            if (segStart >= deadline) break;

            // Svaki segment kreće od najboljeg do sada, sa sve nižom početnom temperaturom
            routes = copy(best);
            List<Integer> loads = loadsList(routes);
            double cur = totalDist(routes);

            double t0 = Math.max(1e-6, bestCost * 0.03 / (seg + 1)); // početna temperatura
            double tEnd = Math.max(1e-9, bestCost * 1e-5);           // krajnja temperatura
            double T = t0;
            long segDur = Math.max(1, segEnd - segStart);
            long iter = 0;

            while (true) {
                // Vreme i hlađenje proveravamo na svakih 1024 iteracije (brže)
                if ((iter++ & 1023L) == 0L) {
                    long now = System.currentTimeMillis();
                    if (now >= segEnd) break;
                    double progress = (double) (now - segStart) / segDur;
                    T = t0 * Math.pow(tEnd / t0, progress); // geometrijsko hlađenje
                }

                cur += tryRandomMove(routes, loads, T);

                if (cur < bestCost - 1e-9 && routes.size() <= maxVehicles) {
                    double real = totalDist(routes); // tačan trošak (bez numeričkog drifta)
                    if (real < bestCost - 1e-9) {
                        bestCost = real;
                        best = copy(routes);
                    }
                    cur = real;
                }
            }
        }

        // Sastavi Solution iz najboljeg rešenja 
        List<Route> result = new ArrayList<>();
        for (List<Integer> r : best) {
            if (r.isEmpty()) continue;
            RouteBuilder rb = new RouteBuilder(instance.getVehicleType());
            for (int c : r) rb.add(custs.get(c - 1));
            result.add(rb.build());
        }
        return Solution.of(result);
    }

    
    // Početno rešenje: najbliži sused uz poštovanje kapaciteta
    private List<List<Integer>> nearestNeighbor() {
        boolean[] visited = new boolean[n + 1];
        List<List<Integer>> routes = new ArrayList<>();
        int remaining = n;

        while (remaining > 0) {
            List<Integer> route = new ArrayList<>();
            int load = 0;
            int pos = 0; // krećemo iz skladišta
            while (true) {
                int bestC = -1;
                double bestD = Double.MAX_VALUE;
                for (int c = 1; c <= n; c++) {
                    if (!visited[c] && load + dem[c] <= cap && d[pos][c] < bestD) {
                        bestD = d[pos][c];
                        bestC = c;
                    }
                }
                if (bestC < 0) break; // niko više ne staje — vrati se u skladište
                route.add(bestC);
                visited[bestC] = true;
                load += dem[bestC];
                pos = bestC;
                remaining--;
            }
            routes.add(route);
        }
        return routes;
    }

    /** Ako početno rešenje ima više ruta nego vozila, spajaj najjeftinije izvodljive parove. */
    private void enforceVehicleLimit(List<List<Integer>> routes) {
        while (routes.size() > maxVehicles) {
            int[] load = new int[routes.size()];
            for (int i = 0; i < routes.size(); i++)
                for (int c : routes.get(i)) load[i] += dem[c];

            double best = Double.MAX_VALUE;
            int ba = -1, bb = -1;
            for (int a = 0; a < routes.size(); a++)
                for (int b = 0; b < routes.size(); b++) {
                    if (a == b || load[a] + load[b] > cap) continue;
                    List<Integer> A = routes.get(a), B = routes.get(b);
                    int lastA = A.get(A.size() - 1), firstB = B.get(0);
                    double c = d[lastA][firstB] - d[lastA][0] - d[0][firstB];
                    if (c < best) { best = c; ba = a; bb = b; }
                }
            if (ba < 0) return; // nije moguće dalje spajati
            routes.get(ba).addAll(routes.get(bb));
            routes.remove(bb);
        }
    }

    // Simulirano kaljenje: nasumični potezi
    
    /**
     * Napravi jedan nasumičan potez. Vraća primenjenu delta promenu troška
     * (0 ako je potez odbijen ili nije bio moguć).
     */
    private double tryRandomMove(List<List<Integer>> routes, List<Integer> loads, double T) {
        int type = rnd.nextInt(4);
        if (type <= 1) return moveRelocate(routes, loads, T); // relocate ima duplu šansu
        if (type == 2) return moveSwap(routes, loads, T);
        return moveTwoOpt(routes, T);
    }

    /** Metropolisov kriterijum prihvatanja: bolje uvek, gore sa verovatnoćom e^(-delta/T). */
    private boolean accept(double delta, double T) {
        if (delta <= 0) return true;
        return rnd.nextDouble() < Math.exp(-delta / T);
    }

    /** RELOCATE: premesti jednog nasumičnog kupca na nasumičnu poziciju (ista ili druga ruta). */
    private double moveRelocate(List<List<Integer>> routes, List<Integer> loads, double T) {
        if (routes.isEmpty()) return 0;
        int ra = rnd.nextInt(routes.size());
        List<Integer> A = routes.get(ra);
        if (A.isEmpty()) { routes.remove(ra); loads.remove(ra); return 0; }

        int i = rnd.nextInt(A.size());
        int a = A.get(i);

        // Povremeno (2%) probaj otvaranje potpuno nove rute, ako ima slobodnih vozila
        boolean newRoute = A.size() > 1 && routes.size() < maxVehicles && rnd.nextInt(100) < 2;

        int rb = -1, j = 0;
        List<Integer> B = null;
        if (!newRoute) {
            rb = rnd.nextInt(routes.size());
            B = routes.get(rb);
            j = rnd.nextInt(B.size() + 1);
            if (rb == ra && (j == i || j == i + 1)) return 0;          // isto mesto, nema poteza
            if (rb != ra && loads.get(rb) + dem[a] > cap) return 0;    // ne staje u kapacitet
        }

        int prev = (i == 0) ? 0 : A.get(i - 1);
        int next = (i == A.size() - 1) ? 0 : A.get(i + 1);
        double removeGain = d[prev][a] + d[a][next] - d[prev][next];

        double insertCost;
        if (newRoute) {
            insertCost = d[0][a] + d[a][0];
        } else {
            int p = (j == 0) ? 0 : B.get(j - 1);
            int q = (j == B.size()) ? 0 : B.get(j);
            insertCost = d[p][a] + d[a][q] - d[p][q];
        }

        double delta = insertCost - removeGain;
        if (!accept(delta, T)) return 0;

        // Primeni potez
        A.remove(i);
        if (newRoute) {
            List<Integer> nr = new ArrayList<>();
            nr.add(a);
            routes.add(nr);
            loads.add(dem[a]);
        } else {
            int jj = j;
            if (rb == ra && jj > i) jj--; // indeksi se pomeraju nakon uklanjanja
            B.add(jj, a);
            loads.set(rb, loads.get(rb) + dem[a]);
        }
        loads.set(ra, loads.get(ra) - dem[a]);
        if (A.isEmpty()) {
            routes.remove(ra);
            loads.remove(ra);
        }
        return delta;
    }

    /** SWAP: zameni po jednog nasumičnog kupca između dve različite rute. */
    private double moveSwap(List<List<Integer>> routes, List<Integer> loads, double T) {
        if (routes.size() < 2) return 0;
        int ra = rnd.nextInt(routes.size());
        int rb = rnd.nextInt(routes.size());
        if (ra == rb) return 0;
        List<Integer> A = routes.get(ra), B = routes.get(rb);
        if (A.isEmpty() || B.isEmpty()) return 0;

        int i = rnd.nextInt(A.size()), j = rnd.nextInt(B.size());
        int a = A.get(i), b = B.get(j);
        if (loads.get(ra) - dem[a] + dem[b] > cap) return 0;
        if (loads.get(rb) - dem[b] + dem[a] > cap) return 0;

        int pa = (i == 0) ? 0 : A.get(i - 1);
        int na = (i == A.size() - 1) ? 0 : A.get(i + 1);
        int pb = (j == 0) ? 0 : B.get(j - 1);
        int nb = (j == B.size() - 1) ? 0 : B.get(j + 1);

        double delta = d[pa][b] + d[b][na] - d[pa][a] - d[a][na]
                + d[pb][a] + d[a][nb] - d[pb][b] - d[b][nb];
        if (!accept(delta, T)) return 0;

        A.set(i, b);
        B.set(j, a);
        loads.set(ra, loads.get(ra) - dem[a] + dem[b]);
        loads.set(rb, loads.get(rb) - dem[b] + dem[a]);
        return delta;
    }

    /** 2-OPT: obrni nasumičan segment unutar jedne rute (uklanja "ukrštene" putanje). */
    private double moveTwoOpt(List<List<Integer>> routes, double T) {
        if (routes.isEmpty()) return 0;
        List<Integer> r = routes.get(rnd.nextInt(routes.size()));
        int m = r.size();
        if (m < 3) return 0;

        int i = rnd.nextInt(m - 1);
        int k = i + 1 + rnd.nextInt(m - i - 1);

        int a = (i == 0) ? 0 : r.get(i - 1);
        int b = r.get(i);
        int c = r.get(k);
        int e = (k == m - 1) ? 0 : r.get(k + 1);

        double delta = d[a][c] + d[b][e] - d[a][b] - d[c][e];
        if (!accept(delta, T)) return 0;

        for (int x = i, y = k; x < y; x++, y--) {
            int tmp = r.get(x);
            r.set(x, r.get(y));
            r.set(y, tmp);
        }
        return delta;
    }

    // Pomoćne metode
    private List<Integer> loadsList(List<List<Integer>> routes) {
        List<Integer> loads = new ArrayList<>(routes.size());
        for (List<Integer> r : routes) {
            int l = 0;
            for (int c : r) l += dem[c];
            loads.add(l);
        }
        return loads;
    }

    private double totalDist(List<List<Integer>> routes) {
        double s = 0;
        for (List<Integer> r : routes) {
            int prev = 0;
            for (int c : r) { s += d[prev][c]; prev = c; }
            s += d[prev][0];
        }
        return s;
    }

    private List<List<Integer>> copy(List<List<Integer>> routes) {
        List<List<Integer>> c = new ArrayList<>(routes.size());
        for (List<Integer> r : routes) c.add(new ArrayList<>(r));
        return c;
    }
}