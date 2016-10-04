package core.game;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import enums.Blocks.BLOCKS;
import enums.Materials.MATERIALS;

public class LevelLoader {
	
	private int numBirds;
	private ArrayList<Block> blocks;
	private ArrayList<Block> platforms;
	private ArrayList<Block> pigs;
	
	public LevelLoader(String uri)
	{
		numBirds = 0;
		blocks = new ArrayList<Block>();
		platforms = new ArrayList<Block>();
		pigs = new ArrayList<Block>();
		
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try
		{
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(uri);

			parseDocument(dom);
		}
		catch(ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch(SAXException se)
		{
			se.printStackTrace();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	private void parseDocument(Document dom)
	{
		//get the root element
		Element levelElement = dom.getDocumentElement();

		numBirds = getIntValue(levelElement, "BirdsAmount");
		
		//get a nodelist of  elements
		NodeList nl = levelElement.getElementsByTagName("GameObjects");
		if(nl != null && nl.getLength() > 0)
		{
			Element gameObjectEle = (Element)nl.item(0);
			NodeList nl2 = gameObjectEle.getElementsByTagName("Block");
			if(nl2 != null && nl2.getLength() > 0) {
				for(int i = 0; i < nl2.getLength(); i++)
				{
					//get the employee element
					Element el = (Element)nl2.item(i);

					//get the Employee object
					Block b = getBlock(el);

					blocks.add(b);
				}
			}
			NodeList nl3 = gameObjectEle.getElementsByTagName("Pig");
			if(nl3 != null && nl3.getLength() > 0) {
				for(int i = 0; i < nl3.getLength(); i++)
				{
					//get the employee element
					Element el = (Element)nl3.item(i);

					//get the Employee object
					Block b = getBlock(el);

					pigs.add(b);
				}
			}
			NodeList nl4 = gameObjectEle.getElementsByTagName("Platform");
			if(nl4 != null && nl4.getLength() > 0) {
				for(int i = 0; i < nl4.getLength(); i++)
				{
					//get the employee element
					Element el = (Element)nl4.item(i);

					//get the Employee object
					Block b = getBlock(el);

					platforms.add(b);
				}
			}
		}
	}
	
	/**
	 * I take an employee element and read the values in, create
	 * an Employee object and return it
	 */
	private Block getBlock(Element element) {
		
		BLOCKS typeEnum = BLOCKS.valueOf(element.getAttribute("type"));
		String material = element.getAttribute("material");
		MATERIALS matEnum = MATERIALS.nil;
		if(material.equals(""))
		{
			matEnum = MATERIALS.nil;
		}
		else
		{
			matEnum = MATERIALS.valueOf(material);
		}
		double x = Double.parseDouble(element.getAttribute("x"));
		double y = Double.parseDouble(element.getAttribute("y"));
		double rotation = Double.parseDouble(element.getAttribute("rotation"));

		//Create a new Block with the value read from the xml nodes
		Block b = new Block(typeEnum, matEnum, x, y, rotation);

		return b;
	}

	/**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' I will return John
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}


	/**
	 * Calls getTextValue and returns a int value
	 */
	private int getIntValue(Element ele, String tagName) {
		//in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele,tagName));
	}

	public int getNumBirds() {
		return numBirds;
	}

	public int getNumPigs() {
		return pigs.size();
	}
	
	public ArrayList<Block> getAllBlocks()
	{
		ArrayList<Block> toReturn = new ArrayList<Block>();
		toReturn.addAll(blocks);
		toReturn.addAll(pigs);
		toReturn.addAll(platforms);
		return toReturn;
	}
	
	public ArrayList<Block> getNonPigAndPlatformBlocks()
	{
		return blocks;
	}
}
