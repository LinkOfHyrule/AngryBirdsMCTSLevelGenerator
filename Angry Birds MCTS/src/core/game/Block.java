package core.game;
import java.awt.geom.Point2D;
import java.util.Objects;

import enums.Blocks.BLOCKS;
import enums.Materials.MATERIALS;
import enums.Rotation.ROTATION;

public class Block {

	private static double rectBigLongSide = 2.06;
	private static double rectBigShortSide = 0.22;
	private static double rectFatLongSide = 0.85;
	private static double rectFatShortSide = 0.43;
	private static double rectMediumLongSide = 1.68;
	private static double rectMediumShortSide = 0.22;
	private static double rectSmallLongSide = 0.85;
	private static double rectSmallShortSide = 0.22;
	private static double rectTinyLongSide = 0.43;
	private static double rectTinyShortSide = 0.22;
	
	private BLOCKS blockType;
	private MATERIALS mat;
	private double centerX;
	private double centerY;
	private double rotation;
	private ROTATION rotationType;
	private String xmlTag;
	
	public Block(
		BLOCKS blockType,
		MATERIALS mat,
		double centerX,
		double centerY,
		double rotation)
	{
		this.mat = mat;
		this.blockType = blockType;
		this.centerX = centerX;
		this.centerY = centerY;
		setRotation(rotation);
		setBlockType(this.blockType);
	}

	public Block(Block b)
	{
		this.mat = b.mat;
		this.blockType = b.blockType;
		this.centerX = b.centerX;
		this.centerY = b.centerY;
		setRotation(b.rotation);
		setBlockType(this.blockType);
	}

	public BLOCKS getType() {
		return blockType;
	}

	public MATERIALS getMaterial() {
		return mat;
	}
	
	public double getCenterX() {
		return centerX;
	}
	
	public double getCenterY() {
		return centerY;
	}
	
	public MyRectangle getBlockRect()
	{
		// X and Y are moved to top-left at the end of this function.
		MyRectangle toReturn = null;
		switch(blockType)
		{
		case Circle:
			toReturn = new MyRectangle(centerX, centerY, 0.76, 0.75);
			break;
		case CircleSmall:
			toReturn = new MyRectangle(centerX, centerY, 0.41085, 0.41086);
			break;
		case RectBig:
			if(rotationType == ROTATION.VERTICAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectBigShortSide, rectBigLongSide);
			}
			else if(rotationType == ROTATION.HORIZONTAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectBigLongSide, rectBigShortSide);
			}
			break;
		case RectFat:
			if(rotationType == ROTATION.VERTICAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectFatShortSide, rectFatLongSide);
			}
			else if(rotationType == ROTATION.HORIZONTAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectFatLongSide, rectFatShortSide);
			}
			break;
		case RectMedium:
			if(rotationType == ROTATION.VERTICAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectMediumShortSide, rectMediumLongSide);
			}
			else if(rotationType == ROTATION.HORIZONTAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectMediumLongSide, rectMediumShortSide);
			}
			break;
		case RectSmall:
			if(rotationType == ROTATION.VERTICAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectSmallShortSide, rectSmallLongSide);
			}
			else if(rotationType == ROTATION.HORIZONTAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectSmallLongSide, rectSmallShortSide);
			}
			break;
		case RectTiny:
			if(rotationType == ROTATION.VERTICAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectTinyShortSide, rectTinyLongSide);
			}
			else if(rotationType == ROTATION.HORIZONTAL)
			{
				toReturn = new MyRectangle(centerX, centerY, rectTinyLongSide, rectTinyShortSide);
			}
			break;
		case SquareHole:
			toReturn = new MyRectangle(centerX, centerY, 0.84, 0.84);
			break;
		case SquareSmall:
			toReturn = new MyRectangle(centerX, centerY, 0.43, 0.43);
			break;
		case SquareTiny:
			toReturn = new MyRectangle(centerX, centerY, 0.22, 0.21);
			break;
		case Triangle:
			toReturn = new MyRectangle(centerX, centerY, 0.83431, 0.82);
			break;
		case TriangleHole:
			toReturn = new MyRectangle(centerX, centerY, 0.84, 0.84);
			break;
		case BasicSmall:
			toReturn = new MyRectangle(centerX, centerY, UnityConstants.pigWidth, UnityConstants.pigHeight); 
			break;
		case Platform:
			toReturn = new MyRectangle(centerX, centerY, UnityConstants.platformWidthAndHeight, UnityConstants.platformWidthAndHeight);
			break;
		default:
			toReturn = null;
			break;
		}
		// Need to subtract half the width and add half the height to get x/y in the top left
		toReturn.x -= (toReturn.width * 0.5);
		toReturn.y += (toReturn.height * 0.5);
		return toReturn;
	}
	
	public double getRotation()
	{
		return rotation;
	}
	
	public ROTATION getRotationType()
	{
		return rotationType;
	}
	
	public String getXMLTag()
	{
		return xmlTag;
	}
	
	public String getMaterialString()
	{
		switch(mat)
		{
		case nil:
			return "";
		default:
			return mat.name();
		}
	}
	
	@Override
	public int hashCode(){
	    return Objects.hash(
    		this.blockType,
    		this.mat,
    		this.centerX,
    		this.centerY,
    		this.rotation,
    		this.rotationType,
    		this.xmlTag);
	}
	
	@Override
    public boolean equals(Object o) {
 
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }
 
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Block)) {
            return false;
        }
         
        // typecast o to Complex so that we can compare data members 
        Block b = (Block) o;
         
        // Compare the data members and return accordingly 
        return blockType.equals(b.getType())
    		&& mat.equals(b.getMaterial())
    		&& centerX == b.getCenterX()
    		&& centerY == b.getCenterY()
    		&& rotation == b.getRotation()
    		&& rotationType.equals(b.getRotationType())
    		&& xmlTag.equals(b.getXMLTag());
    }
	
	@Override
	public String toString()
	{
		return "blockType: " + blockType.name() + 
			   " material: " + getMaterialString() + 
			   " x: " + centerX + 
			   " y: " + centerY + 
			   " rotation: " + rotation + 
			   " rotationType: " + rotationType + 
			   " xmlTag: " + xmlTag;
	}

	public Point2D.Double getCenterPoint()
	{
		return new Point2D.Double(centerX, centerY);
	}
	
	private void setRotation(double rotation)
	{
		if(rotation > 80.0 && rotation < 100.0 || rotation > 260.0 && rotation < 280.0)
		{
			this.rotationType = ROTATION.VERTICAL;
			this.rotation = 90.0;
		}
		else if(rotation > -10.0 && rotation < 10.0 || rotation > 170.0 && rotation < 190.0 || rotation > 350.0)
		{
			this.rotationType = ROTATION.HORIZONTAL;
			this.rotation = 0.0;
		}
	}
	
	private void setBlockType(BLOCKS blockType)
	{
		switch(blockType)
		{
		case BasicSmall:
			xmlTag = "Pig";
			break;
		case Platform:
			xmlTag = "Platform";
			break;
		default:
			xmlTag = "Block";
			break;
		}
	}
}
