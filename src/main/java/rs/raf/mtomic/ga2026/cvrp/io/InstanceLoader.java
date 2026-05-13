package rs.raf.mtomic.ga2026.cvrp.io;

import rs.raf.mtomic.ga2026.cvrp.model.Customer;
import rs.raf.mtomic.ga2026.cvrp.model.Depot;
import rs.raf.mtomic.ga2026.cvrp.model.Instance;
import rs.raf.mtomic.ga2026.cvrp.model.VehicleType;

import java.io.*;
import java.util.*;

public final class InstanceLoader {

    private InstanceLoader() {
    }

    /**
     * Učitava CVRP instancu iz .vrp fajla sa classpath.
     * Podržava standardni CVRPLIB format.
     */
    public static Instance load(String resourcePath) {
        InputStream is = InstanceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException("Instance not found on classpath: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return parse(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load instance: " + resourcePath, e);
        }
    }

    /**
     * Učitava CVRP instancu iz fajla na datoj putanji.
     */
    public static Instance loadFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return parse(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load instance: " + filePath, e);
        }
    }

    private static Instance parse(BufferedReader reader) throws IOException {
        String name = "";
        int capacity = -1;
        int dimension = -1;
        Map<Integer, double[]> coords = new LinkedHashMap<>();
        Map<Integer, Integer> demands = new LinkedHashMap<>();
        int depotId = -1;
        List<VehicleType> vehicleTypes = new ArrayList<>();

        String line;
        String section = "";

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("NAME")) {
                name = extractValue(line);
                continue;
            }
            if (line.startsWith("DIMENSION")) {
                dimension = Integer.parseInt(extractValue(line));
                continue;
            }
            if (line.startsWith("CAPACITY")) {
                capacity = Integer.parseInt(extractValue(line));
                continue;
            }
            if (line.startsWith("EDGE_WEIGHT_TYPE") || line.startsWith("TYPE") || line.startsWith("COMMENT")) {
                continue;
            }

            if (line.equals("NODE_COORD_SECTION")) {
                section = "COORDS";
                continue;
            }
            if (line.equals("DEMAND_SECTION")) {
                section = "DEMAND";
                continue;
            }
            if (line.equals("DEPOT_SECTION")) {
                section = "DEPOT";
                continue;
            }
            if (line.equals("VEHICLE_TYPE_SECTION")) {
                section = "VEHICLE_TYPE";
                continue;
            }
            if (line.equals("EOF")) {
                break;
            }

            switch (section) {
                case "COORDS" -> {
                    String[] parts = line.split("\\s+");
                    int id = Integer.parseInt(parts[0]);
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    coords.put(id, new double[]{x, y});
                }
                case "DEMAND" -> {
                    String[] parts = line.split("\\s+");
                    int id = Integer.parseInt(parts[0]);
                    int demand = Integer.parseInt(parts[1]);
                    demands.put(id, demand);
                }
                case "DEPOT" -> {
                    int id = Integer.parseInt(line);
                    if (id != -1 && depotId == -1) {
                        depotId = id;
                    }
                }
                case "VEHICLE_TYPE" -> {
                    // Format: name capacity costPerUnit count
                    String[] parts = line.split("\\s+");
                    String vtName = parts[0];
                    int vtCapacity = Integer.parseInt(parts[1]);
                    double vtCost = Double.parseDouble(parts[2]);
                    int vtCount = Integer.parseInt(parts[3]);
                    vehicleTypes.add(new VehicleType(vtName, vtCapacity, vtCost, vtCount));
                }
            }
        }

        if (depotId == -1) {
            depotId = 1;
        }

        double[] depotCoords = coords.get(depotId);
        if (depotCoords == null) {
            throw new IllegalStateException("Depot node " + depotId + " not found in coordinates");
        }
        Depot depot = new Depot(depotCoords[0], depotCoords[1]);

        List<Customer> customers = new ArrayList<>();
        for (Map.Entry<Integer, double[]> entry : coords.entrySet()) {
            int id = entry.getKey();
            if (id == depotId) continue;
            double[] xy = entry.getValue();
            int demand = demands.getOrDefault(id, 0);
            customers.add(new Customer(id, xy[0], xy[1], demand));
        }

        if (vehicleTypes.isEmpty()) {
            // Za homogenu flotu: odredi broj vozila iz naziva (npr., "A-n32-k5" -> k=5)
            int vehicleCount = inferVehicleCount(name, customers, capacity);
            vehicleTypes.add(new VehicleType("default", capacity, 1.0, vehicleCount));
        }

        return new Instance(name, depot, customers, vehicleTypes);
    }

    private static String extractValue(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex >= 0) {
            return line.substring(colonIndex + 1).trim();
        }
        String[] parts = line.split("\\s+", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

    private static int inferVehicleCount(String name, List<Customer> customers, int capacity) {
        // Probaj da izdvojiš k iz naziva "A-n32-k5"
        int kIndex = name.lastIndexOf("-k");
        if (kIndex >= 0) {
            try {
                return Integer.parseInt(name.substring(kIndex + 2));
            } catch (NumberFormatException ignored) {
            }
        }
        // Ako ne uspe: ceil(totalDemand / capacity)
        int totalDemand = 0;
        for (Customer c : customers) {
            totalDemand += c.getDemand();
        }
        return (int) Math.ceil((double) totalDemand / capacity);
    }
}
