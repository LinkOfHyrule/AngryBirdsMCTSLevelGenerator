package core.game;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import enums.Blocks.BLOCKS;
import enums.Materials.MATERIALS;

public class MyLevelGenerator {
	
	public static void main(String[] args)
	{
		//DefaultLevelGeneration.createDefaultLevelsFromScreenshots();
		// After the former step, open up the XML files in the Science Birds Level Editor and resave them to clean them up
		generateRandomLevelsFromCleanedUpGeneratedLevels();
	}

	static void generateRandomLevelsFromCleanedUpGeneratedLevels()
	{
		// Get list of images to process
		File[] xmlFiles = null;

		// Check if argument is a directory or an image
		if ((new File(UnityConstants.cleanedUpLevelsDirectory)).isDirectory()) {
			xmlFiles = new File(UnityConstants.cleanedUpLevelsDirectory).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
					return fileName.endsWith(".xml");
				}
			});
		} else {
			System.err.println("Invalid xml file directory");
			return;
		}

		// Iterate through the images
		Arrays.sort(xmlFiles);
		int levelIndex = 4;
		for (int i = 0; i < xmlFiles.length; ++i) {
		//for (int i = 0; i < 1; ++i) {
			File file = xmlFiles[i];

			LevelLoader level = new LevelLoader(file.getAbsolutePath());
		    ArrayList< StructuralElementTreeNode > structuralElements = getStructuralElementsFromFile(file, level);
		    
		    StructuralElementTreeNode randomStructuralElement1 =
		    		structuralElements.get(ThreadLocalRandom.current().nextInt(0, structuralElements.size()));
		    StructuralElementTreeNode randomStructuralElement2 =
		    		structuralElements.get(ThreadLocalRandom.current().nextInt(0, structuralElements.size()));
		    StructuralElementTreeNode randomStructuralElement3 =
		    		structuralElements.get(ThreadLocalRandom.current().nextInt(0, structuralElements.size()));
		    
		    ArrayList<StructuralElementTreeNode> chosenElements = new ArrayList<StructuralElementTreeNode>();
		    chosenElements.add(randomStructuralElement1);
		    chosenElements.add(randomStructuralElement2);
		    chosenElements.add(randomStructuralElement3);

			ArrayList<Block> blocks = new ArrayList<Block>();
			ArrayList<MyRectangle> placedRects = new ArrayList<MyRectangle>();
			for(StructuralElementTreeNode structuralElement : chosenElements)
			{
				//int randomDepthIndex = ThreadLocalRandom.current().nextInt(0, 2);
				int randomDepthIndex = 0;
				ArrayList<StructuralElementTreeNode> nodes = breadthFirstTraversal(structuralElement, randomDepthIndex);
				//if(nodes.size() > 1)
				//{
					double minX = Double.MAX_VALUE;
					double minY = Double.MAX_VALUE;
					double maxX = -Double.MAX_VALUE;
					double maxY = -Double.MAX_VALUE;
					for(StructuralElementTreeNode node : nodes)
					{
						MyRectangle rect = node.getRect();
						double thisMinX = rect.x;
						double thisMinY = rect.y - rect.height;
						double thisMaxX = rect.x + rect.width;
						double thisMaxY = rect.y;
						if(thisMinX < minX)
							minX = thisMinX;
						if(thisMinY < minY)
							minY = thisMinY;
						if(thisMaxX > maxX)
							maxX = thisMaxX;
						if(thisMaxY > maxY)
							maxY = thisMaxY;
					}
					
					MyRectangle fullBoundingRect = new MyRectangle(minX, maxY, maxX - minX, maxY - minY);

					double unityWidth = fullBoundingRect.width;
					double halfUnityWidth = unityWidth * 0.5;
					double unityHeight = fullBoundingRect.height;
					double halfUnityHeight = unityHeight * 0.5;

					double curUnityLocationCenterX = fullBoundingRect.x + halfUnityWidth;
					double curUnityLocationCenterY = fullBoundingRect.y - halfUnityHeight;
					
					double translationFactorX = 0.0;
					double translationFactorY = 0.0;
					boolean foundOverlap = true;
					MyRectangle newRect = null;
					int numTries = UnityConstants.numPlacementAttempts;
					double newCenterX = 0.0;
					double newCenterY = 0.0;
					boolean needsPlatforms = false;
					do
					{
						// Find the translation that is required to move the rectangle where we want it in the scene.
						// Ground is at -3 Y, and we can go up to 6 Y.
						// X locations are from -3 to 9.
						newCenterX = ThreadLocalRandom.current().nextDouble(
								UnityConstants.levelWidthMin + halfUnityWidth,
								UnityConstants.levelWidthMax - halfUnityWidth);
						//double y = UnityConstants.absolute_ground + halfUnityHeight; // For now, always put structures on the ground
						newCenterY = ThreadLocalRandom.current().nextDouble(
								UnityConstants.levelHeightMin + halfUnityHeight,
								UnityConstants.levelHeightMax - halfUnityHeight);
						translationFactorX = newCenterX - curUnityLocationCenterX;
						translationFactorY = newCenterY - curUnityLocationCenterY;
						needsPlatforms = newCenterY > UnityConstants.levelHeightMin + halfUnityHeight;
						double wiggleRoom = 0.2;
						newRect = new MyRectangle(
								newCenterX - halfUnityWidth - wiggleRoom,
								newCenterY + halfUnityHeight + wiggleRoom,
								unityWidth + (wiggleRoom * 2),
								unityHeight + (wiggleRoom * 2));
						if(needsPlatforms)
						{
							// Will need to place platforms later. Make our bounding rectangle larger to account for them.
							newRect.setRect(
								newRect.x - UnityConstants.platformWidthAndHeight * 0.5,
								newRect.y + UnityConstants.platformWidthAndHeight,
								newRect.width + UnityConstants.platformWidthAndHeight,
								newRect.height + UnityConstants.platformWidthAndHeight);
						}
						foundOverlap = false;
						for(MyRectangle placedRect : placedRects)
						{
							if(newRect.intersects(placedRect))
							{
								foundOverlap = true;
								break;
							}
						}
						numTries--;
					} while(foundOverlap && numTries > 0);
					
					if(!foundOverlap)
					{
						placedRects.add(newRect);
						System.out.println("X: " + (newRect.x + newRect.width * 0.5) + " Y: " + (newRect.y + newRect.height * 0.5) +
								" Width: " + newRect.width + " Height: " + newRect.height);
						
						for(StructuralElementTreeNode node : nodes)
						{
							Block curBlock = node.getBlock();
							Point2D.Double rectLocation = node.getCenterPoint();
							rectLocation.x = rectLocation.x + translationFactorX;
							rectLocation.y = rectLocation.y + translationFactorY;
							blocks.add(new Block(curBlock.getType(), curBlock.getMaterial(), rectLocation.x, rectLocation.y, curBlock.getRotation()));
						}

						if(needsPlatforms)
						{
							// Need to place platforms
							ArrayList<Block> platforms = getPlatformsToPlace(newCenterX, newCenterY, halfUnityWidth, halfUnityHeight);
							blocks.addAll(platforms);
						}
						
						blocks.add(new BoundingRectBlock(
							BLOCKS.SquareSmall,
							MATERIALS.nil,
							newRect.x + newRect.width * 0.5,
							newRect.y - newRect.height * 0.5,
							0.0,
							newRect.width,
							newRect.height));
					}
		    	//}
			}
			
			// Place pigs
			for(int pigItr = 0; pigItr < level.getNumPigs(); ++pigItr)
			{
				// Choose random location for pig, either on the ground or on a structure
				boolean placeOnGround = ThreadLocalRandom.current().nextBoolean();
				if(placeOnGround)
				{
					boolean foundOverlap = false;
					int numTries = UnityConstants.numPlacementAttempts;
					double x = 0.0;
					double y = 0.0;
					do
					{
						x = ThreadLocalRandom.current().nextDouble(
								UnityConstants.levelWidthMin + UnityConstants.pigWidth,
								UnityConstants.levelWidthMax - UnityConstants.pigWidth);
						y = UnityConstants.levelHeightMin + UnityConstants.pigHeight;
						
						double wiggleRoom = 0.2;
						MyRectangle newRect = new MyRectangle(
							x,
							y,
							UnityConstants.pigWidth + wiggleRoom,
							UnityConstants.pigHeight + wiggleRoom);
						foundOverlap = false;
						for(MyRectangle placedRect : placedRects)
						{
							if(newRect.intersects(placedRect))
							{
								foundOverlap = true;
								break;
							}
						}
						numTries--;
					} while(foundOverlap && numTries > 0);
					
					if(!foundOverlap)
					{
						blocks.add(new Block(
							BLOCKS.BasicSmall,
							MATERIALS.nil,
							x + UnityConstants.pigWidth * 0.5,
							y - UnityConstants.pigHeight * 0.5,
							0.0));
					}
					else
					{
						System.out.println("Could not place pig!");
					}
				}
				else
				{
					// Start with a random block, and try to add the pig to the top. Repeat until spot is found.
					for(int blockItr = 0; blockItr < blocks.size(); ++blockItr)
					{
						Block curBlock = blocks.get(blockItr);
						MyRectangle curBlockRect = curBlock.getBlockRect();
						
						MyRectangle rectToPlace = new MyRectangle(
							curBlockRect.x + curBlockRect.width * 0.5 - UnityConstants.pigWidth * 0.5,
							curBlockRect.y + UnityConstants.pigHeight,
							UnityConstants.pigWidth,
							UnityConstants.pigHeight);
						boolean foundOverlap = false;
						for(int blockItr2 = 0; blockItr2 < blocks.size(); ++blockItr2)
						{
							if(blockItr != blockItr2 && blocks.get(blockItr2).getBlockRect().intersects(rectToPlace))
							{
								foundOverlap = true;
								break;
							}
						}
						if(!foundOverlap)
						{
							blocks.add(new Block(
								BLOCKS.BasicSmall,
								MATERIALS.nil,
								rectToPlace.x + UnityConstants.pigWidth * 0.5,
								rectToPlace.y - UnityConstants.pigHeight * 0.5,
								0.0));
							break;
						}
					}
				}
			}

		    String levelNameString = String.format("level-%02d.xml", levelIndex); 
		    System.out.println("Level: " + levelNameString);
			writeLevelXML(levelNameString, levelIndex, blocks, level.getNumBirds());
			levelIndex++;
		}

	}
	
	// Returns the depth of the tree
	static int formStructuralElementTree(
		ArrayList<Block> allBlocks,
		StructuralElementTreeNode curNode)
	{
		MyRectangle curRect = curNode.getRect();
		MyRectangle overlapRect = new MyRectangle(curRect.x, curRect.y - 0.1, curRect.width, curRect.height);
		
		ArrayList<Integer> depths = new ArrayList<Integer>();
		for(Block block : allBlocks)
		{
			MyRectangle rect = block.getBlockRect();
			if(!curRect.equals(rect) && rect.intersects(overlapRect) && rect.y <= (curRect.y - curRect.height))
			{
				//System.out.println("Intersection. rect.x " + rect.x + " rect.y: " + rect.y + " curRect.x " + curRect.x + " curRect.y: " + curRect.y + " curRect.height: " + curRect.height);
				//boolean checkIntersection = rect.intersects(overlapRect);
				StructuralElementTreeNode toAdd = new StructuralElementTreeNode(block);
				curNode.addChild(toAdd);
				int depth = formStructuralElementTree(allBlocks, toAdd);
				depths.add(depth);
			}
		}
		if(depths.size() == 0)
		{
			return 1;
		}
		else
		{
			Integer maxDepth = Collections.max(depths);
			return maxDepth + 1;
		}
	}
	
	public static ArrayList<StructuralElementTreeNode> detectBasicStructuralElements(
		ArrayList<Block> allBlocks)
	{
		// Start at the topmost rectangles that have nothing resting on them and work our way down.
		ArrayList<Block> topBlocks = new ArrayList<Block>();
		
		for(int i = 0; i < allBlocks.size(); ++i)
		{
			Block curBlock = allBlocks.get(i);
			MyRectangle rect = curBlock.getBlockRect();
			MyRectangle overlapRect = new MyRectangle(rect.x, rect.y + 0.1, rect.width, rect.height);
			
			boolean isTopBlock = true;
			for(int j = 0; j < allBlocks.size(); ++j)
			{
				if(j != i)
				{
					Block intersectBlock = allBlocks.get(j);
					MyRectangle rect2 = intersectBlock.getBlockRect();
					if(overlapRect.intersects(rect2))
					{
						isTopBlock = false;
						break;
					}
				}
			}
			if(isTopBlock)
			{
				topBlocks.add(curBlock);
			}
		}
		
		System.out.println("topBlocks size: " + topBlocks.size());

		// Contains the root nodes of all structural elements
		ArrayList<StructuralElementTreeNode> structuralElements = new ArrayList<StructuralElementTreeNode>();
		
		for(int i = 0; i < topBlocks.size(); ++i)
		{
			StructuralElementTreeNode rootNode = new StructuralElementTreeNode(topBlocks.get(i));
			int treeDepth = formStructuralElementTree(allBlocks, rootNode);
			rootNode.setTreeDepth(treeDepth);
			structuralElements.add(rootNode);
		}
		
		System.out.println("structuralElements size: " + structuralElements.size());
		
		return structuralElements;
	}
	
	static ArrayList<StructuralElementTreeNode> breadthFirstTraversal(
		StructuralElementTreeNode root,
		int numTopLevelsToIgnore)
	{
		if (root == null || numTopLevelsToIgnore < 0)
	        return new ArrayList<StructuralElementTreeNode>();
		
	    ArrayList<StructuralElementTreeNode> toReturn = new ArrayList<StructuralElementTreeNode>();
	    HashMap<StructuralElementTreeNode, Boolean> visitedMap = new HashMap<StructuralElementTreeNode, Boolean>();
	    
		Queue<StructuralElementTreeNode> queue = new LinkedList<StructuralElementTreeNode>();
		int currentDepth = 0;
	    int elementsToDepthIncrease = 1;
		int nextElementsToDepthIncrease = 0;
	    queue.clear();
	    queue.add(root);
	    while(!queue.isEmpty())
	    {
	    	StructuralElementTreeNode node = queue.remove();
	    	if(currentDepth >= numTopLevelsToIgnore)
	    	{
	    		toReturn.add(node);
	    	}
			nextElementsToDepthIncrease += node.getNumberOfChildren();
			if (--elementsToDepthIncrease == 0) {
				++currentDepth;
				elementsToDepthIncrease = nextElementsToDepthIncrease;
				nextElementsToDepthIncrease = 0;
			}
			for (StructuralElementTreeNode child : node.getChildren())
			{
				if(visitedMap.get(child) == null)
				{
					queue.add(child);
					visitedMap.put(child, true);
				}
			}
	    }
	    return toReturn;
	}
	
	// write level out in desired xml format
	public static void writeLevelXML(
		String levelFilename,
		int levelIndex,
		ArrayList<Block> blocks,
		int numBirds)
	{
		PrintWriter f = null;
		try {
			f = new PrintWriter(new OutputStreamWriter(new FileOutputStream(levelFilename, false), "UTF-16"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	
	    f.write("<?xml version=\"1.0\" encoding=\"utf-16\"?>\n");
	    f.write("<Level>\n");
	    f.printf("  <BirdsAmount>%s</BirdsAmount>\n", numBirds);
	    f.write("  <GameObjects>\n");

	    for(int i = 0; i < blocks.size(); ++i)
	    {
	    	Block block = blocks.get(i);
	    	if(block instanceof BoundingRectBlock)
	    	{
	    		BoundingRectBlock rectBlock = (BoundingRectBlock)block;
		        f.printf("    <%s type=\"%s\" material=\"%s\" x=\"%.02f\" y=\"%.02f\" rotation=\"%.02f\" width=\"%.02f\" height=\"%.02f\" />\n",
	        		rectBlock.getXMLTag(),
	        		rectBlock.getType().name(),
	        		rectBlock.getMaterialString(),
	        		rectBlock.getCenterPoint().x,
	        		rectBlock.getCenterPoint().y,
	        		rectBlock.getRotation(),
	        		rectBlock.width,
	        		rectBlock.height);
	    	}
	    	else
	    	{
		    	//System.out.println("Type: wood Width: " + i.width + " Height: " + i.height);
		        f.printf("    <%s type=\"%s\" material=\"%s\" x=\"%.02f\" y=\"%.02f\" rotation=\"%.02f\" />\n",
		        	block.getXMLTag(),
		        	block.getType().name(),
	        		block.getMaterialString(),
	        		block.getCenterPoint().x,
	        		block.getCenterPoint().y,
					block.getRotation());
	    	}
	    }
	        
	    f.write("</GameObjects>\n");
	    f.write("</Level>\n");
	
	    f.close();
	    
	    // Copy to Unity
	    Path from = Paths.get(levelFilename);
	    Path to = Paths.get(UnityConstants.unityLevelsDirectory + levelFilename);
	    try {
			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList< StructuralElementTreeNode > getStructuralElementsFromFile(File file, LevelLoader level)
	{
	    System.out.println("Getting structural elements from file: " + file);
	    
		if (file.isDirectory()) {
			return null;
		}
		
		String fileContents = null;
		try {
			fileContents = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileContents = fileContents.replaceAll("[^\\x20-\\x7e]", "");
		
		try(PrintWriter out = new PrintWriter(file.getAbsolutePath(), "UTF-16")){
			out.print(fileContents);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
	    return detectBasicStructuralElements(level.getNonPigAndPlatformBlocks());
	}
	
	public static ArrayList<Block> getPlatformsToPlace(double centerX, double centerY, double halfWidth, double halfHeight)
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		for(double j = centerX - halfWidth; 
			j < centerX + halfWidth; 
			j += (UnityConstants.platformWidthAndHeight - 0.01))
		{
			blocks.add(new Block(
					BLOCKS.Platform,
					MATERIALS.nil,
					j,
					centerY - halfHeight - (UnityConstants.platformWidthAndHeight * 0.5),
					0.0));
		}
		// Place last block
		blocks.add(new Block(
				BLOCKS.Platform,
				MATERIALS.nil,
				centerX + halfWidth,
				centerY - halfHeight - (UnityConstants.platformWidthAndHeight * 0.5),
				0.0));
		return blocks;
	}
	
	public static Block placePig(ArrayList<Block> blocks, ArrayList<Block> pigs, Random randomGenerator)
	{
		ArrayList<Block> allBlocksIncludingPigs = new ArrayList<Block>();
		allBlocksIncludingPigs.addAll(blocks);
		allBlocksIncludingPigs.addAll(pigs);
		// Choose random location for pig, either on the ground or on a structure
		//boolean placeOnGround = randomGenerator.nextBoolean();
		boolean placeOnGround = false;
		//if(blocks.size() == 0 || placeOnGround)
		if(blocks.size() == 0)
			return null;
		if(placeOnGround)
		{
			boolean foundOverlap = false;
			int numTries = UnityConstants.numPlacementAttempts;
			double x = 0.0;
			double y = 0.0;
			do
			{
				double rangeMin = UnityConstants.levelWidthMin + UnityConstants.pigWidth;
				double rangeMax = UnityConstants.levelWidthMax - UnityConstants.pigWidth;
				x = rangeMin + (rangeMax - rangeMin) * randomGenerator.nextDouble();
				y = UnityConstants.levelHeightMin + UnityConstants.pigHeight;
				
				double wiggleRoom = 0.0;
				MyRectangle newRect = new MyRectangle(
					x,
					y,
					UnityConstants.pigWidth + wiggleRoom,
					UnityConstants.pigHeight + wiggleRoom);
				foundOverlap = false;
				for(Block block : allBlocksIncludingPigs)
				{
					if(newRect.intersects(block.getBlockRect()))
					{
						foundOverlap = true;
						break;
					}
				}
				numTries--;
			} while(foundOverlap && numTries > 0);
			
			if(!foundOverlap)
			{
				return new Block(
					BLOCKS.BasicSmall,
					MATERIALS.nil,
					x + UnityConstants.pigWidth * 0.5,
					y - UnityConstants.pigHeight * 0.5,
					0.0);
			}
		}
		else
		{
			// Start with a random block, and try to add the pig to the top. Repeat until spot is found.
			boolean foundOverlap = false;
			int numTries = UnityConstants.numPlacementAttempts;
			MyRectangle rectToPlace = null;
			do
			{
				int randomBlock = randomGenerator.nextInt(blocks.size());
				Block curBlock = blocks.get(randomBlock);
				MyRectangle curBlockRect = curBlock.getBlockRect();
				
				rectToPlace = new MyRectangle(
					curBlockRect.x + curBlockRect.width * 0.5 - UnityConstants.pigWidth * 0.5,
					curBlockRect.y + UnityConstants.pigHeight,
					UnityConstants.pigWidth,
					UnityConstants.pigHeight);
				foundOverlap = false;
				for(int blockItr2 = 0; blockItr2 < allBlocksIncludingPigs.size(); ++blockItr2)
				{
					if(randomBlock != blockItr2
						&& allBlocksIncludingPigs.get(blockItr2).getBlockRect().intersects(rectToPlace))
					{
						foundOverlap = true;
						break;
					}
				}
				numTries--;
			} while(foundOverlap && numTries > 0);
			
			if(!foundOverlap)
			{
				return new Block(
					BLOCKS.BasicSmall,
					MATERIALS.nil,
					rectToPlace.x + UnityConstants.pigWidth * 0.5,
					rectToPlace.y - UnityConstants.pigHeight * 0.5,
					0.0);
			}
		}
		//System.out.println("Could not place pig!");
		return null;
	}
	
	public static MyRectangle getMasterBoundingRectangleOfStructuralElement(
		ArrayList<StructuralElementTreeNode> structuralElementNodes)
	{
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		for(StructuralElementTreeNode node : structuralElementNodes)
		{
			MyRectangle rect = node.getRect();
			double thisMinX = rect.x;
			double thisMinY = rect.y - rect.height;
			double thisMaxX = rect.x + rect.width;
			double thisMaxY = rect.y;
			if(thisMinX < minX)
				minX = thisMinX;
			if(thisMinY < minY)
				minY = thisMinY;
			if(thisMaxX > maxX)
				maxX = thisMaxX;
			if(thisMaxY > maxY)
				maxY = thisMaxY;
		}
		
		return new MyRectangle(minX, maxY, maxX - minX, maxY - minY);
	}

	public static MyRectangle getMasterBoundingRectangleOfBlocks(
		ArrayList<Block> blocks)
	{
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		for(Block block : blocks)
		{
			MyRectangle rect = block.getBlockRect();
			double thisMinX = rect.x;
			double thisMinY = rect.y - rect.height;
			double thisMaxX = rect.x + rect.width;
			double thisMaxY = rect.y;
			if(thisMinX < minX)
				minX = thisMinX;
			if(thisMinY < minY)
				minY = thisMinY;
			if(thisMaxX > maxX)
				maxX = thisMaxX;
			if(thisMaxY > maxY)
				maxY = thisMaxY;
		}
		
		return new MyRectangle(minX, maxY, maxX - minX, maxY - minY);
	}
	
	public static MyRectangle translateMasterBoundingRectangleAndAddPlatforms(
		MyRectangle masterBoundingRectangle,
		double newCenterX,
		double newCenterY,
		double widthConstraint,
		double heightConstraint,
		boolean needsPlatforms)
	{
		double unityWidth = masterBoundingRectangle.width;
		double halfUnityWidth = unityWidth * 0.5;
		double unityHeight = masterBoundingRectangle.height;
		double halfUnityHeight = unityHeight * 0.5;

		MyRectangle newRect = new MyRectangle(
			newCenterX - halfUnityWidth,
			newCenterY + halfUnityHeight,
			unityWidth,
			unityHeight);
		//boolean needsPlatforms = newCenterY >= UnityConstants.levelHeightMin + halfUnityHeight;
		if(needsPlatforms)
		{
			// Will need to place platforms later. Make our bounding rectangle larger to account for them.
			newRect.setRect(
				newRect.x - UnityConstants.platformWidthAndHeight * 0.5,
				newRect.y + UnityConstants.platformWidthAndHeight,
				newRect.width + UnityConstants.platformWidthAndHeight,
				newRect.height + UnityConstants.platformWidthAndHeight);
		}
		return newRect;
	}
	
	public static boolean masterBoundingRectangleFitConstraints(
		MyRectangle masterBoundingRectangle,
		double widthConstraint,
		double heightConstraint)
	{
		return masterBoundingRectangle.width <= widthConstraint && masterBoundingRectangle.height <= heightConstraint;
	}

	public static ArrayList<Block> moveStructuralElementNodesToNewLocation(
		ArrayList<StructuralElementTreeNode> nodes,
		double newCenterX,
		double newCenterY,
		boolean needsPlatforms)
	{
		MyRectangle fullBoundingRect = getMasterBoundingRectangleOfStructuralElement(nodes);

		double unityWidth = fullBoundingRect.width;
		double halfUnityWidth = unityWidth * 0.5;
		double unityHeight = fullBoundingRect.height;
		double halfUnityHeight = unityHeight * 0.5;

		double curUnityLocationCenterX = fullBoundingRect.x + halfUnityWidth;
		double curUnityLocationCenterY = fullBoundingRect.y - halfUnityHeight;
		
		double translationFactorX = 0.0;
		double translationFactorY = 0.0;

		translationFactorX = newCenterX - curUnityLocationCenterX;
		translationFactorY = newCenterY - curUnityLocationCenterY;
		double wiggleRoom = 0.0;
		MyRectangle newRect = new MyRectangle(
				newCenterX - halfUnityWidth,
				newCenterY + halfUnityHeight + wiggleRoom,
				unityWidth,
				unityHeight + (wiggleRoom * 2));
		if(needsPlatforms)
		{
			// Will need to place platforms later. Make our bounding rectangle larger to account for them.
			newRect.setRect(
				newRect.x - UnityConstants.platformWidthAndHeight * 0.5,
				newRect.y + UnityConstants.platformWidthAndHeight,
				newRect.width + UnityConstants.platformWidthAndHeight,
				newRect.height + UnityConstants.platformWidthAndHeight);
		}
		
		ArrayList<Block> blocks = new ArrayList<Block>();

		//System.out.println("X: " + (newRect.x + newRect.width * 0.5) + " Y: " + (newRect.y + newRect.height * 0.5) +
		//		" Width: " + newRect.width + " Height: " + newRect.height);
		
		for(StructuralElementTreeNode node : nodes)
		{
			Block curBlock = node.getBlock();
			Point2D.Double rectLocation = node.getCenterPoint();
			rectLocation.x = rectLocation.x + translationFactorX;
			rectLocation.y = rectLocation.y + translationFactorY;
			blocks.add(new Block(curBlock.getType(), curBlock.getMaterial(), rectLocation.x, rectLocation.y, curBlock.getRotation()));
		}

		if(needsPlatforms)
		{
			// Need to place platforms
			ArrayList<Block> platforms = getPlatformsToPlace(newCenterX, newCenterY, halfUnityWidth, halfUnityHeight);
			blocks.addAll(platforms);
		}
		
		/*blocks.add(new BoundingRectBlock(
			BLOCKS.SquareSmall,
			MATERIALS.nil,
			newRect.x + newRect.width * 0.5,
			newRect.y - newRect.height * 0.5,
			0.0,
			newRect.width,
			newRect.height));*/
		
		return blocks;
	}
}
