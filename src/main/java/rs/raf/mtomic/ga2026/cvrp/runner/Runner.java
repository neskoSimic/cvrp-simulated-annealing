package rs.raf.mtomic.ga2026.cvrp.runner;

import rs.raf.mtomic.ga2026.cvrp.evaluation.CostCalculator;
import rs.raf.mtomic.ga2026.cvrp.io.InstanceLoader;
import rs.raf.mtomic.ga2026.cvrp.model.Instance;
import rs.raf.mtomic.ga2026.cvrp.model.Solution;
import rs.raf.mtomic.ga2026.cvrp.model.ValidationResult;
import rs.raf.mtomic.ga2026.cvrp.solver.Solver;
import rs.raf.mtomic.ga2026.cvrp.student.StudentSolver;
import rs.raf.mtomic.ga2026.cvrp.validation.SolutionValidator;
import rs.raf.mtomic.ga2026.cvrp.visualization.SolutionVisualizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Runner {

    private static final long TIME_LIMIT_MS = 60_000;

    private static final String[] PART1_INSTANCES = {
            "instances/part1/A-n32-k5.vrp",
            "instances/part1/A-n44-k6.vrp",
            "instances/part1/A-n60-k9.vrp"
    };

    public static void main(String[] args) {
        runPart(new StudentSolver(), PART1_INSTANCES, "instances/part1/best_known.properties");
    }

    private static void runPart(Solver solver, String[] instancePaths, String bksPath) {
        Properties bks = loadBestKnown(bksPath);

        File outputDir = new File("build/visualizations");
        outputDir.mkdirs();

        System.out.println("=".repeat(80));
        System.out.printf("%-20s %12s %12s %10s %10s%n", "Instance", "Cost", "BKS", "Gap", "Time");
        System.out.println("=".repeat(80));

        for (String path : instancePaths) {
            Instance instance = InstanceLoader.load(path);

            long start = System.currentTimeMillis();
            Solution solution;
            try {
                solution = solver.solve(instance, TIME_LIMIT_MS);
            } catch (Exception e) {
                System.out.printf("%-20s ERROR: %s%n", instance.getName(), e.getMessage());
                continue;
            }
            long elapsed = System.currentTimeMillis() - start;

            ValidationResult validation = SolutionValidator.validate(instance, solution);
            double cost = CostCalculator.calculate(instance, solution);

            String bksValue = bks.getProperty(instance.getName());
            String gapStr = "N/A";
            if (bksValue != null) {
                double bestKnown = Double.parseDouble(bksValue);
                double gap = (cost - bestKnown) / bestKnown * 100;
                gapStr = String.format("%+.1f%%", gap);
            }

            String feasible = validation.isValid() ? "YES" : "NO";
            System.out.printf("%-20s %12.1f %12s %10s %8.1fs  Feasible: %s%n",
                    instance.getName(), cost, bksValue != null ? bksValue : "?", gapStr,
                    elapsed / 1000.0, feasible);

            if (!validation.isValid()) {
                for (String v : validation.getViolations()) {
                    System.out.println("  -> " + v);
                }
            }

            // Generate visualization
            String htmlFile = new File(outputDir, instance.getName() + ".html").getPath();
            try {
                SolutionVisualizer.toHtml(instance, solution, validation, cost, htmlFile);
                System.out.println("  -> Visualization: " + htmlFile);
            } catch (IOException e) {
                System.out.println("  -> Could not write visualization: " + e.getMessage());
            }
        }
        System.out.println("=".repeat(80));
        System.out.println("Open build/visualizations/*.html in a browser to see your routes.");
    }

    private static Properties loadBestKnown(String resourcePath) {
        Properties props = new Properties();
        InputStream is = Runner.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            try {
                props.load(is);
                is.close();
            } catch (IOException e) {
                System.err.println("Warning: could not load best known values from " + resourcePath);
            }
        }
        return props;
    }
}
