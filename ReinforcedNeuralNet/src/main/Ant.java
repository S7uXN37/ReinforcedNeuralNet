package main;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import neural.ISimulated;
import neural.NeuralNet;
import util.ImmutableVector2f;
import util.Util;

public class Ant extends ISimulated{
	public static final float bodyRadius = 10f;
	public static final float headRadius = 7.5f;
	public static final float headDist = bodyRadius;
	private static final Color GOOD_ANT_COLOR = Color.blue;
	private static final Color BAD_ANT_COLOR = Color.red;
	public static double maxFit;
	public static double minFit;
	
	double speed;
	ImmutableVector2f velocity = new ImmutableVector2f(0, 0);
	ImmutableVector2f position = new ImmutableVector2f(0, 0);
	public ImmutableVector2f headPosition = new ImmutableVector2f(0, 0);
	
	public Ant (NeuralNet n, double speed, Vector2f initPos) {
		super(n);
		this.speed = speed;
		position = new ImmutableVector2f(initPos);
		net = n;
	}
	
	@Override
	public void tick (double[] input, float deltaSec) {	
		net.tick(input);
		float[] v = new float[]{
				(float) net.getOutput()[0],
				(float) net.getOutput()[1]
		};
		Vector2f newV = new Vector2f(v[0], v[1]);
		if (newV.lengthSquared() > 1)
			newV.normalise();
		velocity = new ImmutableVector2f(newV);
		headPosition = new ImmutableVector2f(velocity.normalise().scale(headDist).add(position));
		
		ImmutableVector2f dist = velocity.scale(deltaSec * (float) speed);
		Vector2f newPos = position.add(dist).makeVector2f();
		newPos.x = Math.min(newPos.x, AntSimulation.WIDTH);
		newPos.x = Math.max(newPos.x, 0);
		newPos.y = Math.min(newPos.y, AntSimulation.HEIGHT);
		newPos.y = Math.max(newPos.y, 0);
		
		position = new ImmutableVector2f(newPos);
	}

	@Override
	public void draw(GameContainer gc, Graphics g) {
		float x = position.getScreenX() - bodyRadius;
		float y = position.getScreenY() - bodyRadius;
		
		float hx = headPosition.getScreenX();
		float hy = headPosition.getScreenY();
		
		g.setColor(Util.colorLerp(BAD_ANT_COLOR, GOOD_ANT_COLOR, Util.lerp(minFit, maxFit, 0, 1, net.fitness)));
		g.fillOval(x, y, Ant.bodyRadius*2, Ant.bodyRadius*2);
		g.fillOval(hx, hy, Ant.headRadius*2, Ant.headRadius*2);
	}
	
	public void pickupFood() {
		net.fitness++;
	}
}
