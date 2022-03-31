
package neo4j;

import entities.Entity;
import entities.PaprikaApp;
import entities.PaprikaArgument;
import entities.PaprikaClass;
import entities.PaprikaExternalArgument;
import entities.PaprikaExternalClass;
import entities.PaprikaExternalMethod;
import entities.PaprikaLibrary;
import entities.PaprikaMethod;
import entities.PaprikaVariable;
import metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import analyser.AppProcessor;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public class ModelToGraph {
    private static final Logger logger = LoggerFactory.getLogger(ModelToGraph.class.getName());

    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;
    private static final Label appLabel = DynamicLabel.label("App");
    private static final Label classLabel = DynamicLabel.label("Class");
    private static final Label externalClassLabel = DynamicLabel.label("ExternalClass");
    private static final Label methodLabel = DynamicLabel.label("Method");
    private static final Label externalMethodLabel = DynamicLabel.label("ExternalMethod");
    private static final Label variableLabel = DynamicLabel.label("Variable");
    private static final Label argumentLabel = DynamicLabel.label("Argument");
    private static final Label externalArgumentLabel = DynamicLabel.label("ExternalArgument");
    private static final Label libraryLabel = DynamicLabel.label("Library");

    private Map<Entity, Node> methodNodeMap;
    private Map<PaprikaClass, Node> classNodeMap;
    private Map<PaprikaVariable, Node> variableNodeMap;

    private String key;
    private String appName;
    private AppProcessor appProcessor;
    

    public ModelToGraph(String DatabasePath) {
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        methodNodeMap = new HashMap<>();
        classNodeMap = new HashMap<>();
        variableNodeMap = new HashMap<>();
        IndexManager indexManager = new IndexManager(graphDatabaseService);
        indexManager.createIndex();
        this.appProcessor = new AppProcessor();
    }

    public Node insertApp(PaprikaApp paprikaApp) {
        this.key = paprikaApp.getKey();
        this.appName = paprikaApp.getName();
        this.appProcessor.process(paprikaApp);
        Node appNode;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            appNode = graphDatabaseService.createNode(appLabel);
            appNode.setProperty("app_key", key);
            appNode.setProperty("name", appName);
            appNode.setProperty("version", paprikaApp.getVersion());
            appNode.setProperty("sdk_version", paprikaApp.getSdkVersion());
            Date date = new Date();
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
            appNode.setProperty("date_analysis", simpleFormat.format(date));
            appNode.setProperty("path", paprikaApp.getPath());

            Node classNode;
            for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
                classNode = insertClass(paprikaClass);
                appNode.createRelationshipTo(classNode, RelationTypes.APP_OWNS_CLASS);
            }
            for (PaprikaExternalClass paprikaExternalClass : paprikaApp.getPaprikaExternalClasses()) {
                insertExternalClass(paprikaExternalClass);
            }
            for (Metric metric : paprikaApp.getMetrics()) {
                insertMetric(metric, appNode);
            }

            for (PaprikaLibrary paprikaLibrary : paprikaApp.getPaprikaLibraries()) {
                appNode.createRelationshipTo(insertLibrary(paprikaLibrary), RelationTypes.APP_USES_LIBRARY);
            }
            tx.success();
        }
        try (Transaction tx = graphDatabaseService.beginTx()) {
            createHierarchy(paprikaApp);
            createCallGraph(paprikaApp);
            insertUsesRelationship(paprikaApp);
            tx.success();
        }
        this.rectifyRelationships();
        return appNode;
    }

    private void insertMetric(Metric metric, Node node) {
        node.setProperty(metric.getName(), metric.getValue());
    }


    public Node insertClass(PaprikaClass paprikaClass) {
        Node classNode = graphDatabaseService.createNode(classLabel);
        classNodeMap.put(paprikaClass, classNode);
        classNode.setProperty("app_key", key);
        classNode.setProperty("name", paprikaClass.getName());
        classNode.setProperty("modifier", paprikaClass.getModifier().toString().toLowerCase());
        classNode.setProperty("app_name", appName);
        if (paprikaClass.getParentName() != null) {
            classNode.setProperty("parent_name", paprikaClass.getParentName());
        }
        for (PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()) {
            classNode.createRelationshipTo(insertVariable(paprikaVariable), RelationTypes.CLASS_OWNS_VARIABLE);

        }
        for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
            classNode.createRelationshipTo(insertMethod(paprikaMethod), RelationTypes.CLASS_OWNS_METHOD);
        }
        for (Metric metric : paprikaClass.getMetrics()) {
            insertMetric(metric, classNode);
        }
        return classNode;
    }

    public Node insertLibrary(PaprikaLibrary paprikaLibrary) {
        Node libraryNode = graphDatabaseService.createNode(libraryLabel);
        libraryNode.setProperty("app_key", key);
        libraryNode.setProperty("name", paprikaLibrary.getName());
        libraryNode.setProperty("app_name", appName);
        return libraryNode;
    }

    public Node insertExternalClass(PaprikaExternalClass paprikaClass) {
        Node classNode = graphDatabaseService.createNode(externalClassLabel);
        classNode.setProperty("app_key", key);
        classNode.setProperty("name", paprikaClass.getName());
        classNode.setProperty("app_name", appName);
        if (paprikaClass.getParentName() != null) {
            classNode.setProperty("parent_name", paprikaClass.getParentName());
        }
        for (PaprikaExternalMethod paprikaExternalMethod : paprikaClass.getPaprikaExternalMethods()) {
            classNode.createRelationshipTo(insertExternalMethod(paprikaExternalMethod), RelationTypes.CLASS_OWNS_METHOD);
        }
        for (Metric metric : paprikaClass.getMetrics()) {
            insertMetric(metric, classNode);
        }
        return classNode;
    }

    public Node insertVariable(PaprikaVariable paprikaVariable) {
        Node variableNode = graphDatabaseService.createNode(variableLabel);
        variableNodeMap.put(paprikaVariable, variableNode);
        variableNode.setProperty("app_key", key);
        variableNode.setProperty("name", paprikaVariable.getName());
        variableNode.setProperty("modifier", paprikaVariable.getModifier().toString().toLowerCase());
        variableNode.setProperty("type", paprikaVariable.getType());
        variableNode.setProperty("app_name", appName);
        for (Metric metric : paprikaVariable.getMetrics()) {
            insertMetric(metric, variableNode);
        }
        return variableNode;
    }

    public Node insertMethod(PaprikaMethod paprikaMethod) {
        Node methodNode = graphDatabaseService.createNode(methodLabel);
        methodNodeMap.put(paprikaMethod, methodNode);
        methodNode.setProperty("app_key", key);
        methodNode.setProperty("name", paprikaMethod.getName());
        methodNode.setProperty("modifier", paprikaMethod.getModifier().toString().toLowerCase());
        methodNode.setProperty("full_name", paprikaMethod.toString());
        methodNode.setProperty("app_name", appName);
        methodNode.setProperty("return_type", paprikaMethod.getReturnType());

        for (Metric metric : paprikaMethod.getMetrics()) {
            insertMetric(metric, methodNode);
        }
        Node variableNode;
        for (PaprikaVariable paprikaVariable : paprikaMethod.getUsedVariables()) {
            variableNode = variableNodeMap.get(paprikaVariable);
            if (variableNode != null) {
                methodNode.createRelationshipTo(variableNode, RelationTypes.USES);
            } else {
                logger.warn("problem");
            }

        }
        for (PaprikaArgument arg : paprikaMethod.getArguments()) {
            methodNode.createRelationshipTo(insertArgument(arg), RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertExternalMethod(PaprikaExternalMethod paprikaMethod) {
        Node methodNode = graphDatabaseService.createNode(externalMethodLabel);
        methodNodeMap.put(paprikaMethod, methodNode);
        methodNode.setProperty("app_key", key);
        methodNode.setProperty("name", paprikaMethod.getName());
        methodNode.setProperty("full_name", paprikaMethod.toString());
        methodNode.setProperty("return_type", paprikaMethod.getReturnType());
        methodNode.setProperty("app_name", appName);
        for (Metric metric : paprikaMethod.getMetrics()) {
            insertMetric(metric, methodNode);
        }
        for (PaprikaExternalArgument arg : paprikaMethod.getPaprikaExternalArguments()) {
            methodNode.createRelationshipTo(insertExternalArgument(arg), RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertArgument(PaprikaArgument paprikaArgument) {
        Node argNode = graphDatabaseService.createNode(argumentLabel);
        argNode.setProperty("app_key", key);
        argNode.setProperty("name", paprikaArgument.getName());
        argNode.setProperty("position", paprikaArgument.getPosition());
        argNode.setProperty("app_name", appName);
        return argNode;
    }

    public Node insertExternalArgument(PaprikaExternalArgument paprikaExternalArgument) {
        Node argNode = graphDatabaseService.createNode(externalArgumentLabel);
        argNode.setProperty("app_key", key);
        argNode.setProperty("name", paprikaExternalArgument.getName());
        argNode.setProperty("position", paprikaExternalArgument.getPosition());
        argNode.setProperty("app_name", appName);
        for (Metric metric : paprikaExternalArgument.getMetrics()) {
            insertMetric(metric, argNode);
        }
        return argNode;
    }

    public void insertUsesRelationship(PaprikaApp paprikaApp) {
    	appProcessor.process(paprikaApp);
    	Node node = null, node_dest = null;
    	List<String> list = new ArrayList<>();
    	for (int i=0; i<appProcessor.uses.size(); i++) {
 
    		try (ResourceIterator<Node> class_src = graphDatabaseService.findNodes(classLabel, "name", appProcessor.uses.get(i).get(0))) {
                if (class_src.hasNext()) {
                    node = class_src.next();
                }
            }
    		try (ResourceIterator<Node> class_dest = graphDatabaseService.findNodes(classLabel, "name", appProcessor.uses.get(i).get(1)))
			{
				if (class_dest.hasNext()) {
                    node_dest = class_dest.next();
                }
			}
            if (node != null && node_dest!=null && node!= node_dest) {
    			List<String> ll = new ArrayList<>();
    			ll.add(appProcessor.uses.get(i).get(1));
    			ll.add(appProcessor.uses.get(i).get(0));
    			
    			if (!appProcessor.insertedInGraph.contains(ll)) {

                    switch (appProcessor.uses.get(i).get(2)) {
                        case "COMPOSE":
                            node.createRelationshipTo(node_dest, RelationTypes.COMPOSE);
                            break;
                        case "USES_CLASS":
                            node.createRelationshipTo(node_dest, RelationTypes.USES_CLASS);
                            break;
                    }
                    appProcessor.insertedInGraph.add(ll);
                }

            }
    		else{
    		    if (node_dest == null && !list.contains(appProcessor.uses.get(i).get(1))){
                    PaprikaExternalClass paprikaExternalClass = PaprikaExternalClass.createPaprikaExternalClass(appProcessor.uses.get(i).get(1), paprikaApp);
                    node_dest = this.insertExternalClass(paprikaExternalClass);
                    list.add(appProcessor.uses.get(i).get(1));
                    //System.out.println("node inserted \t"+appProcessor.uses.get(i).get(1));
                    //System.out.println(list);
                }
                else{
                    try (ResourceIterator<Node> class_dest = graphDatabaseService.findNodes(externalClassLabel, "name", appProcessor.uses.get(i).get(1)))
                    {
                        if (class_dest.hasNext()) {
                            node_dest = class_dest.next();
                        }
                    }
                }
                if (node != null  && node_dest !=null) {

                    List<String> ll = new ArrayList<>(), kk = new ArrayList<>();

                    kk.add(appProcessor.uses.get(i).get(0));
                    kk.add(appProcessor.uses.get(i).get(1));
                    if (!appProcessor.insertedInGraph.contains(kk)) {

                        switch (appProcessor.uses.get(i).get(2)) {
                            case "COMPOSE":
                                node.createRelationshipTo(node_dest, RelationTypes.COMPOSE);
                                break;
                            case "USES_CLASS":
                                node.createRelationshipTo(node_dest, RelationTypes.USES_CLASS);
                                break;
                            case "CALLS_CLASS":
                                node.createRelationshipTo(node_dest, RelationTypes.CALLS_CLASS);
                                break;
                        }

                        appProcessor.insertedInGraph.add(kk);
                    }
                }
            }
    		node = null;
			node_dest = null;
    	}
    }
    //rectify the relationship between class and externalClass in case of extending or implementing a class from the framework android
    private void rectifyRelationships(){
        try (Transaction tx = graphDatabaseService.beginTx()) {
            graphDatabaseService.execute("MATCH (n:Class)-[r:CALLS_CLASS]->(p:ExternalClass) WHERE n.parent_name=p.name CREATE (n)-[rr:EXTENDS]->(p)");
            graphDatabaseService.execute("MATCH (n)-[r:EXTENDS|:IMPLEMENTS|:COMPOSE|:USES_CLASS]->(p)<-[rr:USES_CLASS|:CALLS_CLASS]-(n) DELETE rr");
            tx.success();
        }
    }
    
    public void createHierarchy(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            PaprikaClass parent = paprikaClass.getParent();
            if (parent != null) {
            	
            	List<String> ll = appProcessor.getAbstractClasses();
            	List<String> cc = appProcessor.getConcreteClasses();
            	if (ll.contains(parent.getName()) && cc.contains(paprikaClass.getName())) 
            		classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(parent), RelationTypes.REALIZE);            		
            	else
            		classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(parent), RelationTypes.EXTENDS);
                if (parent.isActivity()) 
                	classNodeMap.get(paprikaClass).setProperty("is_activity", true);
                else if (parent.isBroadcastReceiver())
                	classNodeMap.get(paprikaClass).setProperty("is_broadcast_receiver", true);
                else if (parent.isService())
                	classNodeMap.get(paprikaClass).setProperty("is_service", true);
                else if (parent.isContentProvider())
                	classNodeMap.get(paprikaClass).setProperty("is_content_provider", true);
                else if (parent.isFragment())
                	classNodeMap.get(paprikaClass).setProperty("is_fragment", true);
                else if (parent.isView())
                	classNodeMap.get(paprikaClass).setProperty("is_view", true);
                else if (parent.isDialog())
                	classNodeMap.get(paprikaClass).setProperty("is_dialog", true);
                
                List<String> l1 = new ArrayList<String>();
                l1.add(parent.getName());
                l1.add(paprikaClass.getName());
                if (!appProcessor.insertedInGraph.contains(l1))
                	appProcessor.insertedInGraph.add(l1);
               
            }
            for (PaprikaClass pInterface : paprikaClass.getInterfaces()) {
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(pInterface), RelationTypes.IMPLEMENTS);
                List<String> l1 = new ArrayList<String>();
                l1.add(pInterface.getName());
                l1.add(paprikaClass.getName());
                if (!appProcessor.insertedInGraph.contains(l1))
                	appProcessor.insertedInGraph.add(l1);
            }
        }
    }

    public void createCallGraph(PaprikaApp paprikaApp) {
    	for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
        	for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
        		for (Entity calledMethod : paprikaMethod.getCalledMethods()) {
        			if (methodNodeMap.get(paprikaMethod) != null && methodNodeMap.get(calledMethod) != null) {
        				methodNodeMap.get(paprikaMethod).createRelationshipTo(methodNodeMap.get(calledMethod), RelationTypes.CALLS);
        			}
                }
            }
        }
    }
}
