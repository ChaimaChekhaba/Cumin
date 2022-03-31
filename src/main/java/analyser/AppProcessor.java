package analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import entities.PaprikaApp;
import entities.PaprikaClass;

import spoon.Launcher;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

/*

 * Created by chaima on 07/06/2018

 */

public class AppProcessor{

    private Launcher launcher;
    public List<List<String>> uses;
    public List<List<String>> insertedInGraph;

    private List<String> predefined_types = new ArrayList<>();
    private List<CtClass> list_of_class;
    private List<CtInterface> list_of_interface;

    @SuppressWarnings("unchecked")
    public void process(String path) {

        this.uses = new ArrayList<List<String>>();
        this.insertedInGraph = new ArrayList<List<String>>();

        //intialize the list of predefined types
        this.predefined_types.add("String");
        this.predefined_types.add("String[]");
        this.predefined_types.add("int");
        this.predefined_types.add("int[]");
        this.predefined_types.add("CharSequence");
        this.predefined_types.add("CharSequence[]");
        this.predefined_types.add("byte");
        this.predefined_types.add("byte[]");
        this.predefined_types.add("boolean");
        this.predefined_types.add("boolean[]");
        this.predefined_types.add("Integer");
        this.predefined_types.add("Integer[]");
        this.predefined_types.add("float");
        this.predefined_types.add("float[]");
        this.predefined_types.add("long");
        this.predefined_types.add("long[]");
        this.predefined_types.add("Void[]");

        this.launcher = new Launcher();
        launcher.addInputResource(path);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();

        //get all classes in AST
        this.list_of_class = this.launcher.getFactory().Package().getRootPackage().getElements(new TypeFilter(CtClass.class));

        //get all interfaces in AST
        this.list_of_interface = this.launcher.getFactory().Package().getRootPackage().getElements(new TypeFilter(CtInterface.class));

        this.classFieldProcessor();
        this.interfaceFieldProcessor();
        this.classMethodProcessor();
        this.interfaceMethodProcessor();
        this.getIntentInMethod();
        this.getFragment(path);
        this.getImportedClasses();
    }

    //find the relationship compose in the app
    private void classFieldProcessor() {

        //for each class in the code
        for (CtClass ctClass : this.list_of_class) {
            List<?> ctField = ctClass.getFields();
            for (Object aCtField : ctField) {
                if (aCtField instanceof CtVariable) {
                    CtVariable v = (CtVariable) aCtField;
                    if ((!this.predefined_types.contains(v.getType().toString()))
                            && (!ctClass.getQualifiedName().equals(v.getType().toString()))) {
                        List<String> ll = new ArrayList<>();

                        ll.add(ctClass.getQualifiedName());
                        ll.add(v.getType().toString());
                        ll.add("COMPOSE");

                        if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+")) {
                            this.uses.add(ll);
                        }
                    }
                }
            }
        }
    }

    private void interfaceFieldProcessor() {

       //for each interface in the code
        for (CtInterface ctInterface: this.list_of_interface) {
            List<?> ctField = ctInterface.getFields();
            for (int i=0; i< ctField.size(); i++) {
                if (ctField.get(i) instanceof CtVariable){
                    CtVariable v = (CtVariable) ctField.get(i);
                    if ((!this.predefined_types.contains(v.getType().toString())) && (!ctInterface.getQualifiedName().equals(v.getType().toString()))){
                        List<String> ll = new ArrayList<>();

                        ll.add(ctInterface.getQualifiedName());
                        ll.add(v.getType().toString());
                        ll.add("COMPOSE");

                        if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                            this.uses.add(ll);
                        }
                    }
                }
            }
        }

   }
    //create the relationship extracted from methods in the class
   private void classMethodProcessor() {

       //for each class in the code
        for (CtClass ctClass : this.list_of_class) {

            Set<CtMethod> ctMethods = ctClass.getMethods();

            for (CtMethod m: ctMethods) {
                //return type of a method
                if ((!m.getType().toString().equals("void")) &&
                        (!this.predefined_types.contains(m.getType().toString())) &&
                        (!ctClass.getQualifiedName().equals(m.getType().toString())))
                {
                    List<String> ll = new ArrayList<>();

                    ll.add(ctClass.getQualifiedName());
                    ll.add(m.getType().toString());
                    ll.add("USES_CLASS");

                    if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                        this.uses.add(ll);
                    }
                }
                //parameters of a method
                List<CtParameter> parameters = m.getParameters();
                for (CtParameter p: parameters) {
                    if ((!p.getType().toString().equals(ctClass.getQualifiedName())) &&
                            (!this.predefined_types.contains(p.getType().toString()))){
                        List<String> ll = new ArrayList<>();

                        ll.add(ctClass.getQualifiedName());
                        ll.add(p.getType().toString());
                        ll.add("USES_CLASS");

                        if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                           this.uses.add(ll);
                        }

                    }
                }
                //varibales of a method
                List<CtVariable> vars = m.getElements(new TypeFilter(CtVariable.class));
                for (CtVariable v: vars) {
                    if (v.getDefaultExpression() != null && !this.predefined_types.contains(v.getType().toString())
                            && (!ctClass.getQualifiedName().equals(v.getType().toString())))
                    {
                        List<String> ll = new ArrayList<>();

                        ll.add(ctClass.getQualifiedName());
                        ll.add(v.getType().toString());
                        ll.add("USES_CLASS");

                        if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                            this.uses.add(ll);
                        }
                    }
                }
            }

        }
    }
    //create the relationship USES_CLASS extracted from methods in the interface
    private void interfaceMethodProcessor() {
        //for each class in the code
        for (CtInterface ctClass : this.list_of_interface) {

            Set<CtMethod> ctMethods = ctClass.getMethods();

            for (CtMethod m: ctMethods) {
                //return type of a method
                if ((!m.getType().toString().equals("void")) &&
                        (!this.predefined_types.contains(m.getType().toString())) &&
                        (!ctClass.getQualifiedName().equals(m.getType().toString())))
                {

                   List<String> ll = new ArrayList<>();

                    ll.add(ctClass.getQualifiedName());
                    ll.add(m.getType().toString());
                    ll.add("USES_CLASS");

                    if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                        this.uses.add(ll);
                    }
                }

              //parameters of a method
                List<CtParameter> parameters = m.getParameters();
                for (CtParameter p: parameters) {
                    if ((!p.getType().toString().equals(ctClass.getQualifiedName())) &&
                            (!this.predefined_types.contains(p.getType().toString()))){
                        List<String> ll = new ArrayList<>();

                        ll.add(ctClass.getQualifiedName());
                        ll.add(p.getType().toString());
                        ll.add("USES_CLASS");

                        if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                            this.uses.add(ll);
                        }

                    }
                }
            }
        }
    }

    public Map<String, PaprikaClass> process(PaprikaApp paprikaApp) {

        this.process(paprikaApp.getPath());
        List<PaprikaClass> paprikaClasses = paprikaApp.getPaprikaClasses();
        Map<String, PaprikaClass> name_classe = new HashMap<String, PaprikaClass>();

        for (PaprikaClass c: paprikaApp.getPaprikaClasses()) {
            name_classe.put(c.getName(), c);
        }
        return name_classe;
    }
    private boolean contains(List<List<String>> ll, List<String> l) {

        for (List<String> elem:ll) {
            if ((elem.get(0).equals(l.get(0)) && elem.get(1).equals(l.get(1)))/* || (elem.get(0).equals(l.get(1)) && elem.get(1).equals(l.get(0)))*/)
                return true;
        }

        return false;
    }
    public List<String> getAbstractClasses(){
        List<String> class_abstract = new ArrayList<>();
        for (CtClass ctClass : this.list_of_class) {
            if (ctClass.isAbstract() && !ctClass.isInterface())

               class_abstract.add(ctClass.getQualifiedName());
        }

        return class_abstract;
    }
    public List<String> getConcreteClasses(){
        List<String> class_concret = new ArrayList<>();
        for (CtClass ctClass : this.list_of_class) {
            if (!ctClass.isAbstract() && !ctClass.isInterface())
                class_concret.add(ctClass.getQualifiedName());
        }

        return class_concret;
    }

    private void getIntentInMethod() throws  spoon.SpoonException{

        for (CtClass ctClass: this.list_of_class) {

            Set<CtMethod> ctMethod = ctClass.getMethods();

            for (CtMethod ctM : ctMethod){

                List<CtStatement> s = ctM.getElements(new TypeFilter<>(CtStatement.class));

                for (CtStatement stm : s) {
                    try {
                        String elem = stm.toString();
                        if (this.isIntent(elem) != null) {
                            String classe = this.getQualifiedNameClasse(this.isIntent(stm.toString()));
                            if (!classe.equals(ctClass.getQualifiedName())) {
                                List<String> ll = new ArrayList<>();
                                ll.add(ctClass.getQualifiedName());
                                ll.add(classe);
                                ll.add("USES_CLASS");

                                if (!this.contains(this.uses, ll) && !this.contains(this.insertedInGraph, ll) && ll.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                                    this.uses.add(ll);
                                }
                            }
                        }
                    }
                    catch (spoon.SpoonException ignored){

                    }
                }
            }
        }
    }

    private String isIntent(String s) {

        Pattern p = Pattern.compile("new Intent\\((\\D)*, (\\D)*\\.class\\)");
        Pattern pm = Pattern.compile("intent.setClass\\((\\D)*, (\\D)*\\.class\\)");
        Pattern pp = Pattern.compile(",");
        Pattern ppp = Pattern.compile(".class");
        Matcher m = p.matcher(s);
        Matcher mm = pm.matcher(s);
        if(m.find()) {
            String ss = m.group();

            if (!ss.contains("\n")) {
                return ppp.split(pp.split(ss)[1])[0].replaceFirst(" ", "");
            }
        }
        if (mm.find()){
            String ss = mm.group();
            if (!ss.contains("\n")) {
                return ppp.split(pp.split(ss)[1])[0].replaceFirst(" ", "");
            }
        }
        return null;

    }

    private List<String> getQualifiedNameAllClasses(){

        List<String> ll = new ArrayList<>();

        for (CtClass ctClass:this.list_of_class) {
            ll.add(ctClass.getQualifiedName());
        }
        return ll;

    }

    private String getQualifiedNameClasse(String classe) {

        List<String> ll = this.getQualifiedNameAllClasses();

        for (String name: ll) {
            if (name.contains(classe))
                return name;

        }
        return "";

    }
    private void getFragment(String path){

        FragmentProcessor fragmentProcessor = new FragmentProcessor();
        fragmentProcessor.process(path);

        for (CtClass ctClass: this.list_of_class){
            List<String> layout = this.getLayout(ctClass);
            for (String l: layout)
                for (List<String> ll:fragmentProcessor.layoutFragment){
                    if (l.equals(ll.get(0))){
                        List<String> kk = new ArrayList<>();
                        kk.add(ctClass.getQualifiedName());
                        kk.add(ll.get(1));
                        kk.add("USES_CLASS");

                        if (!this.contains(this.uses, kk) && !this.contains(this.insertedInGraph, kk)  && kk.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+"))  {
                            this.uses.add(kk);
                        }
                    }
                }
        }
    }

    private List<String> getLayout(CtClass ctClass){

        List<String> layout = new ArrayList<>();
        List<CtStatement> elements = ctClass.getElements(new TypeFilter<>(CtStatement.class));

        for (CtStatement elem: elements){

            try {
                String statement = elem.toString();
                String s =this.findLayoutInStatement(statement);
                if (s!= null)
                    layout.add(s);
            }
            catch (spoon.SpoonException ignored){}

        }
        return layout;
    }
    private String findLayoutInStatement(String s){
        Pattern p = Pattern.compile("setContentView\\([a-zA-Z_]*\\);");
        Matcher m = p.matcher(s);
        if (m.find())
        {
            Pattern pp = Pattern.compile("setContentView\\(");
            String ss = m.group();
            return  pp.split(ss)[1].replace(");", "");
        }
        else
            return null;

    }
    private void getImportedClasses(){

       for (CtClass ctClass: this.list_of_class){
           List<String> ll = getImportInClass(ctClass);
           for (String s: ll){
               if (!s.equals(ctClass.getQualifiedName()) && s.matches("[a-zA-Z]+[\\.]+[a-zA-Z\\.]+[a-z]+") && /*s.matches("[^\\[\\]_<>]") &&*/ !s.contains("R.")){
                   List<String> kk = new ArrayList<>();
                   kk.add(ctClass.getQualifiedName());
                   kk.add(s);
                   kk.add("CALLS_CLASS");

                   if (!this.contains(this.uses, kk) && !this.contains(this.insertedInGraph, kk) && kk.get(1).matches("[a-zA-Z]+[\\.][a-zA-Z\\.]+")) {
                       this.uses.add(kk);
                   }

               }

           }
       }
    }
    private List<String> getImportInClass(CtClass ctClass){
        List<String> ll = new ArrayList<>();

        List<CtElement> ctElements = ctClass.getElements(new TypeFilter<>(CtElement.class));
        for (CtElement ctElement: ctElements){
            if (ctElement instanceof CtTypeReferenceImpl)
                try {
                    ll.add(ctElement.toString());
                }
                catch (spoon.SpoonException ignored){

                }
        }
        return ll;
    }
}

