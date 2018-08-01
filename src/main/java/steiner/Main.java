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
		Boolean slowKernel = false;

		for(int i = 0; i < args.length; i++){
			switch (args[i]) {
				case ("-d"):
				case ("-display"):
				case ("-ui"):
				case ("-gui"):
					i++;
					if (i < args.length) {
						switch (args[i]) {
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
								break;
						}
					}
					break;
				case ("-i"):
				case ("-in"):
				case ("-input"):
					i++;
					if (i < args.length) {
						if (args[i].substring(args[i].lastIndexOf('.')).equals(".gr")) {
							inputPath = args[i];
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
				case ("-slow"):
				case ("-slowkernel"):
					slowKernel = true;
					break;
				case ("-a"):
				case ("-accuracy"):
					i++;
					if (i < args.length) {
						try {
							accuracy = Integer.parseInt(args[i]);
						}
						catch (NumberFormatException e){
							try {
								Long.parseLong(args[i]);
								accuracy = 10;
							}
							catch (NumberFormatException nfe){
								System.out.println("Invalid argument \"" + args[i] + "\" to option \"" + args[i-1] + "\"");
								System.out.println("Accuracy should be an integer [0..10]");
							}
						}
					}
					break;
				case ("-s"):
				case ("-sv"):
				case ("-steiner"):
				case ("-steinervertex"):
					i++;
					if (i < args.length) {
						try {
							sCutOff = Integer.parseInt(args[i]);
						}
						catch (NumberFormatException e){
							try {
								Long.parseLong(args[i]);
								sCutOff = Integer.MAX_VALUE-1;
							}
							catch (NumberFormatException nfe){
								System.out.println("Invalid argument \"" + args[i] + "\" to option \"" + args[i-1] + "\"");
								System.out.println("Argument must be an integer");
							}
						}
					}
					break;
				case ("-t"):
				case ("-terminal"):
					i++;
					if (i < args.length) {
						try {
							tCutOff = Integer.parseInt(args[i]);
						}
						catch(NumberFormatException e){
							try {
								Long.parseLong(args[i]);
								tCutOff = Integer.MAX_VALUE-1;
							}
							catch (NumberFormatException nfe){
								System.out.println("Invalid argument \"" + args[i] + "\" to option \"" + args[i-1] + "\"");
								System.out.println("Argument must be an integer");
							}
						}
					}
					break;
				default:
					System.out.println("Ignoring unknown parameter \"" + args[i] + "\"");
					break;
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

			PreProcessor pp = new PreProcessor(new SteinerGraph(inputGraph));
			pp.run();
			SteinerGraph processedGraph = pp.getGraph();
			Integer preProcessSteinerDifference = inputGraph.vertices.size()-processedGraph.vertices.size();

			if (!silent) {
				System.out.println("Pre-processing removed " + preProcessSteinerDifference + " Steiner vertices");
				if (longOut){
				    if (!pp.getDegreeZeroSVs().isEmpty()) System.out.println("Degree 0: " + pp.getDegreeZeroSVs());
                    if (!pp.getDegreeOneSVs().isEmpty()) System.out.println("Degree 1: " + pp.getDegreeOneSVs());
                    if (!pp.getDegreeTwoSVs().isEmpty()) System.out.println("Degree 2: " + pp.getDegreeTwoSVs());
                }
                System.out.println();
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
			List<SteinerGraph> saves;
			String contractionInfo;
			if (slowKernel){
				SlowKernel kernel = new SlowKernel(processedGraph);
				kernel.contract();
				saves = kernel.getSnapshots();
				contractionInfo = kernel.getContractionInfos();
			}
			else {
				FastKernel kernel = new FastKernel(processedGraph);
				kernel.contract();
				saves = kernel.getSnapshots();
				contractionInfo = kernel.getContractionInfos();
			}

			if (!silent){
				System.out.println("Kernel finished after " + (saves.size() - 1) + " contractions\n");
				if (longOut){
				    System.out.println(contractionInfo);
                }
			}

			Integer selectedIndex = Integer.MAX_VALUE;
			int accuracyRange = 10;
			if (!continuous) {

				Map<Integer, Integer> tCutoffToSave = new HashMap<>();
				Map<Integer, Integer> sCutoffToSave = new HashMap<>();
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				while ((selectedIndex == Integer.MAX_VALUE) && !continuous) {
					for (int i = 0; i < saves.size(); i++) {
						SteinerGraph save = saves.get(i);
						tCutoffToSave.put(save.terminals.size(), i);
						sCutoffToSave.put(save.vertices.size() - save.terminals.size() + preProcessSteinerDifference, i);
					}
					if ((accuracy == null) && (sCutOff == null) && (tCutOff == null)) {
						if (!silent) {
							List<Integer> tcop = new ArrayList<>(tCutoffToSave.keySet());
							tcop.sort(Comparator.reverseOrder());
							System.out.println("Terminal cut-off points: " + tcop);
							List<Integer> scop = new ArrayList<>(sCutoffToSave.keySet());
							scop.sort(Comparator.reverseOrder());
							System.out.println("Steiner vertex cut-off points: " + scop);
						}
						String[] line = new String[0];
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
										try {
											accuracy = Integer.parseInt(line[i]);
										}
										catch (NumberFormatException e){
											try {
												Long.parseLong(line[i]);
												accuracy = 10;
											}
											catch (NumberFormatException nfe){
												System.out.println("Invalid argument \"" + line[i] + "\" to option \"" + line[i-1] + "\"");
												System.out.println("Accuracy should be an integer [0..10]");
											}
										}
									}
									break;
								case ("-s"):
								case ("-sv"):
								case ("-steiner"):
								case ("-steinervertex"):
									i++;
									if (i < line.length) {
										try {
											sCutOff = Integer.parseInt(line[i]);
										}
										catch (NumberFormatException e){
											try {
												Long.parseLong(line[i]);
												sCutOff = Integer.MAX_VALUE-1;
											}
											catch (NumberFormatException nfe){
												System.out.println("Invalid argument \"" + line[i] + "\" to option \"" + line[i-1] + "\"");
												System.out.println("Argument must be an integer1");
											}
										}
									}
									break;
								case ("-t"):
								case ("-terminal"):
									i++;
									if (i < line.length) {
										try {
											tCutOff = Integer.parseInt(line[i]);
										}
										catch(NumberFormatException e){
											try {
												Long.parseLong(line[i]);
												tCutOff = Integer.MAX_VALUE-1;
											}
											catch (NumberFormatException nfe){
												System.out.println("Invalid argument \"" + line[i] + "\" to option \"" + line[i-1] + "\"");
												System.out.println("Argument must be an integer2");
											}
										}
									}
									break;
								default:
									System.out.println("Ignoring unknown parameter \"" + line[i] + "\"");
									break;
							}
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
							Integer max = Integer.MIN_VALUE;
							Integer maxIndex = Integer.MIN_VALUE;
							for (Map.Entry<Integer, Integer> entry : tCutoffToSave.entrySet()) {
								if (entry.getKey() > max){
									max = entry.getKey();
									maxIndex = entry.getValue();
								}
								if (entry.getKey() > tCutOff) {
									if (entry.getKey() < closestTerminalCount) {
										closestTerminalCount = entry.getKey();
										closestSaveIndex = entry.getValue();
									}
								}
							}
							if (tCutOff >= max) selectedIndex = Math.min(maxIndex, selectedIndex);
							else selectedIndex = Math.min(closestSaveIndex, selectedIndex);
						}
					}
					if (sCutOff != null) {
						if (sCutoffToSave.keySet().contains(sCutOff)) {
							selectedIndex = Math.min(sCutoffToSave.get(sCutOff), selectedIndex);
						} else {
							Integer closestSaveIndex = null;
							Integer closestSteinerVertexCount = Integer.MAX_VALUE;
							Integer max = Integer.MIN_VALUE;
							Integer maxIndex = Integer.MIN_VALUE;
							for (Map.Entry<Integer, Integer> entry : sCutoffToSave.entrySet()) {
								if (entry.getKey() > max){
									max = entry.getKey();
									maxIndex = entry.getValue();
								}
								if ((entry.getKey() > sCutOff) && (entry.getKey() < closestSteinerVertexCount)) {
									closestSteinerVertexCount = entry.getKey();
									closestSaveIndex = entry.getValue();
								}
							}
							if (sCutOff >= max) selectedIndex = Math.min(maxIndex, selectedIndex);
							else selectedIndex = Math.min(closestSaveIndex, selectedIndex);
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
            List<Integer> selectedIndices = new ArrayList<>();
			if (continuous) {
				for (int i = saves.size()-1; i > -1 ; i--) {
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
					System.out.print("Approximated edges: ");
                    for(Integer i : approxTree){
                        System.out.print(inputGraph.IDToEdge.get(i) + " ");
                    }
                    if (approxTree.isEmpty()) System.out.print("{}");
                    System.out.println();

					System.out.println("Exact part weight: w2=" + exactPart);
					System.out.print("Exact edges: ");
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
		String styleSheet = "graph {\n" +
				"\t fill-color: white;\n" +
				"}\n" +
				"node {\n" +
				"     fill-color: black;\n" +
				"     size-mode: dyn-size;\n" +
				"     size: 5px;\n" +
				"\t stroke-mode: plain;\n" +
				"\t stroke-color: white;\n" +
				"\t stroke-width: 2px;\n" +
				"}\n" +
				"node.terminal {\n" +
				"\t fill-color: white;\n" +
				"     shape: box;\n" +
				"\t size: 10px, 10px;\n" +
				"\t stroke-mode: plain;\n" +
				"\t stroke-color: black;\n" +
				"\t stroke-width: 3px;\n" +
				"\t z-index: 5;\n" +
				"}\n" +
				"node.steiner {\n" +
				"\t shape: diamond;\n" +
				"\t size: 8px;\n" +
				"\t fill-color: blue;\n" +
				"\t z-index: 5;\n" +
				"}\n" +
				"edge {\n" +
				"\t text-background-mode: plain;\n" +
				"\t text-padding: 1px;\n" +
				"\t text-alignment: along;\n" +
				"\t fill-color: #AAA;\n" +
				"\t text-visibility-mode: under-zoom;\n" +
				"\t text-visibility: 1;\n" +
				"\t text-color: #999;\n" +
				"}\n" +
				"edge.approxEdge {\n" +
				"\t text-color: black;\n" +
				"\t shape: blob;\n" +
				"     fill-color: red;\n" +
				"     size: 5px;\n" +
				"\t text-style: bold;\n" +
				"\t z-index: 4;\n" +
				"}\n" +
				"edge.exactEdge {\n" +
				"\t text-color: black;\n" +
				"\t shape: blob;\n" +
				"     fill-color: darkgreen;\n" +
				"     size: 5px;\n" +
				"\t text-style: bold;\n" +
				"\t z-index: 4;\n" +
				"}\n" +
				" ";
		Graph graph = new SingleGraph("Steiner Tree Problem");
		graph.setStrict(false);
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.quality");

		for(SteinerGraphVertex v : displayGraph.vertices.values()){
			graph.addNode(Integer.toString(v.id));
			if (v.isTerminal) {
				graph.getNode(Integer.toString(v.id)).setAttribute("ui.class", "terminal");
			}
		}
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
