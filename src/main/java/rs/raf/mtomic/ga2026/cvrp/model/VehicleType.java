package rs.raf.mtomic.ga2026.cvrp.model;

public final class VehicleType {

    private final String name;
    private final int capacity;
    private final double costPerUnit;
    private final int count;

    public VehicleType(String name, int capacity, double costPerUnit, int count) {
        this.name = name;
        this.capacity = capacity;
        this.costPerUnit = costPerUnit;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getCostPerUnit() {
        return costPerUnit;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "VehicleType{name='" + name + "', capacity=" + capacity
                + ", costPerUnit=" + costPerUnit + ", count=" + count + "}";
    }
}
