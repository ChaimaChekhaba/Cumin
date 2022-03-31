package metrics;

import entities.PaprikaClass;

public class IsModel extends UnaryMetric<Boolean> {

    private IsModel(PaprikaClass entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "is_model";
    }

    public static IsModel createIsModel(PaprikaClass entity, boolean value) {
    	IsModel isModel= new IsModel(entity, value);
    	isModel.updateEntity();
        return isModel;
    }
}