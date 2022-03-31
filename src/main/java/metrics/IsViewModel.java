package metrics;

import entities.PaprikaClass;

public class IsViewModel extends UnaryMetric<Boolean> {

    private IsViewModel(PaprikaClass entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "is_view_model";
    }

    public static IsViewModel createIsViewModel(PaprikaClass entity, boolean value) {
    	IsViewModel isViewModel= new IsViewModel(entity, value);
    	isViewModel.updateEntity();
        return isViewModel;
    }
}