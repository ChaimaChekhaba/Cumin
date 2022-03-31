package metrics;


import entities.PaprikaClass;

public class DoesUseDatabinding extends UnaryMetric<Boolean> {

    private DoesUseDatabinding(PaprikaClass entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "does_use_data_binding";
    }

    public static DoesUseDatabinding createDoesUseDatabinding(PaprikaClass entity, boolean value) {
    	DoesUseDatabinding doesUseDatabinding= new DoesUseDatabinding(entity, value);
    	doesUseDatabinding.updateEntity();
        return doesUseDatabinding;
    }
}