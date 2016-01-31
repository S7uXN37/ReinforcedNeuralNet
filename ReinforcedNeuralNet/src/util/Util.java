package util;

import org.newdawn.slick.Color;

public class Util {
	public static Color colorLerp (Color a, Color b, double d) {
		float red,green,blue,alpha;
		red = (float) (a.r + ((b.r - a.r) * d));
		green = (float) (a.g + ((b.g - a.g) * d));
		blue = (float) (a.b + ((b.b - a.b) * d));
		alpha = (float) (a.a + ((b.a - a.a) * d));
		
		return new Color(red, green, blue, alpha);
	}

	public static double lerp(double x0, double x1, double y0, double y1, double x) {
		return y0
				+ (y1 - y0)
				* (x - x0)
				/ (x1 - x0);
	}
}
