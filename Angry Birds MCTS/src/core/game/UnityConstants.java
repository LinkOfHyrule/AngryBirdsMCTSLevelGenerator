package core.game;

public class UnityConstants {

	public static String cleanedUpLevelsDirectory = "C:\\AngryBirdsCleanedUpLevels";
	public static String unityLevelsDirectory = "D:/Desktop/ScienceBirds-AIBirdsCompetition/ScienceBirds-AIBirdsCompetition/Assets/StreamingAssets/Levels/";

	static double minimumHeightGap = 3.5;        // y distance min between platforms
	static double platformDistanceBuffer = 0.4;  // x_distance min between platforms / y_distance min between platforms and ground structures

	// defines the levels area (ie. space within which structures/platforms can be placed)
	static double levelWidthMin = -3.0;
	static double levelWidthMax = 9.0;
	static double totalLevelWidthMax = levelWidthMax - levelWidthMin;
	static double levelHeightMin = -3.0;         // only used by platforms, ground structures use absolute_ground to determine their lowest point
	static double levelHeightMax = 5.0;
	static double totalLevelHeightMax = levelHeightMax - levelHeightMin;
	
	// Normal distribution
	static double meanOfLevel = levelWidthMin + (totalLevelWidthMax / 2);
	static double stdDevOfLevel = totalLevelWidthMax / 6;
	static double heightAtMeanOfLevel = getNormalProbabilityAtZ(meanOfLevel);
	static double normalDistributionHeightFactor = totalLevelHeightMax / heightAtMeanOfLevel;

	static double pigWidth = 0.47;
	static double pigHeight = 0.45;
	static double platformWidthAndHeight = 0.64;
	static double halfPlatformWidthAndHeight = platformWidthAndHeight * 0.5;

	static double minGroundWidth = 2.5;                      // minimum amount of space allocated to ground structure
	//static double groundStructureHeightLimit = ((level_height_max - minimum_height_gap) - absolute_ground)/1.5;    // desired height limit of ground structures

	static int numPlacementAttempts = 100;                          // number of times to attempt to place a platform before abandoning it
   
    static double getNormalProbabilityAtZ(double z)
    {
    	// Mean: center in level width
    	// Std. dev: 1/6 of level width
        return Math.exp(-Math.pow((z - UnityConstants.meanOfLevel) / UnityConstants.stdDevOfLevel, 2) / 2) / (UnityConstants.stdDevOfLevel * Math.sqrt(2 * Math.PI));
    }
}
