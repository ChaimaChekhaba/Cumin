package metrics;

import entities.PaprikaApp;

public class NumberOfFragments extends UnaryMetric<Integer> {

    private NumberOfFragments(PaprikaApp paprikaApp, int value) {
        this.value = value;
        this.entity = paprikaApp;
        this.name = "number_of_fragments";
    }

    public static NumberOfFragments createNumberOfFragments(PaprikaApp paprikaApp, int value) {
    	NumberOfFragments numberOfFragments =  new NumberOfFragments(paprikaApp, value);
        numberOfFragments.updateEntity();
        return numberOfFragments;
    }

}