package hms;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * PriorityManager coordinates patient triage ordering using Java's PriorityQueue.
 * Orders patients deterministically by:
 * 1. Severity ascending (1 is highest priority: Critical)
 * 2. Arrival sequence ascending (tie-breaker)
 */
public class PriorityManager {

    private final Queue<AdmissionRequest> priorityQueue;

    public PriorityManager() {
        this.priorityQueue = new PriorityQueue<>((a, b) -> {
            int priorityCompare = Integer.compare(
                a.getPatient().getPriority(),
                b.getPatient().getPriority()
            );
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            return Long.compare(a.getArrivalSequence(), b.getArrivalSequence());
        });
    }

    public void addRequest(AdmissionRequest request) {
        priorityQueue.add(request);
    }

    public AdmissionRequest getNextRequest() {
        return priorityQueue.poll();
    }

    public AdmissionRequest peekNextRequest() {
        return priorityQueue.peek();
    }

    public boolean isEmpty() {
        return priorityQueue.isEmpty();
    }

    public int size() {
        return priorityQueue.size();
    }

    public Queue<AdmissionRequest> getQueue() {
        return priorityQueue;
    }

    public void displayQueue() {
        if (priorityQueue.isEmpty()) {
            System.out.println("\nPriority Queue is empty.");
            return;
        }

        PriorityQueue<AdmissionRequest> temp = new PriorityQueue<>(priorityQueue);
        System.out.println("\n========== PRIORITY QUEUE ==========");
        while (!temp.isEmpty()) {
            AdmissionRequest request = temp.poll();
            request.displayRequest();
        }
    }
}
