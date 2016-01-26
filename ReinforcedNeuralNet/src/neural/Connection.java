package neural;

public class Connection {
	public double weight;
	public double value;
	public Neuron target;
	public Neuron origin;
	
	public Connection(Neuron t1, Neuron t2, double weight) {
		this.weight = weight;
		origin = t1;
		target = t2;
	}
	
	public void update() {
		value = origin.value * weight;
	}
}
