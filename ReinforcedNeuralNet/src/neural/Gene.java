package neural;

public class Gene {
	public int in;
	public int out;
	public double weight;
	public boolean active;
	public int innov;
	
	public Gene(int in, int out, double weight, boolean active, int innov) {
		this.in = in;
		this.out = out;
		this.weight = weight;
		this.active = active;
		this.innov = innov;
	}

	public static Gene[] cleanUp(Gene[] genome) {
		// TODO Auto-generated method stub
		return null;
	}
}