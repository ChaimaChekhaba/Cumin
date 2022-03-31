package metrics;

import entities.PaprikaApp;

public class NumberOfDialogs extends UnaryMetric<Integer> {

    private NumberOfDialogs(PaprikaApp paprikaApp, int value) {
        this.value = value;
        this.entity = paprikaApp;
        this.name = "number_of_dialogs";
    }

    public static NumberOfDialogs createNumberOfDialogs(PaprikaApp paprikaApp, int value) {
    	NumberOfDialogs numberOfDialogs =  new NumberOfDialogs(paprikaApp, value);
    	numberOfDialogs.updateEntity();
        return numberOfDialogs;
    }

}