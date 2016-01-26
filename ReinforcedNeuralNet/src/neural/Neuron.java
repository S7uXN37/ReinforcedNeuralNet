package neural;

public class Neuron {
	private static double ETA = 0.15D;
	private static double ALPHA = 0.5D;
	
	public Connection[] out_weights;
	
	private double m_outputVal;
	private double m_gradient;
	private int myIndex;
	
	public Neuron(int nextLayerSize, int index) {
		myIndex = index;
		out_weights = new Connection[nextLayerSize];
		
		for (int i=0; i < nextLayerSize; i++) {
			out_weights[i] = new Connection();
		}
	}

	public void setOutput(Double double1) {
		m_outputVal = double1;
	}
	public double getOutputVal() {
		return m_outputVal;
	}

	public void feedForward(Layer prevLayer) {
		double sum = 0D;
		
		for (int n = 0; n < prevLayer.neurons.length; n++) {
			sum += prevLayer.neurons[n].getOutputVal() * prevLayer.neurons[n].out_weights[myIndex].weight;
		}
		
		m_outputVal = transfer(sum);
	}
	
	public static double transfer (double x) {
		return Math.tanh(x);
	}
	public static double transferDer (double x) {
		return 1 - Math.tanh(x)*Math.tanh(x);
	}

	public void calcOutputGradients(double target) {
		double d = target - m_outputVal;
		m_gradient = d * transferDer(m_outputVal);
	}

	public void calcHiddenGradients(final Layer nextLayer) {
		double dow = sumDOW(nextLayer);
		m_gradient = dow * transferDer(m_outputVal);
	}

	private final double sumDOW(final Layer nextLayer) {
		double sum = 0D;
		
		for (int n = 0; n < nextLayer.neurons.length - 1; n++) {
			sum += out_weights[n].weight * nextLayer.neurons[n].m_gradient;
		}
		
		return sum;
	}

	public void updateInputWeights(Layer prevLayer) {
		for (int n = 0; n < prevLayer.neurons.length; n++) {
			Neuron neuron = prevLayer.neurons[n];
			double oldDelta = neuron.out_weights[myIndex].deltaWeight;
			
			double newDelta =
					ETA
					* neuron.getOutputVal()
					* m_gradient
					+ ALPHA
					* oldDelta;
			
			neuron.out_weights[myIndex].deltaWeight = newDelta;
			neuron.out_weights[myIndex].weight += newDelta;
		}
	}

	public static void setConstants(double alpha2, double eta2) {
		ALPHA = alpha2;
		ETA = eta2;
	}
}
