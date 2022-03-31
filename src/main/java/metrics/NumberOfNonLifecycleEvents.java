package metrics;

import entities.PaprikaClass;

public class NumberOfNonLifecycleEvents extends UnaryMetric<Integer> {
    private NumberOfNonLifecycleEvents(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "number_of_non_lifecycle_events";
    }

    public static NumberOfNonLifecycleEvents createNumberOfNonLifecycleEvents(PaprikaClass paprikaClass, int value) {
    	NumberOfNonLifecycleEvents numberOfNonLifecycleEvents = new NumberOfNonLifecycleEvents(paprikaClass, value);
    	numberOfNonLifecycleEvents.updateEntity();
        return numberOfNonLifecycleEvents;
    }

}