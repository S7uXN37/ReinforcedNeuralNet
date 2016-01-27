package main;

import org.newdawn.slick.geom.Vector2f;

import neural.Gene;
import neural.NeuralNet;
import util.ImmutableVector2f;

public class Ant {
	private static final float maxFoodLvl = 1000;
	private static final float matureTime = 1;
	private static final float reprodTime = 5000;
	
	Gene[] genes;
	NeuralNet net;
	double speed;
	float reproductionTime;
	float timeUntilEgg;
	float foodLevel;
	ImmutableVector2f velocity = new ImmutableVector2f(0, 0);
	ImmutableVector2f position;
	boolean isAlive = true;
	boolean layEgg = false;
	float timeUntilMature;
	
	public Ant (Gene[] genome, double speed, Vector2f initPos) {
		this.speed = speed;
		foodLevel = maxFoodLvl;
		position = new ImmutableVector2f(initPos);
		genes = genome;
		reproductionTime = reprodTime;
		timeUntilEgg = reproductionTime;
		timeUntilMature = matureTime;
		net = new NeuralNet(2, 2, genes);
	}
	
	public void pickupFood (float amount) {
		foodLevel = Math.min(foodLevel + amount, maxFoodLvl);
		timeUntilEgg -= amount;
	}
	
	public void tick (Vector2f toFoodNorm, float deltaSec) {
		timeUntilMature -= deltaSec;
		
		if (!isMature())
			return;
		
		net.tick(new double[]{toFoodNorm.x, toFoodNorm.y});
		float[] v = new float[]{
				(float) net.getOutput()[0],
				(float) net.getOutput()[1]
		};
		velocity = new ImmutableVector2f(new Vector2f(v).normalise());
		
		if (foodLevel <= 0)
			die();
		
		if (timeUntilEgg <= 0) {
			layEgg = true;
			timeUntilEgg = reproductionTime;
		}
		
		ImmutableVector2f dist = velocity.scale(deltaSec * (float) speed);
		Vector2f newPos = position.add(dist).makeVector2f();
		newPos.x = Math.min(newPos.x, AntSimulation.WIDTH);
		newPos.x = Math.max(newPos.x, 0);
		newPos.y = Math.min(newPos.y, AntSimulation.HEIGHT);
		newPos.y = Math.max(newPos.y, 0);
		
		position = new ImmutableVector2f(newPos);
		foodLevel -= dist.length();
		foodLevel -= deltaSec * 10;
	}

	private void die() {
		isAlive = false;
	}
	
	public boolean isMature() {
		return timeUntilMature <= 0;
	}
}
