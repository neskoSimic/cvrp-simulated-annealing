package rs.raf.mtomic.ga2026.cvrp.model;

import java.util.Objects;

public final class Customer {

    private final int id;
    private final double x;
    private final double y;
    private final int demand;

    public Customer(int id, double x, double y, int demand) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.demand = demand;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getDemand() {
        return demand;
    }

    public double distanceTo(Customer other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceTo(Depot depot) {
        double dx = this.x - depot.getX();
        double dy = this.y - depot.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer c)) return false;
        return id == c.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", x=" + x + ", y=" + y + ", demand=" + demand + "}";
    }
}
