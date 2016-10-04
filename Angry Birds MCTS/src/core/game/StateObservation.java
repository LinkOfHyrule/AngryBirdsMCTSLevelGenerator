package core.game;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import ab.demo.DataLabAgent;
import ab.demo.MyNaiveAgent;
import ab.vision.GameStateExtractor.GameState;
import core.game.Types.ACTIONS;
import enums.Blocks.BLOCKS;
import tools.ElapsedCpuTimer;
import tools.ElapsedCpuTimer.TimerType;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StateObservation {

	// Approach:
	// Start at bottom left, and work our way up to the top right like a sideways typewriter.
	// An action of NIL skips a location that a structure could be placed.
	// If a structure is placed in midair, a platform gets placed underneath.
	
	public enum DIFFICULTY_LEVEL {
		EASY,
		MEDIUM,
		HARD
	};	
	
	private sampleMCTS.Agent controller;
	private ArrayList<Block> blocks;
	private ArrayList<Block> pigs;
	private ArrayList <StructuralElementTreeNode > allStructuralElementTrees;
	private double curX;
	private double curWidth;
	private double curY;
	private DIFFICULTY_LEVEL difficultyLevel;
	private int numBirds;
	private static int randomSeed = 100;
	private CopyableRandom randomStructureGenerator;
	private MyNaiveAgent na;
	private DataLabAgent da;
	private int numStructuresPlaced;
	private ArrayList<Block> lastStructurePlaced;
	private boolean placedPigOnLastStructure;
	private int numStructuresWithPigs;
	
    public StateObservation(
		sampleMCTS.Agent controller,
		DIFFICULTY_LEVEL difficultyLevel,
		int numBirds,
		ArrayList <StructuralElementTreeNode > allStructuralElementTrees)
    {
    	this.controller = controller;
    	this.blocks = new ArrayList<Block>();
    	this.pigs = new ArrayList<Block>();
    	this.curX = UnityConstants.levelWidthMin;
    	this.curWidth = 0.0;
    	this.curY = UnityConstants.levelHeightMin;
    	this.difficultyLevel = difficultyLevel;
    	this.numBirds = numBirds;
    	this.allStructuralElementTrees = allStructuralElementTrees;
    	this.randomStructureGenerator = new CopyableRandom(randomSeed);
    	this.na = new MyNaiveAgent();
    	na.setCurrentLevel(4);
    	this.da = new DataLabAgent();
    	da.setCurrentLevel(4);
    	this.numStructuresPlaced = 0;
    	this.lastStructurePlaced = new ArrayList<Block>();
    	this.placedPigOnLastStructure = false;
    	this.numStructuresWithPigs = 0;
    }
    
    public StateObservation(
		StateObservation observation)
    {
    	this.controller = observation.controller;
    	this.blocks = new ArrayList<Block>(observation.getBlocks().size());
        for (Block block : observation.getBlocks()) {
        	this.getBlocks().add(new Block(block));
        }
    	this.pigs = new ArrayList<Block>(observation.getPigs().size());
        for (Block block : observation.getPigs()) {
        	this.getPigs().add(new Block(block));
        }
        this.curX = observation.curX;
        this.curWidth = observation.curWidth;
        this.curY = observation.curY;
    	this.difficultyLevel = observation.difficultyLevel;
    	this.numBirds = observation.numBirds;
    	// Since this ArrayList doesn't change, no need to do deep copy
    	this.allStructuralElementTrees = observation.allStructuralElementTrees;
    	this.randomStructureGenerator = observation.randomStructureGenerator.copy();
    	this.na = observation.na;
    	this.da = observation.da;
    	this.numStructuresPlaced = observation.numStructuresPlaced;
    	this.placedPigOnLastStructure = observation.placedPigOnLastStructure;
    	this.lastStructurePlaced = new ArrayList<Block>(observation.lastStructurePlaced.size());
        for (Block block : observation.lastStructurePlaced) {
        	this.lastStructurePlaced.add(new Block(block));
        }
    	this.numStructuresWithPigs = observation.numStructuresWithPigs;
    }

    /**
     * Returns an exact copy of the state observation object.
     *
     * @return a copy of the state observation.
     */
    public StateObservation copy() {
        return new StateObservation(this);
    }
    
    public void askControllerForTrainingAction()
    {
    	ElapsedCpuTimer timer = new ElapsedCpuTimer(TimerType.CPU_TIME);
		timer.setMaxTimeMillis(100);
		controller.act(this, timer);
    }
    
    public ACTIONS askControllerForBestAction()
    {
    	ElapsedCpuTimer timer = new ElapsedCpuTimer(TimerType.CPU_TIME);
		timer.setMaxTimeMillis(100);
		ACTIONS bestAction = controller.actBestAction(this, timer);
		System.out.println(bestAction);
		return bestAction;
    }

    /**
     * Advances the state using the action passed as the move of the agent.
     * It updates all entities in the game. It modifies the object 'this' to
     * represent the next state after the action has been executed and all
     * entities have moved.
     * <p/>
     * Note: stochastic events will not be necessarily the same as in the real game.
     *
     * @param action agent action to execute in the next cycle.
     */
    public void advance(Types.ACTIONS action, boolean takingBestAction) {
    	if(takingBestAction)
    	{
    		System.out.println("CurAction: " + action);
    	}

		if(isGameOver())
		{
			// Game is over. Advances should not be happening.
			throw new IllegalArgumentException("advance() was called after game over.");
		}
		
		switch(action)
		{
		case ACTION_NIL:
			if(curY <= UnityConstants.levelHeightMin)
			{
				// If we're starting out a new segment, 
				// choose the width to be 25% of the total stage width.
				curWidth = UnityConstants.totalLevelWidthMax * 0.25;
			}
			// Since it's a NIL action, we just increment
			// by a platform's height (an arbitrary number).
			double incrementMin = UnityConstants.platformWidthAndHeight;
			double incrementMax = UnityConstants.levelHeightMax + 1.0 - curY;
			double randomIncrement = incrementMin + (incrementMax - incrementMin) * randomStructureGenerator.nextDouble();
			curY += randomIncrement;
			break;
		case ACTION_PLACE_STRUCTURE:
			int numPlacementAttempts = UnityConstants.numPlacementAttempts;
			boolean foundStructure = false;
			ArrayList<StructuralElementTreeNode> nodes = null;
			MyRectangle masterBoundingRect = null;
			boolean onGround = curY <= UnityConstants.levelHeightMin;
			do
			{
				int structureToCheck = randomStructureGenerator.nextInt(allStructuralElementTrees.size());
				StructuralElementTreeNode structuralElement = allStructuralElementTrees.get(structureToCheck);
				int numTopLevelsToIgnore = randomStructureGenerator.nextInt(structuralElement.getTreeDepth());
				nodes = MyLevelGenerator.breadthFirstTraversal(
					structuralElement,
					numTopLevelsToIgnore);
				if(nodes.size() <= 0)
				{
					continue;
				}
				masterBoundingRect = MyLevelGenerator.getMasterBoundingRectangleOfStructuralElement(nodes);
				masterBoundingRect = MyLevelGenerator.translateMasterBoundingRectangleAndAddPlatforms(
					masterBoundingRect,
					curX + curWidth * 0.5,
					curY + masterBoundingRect.height * 0.5,
					curWidth,
					UnityConstants.levelHeightMax - curY,
					!onGround);
				if(onGround)
				{
					// If we're starting out a new segment, and the controller chooses a structure,
					// choose the width of the current segment to be the next structure's width.
					curWidth = masterBoundingRect.width;
				}
				foundStructure = MyLevelGenerator.masterBoundingRectangleFitConstraints(
					masterBoundingRect,
					curWidth,
					UnityConstants.levelHeightMax - curY);
		    	
				numPlacementAttempts--;
			} while(!foundStructure && numPlacementAttempts > 0);

			if(foundStructure)
			{
				ArrayList<Block> newNodes = MyLevelGenerator.moveStructuralElementNodesToNewLocation(
					nodes,
					curX + curWidth * 0.5,
					curY + masterBoundingRect.height * 0.5,
					!onGround);
				getBlocks().addAll(newNodes);

				curY += masterBoundingRect.height;
				// TODO: fix this so I don't need this curY hack for the ground-platform transition
				//if(onGround)
				//{
					curY += UnityConstants.platformWidthAndHeight * 2.0;
				//}
				// Give a little wiggle room for the next platform
				//curY += 0.1;
				
				numStructuresPlaced++;
				placedPigOnLastStructure = false;
				lastStructurePlaced.clear();
				lastStructurePlaced.addAll(newNodes);
			}
			else
			{
				// Didn't find a structure to fit. Go to the next segment.
				curY = UnityConstants.levelHeightMax;
			}
			break;
		case ACTION_PLACE_PIG:
			Block pig = MyLevelGenerator.placePig(lastStructurePlaced, pigs, randomStructureGenerator);
			if(pig != null)
			{
				pigs.add(pig);
				
				if(!placedPigOnLastStructure)
				{
					numStructuresWithPigs++;
					placedPigOnLastStructure = true;
				}
				
				Point2D.Double centerPoint = pig.getCenterPoint();
				if(centerPoint.getX() >= curX
					&& centerPoint.getX() <= curX + curWidth
					&& centerPoint.getY() > curY)
				{
					curY = centerPoint.getY() + pig.getBlockRect().getHeight() * 0.5;
				}
			}
			break;
		default:
			break;
		}
		
		if(curY >= UnityConstants.levelHeightMax)
		{
			// We're done with a segment. Reset Y, and increment X.
			if(curX + curWidth >= UnityConstants.levelWidthMax)
			{
				// Incremented to the end of the level. Game over.
				curX = UnityConstants.levelWidthMax;
				curY = UnityConstants.levelHeightMax;
			}
			else
			{
				// End of segment, reset Y
				curX += curWidth;
				curY = UnityConstants.levelHeightMin;
			}
		}
    }

    /**
     * Gets the score of the game at this observation.
     * @return score of the game.
     */
    public double getGameScore()
    {
    	double toReturn = 0.0;
    	int idealNumBlocks = 0;
    	int idealNumPigs = 0;
    	switch(difficultyLevel)
    	{
    	case EASY:
    		idealNumBlocks = 30;
    		idealNumPigs = 3;
    		break;
    	case MEDIUM:
    		// 50
    		idealNumBlocks = 50;
    		idealNumPigs = 4;
    		break;
    	case HARD:
    		// 70
    		idealNumBlocks = 70;
    		idealNumPigs = 5;
    		break;
    	default:
    		break;
    	}
    	
    	// Num Blocks Heuristic: The smaller the distance, the more points awarded
		int distanceFromIdealNumBlocks = Math.abs(idealNumBlocks - getBlocks().size());
		double distancePointsToAward = 7000.0;
		if(distanceFromIdealNumBlocks != 0)
		{
			distancePointsToAward /= (double)distanceFromIdealNumBlocks;
		}
		toReturn += distancePointsToAward;
		
		// Num Pigs Heuristic: The smaller the distance, the more points awarded
		int distanceFromIdealNumPigs = Math.abs(idealNumPigs - getPigs().size());
		double pigPointsToAward = 5000.0;
		if(distanceFromIdealNumPigs != 0)
		{
			pigPointsToAward /= (double)distanceFromIdealNumPigs;
		}
		toReturn += pigPointsToAward;

		// Num Structures With Pigs Heuristic: The smaller the distance, the more points awarded
		int distanceFromIdealNumStructuresWithPigs = Math.abs(numStructuresPlaced - numStructuresWithPigs);
		double structureWithPigPointsToAward = 3000.0;
		if(distanceFromIdealNumStructuresWithPigs != 0)
		{
			structureWithPigPointsToAward /= (double)distanceFromIdealNumStructuresWithPigs;
		}
		toReturn += structureWithPigPointsToAward;
		
		double pigsWithinStructurePoints = 2000.0;
		for(Block pig : pigs)
		{
			MyRectangle curRect = pig.getBlockRect();
			// Increment the pig rect upward until it either hits a block or the top of the level.
			// If it hits a platform or the top of the level, then don't award the points.
			// If it hits a normal block, then award the points, because the pig is inside a structure.
			boolean foundIntersection = false;
			Block intersectionBlock = null;
			do
			{
				curRect.y += 0.1;
				for(Block block : blocks)
				{
					if(curRect.intersects(block.getBlockRect()))
					{
						foundIntersection = true;
						intersectionBlock = block;
						break;
					}
				}
			}
			while(!foundIntersection && curRect.y < UnityConstants.levelHeightMax);
			if(foundIntersection && intersectionBlock.getType() != BLOCKS.Platform)
			{
				toReturn += pigsWithinStructurePoints;
			}
		}

		// The more blocks that are "under" the square root function, the more points awarded
		double squareRootPointsToAwardPerBlock = 2000.0 / blocks.size();
		double normalizedXOffset = Math.abs(UnityConstants.levelWidthMin);
		double normalizedYFactor = UnityConstants.levelHeightMax / Math.sqrt(UnityConstants.totalLevelWidthMax);
		for(Block block : blocks)
		{
			Point2D.Double curPosition = block.getCenterPoint();
			double expectedY = Math.sqrt(curPosition.x + normalizedXOffset) * normalizedYFactor;
			if(curPosition.y < expectedY)
			{
				toReturn += squareRootPointsToAwardPerBlock;
			}
		}
		
		// The more spread out around the center the level is, the more points awarded
		// (normal distribution, bell-curve looking)
		double gaussianPointsToAwardPerBlock = 2000.0 / blocks.size();
		for(Block block : blocks)
		{
			if(block.getCenterY()
				<= UnityConstants.getNormalProbabilityAtZ(block.getCenterX()) * UnityConstants.normalDistributionHeightFactor)
			{
				toReturn += gaussianPointsToAwardPerBlock;
			}
		}
		
        return toReturn;
    }

    /**
     * Indicates if there is a game winner in the current observation.
     * Possible values are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
     * Types.WINNER.NO_WINNER.
     * @return the winner of the game.
     */
    public Types.WINNER getGameWinner()
    {
    	// A game is won by the player if the naive agent can beat it.
    	// Otherwise, the player loses.
    	
    	Types.WINNER winState = Types.WINNER.NO_WINNER;
    	
    	if(pigs.size() == 0)
    	{
    		winState = Types.WINNER.PLAYER_LOSES;
    	}
    	else
    	{
        	writeLevelXML(na.getCurrentLevel(), numBirds);
        	GameState state = na.run();
        	GameState stateDataLab = da.run();
        	
        	if((difficultyLevel != DIFFICULTY_LEVEL.EASY && (state == GameState.WON || stateDataLab == GameState.WON))
    			|| (state == GameState.WON && stateDataLab == GameState.WON))
        	{
        		winState = Types.WINNER.PLAYER_WINS;
        	}
        	else if(state == GameState.LOST || stateDataLab == GameState.LOST)
    		{
        		winState = Types.WINNER.PLAYER_LOSES;
    		}
        	else
        	{
        		winState = Types.WINNER.NO_WINNER;
        	}
        	
        	// Set up level for next iteration
        	if(na.getCurrentLevel() == 4)
        	{
            	na.setCurrentLevel(5);
            	da.setCurrentLevel(5);
        	}
        	else
        	{
            	na.setCurrentLevel(4);
            	da.setCurrentLevel(4);
        	}
    	}

		System.out.println("getGameWinner() " + winState);
    	return winState;
    }

    /**
     * Indicates if the game is over or if it hasn't finished yet.
     * @return true if the game is over.
     */
    public boolean isGameOver()
    {
        return curX >= UnityConstants.levelWidthMax
    		&& curY >= UnityConstants.levelHeightMax;
    }

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	public ArrayList<Block> getPigs() {
		return pigs;
	}
	
	public void writeLevelXML(int levelIndex, int numBirds)
	{
		ArrayList<Block> allBlocks = new ArrayList<Block>();
		ArrayList<Block> blocks = getBlocks();
		ArrayList<Block> pigs = getPigs();
		allBlocks.addAll(blocks);
	    //System.out.println("Blocks size: " + blocks.size());
		allBlocks.addAll(pigs);
	    //System.out.println("Pigs size: " + pigs.size());
		MyLevelGenerator.writeLevelXML(
			"level-0" + levelIndex + ".xml" /*levelFilename*/,
			levelIndex /*levelIndex*/,
			allBlocks,
			numBirds);
	}
}
