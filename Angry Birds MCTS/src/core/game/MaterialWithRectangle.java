package core.game;
import java.awt.Rectangle;
import java.util.Objects;

import enums.Materials.MATERIALS;

public class MaterialWithRectangle {

	MATERIALS mat;
	Rectangle rect;
	
	public MaterialWithRectangle(MATERIALS mat, Rectangle rect)
	{
		this.mat = mat;
		this.rect = rect;
	}
	
	@Override
	public int hashCode(){
	    return Objects.hash(this.mat, this.rect);
	}
	
	@Override
    public boolean equals(Object o) {
 
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }
 
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof MaterialWithRectangle)) {
            return false;
        }
         
        // typecast o to Complex so that we can compare data members 
        MaterialWithRectangle m = (MaterialWithRectangle) o;
         
        // Compare the data members and return accordingly 
        return mat == m.mat
    		&& rect.equals(m.rect);
    }
}
