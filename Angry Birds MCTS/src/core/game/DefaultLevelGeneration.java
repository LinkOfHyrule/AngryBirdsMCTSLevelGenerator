package core.game;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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
import java.util.List;

import javax.imageio.ImageIO;

import ab.utils.ImageSegFrame;
import ab.vision.ABObject;
import ab.vision.ShowSeg;
import ab.vision.Vision;
import ab.vision.VisionRealShape;
import ab.vision.real.shape.Poly;
import enums.Blocks.BLOCKS;
import enums.Materials.MATERIALS;

public class DefaultLevelGeneration {

	private static class BlockWithRotation
	{
		BLOCKS block;
		double rotation;
		
		public BlockWithRotation(BLOCKS block, double rotation)
		{
			this.block = block;
			this.rotation = rotation;
		}
	}
	
	private static String imageDirectory = "C:\\AngryBirdsScreenshots";

	static BlockWithRotation convertRectToBlock(
		Rectangle block,
		int screenWidth,
		int screenHeight)
	{
		double ratio = (double)block.width / (double)block.height;
		
		double normalWidthHeight = screenWidth * 0.00366032210834553440702781844802; // ~5 at 1366 px width
		
		double tinySize = screenWidth * 0.00951683748169838945827232796486; // ~13 at 1366 px width
		double smallSize = screenWidth * 0.02342606149341142020497803806735; // ~32 at 1366 px width
		double mediumSize = screenWidth * 0.05344070278184480234260614934114; // ~73 at 1366 px width
		
		if(ratio < 0.65)
		{
			// Vertical
			if(block.width >= normalWidthHeight - 2 && block.width <= normalWidthHeight + 2)
			{
				// Block is vertical and thin
				if(block.height >= tinySize - 5 && block.height <= tinySize + 5)
				{
					return new BlockWithRotation(BLOCKS.RectTiny, 90.0);
				}
				else if(block.height >= smallSize - 9 && block.height <= smallSize + 9)
				{
					return new BlockWithRotation(BLOCKS.RectSmall, 90.0);
				}
				else if(block.height >= mediumSize - 25 && block.height <= mediumSize + 25)
				{
					return new BlockWithRotation(BLOCKS.RectMedium, 90.0);
				}
			}
			else if(block.width >= tinySize - 5 && block.width <= tinySize + 5)
			{
				// Block is vertical and fat
				return new BlockWithRotation(BLOCKS.RectFat, 90.0);
			}
		}
		if(ratio >= 0.65 && ratio <= 1.35)
		{
			if(block.height >= normalWidthHeight - 2 && block.height <= normalWidthHeight + 2)
			{
				return new BlockWithRotation(BLOCKS.SquareTiny, 0.0);
			}
			else
			{
				return new BlockWithRotation(BLOCKS.SquareSmall, 0.0);
			}
		}
		else
		{
			// Horizontal
			if(block.height >= normalWidthHeight - 2 && block.height <= normalWidthHeight + 2)
			{
				if(block.width >= tinySize - 5 && block.width <= tinySize + 5)
				{
					return new BlockWithRotation(BLOCKS.RectTiny, 0.0);
				}
				else if(block.width >= smallSize - 9 && block.width <= smallSize + 9)
				{
					return new BlockWithRotation(BLOCKS.RectSmall, 0.0);
				}
				else if(block.width >= mediumSize - 25 && block.width <= mediumSize + 25)
				{
					return new BlockWithRotation(BLOCKS.RectMedium, 0.0);
				}
			}
			else if(block.height >= tinySize - 5 && block.height <= tinySize + 5)
			{
				return new BlockWithRotation(BLOCKS.RectFat, 0.0);
			}
		}
		
		return null;
	}
	
	public static void createDefaultLevelsFromScreenshots()
	{
		BufferedImage screenshot = null;

		// Get list of images to process
		File[] images = null;

		// Check if argument is a directory or an image
		if ((new File(imageDirectory)).isDirectory()) {
			images = new File(imageDirectory).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
					return fileName.endsWith(".png");
				}
			});
		} else {
			System.err.println("Invalid image directory");
			return;
		}

		// Iterate through the images
		Arrays.sort(images);
		int levelIndex = 4;
		for (int i = 0; i < images.length; ++i) {
			File file = images[i];
			if (file.isDirectory()) {
				continue;
			}

			// Load the screenshot
			try {
				screenshot = ImageIO.read(file);
			} catch (IOException e) {
				System.err.println("ERROR: could not load image " + file);
				System.exit(1);
			}
			
			// MBRs
			Vision vision = new Vision(screenshot);
			List<Rectangle> pigs = vision.getMBRVision().findPigsMBR();
			List<Rectangle> redBirds = vision.getMBRVision().findRedBirdsMBRs();
			List<Rectangle> blueBirds = vision.getMBRVision().findBlueBirdsMBRs();
			List<Rectangle> yellowBirds = vision.getMBRVision().findYellowBirdsMBRs();
			List<Rectangle> whiteBirds = vision.getMBRVision().findWhiteBirdsMBRs();
			List<Rectangle> blackBirds = vision.getMBRVision().findBlackBirdsMBRs();
			int numBirds = redBirds.size() + blueBirds.size() + yellowBirds.size() + whiteBirds.size() + blackBirds.size();
			List<Rectangle> woodBlocks = vision.getMBRVision().findWoodMBR();
			List<Rectangle> stoneBlocks = vision.getMBRVision().findStonesMBR();
			List<Rectangle> iceBlocks = vision.getMBRVision().findIceMBR();
			List<MaterialWithRectangle> materialBlocks = new ArrayList<MaterialWithRectangle>();
			for(Rectangle woodBlock : woodBlocks)
			{
				materialBlocks.add(new MaterialWithRectangle(MATERIALS.wood, woodBlock));
			}
			for(Rectangle stoneBlock : stoneBlocks)
			{
				materialBlocks.add(new MaterialWithRectangle(MATERIALS.stone, stoneBlock));
			}
			for(Rectangle iceBlock : iceBlocks)
			{
				materialBlocks.add(new MaterialWithRectangle(MATERIALS.ice, iceBlock));
			}
			
			//List<Rectangle> TNTs = vision.getMBRVision().findTNTsMBR();
			//Rectangle sling = vision.findSlingshotMBR();
			
			// Realshapes
		    VisionRealShape visionRealShape = new VisionRealShape(screenshot);
		    //List<ABObject> objectsReal = visionRealShape.findObjects();
		    //List<ABObject> pigsReal = visionRealShape.findPigs();
		    List<ABObject> hillsReal = visionRealShape.findHills();
		    //List<ABObject> birdsReal = visionRealShape.findBirds();
		    //Rectangle slingReal = visionRealShape.findSling();
			
		    int screenHeight = screenshot.getHeight();
		    int screenWidth = screenshot.getWidth();
		    
		    System.out.println("Filename: " + file);
		    String level = String.format("level-%02d.xml", levelIndex); 
		    System.out.println("Level: " + level);
		    try
		    {
		    	writeLevelXML(level, levelIndex, pigs, numBirds, materialBlocks, null, hillsReal, screenWidth, screenHeight, true);
		    }
		    catch(Exception e)
		    {
				e.printStackTrace();

		    	// Pull up window with level that had issues.
				int[][] meta = ShowSeg.computeMetaInformation(screenshot);
				screenshot = ShowSeg.drawMBRs(screenshot);
				//screenshot = ShowSeg.drawRealshape(screenshot);
				ImageSegFrame frame = new ImageSegFrame("Image Segmentation", screenshot, meta);
				//frame.refresh(screenshot, meta);
				frame.waitForKeyPress();
				frame.close();
		    }
			
			levelIndex++;
		}
	}

	// write level out in desired xml format
	static void writeLevelXML(
		String levelFilename,
		int levelIndex,
		List<Rectangle> pigs,
		int numBirds,
		List<MaterialWithRectangle> blocks,
		List<Point2D.Double> blockPoints,
		List<ABObject> hillsReal,
		int screenWidth,
		int screenHeight,
		boolean translationRequired)
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
	    f.printf("<BirdsAmount>%s</BirdsAmount>\n", numBirds);
	    f.write("<GameObjects>\n");

	    TranslatePixelCoordsToUnity translator = new TranslatePixelCoordsToUnity(levelIndex, screenHeight);

	    System.out.println("Blocks size: " + blocks.size());
	    for(int i = 0; i < blocks.size(); ++i)
	    {
	    	MaterialWithRectangle matWithRect = blocks.get(i);
	    	//System.out.println("Type: wood Width: " + i.width + " Height: " + i.height);
	    	BlockWithRotation blockWithRot = convertRectToBlock(matWithRect.rect, screenWidth, screenHeight);
	    	Point2D.Double point = null;
	    	if(translationRequired)
	    		point = translator.convertPixelBlockCoordToUnity(matWithRect.rect);
	    	else
	    		point = blockPoints.get(i);
	        f.printf("<Block type=\"%s\" material=\"%s\" x=\"%.02f\" y=\"%.02f\" rotation=\"%.02f\" />\n",
        		blockWithRot.block.name(),
        		matWithRect.mat.name(),
        		point.x,
				point.y,
				blockWithRot.rotation);
	    }
	    
	    for(Rectangle i : pigs)
	    {	    	
	    	Point2D.Double point = translator.convertPixelBlockCoordToUnity(i);
	        f.printf("<Pig type=\"BasicSmall\" material=\"\" x=\"%.02f\" y=\"%.02f\" rotation=\"0\" />\n",
        		point.x,
        		point.y);
	    }
	    
	    for(ABObject i : hillsReal)
	    {
	    	Polygon p = ((Poly)i).polygon;
	    	for(int j = 0; j < p.npoints; ++j)
	    	{
	    		int toCompareIndex = 0;
	    		if(j == p.npoints - 1)
	    		{
	    			toCompareIndex = 0;
	    		}
	    		else
	    		{
	    			toCompareIndex = j + 1;
	    		}
	    		int toCompare = p.ypoints[toCompareIndex];
	    		int horizontalCheck = p.ypoints[j] - toCompare;
	    		if(horizontalCheck >= -2 && horizontalCheck <= 2)
	    		{
	    			// Line segment is roughly horizontal. Put down platforms along it.
	    			int startX = p.xpoints[j];
	    			int endX = p.xpoints[toCompareIndex];
    				// System.out.println("Found horizontal. StartX: " + startX + " EndX: " + endX);
	    			if(startX > endX)
	    			{
	    				int temp = endX;
	    				endX = startX;
	    				startX = temp;
	    			}
    				ArrayList<Point2D.Double> platforms = translator.getPlatformCoords(startX, endX, toCompare);
	    			for(Point2D.Double platform : platforms)
	    			{
	    				f.printf("<Platform type=\"Platform\" material=\"\" x=\"%.02f\" y=\"%.02f\" />\n",
							platform.x,
							platform.y);
	    			}
		        }
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
	
}
