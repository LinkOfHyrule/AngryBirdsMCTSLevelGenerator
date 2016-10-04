package core.game;

public class MBRConstraints {

	public boolean fitsConstraints;
	public MyRectangle masterBoundingRectangleWithPlatforms;
	
	public MBRConstraints(
		boolean fitsConstraints,
		MyRectangle masterBoundingRectangleWithPlatforms)
	{
		this.fitsConstraints = fitsConstraints;
		this.masterBoundingRectangleWithPlatforms = masterBoundingRectangleWithPlatforms;
	}
}
