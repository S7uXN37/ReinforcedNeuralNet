package neural;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public abstract class ISimulated {
	public NeuralNet net;
	
	public abstract void draw(GameContainer gc, Graphics g);
	public abstract void tick(double[] neuralInput, float deltaSec);
	public ISimulated(NeuralNet net) {
		this.net = net;
	}
}
