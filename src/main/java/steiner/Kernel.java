package steiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			contractStar(id, min.getSecond());
			return true;
		}
		return false;
	}

	/**
	 * Contracts a star with given center and edges
	 * @param center center of star
	 * @param staredges list of edges of the star
	 */
	private void contractStar(Integer center, List<SteinerGraphEdge> staredges) {
		steinerGraph.vertices.put(steinerGraph.vertexCount, new SteinerGraphVertex(steinerGraph.vertexCount));
		steinerGraph.setTerminal(steinerGraph.vertexCount);
		ratios.put(steinerGraph.vertexCount, new ArrayList<>());
		Set<Integer> starvertices = new HashSet<Integer>();
		for(SteinerGraphEdge staredge : staredges) {
			for(Integer edgeID : staredge.getID()) {
				if (steinerGraph.vertices.containsKey(steinerGraph.IDToEdge.get(edgeID).getStart())) {
					starvertices.add(steinerGraph.IDToEdge.get(edgeID).getStart());
				}
				if (steinerGraph.vertices.containsKey(steinerGraph.IDToEdge.get(edgeID).getEnd())) {
					starvertices.add(steinerGraph.IDToEdge.get(edgeID).getEnd());
				}
			}
			steinerGraph.steinerTreeEdges.addAll(staredge.getID());
		}
		Set<Integer> outsideneighbours = new HashSet<Integer>();
		for(Integer starvertex : starvertices) {
			for(Integer neighbour : steinerGraph.vertices.get(starvertex).neighbours) {
				if(!starvertices.contains(neighbour)) {
					outsideneighbours.add(neighbour);
					SortedPair p = new SortedPair(starvertex, neighbour);
					SteinerGraphEdge outedge = steinerGraph.edges.get(p);
					steinerGraph.edges.remove(p);
					steinerGraph.addEdge(steinerGraph.vertexCount, neighbour, outedge.getWeight(), outedge.getID());
					for(Integer i : outedge.getID()) {
						SteinerGraphEdge e = steinerGraph.IDToEdge.get(i);
						if (e.getStart().equals(starvertex)) e.setStart(steinerGraph.vertexCount);
						if (e.getEnd().equals(starvertex)) e.setEnd(steinerGraph.vertexCount);
					}
				}
			}
		}

		for(Integer starvertex : starvertices){
			steinerGraph.removeVertex(starvertex);
		}

		for(Integer v : outsideneighbours) {
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
		List<SteinerGraphEdge> terminaledges = steinerGraph.getTerminalEdges(v);
		Double ratio = Double.POSITIVE_INFINITY;
		List<SteinerGraphEdge> star = new ArrayList<>();
		for(int i = 0; i < terminaledges.size(); i++) {
			if(ratios.get(v).get(i) <= ratio) {
				ratio = ratios.get(v).get(i);
				star = terminaledges.subList(0, i+1);
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
		List<SteinerGraphEdge> terminaledges = steinerGraph.getTerminalEdges(v.id);
		for(int i = 0; i < terminaledges.size(); i++) {
			if (i == 0) ratios.get(index).add(terminaledges.get(0).getWeight().doubleValue());
			else {
				ratios.get(index).add(ratios.get(index).get(ratios.get(index).size()-1) + terminaledges.get(i).getWeight());
			}
		}
		int z = 0;
		if (v.isTerminal) z = 1;
		for(int i = 0; i < terminaledges.size(); i++) {
			ratios.get(index).set(i, ratios.get(index).get(i)/(i+z));
		}
	}
	
	/**
	 * Compute ratios of all possible stars
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
