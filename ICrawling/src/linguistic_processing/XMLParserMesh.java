package linguistic_processing;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.lang.model.util.Elements;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class XMLParserMesh {

  public static void main(String argv[]) {

    try {

	File fXmlFile = new File("mesh/desc2017.xml");
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);

	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();

	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

	NodeList nList = doc.getElementsByTagName("DescriptorRecord");
	 
     

	System.out.println("----------------------------");

	for (int temp = 0; temp < 2; temp++) {
		Node nNode = nList.item(temp);

		System.out.println("\nCurrent Element :" + nNode.getNodeName());

		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) nNode;
			NodeList nl = eElement.getElementsByTagName("Term");
			for(int i = 0; i<nl.getLength(); i++){
				Node termNode = nl.item(i);
				Element term = (Element) nNode;
				
				System.out.println(term.getElementsByTagName("String").item(0).getTextContent());
			}
			
			
			
			/*NodeList conceptLists = eElement.getElementsByTagName("ConceptList");
			// iterate over concepts
			for (int i = 0; i < 2; i++) {
				Element concept = (Element) conceptLists.item(i);
				NodeList nl = concept.getElementsByTagName("Term");
				// iterate over terms
				for(int j=0; j< nl.getLength(); j++){
					Element term = (Element) nl.item(0);
					System.out.println(term.getTextContent());
				}
			}*/
			

		}
	}
    } catch (Exception e) {
	e.printStackTrace();
    }
  }

}
