package main;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

import javax.management.InstanceAlreadyExistsException;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Vector2f;

import graphing.Diagram;
import neural.NeuralNet;
import neural.Population;
import util.ImmutableVector2f;

public class AntSimulation extends BasicGame {
	public boolean isRunning = true;

	public static void main(String[] args) {
		try {
			AntSimulation sim = new AntSimulation();
			AppGameContainer appgc = new AppGameContainer(sim, WIDTH+100, HEIGHT+100, false);
			appgc.setAlwaysRender(true);
			appgc.setShowFPS(false);
			appgc.setForceExit(false);
			appgc.start();
			sim.OnClose();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
	public void OnClose() {
		try {
			Diagram.setup("Gen", "Food", 0, 0, gen+2, 40, "Ants");
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		}
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
	
	private static final int foodAmount = 200;
	private static final float SIM_SPEED = 20f;
	private static final float GEN_LENGTH = 20f;
	private static final Color BCKG_COLOR = Color.gray;
	private static final Color TEXT_COLOR = Color.white;
	private static final Color FOOD_COLOR = Color.green;
	
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 800;
	
	private ArrayList<Ant> ants = new ArrayList<Ant>();
	private Population population;
	private ArrayList<ImmutableVector2f> food = new ArrayList<ImmutableVector2f>();
	private Random pseudo = new Random();
	private ArrayList<Integer> passedGenerations = new ArrayList<Integer>();
	private ArrayList<Double> maxFoodPerGen = new ArrayList<Double>();
	private ArrayList<Double> minFoodPerGen = new ArrayList<Double>();
	private float timeUntilNewGen = GEN_LENGTH;
	private TrueTypeFont ttf;
	private int gen = 1;
	
	public AntSimulation(String title) {
		super(title);
	}
	public AntSimulation() {
		this("Ant Simulation");
	}
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		ttf = new TrueTypeFont(new Font("Courier New", Font.BOLD, 20), true);
		gc.getInput().addKeyListener(new InputListener(this));
		gc.getInput().addMouseListener(new InputListener(this));
		
		// spawn ants randomly
		population = new Population();
		
		
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
		g.fillRect(0, 0, WIDTH+100, HEIGHT+100);
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
		g.setFont(ttf);
		g.drawString("FPS: " + gc.getFPS(), 10, infoY);
		
		// draw generation
		g.drawString("GENERATION: " + gen, 150, infoY);
		
		// draw generation progress bar
		g.setAntiAlias(false);
		float perc = (GEN_LENGTH - timeUntilNewGen) / GEN_LENGTH;
		int startX = 350;
		int width = 500;
		int height = 20;
		int border = 5;
		g.setColor(Color.darkGray);
		g.fillRect(startX - border, infoY - border, width + 2*border, height + 2*border);
		g.setColor(Color.lightGray);
		g.fillRect(startX, infoY, width, height);
		g.setColor(Color.green);
		g.fillRect(startX, infoY, perc * width, height);
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
		System.out.println("Generation " + gen + " ended. MaxFood="+Ant.maxFit + " minFood=" + Ant.minFit);
		passedGenerations.add(gen);
		maxFoodPerGen.add(Ant.maxFit);
		minFoodPerGen.add(Ant.minFit);
		
		population.nextGeneration(pseudo);
		getAntsFromPopulation();
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
}
