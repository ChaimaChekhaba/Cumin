package analyser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

public class FragmentProcessor {
    public String path;
    public List<List<String>> layoutFragment ;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private void listf(String directoryName, List<File> files) {
        File directory = new File(directoryName);

        // Get all the files from a directory.
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile() && file.getName().endsWith(".xml") && !file.getName().equals("AndroidManifest.xml") && file.getAbsolutePath().contains("/res/")) {
                files.add(file);
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }

    public void process(String path){
        this.path = path;
        this.layoutFragment = new ArrayList<>();

        List<File> files =new ArrayList<>();
        this.listf(this.path, files);

        for (File file: files){
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document= builder.parse(file);
                NodeList fragment = document.getElementsByTagName("fragment");
                List<String> element = new ArrayList<>();
                element.add(file.getName().replace(".xml", ""));
                for (int i=0; i < fragment.getLength(); i++){
                    Node n = fragment.item(i);
                    Element node = (Element) n;
                    if (!element.contains(node.getAttribute("class"))) element.add(node.getAttribute("class"));
                }
                if (element.size() >1 && !this.layoutFragment.contains(element)){
                    this.layoutFragment.add(element);
                }
            }
            catch (final ParserConfigurationException e){e.printStackTrace();}
            catch (SAXException e) {e.printStackTrace();}
            catch (IOException e) {e.printStackTrace();}
        }
    }
	}
