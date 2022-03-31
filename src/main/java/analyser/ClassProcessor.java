package analyser;

import entities.PaprikaClass;
import entities.PaprikaModifiers;
import entities.PaprikaVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtImportImpl;
import spoon.support.reflect.declaration.CtInterfaceImpl;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by sarra on 17/02/17.
 */
public class ClassProcessor extends TypeProcessor<CtClass> {
    private static final Logger logger = LoggerFactory.getLogger(ClassProcessor.class.getName());
    private static final URLClassLoader classloader;
    private final HashMap<String, Integer> GUIMetrics;
    private final HashMap<String, Integer> listenerBasedInputEventsInLayouttFiles;
    private final HashMap<String, Integer> dataTagsInLayoutFiles;

    static {
        classloader = new URLClassLoader(MainProcessor.paths.toArray(new URL[MainProcessor.paths.size()]));
    }
    
    public ClassProcessor(HashMap<String, Integer> GUIMetrics, 
    		HashMap<String, Integer> listenerBasedInputEventsInLayouttFiles, 
    		HashMap<String, Integer> dataTagsInLayoutFiles) {
    	this.GUIMetrics = GUIMetrics;
    	this.listenerBasedInputEventsInLayouttFiles = listenerBasedInputEventsInLayouttFiles;
    	this.dataTagsInLayoutFiles = dataTagsInLayoutFiles;
    }

    @Override
    public void process(CtClass ctType) {
        String qualifiedName = ctType.getQualifiedName();
        if (ctType.isAnonymous()) {
            String[] splitName = qualifiedName.split("\\$");
            qualifiedName = splitName[0] + "$" +
                    ((CtNewClass) ctType.getParent()).getType().getQualifiedName() + splitName[1];
        }
        String visibility = ctType.getVisibility() == null ? "null" : ctType.getVisibility().toString();
        PaprikaModifiers paprikaModifiers = DataConverter.convertTextToModifier(visibility);
        if (paprikaModifiers == null) {
            paprikaModifiers = PaprikaModifiers.DEFAULT;
        }
        PaprikaClass paprikaClass = PaprikaClass.createPaprikaClass(qualifiedName, MainProcessor.currentApp, paprikaModifiers);
        MainProcessor.currentClass = paprikaClass;
        handleProperties(ctType, paprikaClass);
        handleAttachments(ctType, paprikaClass);
        if (ctType.getQualifiedName().contains("$")) {
            paprikaClass.setInnerClass(true);
        }
        
        processMethods(ctType);
    }

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        return super.isToBeProcessed(candidate) && !(candidate instanceof CtInterfaceImpl);
    }

    @Override
    public void processMethods(CtClass ctClass) {
        MethodProcessor methodProcessor = new MethodProcessor();
        ConstructorProcessor constructorProcessor = new ConstructorProcessor();
        for (Object o : ctClass.getMethods()) {
            methodProcessor.process((CtMethod) o);
        }
        CtConstructor ctConstructor;
        for (Object o : ctClass.getConstructors()) {
            ctConstructor = (CtConstructor) o;
            constructorProcessor.process(ctConstructor);
        }

    }

    @Override
    public void handleAttachments(CtClass ctClass, PaprikaClass paprikaClass) {
        if (ctClass.getSuperclass() != null) {
            paprikaClass.setParentName(ctClass.getSuperclass().getQualifiedName());
        }
        for (CtTypeReference<?> ctTypeReference : ctClass.getSuperInterfaces()) {
            paprikaClass.getInterfacesNames().add(ctTypeReference.getQualifiedName());
        }
        String modifierText;
        PaprikaVariable paprikaVariable;
        PaprikaModifiers paprikaModifiers1;
        boolean isStatic;
        for (CtField<?> ctField : (List<CtField>) ctClass.getFields()) {
            modifierText = ctField.getVisibility() == null ? "null" : ctField.getVisibility().toString();
            paprikaModifiers1 = DataConverter.convertTextToModifier(modifierText);
            if (paprikaModifiers1 == null) {
                paprikaModifiers1 = PaprikaModifiers.DEFAULT;
            }
            paprikaVariable = PaprikaVariable.createPaprikaVariable(ctField.getSimpleName(), ctField.getType().getQualifiedName(), paprikaModifiers1, paprikaClass);
            isStatic = false;
            for (ModifierKind modifierKind : ctField.getModifiers()) {
                if (modifierKind.toString().toLowerCase().equals("static")) {
                    isStatic = true;
                    break;
                }
            }
            paprikaVariable.setStatic(isStatic);
        }

    }

    @Override
    public void handleProperties(CtClass ctClass, PaprikaClass paprikaClass) {
        Integer doi = 0;
        boolean isApplication = false;
        boolean isContentProvider = false;
        boolean isAsyncTask = false;
        boolean isService = false;
        boolean isView = false;
        boolean isActivity = false;
        boolean isBroadcastReceiver = false;
        boolean isInterface = ctClass.isInterface();
        boolean isStatic = false;
        boolean isFragment = false;
        boolean isDialog = false;
        boolean isViewModel = false;
        boolean hasDataTagInLayoutFile = false;
        int handleInputEventsInXMLFile = 0;
        boolean doesUseDatabinding = false;
        
        for (ModifierKind modifierKind : ctClass.getModifiers()) {
            if (modifierKind.toString().toLowerCase().equals("static")) {
                isStatic = true;
                break;
            }
        }
        
        CtTypeReference reference = findSuperClass(ctClass, doi);

        if (reference != null) {
        	
            try {
                Class myRealClass = classloader.loadClass(reference.getQualifiedName());
                while (myRealClass.getSuperclass() != null) {
                    doi++;
                    if (myRealClass.getSimpleName().endsWith("Activity")) {
                        isActivity = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("ContentProvider")) {
                        isContentProvider = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("AsyncTask")) {
                        isAsyncTask = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("View")) {
                        isView = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("BroadcastReceiver")) {
                        isBroadcastReceiver = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("Service")) {
                        isService = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("Application")) {
                        isApplication = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("Fragment")) {
                    	isFragment = true;
                    	break;
                    } else if (myRealClass.getSimpleName().endsWith("Dialog")) {
                    	isDialog = true;
                    	break;
                    } else if (myRealClass.getSimpleName().endsWith("ViewModel")) {
                    	isViewModel = true;
                    	break;
                    }
                    
                    myRealClass = myRealClass.getSuperclass();
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                
                if (ctClass.getSimpleName().endsWith("Activity")) {
                    isActivity = true;
                } else if (ctClass.getSimpleName().endsWith("ContentProvider")) {
                    isContentProvider = true;
                } else if (ctClass.getSimpleName().endsWith("AsyncTask")) {
                    isAsyncTask = true;
                } else if (ctClass.getSimpleName().endsWith("View")) {
                    isView = true;
                } else if (ctClass.getSimpleName().endsWith("BroadcastReceiver")) {
                    isBroadcastReceiver = true;
                } else if (ctClass.getSimpleName().endsWith("Service")) {
                    isService = true;
                } else if (ctClass.getSimpleName().endsWith("Application")) {
                    isApplication = true;
                } else if (ctClass.getSimpleName().endsWith("Fragment")) {
                	isFragment = true;
                } else if (ctClass.getSimpleName().endsWith("Dialog")) {
                	isDialog = true;
                } else if (ctClass.getSimpleName().endsWith("ViewModel")) {
                	isViewModel = true;
                }
            }
        }
        
        //if (isActivity || isFragment || isView || isDialog) {
        List<Integer> metrics = computeNumberMetricsFromStatements(ctClass); 
        paprikaClass.setNumberOfGUIStatements(metrics.get(0));
        paprikaClass.setNumberOfInputEventListeners(metrics.get(1));
        paprikaClass.setLifecycleEvents(metrics.get(2));
        paprikaClass.setNonLifecycleEvents(metrics.get(3));
        
        
	    List<String> layoutFiles = getLayout(ctClass);
	    doesUseDatabinding = getDatabinding(ctClass);
	    if (layoutFiles.size() != 0) {
	    	//System.out.println("hhh "+layoutFiles);
	        	if (layoutFiles.size() == 1)
	        		paprikaClass.setNumberOfGUIComponents(this.GUIMetrics.get(layoutFiles.get(0)));
	        	else {
	        		//TODO: implement the case where the UIC class has multiple Layout files
	        	}
	        	
	        	int nb = this.dataTagsInLayoutFiles.get(layoutFiles.get(0));
	        	if (nb > 0)
	        		hasDataTagInLayoutFile = true;
	        	else
	        		hasDataTagInLayoutFile = false;
	        	
	        	handleInputEventsInXMLFile = this.listenerBasedInputEventsInLayouttFiles.get(layoutFiles.get(0));
	    }
        //}
        if (!isApplication && !isBroadcastReceiver && !isAsyncTask && !isContentProvider && 
        		!isService && !isViewModel && !ctClass.getSimpleName().matches("\\d+"))
        	paprikaClass.setModel(isModelClass(ctClass));
        
        paprikaClass.setInterface(isInterface);
        paprikaClass.setActivity(isActivity);
        paprikaClass.setStatic(isStatic);
        paprikaClass.setAsyncTask(isAsyncTask);
        paprikaClass.setContentProvider(isContentProvider);
        paprikaClass.setBroadcastReceiver(isBroadcastReceiver);
        paprikaClass.setService(isService);
        paprikaClass.setView(isView);
        paprikaClass.setFragment(isFragment);
        paprikaClass.setDialog(isDialog);
        paprikaClass.setApplication(isApplication);
        paprikaClass.setDepthOfInheritance(doi);
        paprikaClass.setViewModel(isViewModel);
        paprikaClass.setDataTagInLayoutFile(hasDataTagInLayoutFile);
        paprikaClass.SetHandleInputEventsInXMLFile(handleInputEventsInXMLFile);
        paprikaClass.setUseDatabinding(doesUseDatabinding);
        
    }
    /*
     * Search the Layout files used in a given class
     * TODO the case of referencing layout files in constructors is not implemented
    */
    private List<String> getLayout(CtClass ctClass){
      Set<CtMethod> ctMethod = ctClass.getMethods();
      List<String> layout = new ArrayList<>();
      for (CtMethod ctM : ctMethod){
    	  List<CtInvocation> ctInvocation = ctM.getElements(new TypeFilter(CtInvocation.class));
		   	for (CtInvocation elem : ctInvocation) { 
	            try {
	                String s = this.findLayoutInStatement(elem.toString());
	                if (s != null)
	                    layout.add(s);
	                
	            }catch (spoon.SpoonException ignored){}
	        }
      } 
      return layout;
    }
    /*
     * Search the name of layout files in a given statement
     */
    private String findLayoutInStatement(String s){
    	Pattern pattern = Pattern.compile("setContentView(.+)");
    	Matcher matcher = pattern.matcher(s);
    	if (s.contains("setContentView"))
    		System.out.println(s);
    	if (matcher.find())
    	{
    		if (matcher.group(1).contains("R.layout.")) {
    			String[] result = matcher.group(1).split("\\.");
        		String str = result[result.length-1];
        		str = str.substring(0, str.length()-1);
        	    return str;
    		}
    		else 
    			return null;
    	}
  
        return null;
    }
    /*
     * check if the class uses DataBinding Util for databinding
    */
    private boolean findDataBindingStatement(String s) {
    	if (s.contains("DataBindingUtil"))
    		return true;
    	else return false;
    }
    
    /*
     * */
    private boolean getDatabinding(CtClass ctClass){
        Set<CtMethod> ctMethod = ctClass.getMethods();
        for (CtMethod ctM : ctMethod){
      	  List<CtInvocation> ctInvocation = ctM.getElements(new TypeFilter(CtInvocation.class));
  		   	for (CtInvocation elem : ctInvocation) { 
  	            try {
  	               if (this.findDataBindingStatement(elem.toString()) == true)
  	            	   return true;
  	                
  	            }catch (spoon.SpoonException ignored){}
  	        }
        } 
        return false;
      }
    
    
    /*
     * Compute the number of GUI operations in a given class
     */
    private List<Integer> computeNumberMetricsFromStatements(CtClass ctClass){
    	Set<CtMethod> ctMethod = ctClass.getMethods();
    	int numberOfGUIStatements = 0;
    	int numberOfInputEventsBasedOnListeners = 0;
    	int numberOfLifecycleEvents = 0;
    	int numberOfNonLifecycleEvents = 0;
        for (CtMethod ctM : ctMethod){
        	List<CtInvocation> ctInvocation = ctM.getElements(new TypeFilter(CtInvocation.class));
		   	for (CtInvocation stm : ctInvocation) {              
  	          	numberOfGUIStatements = numberOfGUIStatements + checkOccurrencesInFile(stm.toString(), "./UIC/guiStatements.txt", true);
  	          	numberOfInputEventsBasedOnListeners = numberOfInputEventsBasedOnListeners + 
  	          			checkOccurrencesInFile(stm.toString(), "./UIC/ListenerBasedInputEvent.txt", true);
  	          	numberOfLifecycleEvents = numberOfLifecycleEvents + 
	          			checkOccurrencesInFile(stm.toString(), "./UIC/lifecycleEvent.txt", true);
  	          	numberOfNonLifecycleEvents = numberOfNonLifecycleEvents + 
	          			checkOccurrencesInFile(stm.toString(), "./UIC/NonLifecycleEvent.txt", true);
  	        }
        } 
    	return Arrays.asList(numberOfGUIStatements, numberOfInputEventsBasedOnListeners, numberOfLifecycleEvents, numberOfNonLifecycleEvents);
    }
    /*
     * Check if the the string does contain a GUI operations/Listener based inut events/Lifecycle events/NonLifecyle events and returns their number
     */
    private int checkOccurrencesInFile(String s, String file, boolean separate) {
    	int result = 0;
    	try {
			Scanner scanner = new Scanner(new File(file));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (separate)
					line = line.split("\\(")[0] + "(";				
				if (s.contains(line)) {
					result++;
					//if (!separate) System.out.println("the statement---"+s+"----"+line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	return result;
    }
    /*
     * Check if the class is a model class
     */
    private boolean isModelClass(CtClass ctClass){
    	int numberOfAttributs = ctClass.getFields().size();
    	int numberOfConstructors = ctClass.getConstructors().size();
    	int numberOfGetters = 0;
    	int numberOfSetters = 0;
    	
    	Set<CtMethod> ctMethod = ctClass.getMethods();
    	for (CtMethod ctM : ctMethod){
    		if (ctM.getSimpleName().startsWith("get"))
    			numberOfGetters++; 
    		else if (ctM.getSimpleName().startsWith("set"))
    			numberOfSetters++;
        } 
    	boolean result = (numberOfAttributs > 0 && numberOfConstructors > 0 && numberOfGetters > 0 && numberOfSetters > 0)||(hasReferenceToModelLibraries(ctClass));
    	result = result && !ctClass.getSimpleName().endsWith("Adapter") && 
    			!ctClass.getSimpleName().endsWith("Listener") && 
    			!ctClass.getSimpleName().endsWith("ViewHolder");
    	return result;
    }
    /*
     * Check if the class has references to model libraries
     */
    private boolean hasReferenceToModelLibraries(CtClass ctClass) {
    	 List<CtElement> ctElements = ctClass.getElements(new TypeFilter<>(CtElement.class));
         for (CtElement ctElement: ctElements){
             if (ctElement instanceof CtTypeReferenceImpl && checkOccurrencesInFile(ctElement.toString(), "./UIC/ModelLibraries.txt", false) > 0) {
            	 //System.out.println(ctClass.getQualifiedName()+"---"+ctElement.toString()+"--- true");
            	 return true;
             }
         }
   
         return false;
    }
    
}
