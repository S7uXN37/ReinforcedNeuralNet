package neural;

public class Gene {
	public static int globalInnov = 0;
	public int in;
	public int out;
	public double weight;
	public boolean active;
	public int innov;
	
	public Gene(int in, int out, double weight, boolean active) {
		assert (in != out) : "Illegal Gene Configuration";
		
		this.in = in;
		this.out = out;
		this.weight = weight;
		this.active = active;
		
		innov = (in + "-" + out).hashCode();
	}
	
	@Override
	public String toString() {
		return innov + ": " + in + "-" + out + "/" + weight + "/" + active;
	}
}