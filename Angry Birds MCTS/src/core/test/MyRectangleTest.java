package core.test;

import core.game.MyRectangle;

public class MyRectangleTest {

	public static void main(String[] args)
	{
		double centerX1 = -1.50;
		double centerY1 = -0.19;
		double width1 = 2.81;
		double height1 = 3.69;
		
		double centerX2 = -1.50;
		double centerY2 = 3.25;
		double width2 = 1.89;
		double height2 = 1.92;
		MyRectangle rect1 = new MyRectangle(centerX1 - width1 * 0.5, centerY1 + height1 * 0.5, width1, height1);
		MyRectangle rect2 = new MyRectangle(centerX2 - width2 * 0.5, centerY2 + height2 * 0.5, width2, height2);
		System.out.println(rect1.intersects(rect2));
	}
}
