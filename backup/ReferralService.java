package hms;

import java.util.ArrayList;
import java.util.List;

/**
 * ReferralService coordinates inter-hospital patient transfers by validating
 * target bed availability and calculating optimal transfer routes via RoutePlanner.
 */
public class ReferralService {

    public static class DestinationCandidate {
        private final Hospital hospital;
        private final int availableCompatibleBeds;
        private final RoutePlanner.RouteResult route;

        public DestinationCandidate(Hospital hospital, int availableCompatibleBeds, RoutePlanner.RouteResult route) {
            this.hospital = hospital;
            this.availableCompatibleBeds = availableCompatibleBeds;
            this.route = route;
        }

        public Hospital getHospital() {
            return hospital;
        }

        public int getAvailableCompatibleBeds() {
            return availableCompatibleBeds;
        }

        public RoutePlanner.RouteResult getRoute() {
            return route;
        }

        public int getEstimatedMinutes() {
            return route != null ? route.getTotalMinutes() : 0;
        }
    }

    private final RoutePlanner routePlanner;

    public ReferralService(RoutePlanner routePlanner) {
        this.routePlanner = routePlanner != null ? routePlanner : new RoutePlanner();
    }

    public RoutePlanner getRoutePlanner() {
        return routePlanner;
    }

    /**
     * Identifies eligible destination hospitals with at least 1 available compatible bed.
     */
    public List<DestinationCandidate> findEligibleDestinations(String sourceHospitalId, 
                                                              String requiredBedType, 
                                                              List<Hospital> hospitals) {
        List<DestinationCandidate> candidates = new ArrayList<>();
        if (sourceHospitalId == null || hospitals == null) return candidates;

        boolean isIcu = requiredBedType != null && requiredBedType.toUpperCase().contains("ICU");

        for (Hospital h : hospitals) {
            // Cannot refer to same hospital
            if (h.getHospitalId().equalsIgnoreCase(sourceHospitalId)) continue;

            int availBeds = isIcu ? h.getAvailableICUBedsCount() : h.getAvailableGeneralBedsCount();
            if (availBeds > 0) {
                RoutePlanner.RouteResult route = routePlanner.findShortestPath(sourceHospitalId, h.getHospitalId());
                candidates.add(new DestinationCandidate(h, availBeds, route));
            }
        }

        // Sort candidates by lowest route travel time
        candidates.sort((a, b) -> Integer.compare(a.getEstimatedMinutes(), b.getEstimatedMinutes()));
        return candidates;
    }
}
