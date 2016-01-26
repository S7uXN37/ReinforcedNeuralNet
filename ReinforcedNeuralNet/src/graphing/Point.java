package graphing;

public class Point {
	public double x,y;
	
	public Point(double x2, double y2) {
		x = x2;
		y = y2;
	}
	public int getX(){
		return (int) x;
	}
	public int getY(){
		return (int) (800-y);
	}
}