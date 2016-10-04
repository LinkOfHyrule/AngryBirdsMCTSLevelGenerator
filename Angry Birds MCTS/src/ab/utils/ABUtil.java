package ab.utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.Vision;

public class ABUtil {
	
	public static int gap = 5; //vision tolerance.
	private static TrajectoryPlanner tp = new TrajectoryPlanner();

	// If o1 supports o2, return true
	public static boolean isSupport(ABObject o2, ABObject o1)
	{
		if(o2.x == o1.x && o2.y == o1.y && o2.width == o1.width && o2.height == o1.height)
				return false;
		
		int ex_o1 = o1.x + o1.width;
		int ex_o2 = o2.x + o2.width;
		
		int ey_o2 = o2.y + o2.height;
		if(
			(Math.abs(ey_o2 - o1.y) < gap)
			&&  
 			!( o2.x - ex_o1  > gap || o1.x - ex_o2 > gap )
		  )
	        return true;	
		
		return false;
	}
	//Return a link list of ABObjects that support o1 (test by isSupport function ). 
	//objs refers to a list of potential supporters.
	//Empty list will be returned if no such supporters. 
	public static List<ABObject> getSupporters(ABObject o2, List<ABObject> objs)
			{
				List<ABObject> result = new LinkedList<ABObject>();
				//Loop through the potential supporters
		        for(ABObject o1: objs)
		        {
		        	if(isSupport(o2,o1))
		        		result.add(o1);
		        }
		        return result;
			}

	//Return true if the target can be hit by releasing the bird at the specified release point
	public static boolean isReachable(Vision vision, Point target, Shot shot)
	{ 
		//test whether the trajectory can pass the target without considering obstructions
		Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy()); 
		int traY = tp.getYCoordinate(vision.findSlingshotMBR(), releasePoint, target.x);
		if (Math.abs(traY - target.y) > 100)
		{	
			//System.out.println(Math.abs(traY - target.y));
			return false;
		}
		boolean result = true;
		List<Point> points = tp.predictTrajectory(vision.findSlingshotMBR(), releasePoint);		
		for(Point point: points)
		{
		  if(point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400)
			for(ABObject ab: vision.findBlocksMBR())
			{
				if( 
						((ab.contains(point) && !ab.contains(target))||Math.abs(vision.getMBRVision()._scene[point.y][point.x] - 72 ) < 10) 
						&& point.x < target.x
						)
					return false;
			}
		  
		}
		return result;
	}
	
	/**
	*	@return the percentage of the trajectory when the first object is hit
	*/	
	public static int reachablePercentage(Rectangle sling, List<ABObject> objects, Point targetPoint, ABObject targetObject, Point releasePoint, int range)
	{
	
		List<Point> trajectoryPoints = tp.predictTrajectory(sling, releasePoint);
		
		return reachablePercentage(sling, objects, targetPoint,targetObject, releasePoint, range, trajectoryPoints);
	}
	/**
	*	@return the percentage of the trajectory when the first object is hit
	*/	
	public static int reachablePercentage(Rectangle sling, List<ABObject> objects, Point targetPoint, ABObject targetObject, Point releasePoint, int range, List<Point> trajectoryPoints)
	{
		int traY = tp.getYCoordinate(sling, releasePoint, targetPoint.x);
	
		if (Math.abs(traY - targetPoint.y) > 100)
			return 100;
		
		Point collision = isReachable(trajectoryPoints, sling, objects, targetPoint, targetObject, releasePoint, range);

		if (collision == null)
			return 100;
		
		int distance = collision.x - sling.x;
		int totDistance = targetPoint.x - sling.x;

		return (int) ((distance*100.0)/totDistance);
	}
	/**
	*	estimates the objects that are in the trajectory
	*/
	public static List<ABObject> estimateObjectsInTheWay(Rectangle sling, List<ABObject> objects, Point targetPoint, ABObject targetObject, Point releasePoint, int range)
	{ 
		List<Point> trajectoryPoints = tp.predictTrajectory(sling, releasePoint);
		
		return estimateObjectsInTheWay(trajectoryPoints, objects, targetPoint, targetObject, range);
	} 
	/**
	*	estimates the objects that are in the trajectory
	*/
	public static List<ABObject> estimateObjectsInTheWay(List<Point> trajectoryPoints, List<ABObject> objects, Point targetPoint, ABObject targetObject, int range)
	{ 
		List<ABObject> foundObjects = new ArrayList<ABObject>();

		for (Point point : trajectoryPoints)
		{
			boolean reachedTarget = false;

			if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 200)
			{
				for (ABObject ab: objects)
				{
					if (pointHitsObjectWithinARange(ab, point, range)
						&& !pointHitsObjectWithinARange(targetObject, point, range))
					{
						if (!foundObjects.contains(ab))
						{
							ab.trajectoryHitPoint = point;
							foundObjects.add(ab);
						}
					}
					else if (pointHitsObjectWithinARange(targetObject, point, range))
					{
						reachedTarget = true;
						break;
					}
				}
			}

			if (reachedTarget)
				break;
		}
		
		return foundObjects;
	}     
	/**
	*	@return the first point it hits, null if the trajectory is clear
	*/
	public static Point isReachable(List<Point> points, Rectangle sling, List<ABObject> objects, Point targetPoint, ABObject targetObject, Point releasePoint, int range)
	{ 
		for (Point point: points)
		{
			boolean reachedTarget = false;

			if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 200)
			{
				for (ABObject ab: objects)
				{
					if (pointHitsObjectWithinARange(ab, point, range)
						&& !pointHitsObjectWithinARange(targetObject, point, range))
					{
						return point;
					}
					else if (pointHitsObjectWithinARange(targetObject, point, range))
					{
						reachedTarget = true;
						break;
					}
				}
			}

			if (reachedTarget)
				break;
		}
		
		return null;
	}
	/**
	*	@return true if there is no object in the trajectory, false otherwise
	*/
	public static boolean isReachableBool(Rectangle sling, List<ABObject> objects, Point targetPoint, ABObject targetObject, Point releasePoint, int range)
	{

		int traY = tp.getYCoordinate(sling, releasePoint, targetPoint.x);
	
		if (Math.abs(traY - targetPoint.y) > 100)
			return false;

		List<Point> points = tp.predictTrajectory(sling, releasePoint);

		return isReachable(points, sling, objects, targetPoint, targetObject, releasePoint,  range) == null ? true : false;

	}    

	/**
	*	@return true if the target can be hit by a clear shot releasing the bird at the specified release point, false otherwise
	*/
	public static boolean isReachableBoolByAClearShot(Rectangle sling, List<ABObject> blocks, List<ABObject> hills, Point targetPoint, ABObject targetObject, Point releasePoint, int range)
	{
		ArrayList<ABObject> allObjects = new ArrayList<>(blocks);
		allObjects.addAll(hills);

		return isReachableBool(sling, allObjects, targetPoint, targetObject, releasePoint, range);
	}   
	
	/**
	* @return true if a point hits object within a range (point included) - the range is usually the bird's radius
	*/
	public static boolean pointHitsObjectWithinARange(ABObject object, Point point, int range)
	{
		if (object.contains(point))
			return true;
		
		int pX = point.x;
		int pY = point.y;
		
		if (range > 0
			&& (object.contains(new Point(pX - range, pY))
				|| object.contains(new Point(pX + range, pY))
				|| object.contains(new Point(pX, pY + range))
				|| object.contains(new Point(pX, pY - range))
				)
			)
			return true;
		
		return false;
	}
	

	/**
	*	@return number of pigs that are in the trajectory
	*/
	public static int countThePigsInTheTrajectory(Rectangle sling, List<ABObject> pigs, Point targetPoint, ABObject targetObject, Shot shot, int range)
	{
		Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy());
		
		return countThePigsInTheTrajectory(sling, pigs, targetPoint, targetObject, releasePoint, range);
	}
	
	/**
	*	@return number of pigs that are in the trajectory
	*/
	public static int countThePigsInTheTrajectory(Rectangle sling, List<ABObject> pigs, Point targetPoint, ABObject targetObject, Point releasePoint, int range)
	{
		List<ABObject> pigsInTheTrajectory = estimateObjectsInTheWay(sling, pigs, targetPoint, targetObject, releasePoint, range);
		
		return pigsInTheTrajectory.size();
	}
}
