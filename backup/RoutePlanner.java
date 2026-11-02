package hms;

import java.util.*;

/**
 * RoutePlanner calculates the optimal inter-hospital transfer routes
 * using Dijkstra's Shortest Path Algorithm over a weighted graph of medical
 * facilities.
 */
public class RoutePlanner {

    public static class RouteResult {
        private final List<String> path;
        private final int totalMinutes;
        private final boolean found;

        public RouteResult(List<String> path, int totalMinutes, boolean found) {
            this.path = path != null ? path : Collections.emptyList();
            this.totalMinutes = totalMinutes;
            this.found = found;
        }

        public List<String> getPath() {
            return path;
        }

        public int getTotalMinutes() {
            return totalMinutes;
        }

        public boolean isFound() {
            return found;
        }

        public String getPathString() {
            if (!found || path.isEmpty())
                return "No route available";
            return String.join(" ➔ ", path);
        }
    }

    private static class Edge {
        final String target;
        final int weight;

        Edge(String target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    private final Map<String, List<Edge>> adjacencyList;

    public RoutePlanner() {
        this.adjacencyList = new HashMap<>();
        initializeDefaultNetwork();
    }

    public void addHospital(String hospitalId) {
        adjacencyList.putIfAbsent(hospitalId, new ArrayList<>());
    }

    public void addRoute(String hospitalA, String hospitalB, int travelMinutes) {
        addHospital(hospitalA);
        addHospital(hospitalB);
        adjacencyList.get(hospitalA).add(new Edge(hospitalB, travelMinutes));
        adjacencyList.get(hospitalB).add(new Edge(hospitalA, travelMinutes));
    }

    /**
     * Initializes default fictional inter-hospital transportation grid for
     * demonstration.
     */
    private void initializeDefaultNetwork() {
        // H001: Government General Hospital (District Center)
        // H002: Government Multi-Specialty Hospital (North Zone)
        // H003: District Medical College & Hospital (East Zone)
        // H004: City Emergency & Trauma Care Center (South Zone)
        // H005: Suburban Community Health Center (West Zone)
        addRoute("H001", "H002", 18);
        addRoute("H001", "H003", 25);
        addRoute("H001", "H004", 12);
        addRoute("H001", "H005", 35);
        addRoute("H002", "H003", 15);
        addRoute("H003", "H004", 22);
        addRoute("H004", "H005", 28);
        addRoute("H002", "H005", 40);
    }

    /**
     * Dijkstra's Shortest Path Algorithm
     */
    public RouteResult findShortestPath(String sourceId, String targetId) {
        if (sourceId == null || targetId == null) {
            return new RouteResult(Collections.emptyList(), 0, false);
        }
        if (sourceId.equalsIgnoreCase(targetId)) {
            return new RouteResult(Collections.singletonList(sourceId), 0, true);
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (String node : adjacencyList.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        if (!distances.containsKey(sourceId))
            distances.put(sourceId, Integer.MAX_VALUE);
        if (!distances.containsKey(targetId))
            distances.put(targetId, Integer.MAX_VALUE);

        distances.put(sourceId, 0);
        pq.add(sourceId);

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (visited.contains(current))
                continue;
            visited.add(current);

            if (current.equalsIgnoreCase(targetId))
                break;

            List<Edge> neighbors = adjacencyList.getOrDefault(current, Collections.emptyList());
            for (Edge edge : neighbors) {
                if (visited.contains(edge.target))
                    continue;

                int currentDist = distances.getOrDefault(current, Integer.MAX_VALUE);
                int newDist = currentDist + edge.weight;
                int existingDist = distances.getOrDefault(edge.target, Integer.MAX_VALUE);

                if (newDist < existingDist) {
                    distances.put(edge.target, newDist);
                    predecessors.put(edge.target, current);
                    pq.remove(edge.target);
                    pq.add(edge.target);
                }
            }
        }

        if (distances.getOrDefault(targetId, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
            return new RouteResult(Collections.emptyList(), 0, false);
        }

        // Reconstruct path
        LinkedList<String> path = new LinkedList<>();
        String step = targetId;
        while (step != null) {
            path.addFirst(step);
            step = predecessors.get(step);
        }

        return new RouteResult(path, distances.get(targetId), true);
    }
}
