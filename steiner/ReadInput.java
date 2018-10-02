package steiner;

import java.io.*;
import java.util.*;

/**
 * Auxiliary class used to read file in the .gr format and output an instance of the SteinerGraph class
 * @author Kemeny Tamas
 */
public class ReadInput {
	
	public SteinerGraph steinerGraph;

	ReadInput(String path) {
		String line;
		String[] split;
		int n = 0, m = 0, edgeCount = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while((line = br.readLine()) != null) {
				split = line.split(" ");
				switch(split[0]) {
					case("Nodes"):
						n = Integer.parseInt(split[1]);
						steinerGraph = new SteinerGraph(path);
						break;
					case("Edges"):
						m = Integer.parseInt(split[1]);
						break;
					case("E"):
						steinerGraph.defineEdge(Integer.parseInt(split[1]),
								Integer.parseInt(split[2]),
								Integer.parseInt(split[3]));
						edgeCount++;
						break;
					case("T"):
						steinerGraph.setTerminal(Integer.parseInt(split[1]));
						break;
					default:
						break;
				}
			}   
			br.close();
			if(m != edgeCount) {
				System.out.println("Input error: wrong number of edges");
				System.exit(0);
			}
			if(n != steinerGraph.vertices.keySet().size()) {
				System.out.println("Input error: wrong number of vertices");
				System.exit(0);
			}
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: '" + path + "'");
			System.exit(0);
		}
		catch(IOException e) {
			System.out.println("Failed to read file: '" + path + "'");
			System.exit(0);
		}
	}

	public SteinerGraph getSteinerGraph() {
		return steinerGraph;
	}
}
