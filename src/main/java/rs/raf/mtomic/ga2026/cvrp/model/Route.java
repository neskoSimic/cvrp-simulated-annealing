package rs.raf.mtomic.ga2026.cvrp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Route {

    private final VehicleType vehicleType;
    private final List<Customer> customers;

    public Route(VehicleType vehicleType, List<Customer> customers) {
        this.vehicleType = vehicleType;
        this.customers = Collections.unmodifiableList(new ArrayList<>(customers));
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public int getTotalDemand() {
        int total = 0;
        for (Customer c : customers) {
            total += c.getDemand();
        }
        return total;
    }

    public int size() {
        return customers.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Route[").append(vehicleType.getName())
                .append(", demand=").append(getTotalDemand())
                .append("/").append(vehicleType.getCapacity())
                .append(", customers=");
        for (int i = 0; i < customers.size(); i++) {
            if (i > 0) sb.append("->");
            sb.append(customers.get(i).getId());
        }
        sb.append("]");
        return sb.toString();
    }
}
