package core.game;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Objects;

import enums.Materials.MATERIALS;

public class StructuralElementTreeNode {

	private Block block;
	private ArrayList<StructuralElementTreeNode> children;
	private int treeDepth;
	
	public StructuralElementTreeNode(Block block)
	{
		this.block = block;
		children = new ArrayList<StructuralElementTreeNode>();
	}
	
	public void addChild(StructuralElementTreeNode node)
	{
		children.add(node);
	}

	public Block getBlock()
	{
		return block;
	}
	
	public MyRectangle getRect()
	{
		return block.getBlockRect();
	}

	public Point2D.Double getCenterPoint()
	{
		return block.getCenterPoint();
	}

	public MATERIALS getMaterial()
	{
		return block.getMaterial();
	}
	
	public ArrayList<StructuralElementTreeNode> getChildren() {
		return children;
	}
	
	public int getNumberOfChildren() {
		return children.size();
	}

	@Override
	public int hashCode(){
	    return Objects.hash(this.block, this.children);
	}
	
	@Override
    public boolean equals(Object o) {
 
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }
 
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof StructuralElementTreeNode)) {
            return false;
        }
         
        // typecast o to Complex so that we can compare data members 
        StructuralElementTreeNode s = (StructuralElementTreeNode) o;
         
        // Compare the data members and return accordingly 
        return block.equals(s.block)
    		&& children.equals(s.children);
    }
	
	@Override
	public String toString()
	{
		return "block: " + block.toString() + 
			   " numChildren: " + children.size();
	}

	public int getTreeDepth() {
		return treeDepth;
	}

	public void setTreeDepth(int treeDepth) {
		this.treeDepth = treeDepth;
	}
}
