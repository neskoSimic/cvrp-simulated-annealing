package rs.raf.mtomic.ga2026.cvrp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Instance {

    private final String name;
    private final Depot depot;
    private final List<Customer> customers;
    private final List<VehicleType> vehicleTypes;
    private double[][] distanceMatrix;

    public Instance(String name, Depot depot, List<Customer> customers, List<VehicleType> vehicleTypes) {
        this.name = name;
        this.depot = depot;
        this.customers = Collections.unmodifiableList(new ArrayList<>(customers));
        this.vehicleTypes = Collections.unmodifiableList(new ArrayList<>(vehicleTypes));
    }

    public String getName() {
        return name;
    }

    public Depot getDepot() {
        return depot;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<VehicleType> getVehicleTypes() {
        return vehicleTypes;
    }

    /**
     * Vraća kapacitet vozila.
     */
    public int getVehicleCapacity() {
        return vehicleTypes.get(0).getCapacity();
    }

    /**
     * Vraća tip vozila.
     */
    public VehicleType getVehicleType() {
        return vehicleTypes.get(0);
    }

    public int getTotalVehicles() {
        int total = 0;
        for (VehicleType vt : vehicleTypes) {
            total += vt.getCount();
        }
        return total;
    }

    public int getCustomerCount() {
        return customers.size();
    }

    /**
     * Vraća unapred izračunatu matricu distanci. Na indeksu 0 je skladište,
     * indeksi 1..N odgovaraju kupcima (po redosledu u getCustomers()).
     * Korišćenje: distanceMatrix[i][j] daje distancu između čvora i i čvora j.
     */
    public double[][] getDistanceMatrix() {
        if (distanceMatrix == null) {
            distanceMatrix = computeDistanceMatrix();
        }
        return distanceMatrix;
    }

    /**
     * Vraća indeks kupca u matrici distanci (pomeraj za 1).
     * Indeks 0 je rezervisan za skladište.
     */
    public int getDistanceMatrixIndex(Customer customer) {
        int idx = customers.indexOf(customer);
        if (idx < 0) {
            throw new IllegalArgumentException("Customer " + customer.getId() + " not in this instance");
        }
        return idx + 1;
    }

    private double[][] computeDistanceMatrix() {
        int n = customers.size() + 1;
        double[][] matrix = new double[n][n];

        for (int i = 1; i < n; i++) {
            Customer ci = customers.get(i - 1);
            matrix[0][i] = depot.distanceTo(ci);
            matrix[i][0] = matrix[0][i];

            for (int j = i + 1; j < n; j++) {
                Customer cj = customers.get(j - 1);
                matrix[i][j] = ci.distanceTo(cj);
                matrix[j][i] = matrix[i][j];
            }
        }

        return matrix;
    }

    @Override
    public String toString() {
        return "Instance{name='" + name + "', customers=" + customers.size()
                + ", vehicleTypes=" + vehicleTypes + "}";
    }
}
