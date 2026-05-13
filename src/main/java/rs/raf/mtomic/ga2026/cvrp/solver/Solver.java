package rs.raf.mtomic.ga2026.cvrp.solver;

import rs.raf.mtomic.ga2026.cvrp.model.Instance;
import rs.raf.mtomic.ga2026.cvrp.model.Solution;

public interface Solver {

    /**
     * Rešava datu CVRP instancu i vraća rešenje.
     *
     * @param instance    instanca problema
     * @param timeLimitMs maksimalno dozvoljeno vreme izvršavanja
     * @return izvodljivo rešenje
     */
    Solution solve(Instance instance, long timeLimitMs);
}
