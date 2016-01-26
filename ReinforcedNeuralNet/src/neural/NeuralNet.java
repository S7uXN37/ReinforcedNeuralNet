package neural;

public class NeuralNet {
	private Layer[] layers;
	private double m_error = 0D;
	private double m_recentAverageError = 1D;
	private double m_recentAverageErrorSmoothing = 0.5D;
	
	public NeuralNet(final int[] topology) {
		int numLayers = topology.length;
		layers = new Layer[numLayers];
		
		for(int layerInd = 0; layerInd < numLayers; layerInd++) {
			layers[layerInd] = new Layer(topology[layerInd]);
			int nextLayerSize = layerInd == topology.length-1 ? 0 : topology[layerInd+1];
			
			for (int neuronInd = 0; neuronInd <= topology[layerInd]; neuronInd++) {
				layers[layerInd].neurons[neuronInd] = new Neuron(nextLayerSize, neuronInd);
				
				if (neuronInd == topology[layerInd]) {
					layers[layerInd].neurons[neuronInd].setOutput(1.0D);
				}
				
				System.out.println("Made a neuron!");
			}
		}
	}
	
	public void feedForward(final double[] input) {
		assert (input.length == layers[0].neurons.length-1) : "Invalid size of input";
		
		for (int i = 0; i < input.length; i++) {
			layers[0].neurons[i].setOutput(input[i]);
		}
		
		for (int layerNum = 1; layerNum < layers.length; layerNum++) {
			Layer prevLayer = layers[layerNum - 1];
			for (int n = 0; n < layers[layerNum].neurons.length - 1; n++) {
				layers[layerNum].neurons[n].feedForward(prevLayer);
			}
		}
	}
	
	public void backProp(final double[] target) {
		// net error, as root mean square error
		Layer outputLayer = layers[layers.length-1];
		m_error = 0D;
		
		for (int n = 0; n < outputLayer.neurons.length - 1; n++) {
			double d = target[n] - outputLayer.neurons[n].getOutputVal();
			m_error += d * d;
		}
		m_error /= outputLayer.neurons.length - 1; // average error/neuron
		m_error = Math.sqrt(m_error); // RMS
		
		m_recentAverageError = (m_recentAverageError * m_recentAverageErrorSmoothing + m_error) / (m_recentAverageErrorSmoothing + 1);
		
		// output layer gradients
		
		for (int n = 0; n < outputLayer.neurons.length - 1; n++) {
			outputLayer.neurons[n].calcOutputGradients(target[n]);
		}
		
		// gradients on hidden
		
		for (int layerNum = layers.length - 2; layerNum > 0; layerNum--) {
			Layer hiddenLayer = layers[layerNum];
			Layer nextLayer = layers[layerNum + 1];
			
			for (int n = 0; n < hiddenLayer.neurons.length; n++) {
				hiddenLayer.neurons[n].calcHiddenGradients(nextLayer);
			}
		}
		
		// update weights
		
		for (int layerNum = layers.length - 1; layerNum > 0; layerNum--) {
			Layer layer = layers[layerNum];
			Layer prevLayer = layers[layerNum - 1];
			
			for (int n = 0; n < layer.neurons.length - 1; n++) {
				layer.neurons[n].updateInputWeights(prevLayer);
			}
		}
	}
	
	public double[] getResults() {
		double[] res = new double[layers[layers.length - 1].neurons.length - 1];
		
		for (int n = 0; n < res.length; n++) {
			res[n] = layers[layers.length - 1].neurons[n].getOutputVal();
		}
		
		return res;
	}
	public double getRecentAvgError() {
		return m_recentAverageError;
	}
}
