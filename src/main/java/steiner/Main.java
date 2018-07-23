package steiner;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

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
		String params = "-ui output -in src/instances/instance-test2.gr -output -long";
		String[] paramlist = params.split(" ");

		String inputPath = null;
		Boolean out = false;
		Boolean displayInput = false;
		Boolean displayOutput = false;
		Boolean silent = false;
		Integer tCutOff = null;
		Integer sCutOff = null;
		Integer accuracy = null;
		Boolean continuous = false;
		Boolean longOut = false;

		for(int i = 0; i < paramlist.length; i++){
			try {
				switch (paramlist[i]) {
					case ("-d"):
					case ("-display"):
                    case ("-ui"):
                    case ("-gui"):
						i++;
						if (i < paramlist.length) {
							switch (paramlist[i]) {
								case ("i"):
                                case ("in"):
								case ("input"):
									displayInput = true;
									break;
								case ("o"):
                                case ("out"):
								case ("output"):
									displayOutput = true;
									break;
								default:
									i--;
							}
						}
						break;
					case ("-i"):
					case ("-in"):
					case ("-input"):
						i++;
						if (i < paramlist.length) {
							if (paramlist[i].substring(paramlist[i].lastIndexOf('.')).equals(".gr")) {
								inputPath = paramlist[i];
							} else i--;
						}
						break;
					case ("-o"):
					case ("-out"):
					case ("-output"):
                        out = true;
						break;
					case ("-silent"):
						silent = true;
						break;
                    case ("-l"):
                    case ("-long"):
                        longOut = true;
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

		if (inputPath == null){
			System.out.println("No input file specified");
			System.exit(0);
		}
		int iplen = inputPath.length()+20;
		if (!silent){
			for(int i = 0; i < iplen; i++){
				System.out.print("=");
			}
			System.out.println();
			System.out.println("===== Opening " + inputPath + " =====");
			for(int i = 0; i < iplen; i++){
				System.out.print("=");
			}
			System.out.println();
		}
		SteinerGraph inputGraph = new ReadInput(inputPath).getSteinerGraph();
		if (!silent) {
            System.out.println("Number of Terminals: " + inputGraph.terminals.size());
            if (longOut) System.out.println("Terminals: " + inputGraph.terminals);
            System.out.println("Number of Steiner Vertices: " + (inputGraph.vertices.size() - inputGraph.terminals.size()));
            if (longOut) {
                List<Integer> svs = new ArrayList<>();
                for (SteinerGraphVertex v : inputGraph.vertices.values()) {
                    if (!v.isTerminal) svs.add(v.id);
                }
                System.out.println("Steiner vertices: " + svs);
            }
            System.out.println("Number of Edges: " + inputGraph.edges.keySet().size());
            if (longOut) {
                System.out.print("Edges: ");
                for (SteinerGraphEdge e : inputGraph.edges.values()){
                    System.out.print(e + " ");
                }
                System.out.println();
            }
            System.out.println();
		}
		if (displayInput) display(inputGraph, new ArrayList<>(), new ArrayList<>());
		else {
			if (!silent){
				for(int i = 0; i < iplen; i++){
					System.out.print("=");
				}
				System.out.println();
				if ((iplen-34/2) > 0) {
					for (int i = 0; i < (iplen - 34) / 2; i++) {
						System.out.print("=");
					}
				}
				System.out.print(" Running pre-processing algorithm ");
				if ((iplen-34/2) > 0) {
					for (int i = 0; i < (iplen - 34) / 2; i++) {
						System.out.print("=");
					}
				}
				System.out.println();
				for(int i = 0; i < iplen; i++){
					System.out.print("=");
				}
				System.out.println();
			}
			SteinerGraph processedGraph = new SteinerGraph(inputGraph);
			processedGraph.preProcess();
			Integer preProcessSteinerDifference = inputGraph.vertices.size()-processedGraph.vertices.size();

			if (!silent) {
				System.out.println("Pre-processing removed " + preProcessSteinerDifference + " Steiner vertices\n");
				for(int i = 0; i < iplen; i++){
					System.out.print("=");
				}
				System.out.println();
				if ((iplen-34/2) > 0) {
					for (int i = 0; i < (iplen - 22) / 2; i++) {
						System.out.print("=");
					}
				}
				System.out.print(" Running Lossy Kernel ");
				if ((iplen-34/2) > 0) {
					for (int i = 0; i < (iplen - 22) / 2; i++) {
						System.out.print("=");
					}
				}
				System.out.println();
				for(int i = 0; i < iplen; i++){
					System.out.print("=");
				}
				System.out.println();
			}

			FastKernel kernel = new FastKernel(processedGraph);
			kernel.contract();
			List<SteinerGraph> saves = kernel.getSnapshots();
			if (!silent){
				System.out.println("Kernel finished after " + (saves.size() - 1) + " contractions\n");
				if (longOut){
				    System.out.println(kernel.getContractionInfos());
                }
			}

			Integer selectedIndex = Integer.MAX_VALUE;
			Set<Integer> selectedIndices = new HashSet<>();
			int accuracyRange = 10;
			if (!continuous) {

				Map<Integer, Integer> tCutoffToSave = new HashMap<>();
				Map<Integer, Integer> sCutoffToSave = new HashMap<>();
				String[] line = new String[0];
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				while ((selectedIndex == Integer.MAX_VALUE) && !continuous) {
					if ((accuracy == null) && (sCutOff == null) && (tCutOff == null)) {

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
							System.out.println("Terminal cut-off points: " + tCutOffList);
							System.out.println("Steiner vertex cut-off points: " + sCutOffList);
						}
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
			if (!silent && (selectedIndex != saves.size()-1)) {
                for (int i = 0; i < iplen; i++) {
                    System.out.print("=");
                }
                System.out.println();
                if ((iplen - 34 / 2) > 0) {
                    for (int i = 0; i < (iplen - 24) / 2; i++) {
                        System.out.print("=");
                    }
                }
                System.out.print(" Running Dreyfus-Wagner ");
                if ((iplen - 34 / 2) > 0) {
                    for (int i = 0; i < (iplen - 24) / 2; i++) {
                        System.out.print("=");
                    }
                }
                System.out.println();
                for (int i = 0; i < iplen; i++) {
                    System.out.print("=");
                }
                System.out.println();
            }

			if (continuous) {
				for (int i = 0; i < saves.size(); i++) {
					selectedIndices.add(i);
				}
			}
			else selectedIndices.add(selectedIndex);


			for(Integer index : selectedIndices) {
			    if (continuous) System.out.println((index+1) + ":");
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

				List<Integer> treeEdges = new ArrayList<>();
				treeEdges.addAll(approxTree);
				treeEdges.addAll(exactTree);
				if (!silent) {
					System.out.println("Steiner tree weight: w1+w2=" + (approxPart + exactPart));
					System.out.print("Steiner tree edges: ");
					for(Integer i : treeEdges){
					    System.out.print(inputGraph.IDToEdge.get(i) + " ");
                    }
                    System.out.println();

					System.out.println("Approximated part weight: w1=" + approxPart);
					System.out.print("Approximated part edges: ");
                    for(Integer i : approxTree){
                        System.out.print(inputGraph.IDToEdge.get(i) + " ");
                    }
                    if (approxTree.isEmpty()) System.out.print("{}");
                    System.out.println();

					System.out.println("Exact part weight: w2=" + exactPart);
					System.out.print("Exact part edges: ");
                    for(Integer i : exactTree){
                        System.out.print(inputGraph.IDToEdge.get(i) + " ");
                    }
                    if (exactTree.isEmpty()) System.out.print("{}");
                    System.out.println("\n");
				}

				if (out){
				    try {
				        String pre = inputPath.substring(0, inputPath.lastIndexOf('.'));
                        String outPath = pre + "-tree.gr";
                        BufferedWriter writer = new BufferedWriter(new FileWriter(outPath));
                        String[] inputFileAsStrings = readFile(inputPath).split("\n");
                        StringBuilder inputFileAsString = new StringBuilder();
                        for (int i = 0; i < inputFileAsStrings.length; i++){
                            if (inputFileAsStrings[i].split(" ")[0].equals("Nodes")){
                                inputFileAsString.append("Nodes " + (treeEdges.size()+1) + "\n");
                                continue;
                            }
                            else if (inputFileAsStrings[i].split(" ")[0].equals("Edges")){
                                inputFileAsString.append("Edges " + treeEdges.size() + "\n");
                                continue;
                            }
                            if (inputFileAsStrings[i].split(" ")[0].equals("E")){
                                if (treeEdges.contains(i-3)){
                                    inputFileAsString.append(inputFileAsStrings[i] + "\n");
                                }
                            }
                            else {
                                inputFileAsString.append(inputFileAsStrings[i] + "\n");
                            }
                        }
                        writer.write(inputFileAsString.toString());
                        writer.close();
                    }
                    catch (IOException e){
				        e.printStackTrace();
                    }
                }
                if (!continuous && displayOutput) display(inputGraph, exactTree, approxTree);
			}
		}
	}

	private static void display(SteinerGraph displayGraph, List<Integer> exactEdges, List<Integer> approxEdges){
		String styleSheet = readFile("src/styleSheet.css");
		Graph graph = new SingleGraph("Steiner Tree Problem");
		graph.setStrict(false);
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.quality");

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
				if (!displayGraph.vertices.get(edge.getStart()).isTerminal){
					graph.getNode(Integer.toString(edge.getStart()+1))
							.setAttribute("ui.class", "steiner");
				}
				if (!displayGraph.vertices.get(edge.getEnd()).isTerminal){
					graph.getNode(Integer.toString(edge.getEnd()+1))
							.setAttribute("ui.class", "steiner");
				}
			}
			else if (approxEdges.contains(id)){
				e.setAttribute("ui.class", "approxEdge");
				if (!displayGraph.vertices.get(edge.getStart()).isTerminal){
					graph.getNode(Integer.toString(edge.getStart()+1))
							.setAttribute("ui.class", "steiner");
				}
				if (!displayGraph.vertices.get(edge.getEnd()).isTerminal){
					graph.getNode(Integer.toString(edge.getEnd()+1))
							.setAttribute("ui.class", "steiner");
				}
			}
		}
		graph.display();
	}

}
