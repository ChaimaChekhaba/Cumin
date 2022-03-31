package analyser;

import metrics.MetricsCalculator;
import neo4j.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import entities.PaprikaLibrary;
import entities.PaprikaApp;



/**
 * Created by sarra on 17/02/17.
 * Last modification 07/06/2018 by chaima
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());
    
    public static void main(String[] args) throws Exception{
    	ArgumentParser parser = ArgumentParsers.newArgumentParser("cumin");
        Subparsers subparsers = parser.addSubparsers().dest("sub_command");
        Subparser analyseParser = subparsers.addParser("analyse").help("Analyse an app");
        analyseParser.addArgument("-d", "--directory").required(true).help("Path to android apps");
        analyseParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        analyseParser.addArgument("-s", "--sdkPath").required(true).help("Path to SDK folder");
        
        Subparser queryParser = subparsers.addParser("query").help("Query the database");
        queryParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        queryParser.addArgument("-r", "--request").help("Request to execute");
        queryParser.addArgument("-c", "--csv").help("path to register csv files").setDefault("");
        queryParser.addArgument("-dk", "--delKey").help("key to delete");
        queryParser.addArgument("-dp", "--delPackage").help("Package of the applications to delete");
        queryParser.addArgument("-d", "--details").type(Boolean.class).setDefault(false).help("Show the concerned entity in the results");
           
        try {
            Namespace res = parser.parseArgs(args);
            if(res.getString("sub_command").equals("analyse")){
            	String home = res.getString("directory");
            	String database = res.getString("database");
            	String sdkPath = res.getString("sdkPath");
            	logger.info("Starting analyse with Cumin");
            	//rmdir(database);
            	
        		File file = new File(home);
            	String[] directories = file.list(new FilenameFilter() {
            		public boolean accept(File current, String name) {
            			return new File(current, name).isDirectory();
            		}
            	});
            	ModelToGraph modelToGraph=new ModelToGraph(database);
            	for (String name:directories) {
            		try {
            			String path = home+name;
            			removeUselessPackage(path);
        	    		List<String> libs = new ArrayList<>();
        	    		List<List<File>> allFilesOfApp = getSignificantFiles(path);
        	    		
        	    		List<File> jarsPath =  allFilesOfApp.get(2);
        				List<File> gradles = allFilesOfApp.get(1);
        				List<File> layout = allFilesOfApp.get(0);
        				         				
        	    		int sdkVersion = 1;
        				if (gradles.size() ==0)
        	    			logger.info("No gradle file");

        				else {
        					libs = getDependenciesGradle(gradles.get(0));
        					sdkVersion = getSDKVersion(gradles.get(0));
        				}
        				
        				String key = name;
        	            String module = name;
        	            
        	            MainProcessor mainProcessor = new MainProcessor(name, 1, 0, "build", key, path, sdkPath, jarsPath, layout, sdkVersion, module);
        	            mainProcessor.process();
        	            
        	            GraphCreator graphCreator = new GraphCreator(MainProcessor.currentApp);
        	            graphCreator.createClassHierarchy();
        	            graphCreator.createCallGraph();
        				for(String lib : libs){
        					if(lib != ""){
        							addLibrary(MainProcessor.currentApp,lib);
        					}
        				}
        	            MetricsCalculator.calculateAppMetrics(MainProcessor.currentApp);
        	            modelToGraph.insertApp(MainProcessor.currentApp);
        	            logger.info("Saving "+name+" into database " +database);
        	            logger.info("Done");

            		}
            		catch (Exception e) {
            			logger.error("Application "+name+"\n");
            			e.printStackTrace();
            		}
            	}
            	logger.info("End analysing with Cumin");
            }
            else {
            	 if(res.getString("sub_command").equals("query")){
            		 logger.info("Starting query with Paprika");
                     queryMode(res);
                 }
            }
            
        } catch (ArgumentParserException e) {
            analyseParser.handleError(e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	private static void addLibrary(PaprikaApp paprikaApp, String libraryString){
		PaprikaLibrary.createPaprikaLibrary(libraryString,paprikaApp);
	}
	
	// list recursively the files in a folder
	public static List<File> listf(String path) {
		File directory = new File(path);
		
		//the list of all files in the directory
		List<File> resultList = new ArrayList<File>();
		
	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    resultList.addAll(Arrays.asList(fList));
	    for (File file : fList) {
	    	if (file.isDirectory()) {
	            resultList.addAll(listf(file.getAbsolutePath().toString()));
	        }
	    }
	    return resultList;
	}
	
	// get gradle, layout and jar files from the path of the app
	private static List<List<File>> getSignificantFiles(String path){
		//list of gradle files
		List<File> gradleFiles = new ArrayList<File>();
		
		//list of layout files
		List<File> layoutFiles = new ArrayList<File>();
		
		//list of jar files in the app
		List<File> jarFiles = new ArrayList<File>();
		
		List<File> filesOfApp = listf(path);
		
		for(File file:filesOfApp) {
			if (file.getAbsolutePath().toString().contains("/res/layout/") && file.getPath().toString().endsWith(".xml")) {
    			//a layout file, add it to the layoutFiles list
    			layoutFiles.add(file);
    		}
    		else if (file.getPath().toString().endsWith("build.gradle")) {
    			// a gradle file, add it to the gradleFiles list
    			gradleFiles.add(file);
    		}
    		else if (file.getPath().toString().endsWith("gradle-wrapper.jar")) {
    			// a JAR file, we add it to the jarFiles list
    			jarFiles.add(file);
    		}
		}		
	
		return Arrays.asList(layoutFiles, gradleFiles, jarFiles);
	}
	
	
	private static List<String> getDependenciesGradle(File gradleFile){

    	List<String> dependencies = new ArrayList<>();
    	try {
    		int i = 0;
			Scanner sc = new Scanner(gradleFile);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();

				if(line.contains("compile '") || line.contains("useLibrary '")) {
					dependencies.add(line.substring(line.indexOf("'"), line.lastIndexOf("'")+1));
					i ++;
				}
			}
		}
		catch (FileNotFoundException e){
			logger.error("Gradle file does not respect the specified format (compile and useLibrary)");
		}
		return dependencies;

	}
	private static int getSDKVersion(File gradle){
		try {
			Scanner sc = new Scanner(gradle);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();

				if(line.contains("compileSdkVersion '")) {
					return Integer.parseInt(line.replaceAll("[^0-9]", ""));
				}
			}
			return 1;
		}
		catch (FileNotFoundException e) {
			logger.error("Gradle file not found");
			return 0;
		}
	}
	private static void removeUselessPackage(String path) throws IOException {
        File f= new File(path);

        if (f.isDirectory()){
            for (File file: f.listFiles()){
                if (file.getName().contains("androidTest") || file.getName().contains("test") || file.getName().contains("debug")){
                    Runtime.getRuntime().exec("rm -R "+file.getAbsolutePath());
                    //System.out.println("file to remove "+f.getName());
                }
                else {
                	 removeUselessPackage(file.getPath());
                }               	
            }
        }

    }
	private static void rmdir(String path) throws IOException{
		File f= new File(path);
		//Runtime.getRuntime().exec("rm -R "+f.getAbsolutePath());
		System.out.println("database deleted");
	}
	
	public static void queryMode(Namespace arg) throws Exception {
        System.out.println("Executing Queries");
        QueryEngine queryEngine = new QueryEngine(arg.getString("database"));
        String request = arg.get("request");
        Boolean details = arg.get("details");
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR))+"_"+String.valueOf(cal.get(Calendar.MONTH)+1)+"_"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH))+"_"+String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+"_"+String.valueOf(cal.get(Calendar.MINUTE));
        String csvPrefix = arg.getString("csv")+csvDate;
        System.out.println("Resulting csv file name will start with prefix "+csvPrefix);
        queryEngine.setCsvPrefix(csvPrefix);
        switch(request){
            case "ARGB8888":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
            case "MIM":
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                break;
            case "IGS":
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                break;
            case "LIC":
                LICQuery.createLICQuery(queryEngine).execute(details);
                break;
            case "NLMR":
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                break;
            case "CC":
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                break;
            case "LM":
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                break;
            case "SAK":
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                break;
            case "BLOB":
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                break;
            case "OVERDRAW":
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                break;
            case "HSS":
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                break;
            case "HBR":
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                break;
            case "HAS":
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "THI":
                TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
                break;
            case "ALLHEAVY":
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "ANALYZED":
                queryEngine.AnalyzedAppQuery();
                break;
            case "DELETE":
                queryEngine.deleteQuery(arg.getString("delKey"));
                break;
            case "DELETEAPP":
                if(arg.get("delKey") != null) { queryEngine.deleteEntireApp(arg.getString("delKey")); }
                else {
                    queryEngine.deleteEntireAppFromPackage(arg.getString("delPackage"));
                }
                break;
            case "STATS":
                QuartileCalculator quartileCalculator = new QuartileCalculator(queryEngine);
                quartileCalculator.calculateClassComplexityQuartile();
                quartileCalculator.calculateLackofCohesionInMethodsQuartile();
                quartileCalculator.calculateNumberOfAttributesQuartile();
                quartileCalculator.calculateNumberOfImplementedInterfacesQuartile();
                quartileCalculator.calculateNumberOfMethodsQuartile();
                quartileCalculator.calculateNumberofInstructionsQuartile();
                quartileCalculator.calculateCyclomaticComplexityQuartile();
                quartileCalculator.calculateNumberOfMethodsForInterfacesQuartile();
                break;
            case "ALLLCOM":
                queryEngine.getAllLCOM();
                break;
            case "ALLCYCLO":
                queryEngine.getAllCyclomaticComplexity();
                break;
            case "ALLCC":
                queryEngine.getAllClassComplexity();
                break;
            case "ALLNUMMETHODS":
                queryEngine.getAllNumberOfMethods();
                break;
            case "COUNTVAR":
                queryEngine.countVariables();
                break;
            case "COUNTINNER":
                queryEngine.countInnerClasses();
                break;
            case "COUNTASYNC":
                queryEngine.countAsyncClasses();
                break;
            case "COUNTVIEWS":
                queryEngine.countViews();
                break;
            case "NONFUZZY":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                LICQuery.createLICQuery(queryEngine).execute(details);
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
                InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
                UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine).execute(details);
                HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
                InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
                break;
            case "FUZZY":
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "ALLAP":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                LICQuery.createLICQuery(queryEngine).execute(details);
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
                InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
                UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine).execute(details);
                HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
                InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
                TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
                break;
            case "FORCENOFUZZY":
                CCQuery.createCCQuery(queryEngine).execute(details);
                LMQuery.createLMQuery(queryEngine).execute(details);
                SAKQuery.createSAKQuery(queryEngine).execute(details);
                BLOBQuery.createBLOBQuery(queryEngine).execute(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).execute(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).execute(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).execute(details);
                break;
            default:
                System.out.println("Executing custom request");
                queryEngine.executeRequest(request);
        }
        queryEngine.shutDown();
        System.out.println("Done");
    }
}
