package core.game;

import enums.Blocks.BLOCKS;
import enums.Materials.MATERIALS;

public class BoundingRectBlock extends Block {

	double width;
	double height;
	
	public BoundingRectBlock(
		BLOCKS blockType,
		MATERIALS mat,
		double centerX,
		double centerY,
		double rotation,
		double width,
		double height)
	{
		super(blockType, mat, centerX, centerY, rotation);
		this.width = width;
		this.height = height;
	}

	public BoundingRectBlock(BoundingRectBlock b)
	{
		super(b);
		this.width = b.width;
		this.height = b.height;
	}

	@Override
	public String getXMLTag()
	{
		return "BoundingRect";
	}
}
