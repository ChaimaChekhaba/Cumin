package metrics;

import entities.PaprikaClass;

public class NumberOfGUIStatements extends UnaryMetric<Integer> {
    private NumberOfGUIStatements(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "number_of_gui_statements";
    }

    public static NumberOfGUIStatements createNumberOfGUIStatements(PaprikaClass paprikaClass, int value) {
    	NumberOfGUIStatements numberOfGUIStatements = new NumberOfGUIStatements(paprikaClass, value);
    	numberOfGUIStatements.updateEntity();
        return numberOfGUIStatements;
    }

}