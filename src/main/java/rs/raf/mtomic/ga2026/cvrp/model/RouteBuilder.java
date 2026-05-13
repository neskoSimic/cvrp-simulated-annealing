package rs.raf.mtomic.ga2026.cvrp.model;

import java.util.ArrayList;
import java.util.List;

public final class RouteBuilder {

    private final VehicleType vehicleType;
    private final List<Customer> customers = new ArrayList<>();

    public RouteBuilder(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public RouteBuilder add(Customer customer) {
        customers.add(customer);
        return this;
    }

    public RouteBuilder addAll(List<Customer> customers) {
        this.customers.addAll(customers);
        return this;
    }

    public int getCurrentDemand() {
        int total = 0;
        for (Customer c : customers) {
            total += c.getDemand();
        }
        return total;
    }

    public int getRemainingCapacity() {
        return vehicleType.getCapacity() - getCurrentDemand();
    }

    public boolean canAdd(Customer customer) {
        return getCurrentDemand() + customer.getDemand() <= vehicleType.getCapacity();
    }

    public Route build() {
        return new Route(vehicleType, customers);
    }
}
