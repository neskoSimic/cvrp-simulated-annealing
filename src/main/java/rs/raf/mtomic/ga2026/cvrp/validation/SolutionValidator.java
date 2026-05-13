package rs.raf.mtomic.ga2026.cvrp.validation;

import rs.raf.mtomic.ga2026.cvrp.model.*;

import java.util.*;

public final class SolutionValidator {

    private SolutionValidator() {
    }

    public static ValidationResult validate(Instance instance, Solution solution) {
        List<String> violations = new ArrayList<>();

        if (solution == null) {
            return ValidationResult.fail(List.of("Solution is null"));
        }

        if (solution.getRoutes() == null || solution.getRoutes().isEmpty()) {
            return ValidationResult.fail(List.of("Solution has no routes"));
        }

        Set<Integer> visitedIds = new HashSet<>();
        Map<String, Integer> vehicleTypeUsage = new HashMap<>();

        for (int i = 0; i < solution.getRoutes().size(); i++) {
            Route route = solution.getRoutes().get(i);
            String routeLabel = "Route " + (i + 1);

            if (route.getCustomers().isEmpty()) {
                violations.add(routeLabel + ": empty route");
                continue;
            }

            if (route.getVehicleType() == null) {
                violations.add(routeLabel + ": no vehicle type assigned");
                continue;
            }

            // Proveri kapacitet
            int demand = route.getTotalDemand();
            int cap = route.getVehicleType().getCapacity();
            if (demand > cap) {
                violations.add(routeLabel + ": demand " + demand + " exceeds capacity " + cap);
            }

            // Proveri broj korišćenih vozila
            String vtName = route.getVehicleType().getName();
            vehicleTypeUsage.merge(vtName, 1, Integer::sum);

            // Proveri jedinstvene posete kupcima
            for (Customer c : route.getCustomers()) {
                if (!visitedIds.add(c.getId())) {
                    violations.add(routeLabel + ": customer " + c.getId() + " visited more than once");
                }
            }
        }

        // Proveri da su svi kupci posećeni
        Set<Integer> expectedIds = new HashSet<>();
        for (Customer c : instance.getCustomers()) {
            expectedIds.add(c.getId());
        }
        Set<Integer> missing = new HashSet<>(expectedIds);
        missing.removeAll(visitedIds);
        if (!missing.isEmpty()) {
            violations.add("Customers not visited: " + missing);
        }

        Set<Integer> extra = new HashSet<>(visitedIds);
        extra.removeAll(expectedIds);
        if (!extra.isEmpty()) {
            violations.add("Unknown customer IDs in solution: " + extra);
        }

        // Proveri broj korišćenih vozila po tipu
        for (VehicleType vt : instance.getVehicleTypes()) {
            int used = vehicleTypeUsage.getOrDefault(vt.getName(), 0);
            if (used > vt.getCount()) {
                violations.add("Vehicle type '" + vt.getName() + "': used " + used
                        + " but only " + vt.getCount() + " available");
            }
        }

        if (violations.isEmpty()) {
            return ValidationResult.ok();
        }
        return ValidationResult.fail(violations);
    }
}
