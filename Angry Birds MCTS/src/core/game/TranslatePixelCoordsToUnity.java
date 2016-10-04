package core.game;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class TranslatePixelCoordsToUnity {
	
    // Empirically determined parameters from each screenshot.
	private static double defaultPlatformWidthHeightInPixels = 30;
    private static double defaultPixelOffsetX = 800;
    private static double defaultPixelOffsetY = 280;
    private static double defaultPixelsPerWorldPoint = 45.0;
    
    private double platformWidthHeightInPixels = defaultPlatformWidthHeightInPixels;
    private double pixelOffsetX = defaultPixelOffsetX;
    private double pixelOffsetY = defaultPixelOffsetY;
    private double pixelsPerWorldPoint = defaultPixelsPerWorldPoint;
    
    private int screenHeightInPixels = 0;
    
	public TranslatePixelCoordsToUnity(
		int levelNumber,
		int screenHeightInPixels)
	{
		this.screenHeightInPixels = screenHeightInPixels;
	    switch(levelNumber)
	    {
	    case 4:
		    pixelOffsetY = 315;
	    	break;
	    case 5:
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 6:
		    pixelOffsetY = 260;
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 7:
	    	break;
	    case 8:
	    	break;
	    case 9:
	    	break;
	    case 10:
		    pixelOffsetY = 270;
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 11:
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 12:
		    pixelsPerWorldPoint = 42.0;
	    	break;
	    case 13:
		    pixelOffsetY = 285;
		    pixelsPerWorldPoint = 36.0;
	    	break;
	    case 14:
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 15:
		    pixelOffsetY = 265;
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 16:
		    pixelOffsetY = 265;
		    pixelsPerWorldPoint = 38.0;
	    	break;
	    case 17:
		    pixelOffsetY = 265;
		    pixelsPerWorldPoint = 37.0;
	    	break;
	    case 18:
		    pixelOffsetY = 290;
	    	break;
	    case 19:
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 20:
		    pixelOffsetY = 253;
		    pixelsPerWorldPoint = 34.0;
	    	break;
	    case 21:
		    pixelsPerWorldPoint = 40.0;
	    	break;
	    case 22:
		    pixelOffsetY = 265;
		    pixelsPerWorldPoint = 35.0;
	    	break;
	    case 23:
		    pixelOffsetY = 315;
		    pixelsPerWorldPoint = 55.0;
	    	break;
	    default:
	    	break;
	    }
	    
	    // Correct platform pixels for pixelsPerWorldPoint changes
	    double pixelPointRatio = pixelsPerWorldPoint / defaultPixelsPerWorldPoint;
	    platformWidthHeightInPixels = defaultPlatformWidthHeightInPixels * pixelPointRatio;
	}
	
	public Point2D.Double convertPixelBlockCoordToUnity(Rectangle pixelRect)
	{
		return new Point2D.Double(
    		((double)pixelRect.x + 0.5 * pixelRect.width - pixelOffsetX) / pixelsPerWorldPoint,
			(screenHeightInPixels - ((double)pixelRect.y + 0.5 * pixelRect.height) - pixelOffsetY) / pixelsPerWorldPoint);
	}

	public double getPixelBlockUnityHeight(Rectangle pixelRect)
	{
		return (double)pixelRect.height / pixelsPerWorldPoint;
	}

	public double getPixelBlockUnityWidth(Rectangle pixelRect)
	{
		return (double)pixelRect.width / pixelsPerWorldPoint;
	}
	
	public ArrayList<Point2D.Double> getPlatformCoords(int startX, int endX, int yCoord)
	{
		ArrayList<Point2D.Double> toReturn = new ArrayList<Point2D.Double>();
		boolean placedPlatform = false;
		double platformY = (screenHeightInPixels - ((double)yCoord + 0.5 * platformWidthHeightInPixels) - pixelOffsetY) / pixelsPerWorldPoint;
		for(int k = startX; k < endX - 0.5 * platformWidthHeightInPixels; k += platformWidthHeightInPixels)
		{
			if(platformY > -3.0)
			{
				toReturn.add(new Point2D.Double(
					((double)k + 0.5 * platformWidthHeightInPixels - pixelOffsetX) / pixelsPerWorldPoint,
					platformY));
				placedPlatform = true;
			}
		}
		// Place last platform
		if(placedPlatform)
		{
			toReturn.add(new Point2D.Double(
				((double)endX - 0.5 * platformWidthHeightInPixels - pixelOffsetX) / pixelsPerWorldPoint,
				platformY));
		}
		return toReturn;
	}
}
