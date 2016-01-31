package neural;

public class Connection {
	public double weight;
	public double value;
	public Neuron target;
	public Neuron origin;
	public int targetID, originID;
	
	
	public Connection(Neuron t1, Neuron t2, double weight) {
		this.weight = weight;
		origin = t1;
		target = t2;
		targetID = target.id;
		originID = origin.id;
		
		target.in_conn.add(this);
	}
	
	public void update() {
		value = origin.value * weight;
	}
	
	@Override
	public String toString() {
		return originID + "-" + targetID + "/" + weight;
	}
}
