import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.net.URL;

//https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm

public class prototypeXMLParser{
	
	public static void main(String[] args) {

      try {
		 
		
		 
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(new URL("https://arcane-citadel-48781.herokuapp.com/").openStream());
         doc.getDocumentElement().normalize();
         System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
         NodeList nList = doc.getElementsByTagName("item");
         System.out.println("----------------------------");
		 		 
		 String[] descArray;
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);         
            
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;               
               descArray = eElement.getElementsByTagName("description").item(0).getTextContent().split(",");   
			   write("Company "+ descArray[1] + " with code ("+descArray[0]+") has spot price "+descArray[3]+" .");
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   private static void write(String s){
	   System.out.println(s);
   }
}