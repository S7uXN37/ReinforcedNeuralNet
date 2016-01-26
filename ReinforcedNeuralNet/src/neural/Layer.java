package neural;

public class Layer {
	public Neuron[] neurons;
	
	public Layer(int numNeurons) {
		neurons = new Neuron[numNeurons+1];
	}
}
