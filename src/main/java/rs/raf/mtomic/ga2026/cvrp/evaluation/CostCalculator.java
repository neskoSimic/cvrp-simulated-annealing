package rs.raf.mtomic.ga2026.cvrp.evaluation;

import rs.raf.mtomic.ga2026.cvrp.model.*;

public final class CostCalculator {

    private CostCalculator() {
    }

    /**
     * Računa ukupan trošak rešenja.
     * Za svaku rutu, suma: distance * vehicleType.costPerUnit.
     * Distanca rute = skladište -> c1 -> c2 -> ... -> cN -> skladište.
     */
    public static double calculate(Instance instance, Solution solution) {
        double totalCost = 0;
        Depot depot = instance.getDepot();

        for (Route route : solution.getRoutes()) {
            double routeDistance = calculateRouteDistance(depot, route);
            totalCost += routeDistance * route.getVehicleType().getCostPerUnit();
        }

        return totalCost;
    }

    /**
     * Računa distancu jedne rute (skladište -> kupci -> skladište).
     */
    public static double calculateRouteDistance(Depot depot, Route route) {
        if (route.getCustomers().isEmpty()) return 0;

        double distance = 0;
        Customer prev = null;

        for (Customer c : route.getCustomers()) {
            if (prev == null) {
                distance += depot.distanceTo(c);
            } else {
                distance += prev.distanceTo(c);
            }
            prev = c;
        }

        distance += prev.distanceTo(depot);
        return distance;
    }
}
