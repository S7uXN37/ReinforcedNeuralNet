package graphing;

import java.awt.Font;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.Log;

public class Diagram extends BasicGame
{
	AppGameContainer appgc;
	Line[] lines;
	ArrayList<Graph> channels;
	Text[] texts;
	private double xAxisOffset;
	private double yAxisOffset;
	private double xRange;
	private double yRange;
	private TrueTypeFont font;
	public static boolean isOpen = false;
	
	public Diagram(String gamename, Line[] lines, Text[] texts, double xAxisOffset, double yAxisOffset, double xRange, double yRange)
	{
		super(gamename);
		this.lines = lines;
		this.texts = texts;
		this.xAxisOffset = xAxisOffset;
		this.yAxisOffset = yAxisOffset;
		this.xRange = xRange;
		this.yRange = yRange;
		channels = new ArrayList<Graph>();
	}
	
	public static void main(String[] args) {
		// TEST OUTPUT
		double[] x = new double[600];
		double[] fx = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			x[i] = i * Math.PI * 4 / x.length - Math.PI * 2;
			fx[i] = Math.sin(x[i]);
		}

		double[] px = new double[600];
		double[] py = new double[x.length];
		for (int i = 0; i < px.length; i++) {
			double t = i * Math.PI * 2 / px.length;
			px[i] = Math.sin(t);
			py[i] = Math.cos(t);
		}
		
		setup("x", "y", -Math.PI * 2, -1, Math.PI*4, 2, "TEST", 2);
		addData(x, fx, Color.blue, "sin", 0);
		addData(px, py, Color.red, "sin / cos", 1);
	}

	@Override
	public void init(GameContainer gc) throws SlickException {	}

	@Override
	public void update(GameContainer gc, int i) throws SlickException {}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException
	{
		if (font == null) {
			font = new TrueTypeFont(new Font("Courier New", Font.BOLD, 20), true);
		}
		for(Line l:lines){
			g.setColor(l.c);
			g.drawLine(l.p1.getX(), l.p1.getY(), l.p2.getX(), l.p2.getY());
		}
		for(Text t:texts){
			g.setColor(t.c);
			g.setFont(font);
			g.drawString(t.text, t.p.getX(), t.p.getY());
		}
		for (Graph gr : channels) {
			if (gr == null)
				continue;
			
			for(Line l:gr.lines){
				g.setColor(l.c);
				g.drawLine(l.p1.getX(), l.p1.getY(), l.p2.getX(), l.p2.getY());
			}
			for(Text t:gr.texts){
				g.setColor(t.c);
				g.drawString(t.text, t.p.getX(), t.p.getY());
			}
		}
	}

	public void start()
	{
		try
		{
			appgc = new AppGameContainer(this);
			appgc.setDisplayMode(1200, 800, false);
			appgc.setShowFPS(false);
			appgc.setAlwaysRender(true);
			appgc.start();
		}
		catch (SlickException ex)
		{
			Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IllegalStateException ex)
		{
			// don't know how to avoid this one when closing
		}
	}
	
	public void keyPressed(int key, char c) {
		if (key == Input.KEY_ESCAPE) {
			Log.info("Closing...");
			isOpen = false;
			appgc.destroy();
			System.exit(0);
		}
	}

	
	private static final Point ORIGIN = new Point(50,50);
	private static final double xAxisLength = 1200 - ORIGIN.x*2;
	private static final double yAxisLength = 800 - ORIGIN.y*2;
	private static final int ARROW_ADJUSTMENT = 10;
	private static final int X_AXIS_LABEL_OFFSET = 10;
	private static final int Y_AXIS_LABEL_OFFSET = 50;
	private static final Color DIAGRAM_COLOR = Color.white;
	private static final Color TEXT_COLOR = Color.gray;
	
	private static Diagram diagram;
	private static int numGraphs = 2;
	
	/**
	 * Setup the Diagram, must be called before addData
	 * @param xAxisName Label for x axis
	 * @param yAxisName Label for y axis
	 * @param xAxisOffset Starting value for x axis
	 * @param yAxisOffset Starting value for y axis
	 * @param xRange Range of x axis
	 * @param yRange Range of y axis
	 * @param title Title of Diagram
	 * @param numChannels Intended number of channels to be used, for placing labels correctly
	 * @return
	 */
	public static boolean setup(
			String xAxisName, String yAxisName,
			double xAxisOffset, double yAxisOffset, double xRange, double yRange, String title, int numChannels)
	{
		numGraphs = numChannels;
		
		if (diagram != null)
			return false;
		
		Log.setVerbose(false);
		Line[] lines = new Line[6];
		
		// Set up diagram background and labels
		// axis:
		Line xAxis = new Line(ORIGIN, new Point(ORIGIN.x+xAxisLength, ORIGIN.y), Color.white);
		Line yAxis = new Line(ORIGIN, new Point(ORIGIN.x, ORIGIN.y+yAxisLength), Color.white);
		Line arrX1 = new Line(xAxis.p2, new Point(xAxis.p2.x - ARROW_ADJUSTMENT, xAxis.p2.y - ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		Line arrX2 = new Line(xAxis.p2, new Point(xAxis.p2.x - ARROW_ADJUSTMENT, xAxis.p2.y + ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		Line arrY1 = new Line(yAxis.p2, new Point(yAxis.p2.x - ARROW_ADJUSTMENT, yAxis.p2.y - ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		Line arrY2 = new Line(yAxis.p2, new Point(yAxis.p2.x + ARROW_ADJUSTMENT, yAxis.p2.y - ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		
		lines[0] = xAxis;
		lines[1] = yAxis;
		lines[2] = arrX1;
		lines[3] = arrX2;
		lines[4] = arrY1;
		lines[5] = arrY2;
		
		// axis labels
		Text[] t = new Text[8];
		t[0] = new Text( new Point( ORIGIN.x-30, yAxis.p2.y +20 ) , yAxisName, TEXT_COLOR);
		t[1] = new Text( new Point( xAxis.p2.x, ORIGIN.y+20) , xAxisName, TEXT_COLOR);
		// axis reference values X:
		t[2] = new Text( new Point( (ORIGIN.x+xAxis.p2.x)/2, ORIGIN.y-X_AXIS_LABEL_OFFSET) , ""+(int)((xRange/2+xAxisOffset)*100) /100f, TEXT_COLOR);
		t[3] = new Text( new Point( ORIGIN.x, ORIGIN.y-X_AXIS_LABEL_OFFSET) , ""+(int)(xAxisOffset*100) /100f, TEXT_COLOR);
		t[4] = new Text( new Point( xAxis.p2.x, ORIGIN.y-X_AXIS_LABEL_OFFSET) , ""+(int)((xAxisOffset+xRange)*100) /100f, TEXT_COLOR);
		// axis reference values Y:
		t[5] = new Text( new Point( ORIGIN.x-Y_AXIS_LABEL_OFFSET, (ORIGIN.y+yAxis.p2.y)/2) , ""+(int)((yRange/2+yAxisOffset)*100) /100f, TEXT_COLOR);
		t[6] = new Text( new Point( ORIGIN.x-Y_AXIS_LABEL_OFFSET, ORIGIN.y) , ""+(int)(yAxisOffset*100) /100f, TEXT_COLOR);
		t[7] = new Text( new Point( ORIGIN.x-Y_AXIS_LABEL_OFFSET, yAxis.p2.y) , ""+(int)((yAxisOffset+yRange)*100) /100f, TEXT_COLOR);
		
		// create instance and start
		diagram = new Diagram(title, lines, t, xAxisOffset, yAxisOffset, xRange, yRange);
		Thread renderLoop = new Thread(new Runnable(){
			@Override
			public void run() {
				diagram.start();
			}
		}, "RenderThread");
		renderLoop.start();
		isOpen = true;
		return true;
	}
	
	/**
	 * Add new data to the graph
	 * @param xAxisValues List of x values
	 * @param yAxisValues Corresponding list of y values
	 * @param color Color of the graph
	 * @param graphName Label of the graph
	 * @param channel Channel to use (0-based)
	 */
	public static void addData(double[] xAxisValues, double[] yAxisValues, Color color, String graphName, int channel) {
		Line[] newLines = createLinesFromData(xAxisValues, yAxisValues, diagram.xAxisOffset, diagram.yAxisOffset, diagram.xRange, diagram.yRange, color);
		if (newLines == null)
			return;
		
		Text[] newTexts = new Text[1];
		for(int i=0;i<newTexts.length ;i++) {
			newTexts[i] = new Text( new Point((channel + 1) * xAxisLength / (numGraphs+1) + ORIGIN.x, ORIGIN.y-2*X_AXIS_LABEL_OFFSET), graphName, color);
		}
		
		Graph g = new Graph(newLines, newTexts);
		
		while (diagram.channels.size() <= channel)
			diagram.channels.add(null);
		
		diagram.channels.set(channel, g);
	}
	
	private static Line[] createLinesFromData(double[] inputs,
			double[] outputs, double xAxisOffset, double yAxisOffset,
			double xRange, double yRange, Color color) {
		Point[] vals = new Point[inputs.length];
		
		for(int i = 0; i < vals.length; i++){
			vals[i] = new Point(
					ORIGIN.x + (xAxisLength/xRange)*(inputs[i]-xAxisOffset),
					ORIGIN.y + (yAxisLength/yRange)*(outputs[i]-yAxisOffset)
			);
		}
		
		return createLinesFromData(vals, color);
	}
	
	private static Line[] createLinesFromData(Point[] vals, Color color)
	{
		if (vals.length <= 0)
			return null;
		
		Line[] connectingLines = new Line[vals.length-1];
		for(int i = 0; i < connectingLines.length; i++) {
			connectingLines[i] = new Line(vals[i],vals[i + 1], color);
		}
		
		return connectingLines;
	}
}

class Graph {
	Line[] lines;
	Text[] texts;
	
	public Graph (Line[] l, Text[] t) {
		lines = l;
		texts = t;
	}
}