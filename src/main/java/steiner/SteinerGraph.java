package steiner;

import java.util.*;

/**
 * A steinerGraph data structure capable of working with Steiner trees
 * @author Kemeny Tamas
 */
public class SteinerGraph {
	public String inputPath;
	public Map<Integer, SteinerGraphEdge> IDToEdge;
	public Map<Integer, SteinerGraphVertex> vertices;
	public Map<SortedPair, SteinerGraphEdge> edges;
	public Set<Integer> terminals;
	public Set<Integer> steinerTreeEdges;
	public int edgeCount = 0;
	public int vertexCount = 0;

	public SteinerGraph(int _n, String path) {
		inputPath = path;
		vertexCount = _n;

		IDToEdge = new HashMap<>();
		edges = new HashMap<>();
		steinerTreeEdges = new HashSet<>();
		vertices = new HashMap<>();
		for(int i = 0; i < vertexCount; i++) {
			terminals = new HashSet<>();
			vertices.put(i, new SteinerGraphVertex(i));
		}
	}

	/**
	 * Constructs the SteinerGraph as a deep copy of its parent
	 * @param parent parent to be deep copied
	 */
	public SteinerGraph(SteinerGraph parent){
		edgeCount = parent.edgeCount;
		vertexCount = parent.vertexCount;
		inputPath = parent.inputPath;

		terminals = new HashSet<>();
		terminals.addAll(parent.terminals);

		steinerTreeEdges = new HashSet<>();
		steinerTreeEdges.addAll(parent.steinerTreeEdges);

		IDToEdge = new HashMap<>();
		for(Map.Entry<Integer, SteinerGraphEdge> entry : parent.IDToEdge.entrySet()) {
			IDToEdge.put(entry.getKey(), new SteinerGraphEdge(entry.getValue()));
		}

		vertices = new HashMap<>();
		for(Map.Entry<Integer, SteinerGraphVertex> entry : parent.vertices.entrySet()) {
			vertices.put(entry.getKey(), new SteinerGraphVertex(entry.getValue()));
		}

		edges = new HashMap<>();
		for(Map.Entry<SortedPair, SteinerGraphEdge> entry : parent.edges.entrySet()) {
			edges.put(new SortedPair(entry.getKey()), new SteinerGraphEdge(entry.getValue()));
		}
	}

	/**
	 * Defines a new edge in the steinerGraph. This method assume correct input (no loops, no multi-edges)
	 * and is used for the construction of the initial steinerGraph. The edge is assigned a unique identifier.
	 * @param v1 first endpoint
	 * @param v2 second endpoint
	 * @param weight weight of the edge
	 */
	public void defineEdge(Integer v1, Integer v2, Integer weight) {
		SteinerGraphEdge e = new SteinerGraphEdge(edgeCount, v1, v2, weight);
		vertices.get(v1).neighbours.add(v2);
		vertices.get(v2).neighbours.add(v1);
		edges.put(new SortedPair(v1, v2), e);
		IDToEdge.put(edgeCount, e);
		edgeCount++;
	}

	/**
	 * Adds an edge to the steinerGraph. This method is used for modifications of a steinerGraph already read from input.
	 * If there is already an existing edge, the lighter one is kept. Loops are not added.
	 * The edge is assigned a new identifier.
	 * @param v1 first endpoint
	 * @param v2 second endpoint
	 * @param weight weight of the edge
	 * @param id edge identifier
	 */
	public void addEdge(Integer v1, Integer v2, Integer weight, List<Integer> id) {
		if (v1.equals(v2)) return;
		SortedPair p = new SortedPair(v1, v2);
		SteinerGraphEdge e = new SteinerGraphEdge(v1, v2, weight, id);
		if(!edges.containsKey(p)) {
			vertices.get(v1).neighbours.add(v2);
			vertices.get(v2).neighbours.add(v1);
			edges.put(p, e);
			edgeCount++;
		}
		else {
			SteinerGraphEdge existingPath = edges.get(p);
			if (weight < existingPath.getWeight()) {
				edges.get(p).setWeight(weight);
				edges.get(p).setID(id);
			}
		}
		if(vertices.get(v2).isTerminal) {
			vertices.get(v1).terminalneighbours.add(v2);
		}
		if(vertices.get(v1).isTerminal) {
			vertices.get(v2).terminalneighbours.add(v1);
		}
	}

	/**
	 * Removes an edge
	 * @param v1 first endpoint
	 * @param v2 second endpoint
	 */
	public void removeEdge(Integer v1, Integer v2) {
		edges.remove(new SortedPair(v1, v2));
		vertices.get(v1).neighbours.remove(v2);			
		vertices.get(v2).neighbours.remove(v1);
		vertices.get(v1).terminalneighbours.remove(v2);			
		vertices.get(v2).terminalneighbours.remove(v1);
	}

	/**
	 * Removes a vertex
	 * @param v vertex to be removed
	 */
	public void removeVertex(Integer v){
		while(!vertices.get(v).neighbours.isEmpty()){
			removeEdge(v, new ArrayList<>(vertices.get(v).neighbours).get(0));
		}
		vertices.remove(v);
		terminals.remove(v);
	}

	/**
	 * Sets a vertex to be terminal
	 * @param t vertex to be set as terminal
	 */
	public void setTerminal(int t) {
		terminals.add(t);
		vertices.get(t).isTerminal = true;
		for(int i : vertices.get(t).neighbours) {
			vertices.get(i).terminalneighbours.add(t);
		}
	}

	/**
	 * Sets a vertex to be non-terminal
	 * @param t vertex to be set as non-terminal
	 */
	public void unTerminal(int t) {
		if(vertices.get(t).isTerminal) {
			terminals.remove(t);
			vertices.get(t).isTerminal = false;
			for(int i : vertices.get(t).neighbours) {
				vertices.get(i).terminalneighbours.remove(t);
			}
		}
	}

	/**
	 * Finds edges connecting to terminals from a given vertex
	 * @param v common point of terminal edges
	 * @return a list of terminal edges
	 */
	public List<SteinerGraphEdge> getTerminalEdges(Integer v){
		List<SteinerGraphEdge> neighbours = new ArrayList<>();
		for(Integer i : vertices.get(v).terminalneighbours) {
			neighbours.add(edges.get(new SortedPair(v, i)));
		}
		neighbours.sort(new Comparator<SteinerGraphEdge>() {
			public int compare(SteinerGraphEdge e1, SteinerGraphEdge e2) {
				if (e1.getWeight()-e2.getWeight()!=0) {
					return e1.getWeight()-e2.getWeight();
				}
				return e1.getID().get(0)-e2.getID().get(0);
			}
		});
		return neighbours;
	}

	public String toString() {
		String s = "";
		for(SteinerGraphVertex v : vertices.values()) {
			s += v.vertexInfo();
			s += " Edges: [\n";
			for(Integer i : v.neighbours) {
				s += " w("+ v.id + "," + i + ")=" + edges.get(new SortedPair(i, v.id)).getWeight() + "\n";
			}
			s += " ]\n";
			//			s += "\n Best star centered at " + v.id + " : " + bestStar(v.id).getFirst() + " " + bestStar(v.id).getSecond() + "\n\n";
		}
		return s;
	}


	/**
	 * @return Sum of all weights of edges in the Steiner tree
	 */
	public int getSteinerTreeWeight() {
		return new ArrayList<>(steinerTreeEdges).stream().mapToInt(i->IDToEdge.get(i).getWeight()).sum();
	}

	/**
	 * Performs pre-processing step
	 */
	public void preProcess() {
		removeIsolated();
		removeLeaves();
		undoSubdivisions();
	}

	/**
	 * Vertices with degree 2 may be seen as a subdivision of a single edge
	 */
	public void undoSubdivisions() {
		Stack<Integer> stack = new Stack<Integer>();
		stack.addAll(vertices.keySet());
		while(!stack.isEmpty()) {
			Integer middle = stack.pop();
			if((vertices.get(middle).neighbours.size()!=2)||vertices.get(middle).isTerminal) continue;
			Integer neighbourOne = vertices.get(new ArrayList<>(vertices.get(middle).neighbours).get(0)).id;
			Integer neighbourTwo = vertices.get(new ArrayList<>(vertices.get(middle).neighbours).get(1)).id;
			SteinerGraphEdge edgeOne = edges.get(new SortedPair(middle, neighbourOne));
			SteinerGraphEdge edgeTwo = edges.get(new SortedPair(middle, neighbourTwo));
			List<Integer> tempID = new ArrayList<>();
			tempID.addAll(edgeOne.getID());
			tempID.addAll(edgeTwo.getID());
			addEdge(neighbourOne, neighbourTwo, edgeOne.getWeight() + edgeTwo.getWeight(), tempID);
			removeVertex(middle);
		}
	}

	/**
	 * Removes all Steiner vertices that are isolated
	 */
	public void removeIsolated() {
		List<Integer> steinerIsolated = new ArrayList<>();
		for(SteinerGraphVertex v : vertices.values()) {
			if (v.neighbours.size() == 0) {
				if (!v.isTerminal) {
					steinerIsolated.add(v.id);
				}
				else {
					System.out.println("Terminal number " + (v.id+1) + " is isolated");
					System.out.println("Problem is infeasible");
					System.exit(0);
				}
			}
		}
		for(Integer i : steinerIsolated) {
			removeVertex(i);
		}
	}

	/**
	 * Removes all Steiner vertices that are leaves
	 */
	public void removeLeaves() {
		Stack<Integer> stack = new Stack<Integer>();
		stack.addAll(vertices.keySet());
		while(!stack.isEmpty()) {
			Integer leaf = stack.pop();
			if((vertices.get(leaf).neighbours.size()!=1)||vertices.get(leaf).isTerminal) continue;
			Integer neighbour = vertices.get(new ArrayList<>(vertices.get(leaf).neighbours).get(0)).id;
			removeEdge(leaf, neighbour);
			vertices.remove(leaf);
			stack.remove(neighbour);
			stack.add(neighbour);
		}
	}

	public List<Integer> getSteinerTreeEdges(){
		List<Integer> ste = new ArrayList<Integer>(steinerTreeEdges);
		Collections.sort(ste);
		return ste;
	}
}
