package metrics;

import entities.PaprikaClass;

public class NumberOfInputEventListeners extends UnaryMetric<Integer> {
    private NumberOfInputEventListeners(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "number_of_input_events_listeners";
    }

    public static NumberOfInputEventListeners createNumberOfInputEventListeners(PaprikaClass paprikaClass, int value) {
    	NumberOfInputEventListeners numberOfInputEventListeners = new NumberOfInputEventListeners(paprikaClass, value);
    	numberOfInputEventListeners.updateEntity();
        return numberOfInputEventListeners;
    }

}