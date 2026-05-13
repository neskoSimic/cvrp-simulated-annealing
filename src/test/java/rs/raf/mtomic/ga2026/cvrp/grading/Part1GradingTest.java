package rs.raf.mtomic.ga2026.cvrp.grading;

import org.junit.jupiter.api.*;
import rs.raf.mtomic.ga2026.cvrp.evaluation.CostCalculator;
import rs.raf.mtomic.ga2026.cvrp.io.InstanceLoader;
import rs.raf.mtomic.ga2026.cvrp.model.Instance;
import rs.raf.mtomic.ga2026.cvrp.model.Solution;
import rs.raf.mtomic.ga2026.cvrp.model.ValidationResult;
import rs.raf.mtomic.ga2026.cvrp.solver.Solver;
import rs.raf.mtomic.ga2026.cvrp.student.StudentSolver;
import rs.raf.mtomic.ga2026.cvrp.validation.SolutionValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Part1GradingTest {

    private static final long TIME_LIMIT_MS = 60_000;
    private static final long GRACE_MS = 5_000;
    private static final int RUNS_PER_INSTANCE = 3;

    private static final String[] GRADED_INSTANCES = {
            "instances/part1/A-n44-k6.vrp",
            "instances/part1/A-n60-k9.vrp"
    };

    private static Map<String, Double> bestKnown;
    private static Map<String, Double> bestCosts;
    private static Map<String, ValidationResult> bestValidations;

    @BeforeAll
    static void solveAll() {
        bestKnown = loadProperties("instances/part1/best_known.properties");

        bestCosts = new LinkedHashMap<>();
        bestValidations = new LinkedHashMap<>();
        try {
            Solver solver = new StudentSolver();
            for (String path : GRADED_INSTANCES) {
                Instance instance = InstanceLoader.load(path);
                double bestCost = Double.MAX_VALUE;
                ValidationResult bestVr = null;

                for (int run = 0; run < RUNS_PER_INSTANCE; run++) {
                    long start = System.currentTimeMillis();
                    Solution solution = solver.solve(instance, TIME_LIMIT_MS);
                    long elapsed = System.currentTimeMillis() - start;

                    assertTrue(elapsed <= TIME_LIMIT_MS + GRACE_MS,
                            instance.getName() + " run " + (run + 1)
                                    + ": time limit exceeded (" + elapsed + "ms)");

                    ValidationResult vr = SolutionValidator.validate(instance, solution);
                    double cost = CostCalculator.calculate(instance, solution);

                    if (vr.isValid() && cost < bestCost) {
                        bestCost = cost;
                        bestVr = vr;
                    }
                    if (bestVr == null) {
                        bestVr = vr;
                    }
                }

                bestValidations.put(instance.getName(), bestVr);
                if (bestCost < Double.MAX_VALUE) {
                    bestCosts.put(instance.getName(), bestCost);
                }
            }
        } catch (Exception e) {
            // If solver throws, all tests will fail via empty maps
        }
    }

    @Test
    @Order(1)
    @DisplayName("Part 1 - Feasible Solution (5 points)")
    void testFeasible() {
        assertFalse(bestValidations.isEmpty(), "No solutions were produced");
        for (var entry : bestValidations.entrySet()) {
            assertTrue(entry.getValue().isValid(),
                    entry.getKey() + ": no feasible solution in " + RUNS_PER_INSTANCE
                            + " runs. Best result: " + entry.getValue());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Part 1 - Within 30% of Best Known (5 points)")
    void testWithin30Percent() {
        assertAllWithinGap(0.30);
    }

    @Test
    @Order(3)
    @DisplayName("Part 1 - Within 15% of Best Known (5 points)")
    void testWithin15Percent() {
        assertAllWithinGap(0.15);
    }

    @Test
    @Order(4)
    @DisplayName("Part 1 - Within 5% of Best Known (5 points)")
    void testWithin5Percent() {
        assertAllWithinGap(0.05);
    }

    private void assertAllWithinGap(double maxGap) {
        assertFalse(bestCosts.isEmpty(), "No feasible solutions were produced in " + RUNS_PER_INSTANCE + " runs");
        for (var entry : bestCosts.entrySet()) {
            String name = entry.getKey();
            double cost = entry.getValue();

            Double bks = bestKnown.get(name);
            assertNotNull(bks, "No best-known value for " + name);

            double gap = (cost - bks) / bks;
            assertTrue(gap <= maxGap,
                    String.format("%s: best gap %.1f%% (over %d runs) exceeds %.0f%% threshold (best cost=%.1f, BKS=%.0f)",
                            name, gap * 100, RUNS_PER_INSTANCE, maxGap * 100, cost, bks));
        }
    }

    private static Map<String, Double> loadProperties(String resourcePath) {
        Map<String, Double> map = new HashMap<>();
        InputStream is = Part1GradingTest.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) return map;
        Properties props = new Properties();
        try {
            props.load(is);
            is.close();
        } catch (IOException e) {
            return map;
        }
        for (String key : props.stringPropertyNames()) {
            map.put(key, Double.parseDouble(props.getProperty(key)));
        }
        return map;
    }
}
