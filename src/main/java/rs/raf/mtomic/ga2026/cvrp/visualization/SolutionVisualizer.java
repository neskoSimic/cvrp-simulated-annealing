package rs.raf.mtomic.ga2026.cvrp.visualization;

import rs.raf.mtomic.ga2026.cvrp.evaluation.CostCalculator;
import rs.raf.mtomic.ga2026.cvrp.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public final class SolutionVisualizer {

    private static final String[] ROUTE_COLORS = {
            "#e6194b", "#3cb44b", "#4363d8", "#f58231", "#911eb4",
            "#42d4f4", "#f032e6", "#bfef45", "#fabed4", "#469990",
            "#dcbeff", "#9A6324", "#800000", "#aaffc3", "#808000",
            "#ffd8b1", "#000075", "#a9a9a9"
    };

    private SolutionVisualizer() {
    }

    public static void toHtml(Instance instance, Solution solution, ValidationResult validation,
                              double cost, String filePath) throws IOException {
        List<Customer> customers = instance.getCustomers();
        Depot depot = instance.getDepot();

        double minX = depot.getX(), maxX = depot.getX();
        double minY = depot.getY(), maxY = depot.getY();
        for (Customer c : customers) {
            minX = Math.min(minX, c.getX());
            maxX = Math.max(maxX, c.getX());
            minY = Math.min(minY, c.getY());
            maxY = Math.max(maxY, c.getY());
        }

        double padding = Math.max(maxX - minX, maxY - minY) * 0.08;
        minX -= padding;
        minY -= padding;
        maxX += padding;
        maxY += padding;

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        int svgW = 800;
        int svgH = (int) (svgW * rangeY / rangeX);
        if (svgH < 400) svgH = 400;

        try (PrintWriter w = new PrintWriter(new FileWriter(filePath))) {
            w.println("<!DOCTYPE html><html><head><meta charset='utf-8'>");
            w.println("<title>" + instance.getName() + " — CVRP Solution</title>");
            w.println("<style>");
            w.println("body { font-family: sans-serif; margin: 20px; background: #f5f5f5; }");
            w.println(".container { display: flex; gap: 20px; flex-wrap: wrap; }");
            w.println(".map { background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 10px; }");
            w.println(".info { background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 20px; min-width: 280px; }");
            w.println(".route-row { display: flex; align-items: center; gap: 8px; margin: 6px 0; font-size: 14px; }");
            w.println(".color-dot { width: 14px; height: 14px; border-radius: 50%; flex-shrink: 0; }");
            w.printf(".valid { color: #2e7d32; font-weight: bold; } .invalid { color: #c62828; font-weight: bold; }%n");
            w.println("h1 { margin: 0 0 10px 0; font-size: 22px; } h2 { margin: 16px 0 8px 0; font-size: 16px; }");
            w.println("</style></head><body>");

            w.printf("<h1>%s</h1>%n", instance.getName());
            w.println("<div class='container'>");

            // SVG mapa
            w.println("<div class='map'>");
            w.printf("<svg width='%d' height='%d' viewBox='0 0 %d %d'>%n", svgW, svgH, svgW, svgH);
            w.printf("<rect width='%d' height='%d' fill='white'/>%n", svgW, svgH);

            // Crtanje ruta
            List<Route> routes = solution.getRoutes();
            for (int r = 0; r < routes.size(); r++) {
                Route route = routes.get(r);
                String color = ROUTE_COLORS[r % ROUTE_COLORS.length];

                // Skladište -> prvi kupac
                if (!route.getCustomers().isEmpty()) {
                    Customer first = route.getCustomers().get(0);
                    Customer last = route.getCustomers().get(route.getCustomers().size() - 1);
                    int dx = toSvgX(depot.getX(), minX, rangeX, svgW);
                    int dy = toSvgY(depot.getY(), minY, rangeY, svgH);

                    // Od skladišta do prvog
                    w.printf("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='2' opacity='0.7'/>%n",
                            dx, dy,
                            toSvgX(first.getX(), minX, rangeX, svgW),
                            toSvgY(first.getY(), minY, rangeY, svgH),
                            color);

                    // Od kupca do kupca
                    for (int i = 0; i < route.getCustomers().size() - 1; i++) {
                        Customer a = route.getCustomers().get(i);
                        Customer b = route.getCustomers().get(i + 1);
                        w.printf("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='2' opacity='0.7'/>%n",
                                toSvgX(a.getX(), minX, rangeX, svgW),
                                toSvgY(a.getY(), minY, rangeY, svgH),
                                toSvgX(b.getX(), minX, rangeX, svgW),
                                toSvgY(b.getY(), minY, rangeY, svgH),
                                color);
                    }

                    // Od poslednjeg do skladišta
                    w.printf("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='2' opacity='0.7'/>%n",
                            toSvgX(last.getX(), minX, rangeX, svgW),
                            toSvgY(last.getY(), minY, rangeY, svgH),
                            dx, dy, color);
                }
            }

            // Crtanje kupaca
            for (Customer c : customers) {
                int cx = toSvgX(c.getX(), minX, rangeX, svgW);
                int cy = toSvgY(c.getY(), minY, rangeY, svgH);
                w.printf("<circle cx='%d' cy='%d' r='5' fill='#333' stroke='white' stroke-width='1'/>%n", cx, cy);
                w.printf("<text x='%d' y='%d' font-size='9' fill='#666' text-anchor='middle'>%d</text>%n",
                        cx, cy - 8, c.getId());
            }

            // Crtanje skladišta
            int depotSvgX = toSvgX(depot.getX(), minX, rangeX, svgW);
            int depotSvgY = toSvgY(depot.getY(), minY, rangeY, svgH);
            w.printf("<rect x='%d' y='%d' width='12' height='12' fill='red' stroke='white' stroke-width='2'/>%n",
                    depotSvgX - 6, depotSvgY - 6);
            w.printf("<text x='%d' y='%d' font-size='11' fill='red' font-weight='bold' text-anchor='middle'>DEPOT</text>%n",
                    depotSvgX, depotSvgY - 10);

            w.println("</svg></div>");

            // Info
            w.println("<div class='info'>");
            w.printf("<p><b>Cost:</b> %.1f</p>%n", cost);
            w.printf("<p><b>Feasible:</b> <span class='%s'>%s</span></p>%n",
                    validation.isValid() ? "valid" : "invalid",
                    validation.isValid() ? "YES" : "NO — " + validation);
            w.printf("<p><b>Routes:</b> %d</p>%n", routes.size());
            w.printf("<p><b>Customers:</b> %d</p>%n", customers.size());

            w.println("<h2>Routes</h2>");
            for (int r = 0; r < routes.size(); r++) {
                Route route = routes.get(r);
                String color = ROUTE_COLORS[r % ROUTE_COLORS.length];
                double routeDist = CostCalculator.calculateRouteDistance(depot, route);
                double routeCost = routeDist * route.getVehicleType().getCostPerUnit();

                w.printf("<div class='route-row'><div class='color-dot' style='background:%s'></div>", color);
                w.printf("<span><b>%s</b> — %d customers, demand %d/%d, cost %.1f</span></div>%n",
                        route.getVehicleType().getName(),
                        route.size(),
                        route.getTotalDemand(),
                        route.getVehicleType().getCapacity(),
                        routeCost);
            }

            w.println("</div></div></body></html>");
        }
    }

    private static int toSvgX(double x, double minX, double rangeX, int svgW) {
        return (int) ((x - minX) / rangeX * (svgW - 20)) + 10;
    }

    private static int toSvgY(double y, double minY, double rangeY, int svgH) {
        // Flip Y axis so higher Y is at the top
        return svgH - (int) ((y - minY) / rangeY * (svgH - 20)) - 10;
    }
}
