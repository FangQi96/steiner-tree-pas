package steiner;

import java.util.*;

/**
 * A Graph data structure that can store properties of a Steiner Tree
 * @author Kemeny Tamas
 */
public class SteinerGraph {
	public String inputPath;
	public Map<Integer, SteinerGraphEdge> IDToEdge;
	public Map<Integer, SteinerGraphVertex> vertices;
	public Map<SortedPair, SteinerGraphEdge> edges;
	public Set<Integer> terminals;
	public Set<Integer> steinerTreeEdges;
	public Integer vertexCount;
	public int edgeCount;

	public SteinerGraph(String path) {
		inputPath = path;
		IDToEdge = new HashMap<>();
		edges = new HashMap<>();
		steinerTreeEdges = new HashSet<>();
		vertices = new HashMap<>();
        terminals = new HashSet<>();
        vertexCount = Integer.MIN_VALUE;
        edgeCount = 0;
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
	 * Defines a new edge in the graph. This method assume correct input (no loops, no multi-edges)
	 * and is used for the construction of the initial graph. The edge is assigned a unique identifier.
	 * @param v1 first endpoint
	 * @param v2 second endpoint
	 * @param weight weight of the edge
	 */
	public void defineEdge(Integer v1, Integer v2, Integer weight) {
		SteinerGraphEdge e = new SteinerGraphEdge(edgeCount, v1, v2, weight);
		if (!vertices.containsKey(v1)){
		    vertices.put(v1, new SteinerGraphVertex(v1));
		    if (v1 >= vertexCount){
                vertexCount = v1 + 1;
            }
        }
        if (!vertices.containsKey(v2)){
            vertices.put(v2, new SteinerGraphVertex(v2));
            if (v2 >= vertexCount){
                vertexCount = v2 + 1;
            }
        }
		vertices.get(v1).neighbours.add(v2);
		vertices.get(v2).neighbours.add(v1);
		edges.put(new SortedPair(v1, v2), e);
		IDToEdge.put(edgeCount, e);
		edgeCount++;
	}

	/**
	 * Adds an edge to the graph. This method is used for modifications of a graph already read from input.
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

	/**
	 * @return Sum of all weights of edges in the Steiner tree
	 */
	public int getSteinerTreeWeight() {
		return new ArrayList<>(steinerTreeEdges).stream().mapToInt(i->IDToEdge.get(i).getWeight()).sum();
	}


	public List<Integer> getSteinerTreeEdges(){
		List<Integer> ste = new ArrayList<Integer>(steinerTreeEdges);
		Collections.sort(ste);
		return ste;
	}
}
