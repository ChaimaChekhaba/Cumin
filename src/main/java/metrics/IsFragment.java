package metrics;

import entities.PaprikaClass;

public class IsFragment extends UnaryMetric<Boolean> {

    private IsFragment(PaprikaClass entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "is_fragment";
    }

    public static IsFragment createIsFragment(PaprikaClass entity, boolean value) {
    	IsFragment isFragment= new IsFragment(entity, value);
    	isFragment.updateEntity();
        return isFragment;
    }
}