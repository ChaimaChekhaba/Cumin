package metrics;
import entities.*;

import java.util.HashMap;
/**
 * Created by sarra on 08/03/17.
 * Updated by chaima on 10/02/2021
 */

public class MetricsCalculator {

		/*public static void computeUICMetrics(PaprikaClass paprikaClass, ) {
			
		}*/

        public static void calculateAppMetrics(PaprikaApp app)
        {
            NumberOfClasses.createNumberOfClasses(app, app.getPaprikaClasses().size());
            int numberOfInterfaces=0;
            int numberOfContentProviders =0 ;
            int numberOfAsyncTasks =0;
            int numberOfInnerClasses =0;
            int numberOfBroadcastReceivers =0;
            int numberOfMethods =0 ;
            int numberOfServices =0;
            int numberOfViews =0;
            int numberOfActivities =0;
            int numberOfVariables = 0;
            int numberOfFragments = 0;
            int numberOfDialogs = 0;
            int numberOfViewModels = 0;

            for(PaprikaClass c: app.getPaprikaClasses()){
                if(c.isInterface()){
                    numberOfInterfaces++;
                }
                if(c.isInnerClass()){
                    numberOfInnerClasses++;
                }
                if (c.isActivity()){
                    numberOfActivities++;
                }else if (c.isBroadcastReceiver()){
                    numberOfBroadcastReceivers++;
                }else if (c.isContentProvider()){
                    numberOfContentProviders++;
                }else if (c.isService()){
                    numberOfServices++;
                }else if (c.isView()){
                    numberOfViews++;
                }else if (c.isAsyncTask()){
                    numberOfAsyncTasks++;
                }else if (c.isFragment()) {
                	numberOfFragments++;
                }else if (c.isDialog()) {
                	numberOfDialogs++;
                }else if (c.isViewModel()) {
                	numberOfViewModels++;
                }
                numberOfVariables += c.getPaprikaVariables().size();
                numberOfMethods += c.getPaprikaMethods().size();
            }
            NumberOfInterfaces.createNumberOfInterfaces(app, numberOfInterfaces);
            NumberOfActivities.createNumberOfActivities(app, numberOfActivities);
            NumberOfFragments.createNumberOfFragments(app, numberOfFragments);
            NumberOfDialogs.createNumberOfDialogs(app, numberOfDialogs);
            NumberOfMethods.createNumberOfMethods(app, numberOfMethods);
            NumberOfViews.createNumberOfViews(app, numberOfViews);
            NumberOfServices.createNumberOfServices(app, numberOfServices);
            NumberOfBroadcastReceivers.createNumberOfBroadcastReceivers(app, numberOfBroadcastReceivers);
            NumberOfInnerClasses.createNumberOfInnerClasses(app, numberOfInnerClasses);
            NumberOfAsyncTasks.createNumberOfAsyncTasks(app, numberOfAsyncTasks);
            NumberOfContentProviders.createNumberOfContentProviders(app, numberOfContentProviders);
            NumberOfVariables.createNumberOfVariables(app, numberOfVariables);
            for(PaprikaClass paprikaClass: app.getPaprikaClasses()){
                calculateClassMetrics(paprikaClass);
            }
            calculateGraphMetrics(app);
        }


        public static void calculateClassMetrics(PaprikaClass paprikaClass){
            if(paprikaClass.isInterface()){
                IsInterface.createIsInterface(paprikaClass,true);
            }
            if(paprikaClass.isInnerClass()){
                IsInnerClass.createIsInnerClass(paprikaClass,true);
            }
            if (paprikaClass.isActivity()){
                IsActivity.createIsActivity(paprikaClass,true);
            }else if (paprikaClass.isFragment()){
            	IsFragment.createIsFragment(paprikaClass, true);
            }else if (paprikaClass.isBroadcastReceiver()){
                IsBroadcastReceiver.createIsBroadcastReceiver(paprikaClass,true);
            }else if (paprikaClass.isContentProvider()){
                IsContentProvider.createIsContentProvider(paprikaClass,true);
            }else if (paprikaClass.isService()){
                IsService.createIsService(paprikaClass,true);
            }else if (paprikaClass.isView()){
                IsView.createIsView(paprikaClass,true);
            }else if (paprikaClass.isAsyncTask()){
                IsAsyncTask.createIsAsyncTask(paprikaClass,true);
            }else if (paprikaClass.isApplication()){
                IsApplication.createIsApplication(paprikaClass,true);
            }else if (paprikaClass.isViewModel()) {
            	IsViewModel.createIsViewModel(paprikaClass, true);
            }else if (paprikaClass.isModel()) {
            	IsModel.createIsModel(paprikaClass, true);
            }
            
            NumberOfAttributes.createNumberOfAttributes(paprikaClass,paprikaClass.getPaprikaVariables().size());
            NumberOfMethods.createNumberOfMethods(paprikaClass, paprikaClass.getPaprikaMethods().size());
            NumberOfImplementedInterfaces.createNumberOfImplementedInterfaces(paprikaClass,
                    paprikaClass.getInterfacesNames().size());
            CouplingBetweenObjects.createCouplingBetweenObjects(paprikaClass);
            DepthOfInheritance.createDepthOfInheritance(paprikaClass,paprikaClass.getDepthOfInheritance());
            LackofCohesionInMethods.createLackofCohesionInMethods(paprikaClass);
            ClassComplexity.createClassComplexity(paprikaClass);
            NPathComplexity.createNPathComplexity(paprikaClass);
            /*
             * the new metrics added for MVC based patterns
             * Added by Chaima on 12/02/2021
             */
            //GUI metrics
            NumberOfGUIComponents.createNumberOfGUIComponents(paprikaClass, paprikaClass.getNumberOfGUIComponents());
            NumberOfGUIStatements.createNumberOfGUIStatements(paprikaClass, paprikaClass.getNumberOfGUIStatements());
            DoesUseDatabinding.createDoesUseDatabinding(paprikaClass, paprikaClass.doesUseDatabinding());
            HandleInputEventsInXMLFile.createHandleInputEventsInXMLFile(paprikaClass, paprikaClass.getHandleInputEventsInXMLFile());
            HasDataTagInLayoutFile.createHasDataTagInLayoutFile(paprikaClass, paprikaClass.hasDataTagInLayoutFile());
            
            //interaction metrics
            NumberOfInputEventListeners.createNumberOfInputEventListeners(paprikaClass, paprikaClass.getNumberOfInputEventListeners());
            NumberOfNonLifecycleEvents.createNumberOfNonLifecycleEvents(paprikaClass, paprikaClass.getNonLifecycleEvents());
            NumberOfLifecycleEvents.createNumberOfLifecycleEvents(paprikaClass, paprikaClass.getLifecycleEvents());
            
            
            if(paprikaClass.isStatic())
            {
                IsStatic.createIsStatic(paprikaClass,true);
            }
            NumberOfChildren.createNumberOfChildren(paprikaClass);
            for(PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()){
                calculateMethodMetrics(paprikaMethod);
            }

            for (PaprikaVariable paprikaVariable: paprikaClass.getPaprikaVariables()){
                if(paprikaVariable.isStatic()){
                    IsStatic.createIsStatic(paprikaVariable,true);
                }
            }

        }

        public static void calculateMethodMetrics(PaprikaMethod paprikaMethod){
            NumberOfParameters.createNumberOfParameters(paprikaMethod, paprikaMethod.getArguments().size());
            NumberOfDirectCalls.createNumberOfDirectCalls(paprikaMethod, paprikaMethod.getCalledMethods().size());
            if(paprikaMethod.isConstructor()){
                IsInit.createIsInit(paprikaMethod,true);
            }else if(paprikaMethod.isGetter()){
                IsGetter.createIsGetter(paprikaMethod,true);
            }else if(paprikaMethod.isSetter()){
                IsSetter.createIsSetter(paprikaMethod,true);
            }
            if(paprikaMethod.isStatic()){
                IsStatic.createIsStatic(paprikaMethod,true);
            }

            NumberOfLines.createNumberOfLines(paprikaMethod, paprikaMethod.getNumberOfLines());
            CyclomaticComplexity.createCyclomaticComplexity(paprikaMethod, paprikaMethod.getComplexity());
        }

        private static void calculateGraphMetrics(PaprikaApp app){
            HashMap<PaprikaMethod, Integer> numberOfCallers = new HashMap<>();
            Integer nb;
            for(PaprikaClass paprikaClass: app.getPaprikaClasses()){
                for (PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()){
                    if(!numberOfCallers.containsKey(paprikaMethod)){
                        numberOfCallers.put(paprikaMethod,0);
                    }
                    for(Entity entity: paprikaMethod.getCalledMethods()){
                        if(entity instanceof PaprikaMethod){
                            nb=numberOfCallers.get((PaprikaMethod)entity);
                            if(nb==null){
                                numberOfCallers.put((PaprikaMethod)entity,1);
                            }else{
                                numberOfCallers.put((PaprikaMethod)entity,nb+1);
                            }
                        }
                    }
                }
            }
            for(PaprikaClass paprikaClass:app.getPaprikaClasses()){
                //compute the number of callers
                for(PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()){
                    NumberOfCallers.createNumberOfCallers(paprikaMethod,numberOfCallers.get(paprikaMethod));
                }
            }
        }

}
