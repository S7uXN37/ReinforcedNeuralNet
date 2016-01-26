package graphing;

import org.newdawn.slick.Color;

public class Line {
	public Point p1,p2;
	public Color c;
	public Line(Point p1, Point p2, Color c) {
		this.p1=p1;
		this.p2=p2;
		this.c=c;
	}
}