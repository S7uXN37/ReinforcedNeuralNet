package util;

import org.newdawn.slick.Color;

public class Util {
	public static Color colorLerp (Color a, Color b, float t) {
		float red,green,blue,alpha;
		red = a.r + ((b.r - a.r) * t);
		green = a.g + ((b.g - a.g) * t);
		blue = a.b + ((b.b - a.b) * t);
		alpha = a.a + ((b.a - a.a) * t);
		
		return new Color(red, green, blue, alpha);
	}
}
