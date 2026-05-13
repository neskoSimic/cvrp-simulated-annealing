package rs.raf.mtomic.ga2026.cvrp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Solution {

    private final List<Route> routes;

    public Solution(List<Route> routes) {
        this.routes = Collections.unmodifiableList(new ArrayList<>(routes));
    }

    public static Solution of(List<Route> routes) {
        return new Solution(routes);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public int getRouteCount() {
        return routes.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Solution with ").append(routes.size()).append(" routes:\n");
        for (int i = 0; i < routes.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(routes.get(i)).append("\n");
        }
        return sb.toString();
    }
}
