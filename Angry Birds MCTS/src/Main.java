import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import core.game.LevelLoader;
import core.game.MyLevelGenerator;
import core.game.StateObservation;
import core.game.StructuralElementTreeNode;
import core.game.Types;
import core.game.UnityConstants;

public class Main {

	static StateObservation stateObservation;

	public static void main(String[] args)
	{
		try
		{
			ArrayList<Types.ACTIONS> actions = new ArrayList<Types.ACTIONS>();
			actions.add(Types.ACTIONS.ACTION_PLACE_STRUCTURE);
			actions.add(Types.ACTIONS.ACTION_PLACE_PIG);
			actions.add(Types.ACTIONS.ACTION_NIL);
			sampleMCTS.Agent controller = new sampleMCTS.Agent(actions);
			
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

			ArrayList <StructuralElementTreeNode > allStructuralElementTrees = new ArrayList< StructuralElementTreeNode >();
			// Iterate through the images
			Arrays.sort(xmlFiles);
			for (int i = 0; i < xmlFiles.length; ++i) {
				File file = xmlFiles[i];

				LevelLoader level = new LevelLoader(file.getAbsolutePath());
			    ArrayList< StructuralElementTreeNode > structuralElementTrees = MyLevelGenerator.getStructuralElementsFromFile(file, level);
			    allStructuralElementTrees.addAll(structuralElementTrees);
			}
			
			for(int levelItr = 0; levelItr < 30; ++levelItr)
			{
				StateObservation.DIFFICULTY_LEVEL level = StateObservation.DIFFICULTY_LEVEL.HARD;
				int numBirds = 5;
				switch(level)
				{
				case EASY:
					numBirds = 3;
					break;
				case MEDIUM:
					numBirds = 4;
					break;
				case HARD:
					numBirds = 5;
					break;
				default:
					break;
				}
				stateObservation = new StateObservation(controller, level, numBirds, allStructuralElementTrees);
				int numIterations = 1000; // training iterations
		        for(int i = 0; i < numIterations; ++i)
		        {
		        	System.out.println("CurTrainingIteration: " + i);
		        	stateObservation.askControllerForTrainingAction();
		        }
		        
		        int i = 0;
		        while(!stateObservation.isGameOver())
		        {
		        	System.out.println("CurBestActionIteration: " + i);
		        	stateObservation.advance(stateObservation.askControllerForBestAction(), true /*takingBestAction*/);
		        	++i;
		        }
		        
	    		stateObservation.writeLevelXML(6, numBirds);
	    		
	    	    Path from = Paths.get("level-06.xml");
	    	    String levelFilenameString = "";
	    	    if(levelItr < 10)
	    	    {
	    	    	levelFilenameString = "level-0" + (levelItr + 6) + ".xml";
	    	    }
	    	    else
	    	    {
	    	    	levelFilenameString = "level-" + (levelItr + 6) + ".xml";
	    	    }
	    	    Path to = Paths.get("D:/Desktop/HardLevels/" + levelFilenameString);
	    	    try {
	    			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
			}
		}
		catch(Exception e)
		{
	        e.printStackTrace();
		}
	}
	
}
