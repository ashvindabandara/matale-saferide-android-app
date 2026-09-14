package lk.matale.saferide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure Java model that stores the demonstration trip state.
 * Keeping this class Android-free makes its core behaviour easy to unit-test.
 */
public class SchoolTransportState {

    public enum ChildStatus {
        WAITING,
        PICKED_UP,
        DROPPED_OFF
    }

    public static class Child {
        private final String name;
        private final String stop;
        private ChildStatus status;

        public Child(String name, String stop) {
            this.name = name;
            this.stop = stop;
            this.status = ChildStatus.WAITING;
        }

        public String getName() { return name; }
        public String getStop() { return stop; }
        public ChildStatus getStatus() { return status; }
        public void setStatus(ChildStatus status) { this.status = status; }
    }

    private boolean tripActive;
    private String delayMessage;
    private String emergencyMessage;
    private final List<Child> children = new ArrayList<>();

    public SchoolTransportState() {
        children.add(new Child("Nethmi Perera", "Pallepola Junction"));
        children.add(new Child("Kavindu Silva", "Ukuwela Road"));
        children.add(new Child("Ayeshani Fernando", "Matale Town"));
        children.add(new Child("Dinuka Jayasinghe", "School Gate"));
    }

    /** Starts a new journey and resets all per-trip student states. */
    public void startTrip() {
        tripActive = true;
        delayMessage = null;
        emergencyMessage = null;
        for (Child child : children) {
            child.setStatus(ChildStatus.WAITING);
        }
    }

    /** Ends the active trip without deleting the final pickup/drop-off history. */
    public void endTrip() {
        tripActive = false;
    }

    public boolean isTripActive() {
        return tripActive;
    }

    /** Records that a student entered the correct school vehicle. */
    public void pickupChild(int index) {
        requireValidIndex(index);
        children.get(index).setStatus(ChildStatus.PICKED_UP);
    }

    /** Records that a student safely left the vehicle at the destination. */
    public void dropOffChild(int index) {
        requireValidIndex(index);
        children.get(index).setStatus(ChildStatus.DROPPED_OFF);
    }

    public void setDelayMessage(String delayMessage) {
        this.delayMessage = delayMessage;
    }

    public String getDelayMessage() {
        return delayMessage;
    }

    public void setEmergencyMessage(String emergencyMessage) {
        this.emergencyMessage = emergencyMessage;
    }

    public String getEmergencyMessage() {
        return emergencyMessage;
    }

    public List<Child> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public int countByStatus(ChildStatus status) {
        int count = 0;
        for (Child child : children) {
            if (child.getStatus() == status) count++;
        }
        return count;
    }

    private void requireValidIndex(int index) {
        if (index < 0 || index >= children.size()) {
            throw new IllegalArgumentException("Invalid child index: " + index);
        }
    }
}

