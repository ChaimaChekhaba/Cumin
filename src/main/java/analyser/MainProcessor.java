package analyser;

import entities.PaprikaApp;
import entities.PaprikaClass;
import entities.PaprikaMethod;
import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Created by sarra on 21/02/17.
 * Updated by chaima on 12/02/2021.
 */
public class MainProcessor {

    static PaprikaApp currentApp;
    static PaprikaClass currentClass;
    static PaprikaMethod currentMethod;
    static ArrayList<URL> paths;
    String appPath;
    List<File> jarsPath;
    String sdkPath;
    List<File> layoutFiles;

    public MainProcessor(String appName, int appVersion, int commitNumber, String status, String appKey, String appPath, String sdkPath, List<File> jarsPath, List<File> layoutFiles, int sdkVersion, String module) {
        MainProcessor.currentApp = PaprikaApp.createPaprikaApp(appName, appVersion, commitNumber, status, appKey, appPath, sdkVersion, module);
        currentClass = null;
        currentMethod = null;
        this.appPath = appPath;
        this.jarsPath = jarsPath;
        this.sdkPath = sdkPath;
        this.layoutFiles = layoutFiles;
    }

    public void process() {
        Launcher launcher = new Launcher();
        launcher.addInputResource(appPath);
        launcher.getEnvironment().setNoClasspath(true);

        try {
            paths = toURLFiles(jarsPath);
            paths.add(new File(sdkPath).toURI().toURL());
            String[] cl = new String[paths.size()];
            for (int i = 0; i < paths.size(); i++) {
                URL url = paths.get(i);
                cl[i] = url.getPath();
            }
            
            HashMap<String, Integer> GUIMetrics = getNumberOfGUIComponentsInLayoutFiles();
            HashMap<String, Integer> ListenerBasedInputEventsInLayouttFiles = getNumberOfListenerBasedInputEventHandledInLayoutFile();
            HashMap<String, Integer> dataTagsInLayoutFiles = doesApplyDataBindingThroughDataTag();
            launcher.getEnvironment().setSourceClasspath(cl);
            launcher.buildModel();
            AbstractProcessor<CtClass> classProcessor = new ClassProcessor(GUIMetrics, 
            		ListenerBasedInputEventsInLayouttFiles,
            		dataTagsInLayoutFiles);
            AbstractProcessor<CtInterface> interfaceProcessor = new InterfaceProcessor();
            launcher.addProcessor(classProcessor);
            launcher.addProcessor(interfaceProcessor);
            launcher.process();
            
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    private ArrayList<URL> toURLFiles(final List<File> jarFiles) throws IOException{
    	ArrayList<URL> jars = new ArrayList<>();
            for (final File fileEntry : jarFiles) {
            jars.add(fileEntry.toURI().toURL());
        }
        return jars;
    }
    
    // compute the metric number of GUI components in every XML files
    private HashMap<String, Integer> getNumberOfGUIComponentsInLayoutFiles(){
    	HashMap<String, Integer> results = new HashMap<String, Integer>();
    	for (File file: layoutFiles) {
    		String xml_file = file.getName().toString().substring(0, file.getName().toString().length() - 4);
    		results.put(xml_file, getNumberOfNodesInXMLFile(file));
    	}
    	return results;
    }
    
    // compute the number of nodes in an XML file
    private int getNumberOfNodesInXMLFile(File xmlFile){
  		 try {
  	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	         Document doc = dBuilder.parse(xmlFile);
  	         doc.getDocumentElement().normalize();
  	         int nb = doc.getElementsByTagName("*").getLength();
  	      
  	         // check if the XML file contains include node
  	         if (doc.getElementsByTagName("include").getLength() > 0) {
  	        	
  	        	 NodeList nodeList = doc.getElementsByTagName("include");
  	        	 for(int x=0,size= nodeList.getLength(); x<size; x++) {
  	        		 String layout_name = nodeList.item(x).getAttributes().getNamedItem("layout").getNodeValue();
  	        		 layout_name = layout_name.subSequence(layout_name.indexOf("/") + 1, layout_name.length()).toString();
  	        		 nb = nb - 1 + getNumberOfNodesInXMLFile(getLayoutFileName(layout_name));
  	        	 }
  	  	     }
  	         return nb;         
  	         
  	      } catch (Exception e) {
  	         e.printStackTrace();
  	      }
  		
  		return 0;
  }
  
    // search the absolute path of the XML file
    private File getLayoutFileName(String xmlFile) {
		for (File file: layoutFiles) {
	  		String file_name = file.getName().toString().substring(0, file.getName().toString().length() - 4);
	  		if (xmlFile.equals(file_name))
	  			return file;
	  	}
		  
		return new File(xmlFile);
    }
  
    // compute the metric of number of Listener based input events handled directly in the XML layout file 
    private HashMap<String, Integer> getNumberOfListenerBasedInputEventHandledInLayoutFile(){
    	HashMap<String, Integer> results = new HashMap<String, Integer>();
    	for (File file: layoutFiles) {
    		String xml_file = file.getName().toString().substring(0, file.getName().toString().length() - 4);
    		results.put(xml_file, getNumberOfListenersInXMLFile(file));
    	}
    	return results;
    }
    
    // compute the number of listeners declared in the XML files mainly the onclick listener 
    private int getNumberOfListenersInXMLFile(File xmlFile) {  	
    	try {
			Scanner scanner = new Scanner(xmlFile);
			int result = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();		
				if (line.contains("android:onClick=") || line.contains("android:addTextChangedListener=")) {
					result++;
				}
			}
			scanner.close();
			return result;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    // check if the class uses the data binding through data tag in the Layout file
    private int hasDataTagInLayoutFile(File xmlFile) {
    	try {
 	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 	         Document doc = dBuilder.parse(xmlFile);
 	         doc.getDocumentElement().normalize();
 	         
 	         return doc.getElementsByTagName("data").getLength();
 	         
 	      } catch (Exception e) {
 	         e.printStackTrace();
 	      }
 		
    	return 0;
    }

    // compute the data binding in the XML layout file 
    private HashMap<String, Integer> doesApplyDataBindingThroughDataTag(){
    	HashMap<String, Integer> results = new HashMap<String, Integer>();
    	for (File file: layoutFiles) {
    		String xml_file = file.getName().toString().substring(0, file.getName().toString().length() - 4);
    		results.put(xml_file, hasDataTagInLayoutFile(file));
    	}
    	return results;
    }
}
