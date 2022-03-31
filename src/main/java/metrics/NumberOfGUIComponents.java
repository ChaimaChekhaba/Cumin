package metrics;

import entities.PaprikaClass;

public class NumberOfGUIComponents extends UnaryMetric<Integer> {
    private NumberOfGUIComponents(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "number_of_gui_components";
    }

    public static NumberOfGUIComponents createNumberOfGUIComponents(PaprikaClass paprikaClass, int value) {
    	NumberOfGUIComponents numberOfGUIComponents = new NumberOfGUIComponents(paprikaClass, value);
    	numberOfGUIComponents.updateEntity();
        return numberOfGUIComponents;
    }

}
