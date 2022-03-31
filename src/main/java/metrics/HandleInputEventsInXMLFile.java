package metrics;

import entities.PaprikaClass;

public class HandleInputEventsInXMLFile extends UnaryMetric<Integer> {

    private HandleInputEventsInXMLFile(PaprikaClass entity, int value) {
        this.value = value;
        this.entity = entity;
        this.name = "handle_input_events_in_xml_file";
    }

    public static HandleInputEventsInXMLFile createHandleInputEventsInXMLFile(PaprikaClass entity, int value) {
    	HandleInputEventsInXMLFile handleInputEventsInXMLFile= new HandleInputEventsInXMLFile(entity, value);
    	handleInputEventsInXMLFile.updateEntity();
        return handleInputEventsInXMLFile;
    }
}