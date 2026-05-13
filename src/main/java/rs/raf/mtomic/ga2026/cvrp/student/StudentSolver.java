package rs.raf.mtomic.ga2026.cvrp.student;

import rs.raf.mtomic.ga2026.cvrp.model.Instance;
import rs.raf.mtomic.ga2026.cvrp.model.Solution;
import rs.raf.mtomic.ga2026.cvrp.solver.Solver;

public class StudentSolver implements Solver {

    @Override
    public Solution solve(Instance instance, long timeLimitMs) {
        // TODO: Implementirajte algoritam za rešavanje CVRP ovde.
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

        throw new UnsupportedOperationException("Implement your CVRP solver!");
    }
}
