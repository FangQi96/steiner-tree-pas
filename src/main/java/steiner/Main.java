package steiner;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.view.Viewer;
import scala.xml.Null;


import javax.swing.*;

/**
 * Program entry point
 * @author Kemeny Tamas
 */
public class Main {

	private static String readFile(String path) {
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
		Boolean continuous = false;
		System.out.println("inputPath: " + inputPath + " outputPath: " + outputPath + " displayInput: " + displayInput +
		" displayOutput: " + displayOutput + " silent: " + silent + " tCutOff: " + tCutOff + " sCutOff: " + sCutOff +
		" accuracy: " + accuracy);
		for(int i = 0; i < paramlist.length; i++){
			try {
				switch (paramlist[i]) {
					case ("-d"):
					case ("-display"):
						i++;
						if (i < paramlist.length) {
							switch (paramlist[i]) {
								case ("i"):
								case ("input"):
									displayInput = true;
									break;
								case ("o"):
								case ("output"):
									displayOutput = true;
									break;
								default:
									i--;
							}
						}
						break;
					case ("-i"):
					case ("-input"):
						i++;
						if (i < paramlist.length) {
							if (paramlist[i].substring(paramlist[i].lastIndexOf('.')).equals(".gr")) {
								inputPath = paramlist[i];
							} else i--;
						}
						break;
					case ("-o"):
					case ("-output"):
						i++;
						if (i < paramlist.length) {
							outputPath = paramlist[i];
						}
						break;
					case ("-silent"):
						silent = true;
						break;
					case ("-c"):
					case ("-cont"):
					case ("-continuous"):
						continuous = true;
						break;
					case ("-a"):
					case ("-accuracy"):
						i++;
						if (i < paramlist.length) {
							accuracy = Integer.parseInt(paramlist[i]);
						}
						break;
					case ("-s"):
					case ("-sv"):
					case ("-steiner"):
					case ("-steinervertex"):
						i++;
						if (i < paramlist.length) {
							sCutOff = Integer.parseInt(paramlist[i]);
						}
						break;
					case ("-t"):
					case ("-terminal"):
						i++;
						if (i < paramlist.length) {
							tCutOff = Integer.parseInt(paramlist[i]);
						}
						break;
				}
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		System.out.println("inputPath: " + inputPath + " outputPath: " + outputPath +
				           " displayInput: " + displayInput + " displayOutput: " + displayOutput +
				           " silent: " + silent + " tCutOff: " + tCutOff +
				           " sCutOff: " + sCutOff + " accuracy: " + accuracy);

		if (inputPath == null){
			System.out.println("No input file specified");
			System.exit(0);
		}

		SteinerGraph inputGraph = new ReadInput(inputPath).getSteinerGraph();
		if (displayInput) display(inputGraph);
		else {
			SteinerGraph processedGraph = new SteinerGraph(inputGraph);
			processedGraph.preProcess();
			Integer preProcessSteinerDifference = inputGraph.vertices.size()-processedGraph.vertices.size();
			FastKernel kernel = new FastKernel(processedGraph);
			kernel.contract();
			List<SteinerGraph> saves = kernel.getSnapshots();
			Integer selectedIndex = Integer.MAX_VALUE;
			Set<Integer> selectedIndices = new HashSet<>();
			int accuracyRange = 10;
			if (!continuous) {
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
					System.out.println("Instance with " + inputGraph.terminals.size() + " Terminals and " +
							(inputGraph.vertices.size() - inputGraph.terminals.size()) + " Steiner vertices");
					System.out.println("Number of contractions: " + (saves.size() - 1));
					System.out.println("Terminal cut-off points: " + tCutOffList);
					System.out.println("Steiner vertex cut-off points: " + sCutOffList);
				}

				String[] line = new String[0];
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				while ((selectedIndex == Integer.MAX_VALUE) && !continuous) {
					if ((accuracy == null) && (sCutOff == null) && (tCutOff == null)) {
						while (line.length == 0) {
							System.out.println("[-t|-terminal cutOff] " +
									"[-s|-steinervertex cutOff] " +
									"[-a|-accuracy [0..10]] " +
									"[-c|-cont|-continuous]");
							try {
								line = br.readLine().split(" ");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						try {
							for (int i = 0; i < line.length; i++) {
								switch (line[i]) {
									case ("-c"):
									case ("-cont"):
									case ("-continuous"):
										continuous = true;
										break;
									case ("-a"):
									case ("-accuracy"):
										i++;
										if (i < line.length) {
											accuracy = Integer.parseInt(line[i]);
										}
										break;
									case ("-s"):
									case ("-sv"):
									case ("-steiner"):
									case ("-steinervertex"):
										i++;
										if (i < line.length) {
											sCutOff = Integer.parseInt(line[i]);
										}
										break;
									case ("-t"):
									case ("-terminal"):
										i++;
										if (i < line.length) {
											tCutOff = Integer.parseInt(line[i]);
										}
										break;
								}
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
							System.exit(-1);
						}
					}
					if (accuracy != null) {
						Integer accuracyIndex = 1 + ((saves.size() - 2) * (accuracyRange - accuracy) / accuracyRange);
						if (accuracy <= 0) {
							accuracyIndex = saves.size() - 1;
							accuracy = 0;
						} else if (accuracy >= accuracyRange) {
							accuracyIndex = 0;
							accuracy = accuracyRange;
						}
						selectedIndex = Math.min(accuracyIndex, selectedIndex);
					}
					if (tCutOff != null) {
						if (tCutoffToSave.keySet().contains(tCutOff)) {
							selectedIndex = Math.min(tCutoffToSave.get(tCutOff), selectedIndex);
						} else {
							Integer closestSaveIndex = null;
							Integer closestTerminalCount = Integer.MAX_VALUE;
							for (Map.Entry<Integer, Integer> entry : tCutoffToSave.entrySet()) {
								if ((entry.getKey() > tCutOff) && (entry.getKey() < closestTerminalCount)) {
									closestTerminalCount = entry.getKey();
									closestSaveIndex = entry.getValue();
								}
							}
							selectedIndex = Math.min(closestSaveIndex, selectedIndex);
						}
					}
					if (sCutOff != null) {
						if (sCutoffToSave.keySet().contains(sCutOff)) {
							selectedIndex = Math.min(sCutoffToSave.get(sCutOff), selectedIndex);
						} else {
							Integer closestSaveIndex = null;
							Integer closestSteinerVertexCount = Integer.MAX_VALUE;
							for (Map.Entry<Integer, Integer> entry : sCutoffToSave.entrySet()) {
								if ((entry.getKey() > sCutOff) && (entry.getKey() < closestSteinerVertexCount)) {
									closestSteinerVertexCount = entry.getKey();
									closestSaveIndex = entry.getValue();
								}
							}
							selectedIndex = Math.min(closestSaveIndex, selectedIndex);
						}
					}
				}
			}
			if (continuous) {
				for (int i = 0; i < saves.size(); i++) {
					selectedIndices.add(i);
				}
			}
			else selectedIndices.add(selectedIndex);


			for(Integer index : selectedIndices) {
				SteinerGraph selectedSteinerGraph = new SteinerGraph(saves.get(index));

				int exactPart, approxPart;
				List<Integer> exactTree = new ArrayList<>();
				List<Integer> approxTree = saves.get(index).getSteinerTreeEdges();

				if (index == 0) approxPart = 0;
				else approxPart = selectedSteinerGraph.getSteinerTreeWeight();

				if (index == saves.size() - 1) exactPart = 0;
				else {
					DreyfusWagner fpt = new DreyfusWagner();
					fpt.solve(selectedSteinerGraph);
					exactPart = fpt.getWeight();
					exactTree = fpt.getEdges();
				}

				System.out.print("Selected index: " + index
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
	}

	private static void display(SteinerGraph displayGraph){
		display(displayGraph, new ArrayList<>(), new ArrayList<>());
	}

	private static void display(SteinerGraph displayGraph, List<Integer> exactEdges, List<Integer> approxEdges){

		String styleSheet = readFile("src/styleSheet.css");

		Graph graph = new SingleGraph("Steiner Tree Problem");
		graph.setStrict(false);
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.antialias");

		for(SteinerGraphVertex v : displayGraph.vertices.values()){
			graph.addNode(Integer.toString(v.id+1));
			if (v.isTerminal) {
				graph.getNode(Integer.toString(v.id+1)).setAttribute("ui.class", "terminal");
			}
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

		graph.display();
	}

}
