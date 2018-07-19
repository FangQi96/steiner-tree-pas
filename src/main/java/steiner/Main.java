package steiner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import org.graphstream.ui.swingViewer.*;


import javax.swing.*;
import java.util.Iterator;

/**
 * Program entry point
 * @author Kemeny Tamas
 */
public class Main {

	static String readFile(String path) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded);
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
//		[-display input/output] [-in inputPath] [-out outputPath] [-t|-terminal cutOff] [-s|-steinervertex cutOff] [-a|-accuracy 0..10] [-silent true/false]
//		String params = "-display input -in src/instances/instance-test.gr";
//		String params = "-display output -in src/instances/instance001.gr";
		String params = "-d o -i src/instances/instance001.gr -a 5";
//		String params = "src/instances/instance-test.gr";
		String[] paramlist = params.split(" ");

		String inputPath = null;
		String outputPath = null;
		Boolean displayInput = false;
		Boolean displayOutput = false;
		Boolean silent = false;
		Integer tCutOff = null;
		Integer sCutOff = null;
		Integer accuracy = null;

		for(int i = 0; i < paramlist.length; i+=2){
			String param = paramlist[i];
			String value = paramlist[i+1];
			if (param.equals("-display") || param.equals("-d")){
				if (value.equals("input") || value.equals("i")){
					displayInput = true;
					displayOutput = false;
				}
				if (value.equals("output") || value.equals("o")){
					displayInput = false;
					displayOutput = true;
				}
			}
			if (param.equals("-silent")){
				if (value.equals("true") || value.equals("t")) silent = true;
				if (value.equals("false") || value.equals("f")) silent = false;
			}
			if (param.equals("-in") || param.equals("-i")) inputPath = value;
			if (param.equals("-out") || param.equals("-o")) outputPath = value;
			if (param.equals("-t") || param.equals("-terminal")) tCutOff = Integer.parseInt(value);
			if (param.equals("-s") || param.equals("-steinervertex")) sCutOff = Integer.parseInt(value);
			if (param.equals("-a") || param.equals("-accuracy")) accuracy = Integer.parseInt(value);
		}

		if (inputPath == null){
			System.out.println("No input file specified");
			System.exit(0);
		}

//		File folder = new File("src/instances");
//		File[] listOfFiles = folder.listFiles();
//		Arrays.sort(listOfFiles);
//		for(int i = 0; i < listOfFiles.length; i++) {
//			if (listOfFiles[i].toString().substring(listOfFiles[i].toString().lastIndexOf('.')).equals(".gr")) {
//				String path = listOfFiles[i].toString();
//			}
//		}

		SteinerGraph inputGraph = new ReadInput(inputPath).getSteinerGraph();
		if (displayInput) display(inputGraph);
		else {
			SteinerGraph processedGraph = new SteinerGraph(inputGraph);
			processedGraph.preProcess();
			Integer preProcessSteinerDifference = inputGraph.vertices.size()-processedGraph.vertices.size();
			FastKernel kernel = new FastKernel(processedGraph);
			kernel.contract();
			List<SteinerGraph> saves = kernel.getSnapshots();

			Map<Integer, Integer> tCutoffToSave = new HashMap<>();
			Map<Integer, Integer> sCutoffToSave = new HashMap<>();

			List<Integer> tCutOffList = new ArrayList<>();
			List<Integer> sCutOffList = new ArrayList<>();
			for (int i = 0; i < saves.size(); i++) {
				SteinerGraph save = saves.get(i);
				tCutOffList.add(save.terminals.size());
				tCutoffToSave.put(save.terminals.size(), i);

				sCutOffList.add(save.vertices.size() - save.terminals.size() + preProcessSteinerDifference);
				sCutoffToSave.put(save.vertices.size() - save.terminals.size() + preProcessSteinerDifference, i);
			}
			if (!silent) {
				System.out.println("Instance with " + inputGraph.terminals.size() + " Terminals and " + (inputGraph.vertices.size() - inputGraph.terminals.size()) + " Steiner vertices");
				System.out.println("Number of contractions: " + (saves.size() - 1));
				System.out.println("Terminal cut-off points: " + tCutOffList);
				System.out.println("Steiner vertex cut-off points: " + sCutOffList);
			}

			Integer selectedIndex = Integer.MAX_VALUE;
			int accuracyRange = 10;

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				while (selectedIndex == Integer.MAX_VALUE) {
					if ((tCutOff == null) && (sCutOff == null) && (accuracy == null)) {
						String[] line = new String[1];
						while ((line.length < 2) || (line.length % 2 == 1)) {
							System.out.println("[-t|-terminal cutOff] [-s|-steinervertex cutOff] [-a|-accuracy [0..10]]");
							line = br.readLine().split(" ");
						}
						for (int i = 0; i < line.length; i += 2) {
							String param = line[i];
							String value = line[i + 1];
							if (param.equals("-t") || param.equals("-terminal")) tCutOff = Integer.parseInt(value);
							if (param.equals("-s") || param.equals("-steinervertex")) sCutOff = Integer.parseInt(value);
							if (param.equals("-a") || param.equals("-accuracy")) accuracy = Integer.parseInt(value);
						}
					}
					if (accuracy != null){
						Integer accuracyIndex = 1 + ((saves.size() - 2) * (accuracyRange - accuracy) / accuracyRange);
						if (accuracy <= 0){
							accuracyIndex = saves.size() - 1;
							accuracy = 0;
						}
						else if (accuracy >= accuracyRange){
							accuracyIndex = 0;
							accuracy = accuracyRange;
						}
						selectedIndex = Math.min(accuracyIndex, selectedIndex);
					}
					if (tCutOff != null){
						if (tCutoffToSave.keySet().contains(tCutOff)){
							selectedIndex = Math.min(tCutoffToSave.get(tCutOff), selectedIndex);
						}
						else {
							Integer closestSaveIndex = null;
							Integer closestTerminalCount = Integer.MAX_VALUE;
							for(Map.Entry<Integer, Integer> entry : tCutoffToSave.entrySet()){
								if ((entry.getKey() > tCutOff) && (entry.getKey() < closestTerminalCount)){
									closestTerminalCount = entry.getKey();
									closestSaveIndex = entry.getValue();
								}
							}
							selectedIndex = Math.min(closestSaveIndex, selectedIndex);
						}
					}
					if (sCutOff != null){
						if (sCutoffToSave.keySet().contains(sCutOff)){
							selectedIndex = Math.min(sCutoffToSave.get(sCutOff), selectedIndex);
						}
						else {
							Integer closestSaveIndex = null;
							Integer closestSteinerVertexCount = Integer.MAX_VALUE;
							for(Map.Entry<Integer, Integer> entry : sCutoffToSave.entrySet()){
								if ((entry.getKey() > sCutOff) && (entry.getKey() < closestSteinerVertexCount)){
									closestSteinerVertexCount = entry.getKey();
									closestSaveIndex = entry.getValue();
								}
							}
							selectedIndex = Math.min(closestSaveIndex, selectedIndex);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			SteinerGraph selectedSteinerGraph = new SteinerGraph(saves.get(selectedIndex));
			int exactPart, approxPart;
			List<Integer> exactTree = new ArrayList<>();
			List<Integer> approxTree = saves.get(selectedIndex).getSteinerTreeEdges();

			if (selectedIndex == 0) approxPart = 0;
			else approxPart = selectedSteinerGraph.getSteinerTreeWeight();

			if (selectedIndex == saves.size()-1) exactPart = 0;
			else {
				DreyfusWagner fpt = new DreyfusWagner();
				fpt.solve(selectedSteinerGraph);
				exactPart = fpt.getWeight();
				exactTree = fpt.getEdges();
			}

			System.out.print("Selected index: " + selectedIndex
					+ " Steiner tree weight: " + approxPart + " + " + exactPart
					+ " = " + (approxPart + exactPart) + " " + approxTree + " " + exactTree);
			List<Integer> combinedEdges = new ArrayList<>();

			combinedEdges.addAll(approxTree);
			combinedEdges.addAll(exactTree);

			SteinerGraph steinerTree = new ReadInput(inputPath, combinedEdges).getSteinerGraph();
			steinerTree.removeIsolated();
			SteinerTreeValidator validator = new SteinerTreeValidator();
			System.out.println(" Valid Steiner tree? " + validator.validate(steinerTree));


			if (displayOutput) display(inputGraph, exactTree, approxTree);

		}
	}

	public static void display(SteinerGraph displayGraph){
		display(displayGraph, new ArrayList<>(), new ArrayList<>());
	}

	public static void display(SteinerGraph displayGraph, List<Integer> exactEdges, List<Integer> approxEdges){
		String styleSheet = readFile("src/styleSheet.css");

		Graph graph = new SingleGraph("Steiner Tree Problem");
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.antialias");
		graph.setStrict(false);
		for(SteinerGraphVertex v : displayGraph.vertices.values()){
			graph.addNode(Integer.toString(v.id+1));
			if (v.isTerminal) graph.getNode(Integer.toString(v.id+1)).setAttribute("ui.class", "terminal");
		}
		for(SteinerGraphEdge edge : displayGraph.edges.values()){
			Integer id = edge.getID().get(0);
			graph.addEdge(id.toString(), Integer.toString(edge.getStart()+1), Integer.toString(edge.getEnd()+1));
			Edge e = graph.getEdge(id.toString());
			e.addAttribute("ui.label", edge.getWeight());
			if (exactEdges.contains(id)){
				e.setAttribute("ui.class", "exactEdge");
			}
			else if (approxEdges.contains(id)){
				e.setAttribute("ui.class", "approxEdge");
			}
		}
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		graph.display();
	}

}
