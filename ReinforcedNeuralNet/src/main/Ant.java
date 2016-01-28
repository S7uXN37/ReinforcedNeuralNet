package main;

import java.util.Comparator;

import org.newdawn.slick.geom.Vector2f;

import neural.Gene;
import neural.NeuralNet;
import util.ImmutableVector2f;

public class Ant implements Comparable{	
	Gene[] genes;
	NeuralNet net;
	double speed;
	float reproductionTime;
	ImmutableVector2f velocity = new ImmutableVector2f(0, 0);
	ImmutableVector2f position;
	int foodCollected = 0;
	
	public Ant (Gene[] genome, double speed, Vector2f initPos) {
		this.speed = speed;
		position = new ImmutableVector2f(initPos);
		genes = genome;
		net = new NeuralNet(2, 2, genes);
	}
	
	public void pickupFood (float amount) {
		foodCollected += amount;
	}
	
	public void tick (Vector2f toFoodNorm, float deltaSec) {	
		net.tick(new double[]{toFoodNorm.x, toFoodNorm.y});
		float[] v = new float[]{
				(float) net.getOutput()[0],
				(float) net.getOutput()[1]
		};
		Vector2f newV = new Vector2f(v[0], v[1]);
		if (newV.lengthSquared() > 1)
			newV.normalise();
		velocity = new ImmutableVector2f(newV);
		
		ImmutableVector2f dist = velocity.scale(deltaSec * (float) speed);
		Vector2f newPos = position.add(dist).makeVector2f();
		newPos.x = Math.min(newPos.x, AntSimulation.WIDTH);
		newPos.x = Math.max(newPos.x, 0);
		newPos.y = Math.min(newPos.y, AntSimulation.HEIGHT);
		newPos.y = Math.max(newPos.y, 0);
		
		position = new ImmutableVector2f(newPos);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@Override
	public int compareTo(Object arg0) {
		Ant o2 = (Ant) arg0;
		if (this.foodCollected == o2.foodCollected)
			return 0;
		else
			return this.foodCollected > o2.foodCollected ? 1 : -1;
	}
}
