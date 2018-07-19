package steiner;

import java.util.*;

/**
 * A class representing a vertex of a steinerGraph
 * @author Kemeny Tamas
 */
public class SteinerGraphVertex {
	public Integer id;
	public Set<Integer> neighbours;
	public Set<Integer> terminalneighbours;
	public boolean isTerminal;
	
	public SteinerGraphVertex(int i) {
		id = i;
		neighbours = new HashSet<>();
		terminalneighbours = new HashSet<>();
		isTerminal = false;
	}
	
	public SteinerGraphVertex(SteinerGraphVertex parent) {
		id = parent.id;
		neighbours = new HashSet<Integer>();
		for(Integer i : parent.neighbours) {
			neighbours.add(i);
		}
		terminalneighbours = new HashSet<Integer>();
		for(Integer i : parent.terminalneighbours) {
			terminalneighbours.add(i);
		}
		isTerminal = parent.isTerminal;
	}

	public String vertexInfo() {
		String s = "----------";
		if (isTerminal) s += " TERMINAL " + id + " --------";
		else s += " steiner.SteinerGraphVertex " + id + " ----------";
		s += "\n Neighbours: " + neighbours;
		s += "\n Terminal neighbours: " + terminalneighbours + "\n";
		return s;
	}
}
