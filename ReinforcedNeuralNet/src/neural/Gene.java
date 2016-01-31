package neural;

public class Gene {
	public static int globalInnov = 0;
	public int in;
	public int out;
	public double weight;
	public boolean active;
	public int innov;
	
	public Gene(int in, int out, double weight, boolean active) {
		this.in = in;
		this.out = out;
		this.weight = weight;
		this.active = active;
		
		boolean found = false;
		for (int innov : NeuralNet.newInnovations.keySet()) {
			Gene g = NeuralNet.newInnovations.get(innov);
			if (g.in == in && g.out == out) {
				found = true;
				this.innov = innov;
				break;
			}
		}
		if (!found) {
			globalInnov++;
			this.innov = globalInnov;
			NeuralNet.newInnovations.put(innov, this);
		}
	}
	
	@Override
	public String toString() {
		return innov + ": " + in + "-" + out + "/" + weight + "/" + active;
	}
}