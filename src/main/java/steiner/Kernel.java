package steiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parent class for reduction methods that uses star contractions
 */
public class Kernel {
	protected SteinerGraph steinerGraph;
	protected List<SteinerGraph> saves;
	private Map<Integer, List<Double>> ratios;
	protected StringBuilder contractionInfo;
	protected Kernel(SteinerGraph steinerGraph) {
	    contractionInfo = new StringBuilder();
		this.steinerGraph = new SteinerGraph(steinerGraph);
		saves = new ArrayList<>();
		saves.add(new SteinerGraph(steinerGraph));
		ratios = new HashMap<>();
		for(Integer i : steinerGraph.vertices.keySet()) {
			ratios.put(i, new ArrayList<>());
		}
	}

	/**
	 * Contracts the best star among all vertices.
	 * @return returns false if no star with finite ratio was contracted, otherwise true 
	 */
	protected boolean contractBestStar() {
		Integer id = null;
		Pair<Double, List<SteinerGraphEdge>> tmp;
		Pair<Double, List<SteinerGraphEdge>> min = new Pair<Double, List<SteinerGraphEdge>>(Double.MAX_VALUE, new ArrayList<>());
		for(Integer i : steinerGraph.vertices.keySet()) {
			tmp = bestStar(i);
			if (tmp.getFirst() < min.getFirst() || (tmp.getFirst().equals(min.getFirst()) && tmp.getSecond().size() > min.getSecond().size())) {
				min = tmp;
				id = i;
			}
		}
		if (id != null) {
		    contractionInfo.append("Contraction: #" + saves.size());
		    contractionInfo.append("\nStar edges: ");
		    for(SteinerGraphEdge e : min.getSecond()){
		        contractionInfo.append("{" + e.getStart() + "," + e.getEnd() + "} ");
            }
            contractionInfo.append("\nStar ratio: " + min.getFirst() + "\nNew Terminal: #" + steinerGraph.vertexCount + "\n\n");
			contractStar(min.getSecond());
			return true;
		}
		return false;
	}

	/**
	 * Contracts a star with given center and edges
	 * @param starEdges list of edges of the star
	 */
	private void contractStar(List<SteinerGraphEdge> starEdges) {
		steinerGraph.vertices.put(steinerGraph.vertexCount, new SteinerGraphVertex(steinerGraph.vertexCount));
		steinerGraph.setTerminal(steinerGraph.vertexCount);
		ratios.put(steinerGraph.vertexCount, new ArrayList<>());
		Set<Integer> starVertices = new HashSet<Integer>();
		for(SteinerGraphEdge starEdge : starEdges) {
			for(Integer edgeID : starEdge.getID()) {
				if (steinerGraph.vertices.containsKey(steinerGraph.IDToEdge.get(edgeID).getStart())) {
					starVertices.add(steinerGraph.IDToEdge.get(edgeID).getStart());
				}
				if (steinerGraph.vertices.containsKey(steinerGraph.IDToEdge.get(edgeID).getEnd())) {
					starVertices.add(steinerGraph.IDToEdge.get(edgeID).getEnd());
				}
			}
			steinerGraph.steinerTreeEdges.addAll(starEdge.getID());
		}
		Set<Integer> outsideNeighbours = new HashSet<Integer>();
		for(Integer starVertex : starVertices) {
			for(Integer neighbour : steinerGraph.vertices.get(starVertex).neighbours) {
				if(!starVertices.contains(neighbour)) {
					outsideNeighbours.add(neighbour);
					SortedPair p = new SortedPair(starVertex, neighbour);
					SteinerGraphEdge outEdge = steinerGraph.edges.get(p);
					steinerGraph.edges.remove(p);
					steinerGraph.addEdge(steinerGraph.vertexCount, neighbour, outEdge.getWeight(), outEdge.getID());
					for(Integer i : outEdge.getID()) {
						SteinerGraphEdge e = steinerGraph.IDToEdge.get(i);
						if (e.getStart().equals(starVertex)) e.setStart(steinerGraph.vertexCount);
						if (e.getEnd().equals(starVertex)) e.setEnd(steinerGraph.vertexCount);
					}
				}
			}
		}

		for(Integer starVertex : starVertices){
			steinerGraph.removeVertex(starVertex);
		}

		for(Integer v : outsideNeighbours) {
			computeRatio(v);
		}
		computeRatio(steinerGraph.vertexCount);
		steinerGraph.vertexCount++;
	}

	/**
	 * Finds the best ratio star center at a given index
	 * @param v star center
	 * @return a pair where the first element is ratio of the star,
	 * and the second element is the corresponding list of edges.
	 */
	private Pair<Double, List<SteinerGraphEdge>> bestStar(Integer v) {
		List<SteinerGraphEdge> terminalEdges = steinerGraph.getTerminalEdges(v);
		Double ratio = Double.POSITIVE_INFINITY;
		List<SteinerGraphEdge> star = new ArrayList<>();
		for(int i = 0; i < terminalEdges.size(); i++) {
			if(ratios.get(v).get(i) <= ratio) {
				ratio = ratios.get(v).get(i);
				star = terminalEdges.subList(0, i+1);
			}
		}
		return new Pair<>(ratio, star);
	}

	/**
	 * Computes the ratios of a star centered at a given index
	 * @param index center of star
	 */
	private void computeRatio(Integer index) {
		SteinerGraphVertex v = steinerGraph.vertices.get(index);
		if(!ratios.get(index).isEmpty()) ratios.get(index).clear();
		List<SteinerGraphEdge> terminalEdges = steinerGraph.getTerminalEdges(v.id);
		for(int i = 0; i < terminalEdges.size(); i++) {
			if (i == 0) ratios.get(index).add(terminalEdges.get(0).getWeight().doubleValue());
			else {
				ratios.get(index).add(ratios.get(index).get(ratios.get(index).size()-1) + terminalEdges.get(i).getWeight());
			}
		}
		int z = 0;
		if (v.isTerminal) z = 1;
		for(int i = 0; i < terminalEdges.size(); i++) {
			ratios.get(index).set(i, ratios.get(index).get(i)/(i+z));
		}
	}
	
	/**
	 * Compute star ratios for a set of vertices
	 * @param s Set of star centers
	 */
	protected void computeRatios(Set<Integer> s) {
		for(Integer v : s) {
			computeRatio(v);
		}
	}
	
	public List<SteinerGraph> getSnapshots() {
		return saves;
	}

	public String getContractionInfos(){
        return contractionInfo.toString();
    }
}
