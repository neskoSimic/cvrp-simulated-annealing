package rs.raf.mtomic.ga2026.cvrp.model;

public final class Depot {

    private final double x;
    private final double y;

    public Depot(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double distanceTo(Customer customer) {
        double dx = this.x - customer.getX();
        double dy = this.y - customer.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "Depot{x=" + x + ", y=" + y + "}";
    }
}
