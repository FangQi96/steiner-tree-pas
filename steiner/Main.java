package steiner;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import gnu.getopt.Getopt;

/**
 * Program entry point
 * @author Kemeny Tamas
 */
public class Main {

	private static String readFile(String path) {
		StringBuffer buffer = new StringBuffer();
		InputStream in = Main.class.getResourceAsStream(path);
		if (in == null) return null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			while((line = reader.readLine()) != null) {
				buffer.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	public static void printopts(String inputPath, Boolean view, Boolean quiet, Integer tCutOff, Integer accuracy, 
			Boolean longOut, Boolean slowKernel) {
		System.out.println(
				"		String inputPath = " + inputPath + "\n" + 
				"		Boolean view = " + view + "\n" + 
				"		Boolean quiet = " + quiet + "\n" + 
				"		Integer tCutOff = " + tCutOff + "\n" + 
				"		Integer accuracy = " + accuracy + "\n" + 
				"		Boolean longOut = " + longOut + "\n" + 
				"		Boolean slowKernel = " + slowKernel + "\n"
				);
	}
	
	public static void die(String msg) {
		System.out.println(msg);
		System.exit(1);
	}

	public static void main(String[] args) {
		String inputPath = null;
		Boolean view = false,
				quiet = false,
				longOut = false,
				slowKernel = false;
		Integer tCutOff = null,
				accuracy = null;
		printopts(inputPath, view, quiet, tCutOff, accuracy, longOut, slowKernel);
		Getopt g = new Getopt("parser", args, "a:t:svq");
		int c;
		String arg;
		while ((c = g.getopt()) != -1)
		{
			switch(c)
			{
			case 's':
				slowKernel = true;
				break;
			case 'v':
				view = true;
				break;
			case 'q':
				quiet = true;
				break;
			case 'a':
				arg = g.getOptarg();
				try {
					accuracy = Integer.parseInt(arg);
					if (accuracy < 0 || accuracy > 10) throw new NumberFormatException();
				}
				catch (NumberFormatException e) {
					die("Option -a must have an integer argument [0..10]");
				}
				break;
			case 't':
				arg = g.getOptarg();
				try {
					tCutOff = Integer.parseInt(arg);
				}
				catch (NumberFormatException e) {
					die("Option -t must have an integer argument");
				}
				break;
			case 'h':
			case '?':
				die("Options: [-svh [-q|-l] [-a [0..10]|-t [:integer:]]]");
				break;
			default:
				System.out.print("getopt() returned " + c + "\n");
			}
		}
		List<String> largs = new ArrayList<String>(args.length-g.getOptind());
		for (int i = g.getOptind(); i < args.length; i++) 
		{
			largs.add(args[i]);
		}
		for (String s : largs) System.out.println(s);
			
		printopts(inputPath, view, quiet, tCutOff, accuracy, longOut, slowKernel);
	
	}

	private static void display(SteinerGraph displayGraph, List<Integer> exactEdges, List<Integer> approxEdges){
		Graph graph = new SingleGraph("DisplayGraph");
		graph.setStrict(false);
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.quality");
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		// Load style sheet
		String styleSheet;
		if ((styleSheet = readFile("styleSheet.css")) != null) {
			graph.addAttribute("ui.stylesheet", styleSheet);
		}
		else {
			System.out.println("No stylesheet was found");
		}

		// Draw vertices
		for(SteinerGraphVertex v : displayGraph.vertices.values()){
			graph.addNode(Integer.toString(v.id));
			if (v.isTerminal) {
				graph.getNode(Integer.toString(v.id)).setAttribute("ui.class", "terminal");
			}
		}
		
		// Draw edges
		for(SteinerGraphEdge edge : displayGraph.edges.values()){
			Integer id = edge.getID().get(0);
			graph.addEdge(id.toString(), Integer.toString(edge.getStart()), Integer.toString(edge.getEnd()));
			Edge e = graph.getEdge(id.toString());
			e.addAttribute("ui.label", edge.getWeight());
			if (exactEdges.contains(id)){
				e.setAttribute("ui.class", "exactEdge");
				if (!displayGraph.vertices.get(edge.getStart()).isTerminal){
					graph.getNode(Integer.toString(edge.getStart()))
							.setAttribute("ui.class", "steiner");
				}
				if (!displayGraph.vertices.get(edge.getEnd()).isTerminal){
					graph.getNode(Integer.toString(edge.getEnd()))
							.setAttribute("ui.class", "steiner");
				}
			}
			else if (approxEdges.contains(id)){
				e.setAttribute("ui.class", "approxEdge");
				if (!displayGraph.vertices.get(edge.getStart()).isTerminal){
					graph.getNode(Integer.toString(edge.getStart()))
							.setAttribute("ui.class", "steiner");
				}
				if (!displayGraph.vertices.get(edge.getEnd()).isTerminal){
					graph.getNode(Integer.toString(edge.getEnd()))
							.setAttribute("ui.class", "steiner");
				}
			}
		}
		graph.display();
	}

}
