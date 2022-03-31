package metrics;

import entities.PaprikaClass;

public class HasDataTagInLayoutFile extends UnaryMetric<Boolean> {

    private HasDataTagInLayoutFile(PaprikaClass entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "has_data_tag_in_layout_file";
    }

    public static HasDataTagInLayoutFile createHasDataTagInLayoutFile(PaprikaClass entity, boolean value) {
    	HasDataTagInLayoutFile hasDataTagInLayoutFile= new HasDataTagInLayoutFile(entity, value);
    	hasDataTagInLayoutFile.updateEntity();
        return hasDataTagInLayoutFile;
    }
}