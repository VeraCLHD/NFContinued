package linguistic_processing;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.lang.model.util.Elements;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import io.Writer;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class XMLParserMesh {

  public static void main(String argv[]) {
	Writer.overwriteFile("", "meshVariants.txt");
    extractEntryTermsMeshHeadings("mesh/desc2017.xml", "DescriptorRecord", "DescriptorUI", "DescriptorName");
    //extractEntryTermsMeshHeadings("mesh/supp2017.xml", "SupplementalRecord", "SupplementalRecordUI", "SupplementalRecordName");
    extractEntryTermsMeshHeadings("mesh/qual2017.xml", "QualifierRecord", "QualifierUI", "QualifierName");
    
  }

public static void extractEntryTermsMeshHeadings(String path, String rootTag, String idTag, String nameTag) {
	try {

	File fXmlFile = new File(path);
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);

	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();
	
	NodeList nList = doc.getElementsByTagName(rootTag);
	System.out.println(doc.getTextContent());
	for (int temp = 0; temp < nList.getLength(); temp++) {
		Node nNode = nList.item(temp);
		String node = "";
		System.out.println("\nCurrent Element :" + nNode.getNodeName());

		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			
			Element eElement = (Element) nNode;
			String name = eElement.getElementsByTagName(nameTag).item(0).getTextContent().toLowerCase();
			
			System.out.println(name);
			
			if(name.contains(",") && name.contains(" ")){
				String[] arr = name.split(",");
				String first = arr[1].trim();
				String second = arr[0].trim();
				name = first + " " + second;
			} 
			node += name + "\t";
			node += eElement.getElementsByTagName(idTag).item(0).getTextContent() + "\t";
			
			NodeList nl = eElement.getElementsByTagName("Term");
			for(int i = 0; i<nl.getLength(); i++){
				Node termNode = nl.item(i);
				Element term = (Element) termNode;
				String meshVariant = term.getElementsByTagName("String").item(0).getTextContent();
				
				if( meshVariant != null && !meshVariant.equals("") && !meshVariant.isEmpty()){
					
					
					if(meshVariant.contains(",") && meshVariant.contains(" ")){
						String[] arr = meshVariant.split(",");
						String first_word = arr[1].trim();
						String second_word = arr[0].trim();
						meshVariant = first_word + " " + second_word;
					}
					
					node += meshVariant.toLowerCase() + ",";
				} 
				
				

			}
			
			

		}
		
		Writer.appendLineToFile(node, "meshVariants.txt");
		
	}
    } catch (Exception e) {
	e.printStackTrace();
    }
}

}
