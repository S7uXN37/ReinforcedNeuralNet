package main;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Vector2f;

import graphing.Diagram;
import neural.Connection;
import neural.NeuralNet;
import neural.Population;
import util.ImmutableVector2f;
import util.Util;

public class AntSimulation extends BasicGame {
	public boolean isRunning = true;

	public static void main(String[] args) {
		try {
			AntSimulation sim = new AntSimulation();
			AppGameContainer appgc = new AppGameContainer(sim, WIDTH+2*GAME_AREA_BORDER, HEIGHT+2*GAME_AREA_BORDER, false);
			appgc.setAlwaysRender(true);
			appgc.setShowFPS(false);
			appgc.setForceExit(false);
			try {
				appgc.start();
			} catch (IllegalStateException e) {
				// do nothing
			} finally {
				sim.OnClose();
			}
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
	public void OnClose() {
		Diagram.setup("Gen", "Food", 1, 0, gen - 2, 40, "Ants", 2);
		
		double[] xAxis = new double[passedGenerations.size()];
		for (int i = 0; i < xAxis.length; i++) {
			xAxis[i] = passedGenerations.get(i);
		}
		double[] yMax = new double[maxFoodPerGen.size()];
		for (int i = 0; i < xAxis.length; i++) {
			yMax[i] = maxFoodPerGen.get(i);
		}
		double[] yMin = new double[minFoodPerGen.size()];
		for (int i = 0; i < xAxis.length; i++) {
			yMin[i] = minFoodPerGen.get(i);
		}
		Diagram.addData(xAxis, yMax, Color.blue, "Max Food", 0);
		Diagram.addData(xAxis, yMin, Color.red, "Min Food", 1);
	}
	
	// PRIVATE SIM CONSTS
	private static final int foodAmount = 200;
	private static final float SIM_SPEED = 20f;
	private static final float GEN_LENGTH = 20f;
	
	// PUBLIC GRAPHICS CONSTS
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 800;
	public static final int GAME_AREA_BORDER = 50;
	
	// PRIVATE GRAPHICS CONSTS
	private static final Color BCKG_COLOR = Color.gray;
	private static final Color TEXT_COLOR = Color.white;
	private static final Color FOOD_COLOR = Color.green;
	private static final Color TINT_COLOR = new Color(0, 0, 0, 100);
	// progress bar
	private static final float PROG_BAR_START_X = 350;
	private static final float PROG_BAR_WIDTH = 500;
	private static final float PROG_BAR_HEIGHT = 20;
	private static final float PROG_BAR_BORDER = 5;
	// net representation
	private static final float NET_INDENT = 50;
	private static final float NET_UPPER_Y = GAME_AREA_BORDER + NET_INDENT;
	private static final float NET_HEIGHT = HEIGHT - 2 * NET_INDENT;
	private static final float NET_LEFT_X = GAME_AREA_BORDER + NET_INDENT;
	private static final float NET_WIDTH = WIDTH - 2 * NET_INDENT;
	private static final float NET_NON_HIDDEN_INTERV = 200;
	private static final float NET_HIDDEN_WIDTH = 800;
	private static final float NET_NODE_SIZE = 50;
	private static final Color NET_NODE_BCKG_COLOR = Color.blue;
	private static final Color NET_NODE_POS_COLOR = Color.green;
	private static final Color NET_NODE_NEG_COLOR = Color.red;
	private static final Color NET_NODE_TEXT_COLOR = Color.orange;
	private static final Color NET_CONN_POS_COLOR = Color.green;
	private static final Color NET_CONN_NEG_COLOR = Color.red;
	private static final Color NET_CONN_ZERO_COLOR = new Color(Color.cyan.r, Color.cyan.g, Color.cyan.b, 0.1f);
	private static final float NET_CONN_MIN_WIDTH = 25;
	private static final float NET_CONN_MAX_WIDTH = 50;
	
	// PRIVATE SIM VARS
	private ArrayList<Ant> ants = new ArrayList<Ant>();
	private Population population;
	private ArrayList<ImmutableVector2f> food = new ArrayList<ImmutableVector2f>();
	private Random pseudo = new Random();
	private float timeUntilNewGen = GEN_LENGTH;
	private int gen = 1;
	
	// PRIVATE METRICS VARS
	private ArrayList<Integer> passedGenerations = new ArrayList<Integer>();
	private ArrayList<Double> maxFoodPerGen = new ArrayList<Double>();
	private ArrayList<Double> minFoodPerGen = new ArrayList<Double>();
	
	// PRIVATE GRAPHICS VARS
	private TrueTypeFont bckgFont;
	private TrueTypeFont netFont;
	private int netToDisplay = -1;
	
	public AntSimulation(String title) {
		super(title);
	}
	public AntSimulation() {
		this("Ant Simulation");
	}
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		bckgFont = new TrueTypeFont(new Font("Courier New", Font.BOLD, 20), true);
		netFont = new TrueTypeFont(new Font("Courier New", Font.BOLD, 50), true);
		gc.getInput().addKeyListener(new InputListener(this));
		gc.getInput().addMouseListener(new InputListener(this));
		
		// spawn ants randomly
		population = new Population(pseudo);
		population.firstGeneration(pseudo);
		getAntsFromPopulation();
		
		// spawn food randomly
		for (int i = 0; i < foodAmount; i++) {
			ImmutableVector2f pos = new ImmutableVector2f(
					(float) pseudo.nextDouble() * WIDTH,
					(float) pseudo.nextDouble() * HEIGHT
			);
			food.add(pos);
		}
	}
	
	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		g.setColor(BCKG_COLOR);
		g.fillRect(0, 0, WIDTH + 2 * GAME_AREA_BORDER, HEIGHT + 2 * GAME_AREA_BORDER);
		g.setAntiAlias(true);
		
		// draw food
		for (ImmutableVector2f p : food) {
			float x = p.getScreenX();
			float y = p.getScreenY();
			
			g.setColor(FOOD_COLOR);
			g.fillOval(x, y, 10, 10);
		}
		
		int infoY = 10;
		// draw ants
		for (Ant a : ants) {
			if (a == null)
				continue;
			a.draw(gc, g);
		}
		
		// draw FPS
		g.setColor(TEXT_COLOR);
		g.setFont(bckgFont);
		g.drawString("FPS: " + gc.getFPS(), 10, infoY);
		
		// draw generation
		g.drawString("GENERATION: " + gen, 150, infoY);
		
		// draw generation progress bar
		g.setAntiAlias(false);
		float perc = (GEN_LENGTH - timeUntilNewGen) / GEN_LENGTH;
		g.setColor(Color.darkGray);
		g.fillRect(PROG_BAR_START_X - PROG_BAR_BORDER, infoY - PROG_BAR_BORDER, PROG_BAR_WIDTH + 2*PROG_BAR_BORDER, PROG_BAR_HEIGHT + 2*PROG_BAR_BORDER);
		g.setColor(Color.lightGray);
		g.fillRect(PROG_BAR_START_X, infoY, PROG_BAR_WIDTH, PROG_BAR_HEIGHT);
		g.setColor(Color.green);
		g.fillRect(PROG_BAR_START_X, infoY, perc * PROG_BAR_WIDTH, PROG_BAR_HEIGHT);
		
		
		// draw nets
		if (netToDisplay != -1) {
			NeuralNet net = ants.get(netToDisplay).net;
			
			// background tint
			g.setColor(TINT_COLOR);
			g.fillRect(0, 0, WIDTH + 2 * GAME_AREA_BORDER, HEIGHT + 2 * GAME_AREA_BORDER);
			g.fillRect(GAME_AREA_BORDER, GAME_AREA_BORDER, WIDTH, HEIGHT);
			
			// #### draw net ####
			g.setAntiAlias(true);
			// draw nodes:
				// calculate semi-constants
				int totNodes = net.neurons.size();
				int numHidden = totNodes - NeuralNet.INPUTS - NeuralNet.OUTPUTS;
				float hiddenInterv = NET_HIDDEN_WIDTH / (numHidden - 1);
				
				g.setFont(netFont);
				for (int node : net.neurons.keySet()) {
					// get centred position
					ImmutableVector2f pos = nodeToPos(node, hiddenInterv, numHidden);
					// move to upper left
					float x = pos.x - NET_NODE_SIZE / 2;
					float y = pos.y - NET_NODE_SIZE / 2;
					
					// draw node
					g.setColor(NET_NODE_BCKG_COLOR);
					g.fillOval(x, y, NET_NODE_SIZE, NET_NODE_SIZE);
					
					float val = (float) net.neurons.get(node).value;
					if (val > 0) {
						g.setColor(NET_NODE_POS_COLOR);
						g.fillArc(x, y, NET_NODE_SIZE, NET_NODE_SIZE, 90 - 180 * val, 90);
					} else {
						g.setColor(NET_NODE_NEG_COLOR);
						g.fillArc(x, y, NET_NODE_SIZE, NET_NODE_SIZE, 90, 90 - 180 * val);
					}
					
					// draw number
					g.setColor(NET_NODE_TEXT_COLOR);
					g.drawString("" + node, x + NET_NODE_SIZE / 5, y);
				}
			
			// draw connections:
				for (Connection c : net.connections) {
					float width = (float) Util.lerp(-1, 1, NET_CONN_MIN_WIDTH, NET_CONN_MAX_WIDTH, c.weight);
					Color color = 
							c.weight > 0 ? 
								Util.colorLerp(NET_CONN_ZERO_COLOR, NET_CONN_POS_COLOR, c.weight)
							:
								Util.colorLerp(NET_CONN_NEG_COLOR, NET_CONN_ZERO_COLOR, -c.weight)
							;
					ImmutableVector2f start = nodeToPos(c.originID, hiddenInterv, numHidden);
					ImmutableVector2f end = nodeToPos(c.targetID, hiddenInterv, numHidden);
					
					// get vector perpendicular to vector start -> end
					ImmutableVector2f sToE = end.sub(start).normalise();
					ImmutableVector2f perp = new ImmutableVector2f(1/(sToE.x + 0.01f), -1/(sToE.y + 0.01f)).normalise().scale(width/2);
					ImmutableVector2f off = sToE.scale(NET_NODE_SIZE / 2);
					
					// create points for base of triangle
					ImmutableVector2f s1 = start.sub(perp).add(off);
					ImmutableVector2f s2 = start.add(perp).add(off);
					ImmutableVector2f e = end.sub(off);
					
					// create triangle
					Polygon p = new Polygon();
					p.addPoint(s1.x, s1.y);
					p.addPoint(s2.x, s2.y);
					p.addPoint(e.x, e.y);
					
					// draw triangle
					g.setColor(color);
					g.fill(p);
				}
		}
	}

	private ImmutableVector2f nodeToPos(int node, float hiddenInterv, int numHidden) {
		float x, y;
		// INPUTS
		if (node < NeuralNet.INPUTS) {
			x = (NET_LEFT_X * 2 + NET_WIDTH) / 2									// middle
				- ((NeuralNet.INPUTS - 1) * NET_NON_HIDDEN_INTERV / 2)				// minus half row-length
				+ NET_NON_HIDDEN_INTERV * node;										// plus n * row interval
			y = NET_UPPER_Y + NET_HEIGHT;											// lower y
		}
		
		// OUTPUTS
		else if (node < NeuralNet.INPUTS + NeuralNet.OUTPUTS) {
			x = (NET_LEFT_X * 2 + NET_WIDTH) / 2									// middle
				- ((NeuralNet.OUTPUTS - 1) * NET_NON_HIDDEN_INTERV / 2)				// minus half row-length
				+ NET_NON_HIDDEN_INTERV * (node - NeuralNet.INPUTS);				// plus (n - (i + o)) * row interval
			y = NET_UPPER_Y;														// upper y
		}
		
		// HIDDEN
		else {
			x = (NET_LEFT_X * 2 + NET_WIDTH) / 2									// middle
				- ((numHidden - 1) * hiddenInterv / 2)						// plus half row-length
				+ hiddenInterv * (node - (NeuralNet.INPUTS + NeuralNet.OUTPUTS));	// minus (l - 1 - n) * row interval
			y = (NET_UPPER_Y * 2 + NET_HEIGHT) / 2;									// mid y
		}
		
		return new ImmutableVector2f(x, y);
	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		if (!isRunning)
			return;
			
		if (timeUntilNewGen <= 0) {
			nextGeneration();
			timeUntilNewGen = GEN_LENGTH;
		}
		timeUntilNewGen -= delta*SIM_SPEED/1000f;
		
		Ant.maxFit = Integer.MIN_VALUE;
		Ant.minFit = Integer.MAX_VALUE;
		for (int i = 0; i < ants.size(); i++) {
			Ant a = ants.get(i);
			if (a == null) {
				ants.remove(i);
				continue;
			}
			if (a.net.fitness > Ant.maxFit)
				Ant.maxFit = a.net.fitness;
			else if (a.net.fitness < Ant.minFit)
				Ant.minFit = a.net.fitness;
			
			ImmutableVector2f closestFood = null;
			float closestFoodDistSqr = Float.MAX_VALUE;
			
			for (int j = 0; j < ants.size(); j++) {
				Ant a2 = ants.get(j);
				if (i != j) {
					ImmutableVector2f toAnt = a2.position.sub(a.position);
					if (toAnt.length() < Ant.bodyRadius * 2) {
						a.position = a.position.add(Ant.bodyRadius*2 - toAnt.length());
					}
				}
			}
			
			for (int k = 0; k < food.size(); k++) {
				Vector2f toBody = food.get(k).makeVector2f();
				toBody.sub(a.position.makeVector2f());
				Vector2f toHead = food.get(k).makeVector2f();
				toHead.sub(a.headPosition.makeVector2f());
				
				if (toBody.length() < Ant.bodyRadius || toHead.length() < Ant.headRadius) {
					a.pickupFood();
					
					food.set(k, getRandPos());
					
					if (food.size() == 0)
						break;
				} else if (toBody.lengthSquared() < closestFoodDistSqr) {
					closestFood = new ImmutableVector2f(toBody);
					closestFoodDistSqr = toBody.lengthSquared();
				}
			}
			
			ImmutableVector2f vec = closestFood.normalise();
			double[] input = new double[]{vec.x, vec.y};
			a.tick(input, SIM_SPEED * delta/1000f);
		}
	}
	
	private void nextGeneration () {
		isRunning = true;
		netToDisplay = -1;
		
		System.out.println("Generation " + gen + " ended. MaxFood="+Ant.maxFit + " minFood=" + Ant.minFit);
		passedGenerations.add(gen);
		maxFoodPerGen.add(Ant.maxFit);
		minFoodPerGen.add(Ant.minFit);
		
		population.nextGeneration(pseudo);
		getAntsFromPopulation();
		Ant.maxFit = 0;
		Ant.minFit = 0;
		
		for (NeuralNet n : population.population) {
			n.fitness = 0;
		}
		
		gen++;
	}
	
	private void getAntsFromPopulation() {
		ArrayList<Ant> newAnts = new ArrayList<Ant>();
		for (NeuralNet n : population.population) {
			Ant a = new Ant(n, 50, getRandPos().makeVector2f());
			newAnts.add(a);
		}
		ants = newAnts;
	}

	private ImmutableVector2f getRandPos() {
		ImmutableVector2f pos = new ImmutableVector2f(
				(float) pseudo.nextDouble() * WIDTH,
				(float) pseudo.nextDouble() * HEIGHT
		);
		return pos;
	}

	public void click(int x, int y) {
		if (netToDisplay != -1) {
			netToDisplay = -1;
			return;
		}
		
		ImmutableVector2f click = new ImmutableVector2f(x - GAME_AREA_BORDER, y - GAME_AREA_BORDER);
		
		int hit = -1;
		for (int i = 0; i < ants.size(); i++) {
			Ant a = ants.get(i);
			if (a == null)
				continue;
			
			double dist = click.sub(a.position).length();
			if (dist <= Ant.bodyRadius) {
				hit = i;
			}
		}
		
		if (hit != -1) {
			netToDisplay = hit;
			isRunning = false;
		}
	}

	public void space() {
		isRunning = !isRunning;
	}

	public void close() {
		Display.destroy();
	}
}
