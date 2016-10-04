/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import dl.heuristics.AbstractHeuristic;
import dl.heuristics.BuildingHeuristic;
import dl.heuristics.DestroyAsManyPigsAtOnceAsPossibleHeuristic;
import dl.heuristics.DynamiteHeuristic;
import dl.heuristics.RoundStoneHeuristic;
import dl.heuristics.SceneState;
import dl.utils.DLUtils;
import dl.utils.LogWriter;
import ab.vision.Vision;

public class DataLabAgent {

	private ActionRobot aRobot;
	private Random randomGenerator;
	private int currentLevel = 0;
	public static int time_limit = 12;
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;
	
	// a standalone implementation of the Naive Agent
	public DataLabAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();
		
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();
	}

	// run the client
	public GameState run() {

		aRobot.loadLevel(currentLevel);

		GameState state = null;
		do {
			state = solve();
			System.out.println("CURRENT STATE DATALAB AGENT: " + state.toString());
			
			if(state != GameState.PLAYING)
			{
				LogWriter.lastScore = 0;
			}

			//If the level is solved , go to the next level
			if (state == GameState.WON) 
			{
				//levelSchemer.updateStats(aRobot, true);
				
				aRobot.loadLevel(currentLevel);

				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			}
			//If lost, then restart the level
			else if (state == GameState.LOST) 
			{
				//levelSchemer.updateStats(aRobot, false);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}
		}
		while (state != GameState.WON && state != GameState.LOST);
		return state;
	}
	
	public void setCurrentLevel(int levelID)
	{
		// make a new trajectory planner whenever a new level is entered
		tp = new TrajectoryPlanner();

		// first shot on this level, try high shot first
		firstShot = true;
		
		currentLevel = levelID;
	}

	public int getCurrentLevel()
	{
		return currentLevel;
	}
	
	private double distance(Point p1, Point p2) {
		
		return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
	}

	public GameState solve()
	{
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);
		
		// find the slingshot
		Rectangle sling = vision.findSlingshotRealShape();

		// Get bird type on sling.
		ABType birdOnSling = vision.getBirdTypeOnSling();

		GameState startState = aRobot.getState();		
		if (startState != GameState.PLAYING)
		{
			return startState;
		}

		// If the level is loaded (in PLAYING　state) but no slingshot detected or no bird on sling is detected, then the agent will try to do something with it.
		while ((sling == null || birdOnSling == ABType.Unknown)
			&& aRobot.getState() == GameState.PLAYING) 
		{
			visionInfo retValues = new visionInfo(sling, vision, screenshot, birdOnSling);
			waitTillSlingshotIsFound(retValues);
			sling = retValues.sling;
			vision = retValues.vision;
			screenshot = retValues.screenshot;
			birdOnSling = retValues.birdOnSling;
		}
		

		startState = aRobot.getState();		
		if (startState != GameState.PLAYING)
		{
			return startState;
		}

		final List<ABObject> pigs = vision.findPigsRealShape();
		final List<ABObject> birds = vision.findBirdsRealShape();
		final List<ABObject> hills = vision.findHills();		
		final List<ABObject> blocks = vision.findBlocksRealShape();
		int gnd = vision.getGroundLevel();
		tp.ground = gnd;

		// Get game state.
		GameState state = aRobot.getState();
		
		//creates the logwriter that will be used to store the information about turns		
		final LogWriter log = new LogWriter("output.csv");
		log.appendStartLevel(currentLevel,  pigs,  birds,  blocks, hills,  birdOnSling);
		log.saveStart(ActionRobot.doScreenShot());
		
		//accumulates information about the scene that we are currently playing
		SceneState currentState = new SceneState(pigs, hills, blocks, sling, vision.findTNTs(), prevTarget, firstShot, birds, birdOnSling);
		// Prepare shot.
		Shot shot = null;
			
		// if there is a sling, then play, otherwise just skip.
		if (sling != null)
		{
			
			if (!pigs.isEmpty())
			{					

				shot = findHeuristicAndShoot(currentState, log);
			}
			else
			{
				System.err.println("No Release Point Found, will try to zoom out...");

				// try to zoom out
				ActionRobot.fullyZoomOut();

				return state;
			}
				
			// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
			state = performTheActualShooting(log, currentState, shot);	
		}

		return state;
	}

	/**
	**	performs the waiting for the slingshot to be found by zooming out and in and out
	**/
	private void waitTillSlingshotIsFound(visionInfo inf)
	{
		if (inf.sling == null)
		{
			System.out.println("No slingshot detected. Please remove pop up or zoom out");
		}
		else if (inf.birdOnSling == ABType.Unknown)
		{
			System.out.println("No bird on sling detected!!");
		}

		ActionRobot.fullyZoomOut();	
		inf.screenshot = ActionRobot.doScreenShot();			
		inf.vision = new Vision(inf.screenshot);
		inf.sling = inf.vision.findSlingshotRealShape();
		inf.birdOnSling = inf.vision.getBirdTypeOnSling();

		if ( inf.birdOnSling == ABType.Unknown )
		{
			ActionRobot.fullyZoomIn();
			inf.screenshot = ActionRobot.doScreenShot();			
			inf.vision = new Vision(inf.screenshot);
			inf.birdOnSling = inf.vision.getBirdTypeOnSling();				
			ActionRobot.fullyZoomOut();
			
			inf.screenshot = ActionRobot.doScreenShot();			
			inf.vision = new Vision(inf.screenshot);	
			inf.sling = inf.vision.findSlingshotRealShape();
		}
	}
	/**
	** this is the actual meta-agent that decides which strategy will be played
	** @return Shot that should be fired
	**/
	private Shot findHeuristicAndShoot(SceneState currentState, LogWriter log)
	{
		Random rand = new Random();  
		AbstractHeuristic possibleHeuristics [] = new AbstractHeuristic[4];
		possibleHeuristics[0] = new BuildingHeuristic(currentState, aRobot, tp, log);
		possibleHeuristics[1] = new DestroyAsManyPigsAtOnceAsPossibleHeuristic(currentState, aRobot, tp, log);
		possibleHeuristics[2] = new RoundStoneHeuristic(currentState, aRobot, tp, log);
		possibleHeuristics[3] = new DynamiteHeuristic(currentState, aRobot, tp, log);

		int heuristicId = 0;
		int max = 0xffff0000;

		for (int i = 0; i < possibleHeuristics.length; ++ i)
		{
			AbstractHeuristic tmp = possibleHeuristics[i];

			System.out.println("Utility " + tmp.toString() + ": " + tmp.getUtility());
			tmp.writeToLog();
			if (max < tmp.getUtility() && 
				!(i == 0 && tmp.getUtility() < 1000) )
			{
				max = tmp.getUtility();
				heuristicId = i;
			}
			
		}
		
		Shot shot = possibleHeuristics[heuristicId].getShot();
		if (shot != null)
		{
			System.out.println("Heuristic ID: " + heuristicId);
		}

		// if there are hills in the way, choose a random set of blocks and make them targets
		if (possibleHeuristics[heuristicId].getSelectedDLTrajectory() != null
			&& possibleHeuristics[heuristicId].getSelectedDLTrajectory().hillsInTheWay.size() != 0)
		{
			
			Shot lastBreathShot = getLastBreathShot(currentState, log);

			if (lastBreathShot != null)
			{
				shot = lastBreathShot;
			}
		}

		if( shot == null )
		{
			shot = DLUtils.findRandomShot(tp, currentState._sling,currentState._birdOnSling);
		}

		return shot;
	}
	/**
	**  In case the strategies do not find any possible shooting, i.e. blocks in the way, too far away or too close, than this "random" shot is fired.
	**/
	private Shot getLastBreathShot(SceneState currentState, LogWriter log)
	{
		List<ABObject> randomBlocks = new ArrayList<ABObject>();

		int rndNumber = 0;
		int nOfRandomBlockToChoose = (currentState._blocks.size() > 5 ? 5 : currentState._blocks.size());					

		for (int i = 0; i < nOfRandomBlockToChoose; i++)
		{
			rndNumber = randomGenerator.nextInt(currentState._blocks.size());

			if (!randomBlocks.contains(currentState._blocks.get(rndNumber)))
			{
				randomBlocks.add(currentState._blocks.get(rndNumber));
			}
			else
			{
				i--;
			}
		}

		AbstractHeuristic lastBreathHeuristic = new DestroyAsManyPigsAtOnceAsPossibleHeuristic(currentState, aRobot, tp, log);

		return lastBreathHeuristic.getShot();

	}
	/**
	**	the actual shot is passed to the server
	** 	@return the state of the scene after the performed shot
	**/
	private GameState performTheActualShooting(LogWriter log, SceneState currentState, Shot shot)
	{
		ActionRobot.fullyZoomOut();
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		Rectangle _sling = vision.findSlingshotRealShape();
		
		GameState state = null;
		if (_sling != null)
		{
			double scale_diff = Math.pow((currentState._sling.width - _sling.width),2) +  Math.pow((currentState._sling.height - _sling.height),2);

			if (scale_diff < 25)
			{					
				if (shot.getDx() < 0)
				{
					aRobot.cFastshoot(shot);
				}

				try 
				{

					state = aRobot.getState();
					log.appendScore(aRobot.getScore(), state);
					log.flush(ActionRobot.doScreenShot());
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}					

				if ( state == GameState.PLAYING )
				{
					vision = new Vision(ActionRobot.doScreenShot());
					List<Point> traj = vision.findTrajPoints();
					Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy());

					// adjusts trajectory planner
					tp.adjustTrajectory(traj, vision.findSlingshotRealShape(), releasePoint);
			
					firstShot = false;
				}    
			}
			else
				System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
		}
		else
			System.out.println("no sling detected, can not execute the shot, will re-segement the image");

		return state;		
	}
	/**
	**	I/O class for some methods
	**	it encapsulates info about the scene regarding vision
	**/
	private class visionInfo
	{
		public Rectangle sling;
		public Vision vision;
		public BufferedImage screenshot;
		public ABType birdOnSling;

		public visionInfo(Rectangle sl, Vision vis, BufferedImage sc, ABType birdie)
		{
			sling = sl;
			vision = vis;
			screenshot = sc;
			birdOnSling = birdie;
		}
	}
	
	public static void main(String args[]) {

		NaiveAgent na = new NaiveAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
