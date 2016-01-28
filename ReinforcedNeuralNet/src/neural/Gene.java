package neural;

public class Gene {
	public int in;
	public int out;
	public double weight;
	public boolean active;
	
	public Gene(int in, int out, double weight, boolean active) {
		this.in = in;
		this.out = out;
		this.weight = weight;
		this.active = active;
	}
	
	@Override
	public String toString() {
		return in + "-" + out + "/" + weight + "/" + (active ? 1 : 0);
	}
}