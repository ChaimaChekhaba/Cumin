package metrics;

import entities.PaprikaClass;

public class NumberOfLifecycleEvents extends UnaryMetric<Integer> {
    private NumberOfLifecycleEvents(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "number_of_lifecycle_events";
    }

    public static NumberOfLifecycleEvents createNumberOfLifecycleEvents(PaprikaClass paprikaClass, int value) {
    	NumberOfLifecycleEvents numberOfLifecycleEvents = new NumberOfLifecycleEvents(paprikaClass, value);
    	numberOfLifecycleEvents.updateEntity();
        return numberOfLifecycleEvents;
    }

}