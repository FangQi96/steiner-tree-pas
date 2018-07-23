package steiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class DreyfusWagner {
	
	private SteinerGraph optimalTree;
	private List<Integer> steinerEdges;
	private int optimalTreeWeight;
	
	/**
	 * Computes the exact Steiner tree using the Dreyfus-Wagner algorithm
	 * @return exact weight of optimum Steiner tree
	 */
	public void solve(SteinerGraph steinerGraph) {

		// Make all terminals into leaves
		Stack<Integer> terminalsToModify = new Stack<Integer>();
		terminalsToModify.addAll(steinerGraph.terminals);
		while(!terminalsToModify.isEmpty()) {
			Integer t = terminalsToModify.pop();
			if (steinerGraph.vertices.get(t).neighbours.size() == 1){
				Integer singleNeighbour = new ArrayList<>(steinerGraph.vertices.get(t).neighbours).get(0);
				if(!steinerGraph.vertices.get(singleNeighbour).isTerminal) continue;
				else {
					steinerGraph.unTerminal(singleNeighbour);
					steinerGraph.vertices.put(steinerGraph.vertexCount, new SteinerGraphVertex(steinerGraph.vertexCount));
					steinerGraph.setTerminal(steinerGraph.vertexCount);
					steinerGraph.addEdge(singleNeighbour, steinerGraph.vertexCount, 0, new ArrayList<>());
					steinerGraph.vertexCount++;
					terminalsToModify.remove(singleNeighbour);
					continue;
				}
			}
			steinerGraph.unTerminal(t);
			steinerGraph.vertices.put(steinerGraph.vertexCount, new SteinerGraphVertex(steinerGraph.vertexCount));
			steinerGraph.setTerminal(steinerGraph.vertexCount);
			steinerGraph.addEdge(t, steinerGraph.vertexCount, 0, new ArrayList<>());
			steinerGraph.vertexCount++;
		}
		// Floyd-Warshall algorithm
		Double[][] dist = new Double[steinerGraph.vertexCount][steinerGraph.vertexCount];
		Integer[][] next = new Integer[steinerGraph.vertexCount][steinerGraph.vertexCount];
		for(int i = 0; i < steinerGraph.vertexCount; i++) {
			for(int j = 0; j < steinerGraph.vertexCount; j++) {
				dist[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		for(SteinerGraphEdge e : steinerGraph.edges.values()) {
			dist[e.getStart()][e.getEnd()] = e.getWeight().doubleValue();
			dist[e.getEnd()][e.getStart()] = e.getWeight().doubleValue();
			next[e.getStart()][e.getEnd()] = e.getEnd();
			next[e.getEnd()][e.getStart()] = e.getStart();
		}
		for(Integer v : steinerGraph.vertices.keySet()) {
			dist[v][v] = 0d;
		}
		for(int k = 0; k < steinerGraph.vertexCount; k++) {
			for(int i = 0; i < steinerGraph.vertexCount; i++) {
				for(int j = 0; j < steinerGraph.vertexCount; j++) {
					if (dist[i][j] > dist[i][k] + dist[k][j]) {
						dist[i][j] = dist[i][k] + dist[k][j];
						next[i][j] = next[i][k];
					}
				}
			}
		}
		
		// Map Steiner vertices to columns
		Set<Integer> steinerVertices = new HashSet<Integer>(steinerGraph.vertices.keySet());
		steinerVertices.removeAll(steinerGraph.terminals);
		List<Integer> SV = new ArrayList<Integer>(steinerVertices);
		Map<Integer, Integer> SVToColumn = new HashMap<Integer, Integer>();
		Map<Integer, Integer> ColumnToSV = new HashMap<Integer, Integer>();
		for(int i = 0; i < SV.size(); i++) {
			SVToColumn.put(SV.get(i),i);
			ColumnToSV.put(i,SV.get(i));
		}
		// Map subsets to rows
		List<Set<Integer>> terminalPowerSet = powerSet(steinerGraph.terminals);
		terminalPowerSet.remove(new HashSet<>());
		Map<Set<Integer>, Integer> SubsetToRow = new HashMap<Set<Integer>, Integer>();
		Map<Integer, Set<Integer>> RowToSubset = new HashMap<Integer, Set<Integer>>();
		for(int i = 0; i < terminalPowerSet.size(); i++) {
			SubsetToRow.put(terminalPowerSet.get(i), i);
			RowToSubset.put(i, terminalPowerSet.get(i));
		}
		DreyWagCache[][] cache = new DreyWagCache[(int)(Math.pow(2, steinerGraph.terminals.size())-1)][steinerVertices.size()];
		for(int i = 0; i < cache.length;i++) {
			for(int j = 0; j < cache[0].length;j++) {
				cache[i][j] = new DreyWagCache(Integer.MAX_VALUE, i, j, null, null, null);
			}
		}
		
		// Singleton subset rows may be filled in trivially
		for(Integer i : steinerGraph.terminals) {
			Set<Integer> singletonSet = new HashSet<Integer>();
			singletonSet.add(i);
			for(Integer steinerVertex : steinerVertices) {
				cache[SubsetToRow.get(singletonSet)][SVToColumn.get(steinerVertex)].set((int) Math.round(dist[i][steinerVertex]),
						SubsetToRow.get(singletonSet), SVToColumn.get(steinerVertex), null, null, null);
			}
		}
		
		// Apply recurrence relation to the remaining rows
		for(int i = steinerGraph.terminals.size(); i < cache.length; i++) {
			Set<Integer> D = terminalPowerSet.get(i);
			List<Set<Integer>> Dprimes = powerSet(D);
			Dprimes.remove(Collections.EMPTY_SET);
			Dprimes.remove(D);
			for(Integer v : SV) {
				for(Integer u : SV) {
					for(Set<Integer> Dprime : Dprimes) {
						int first = cache[SubsetToRow.get(Dprime)][SVToColumn.get(u)].getCachedWeight();
						Set<Integer> DminusDprime = new HashSet<Integer>(D);
						DminusDprime.removeAll(Dprime);
						int second = cache[SubsetToRow.get(DminusDprime)][SVToColumn.get(u)].getCachedWeight();
						int distance = (int) Math.round(dist[v][u]);
						if (cache[SubsetToRow.get(D)][SVToColumn.get(v)].getCachedWeight() > first + second + distance) {
							cache[SubsetToRow.get(D)][SVToColumn.get(v)].set(first + second + distance, SubsetToRow.get(D),
								SVToColumn.get(v), SubsetToRow.get(Dprime), SubsetToRow.get(DminusDprime), SVToColumn.get(u));
						}
					}
				}
			}
		}

		// Find minimum in bottom row
		int exact = Integer.MAX_VALUE;
		int exactColumn = -1;
		for(int j = 0; j < cache[0].length;j++) {
			if (cache[cache.length-1][j].getCachedWeight() < exact) {
				exact = cache[cache.length-1][j].getCachedWeight();
				exactColumn = j;
			}
		}
		optimalTreeWeight = exact;
		Set<Integer> edgeIDs = new HashSet<Integer>();
		Stack<DreyWagCache> parents = new Stack<DreyWagCache>();
		parents.add(cache[cache.length-1][exactColumn]);
		while(!parents.isEmpty()) {
			DreyWagCache current = parents.pop();
			if (current.getParentColumn() == null) {
				edgeIDs.addAll(path(new ArrayList<>(RowToSubset.get(current.getRow())).get(0),
						ColumnToSV.get(current.getColumn()), steinerGraph, next));
			}
			else {
				edgeIDs.addAll(path(ColumnToSV.get(current.getColumn()), ColumnToSV.get(current.getParentColumn()), steinerGraph, next));
				parents.add(cache[current.getFirstParentRow()][current.getParentColumn()]);
				parents.add(cache[current.getSecondParentRow()][current.getParentColumn()]);				
			}
		}
		steinerEdges = new ArrayList<Integer>(edgeIDs);
		Collections.sort(steinerEdges);
	}
	
	private Set<Integer> path(Integer u, Integer v, SteinerGraph steinerGraph, Integer[][] next){
		if (next[u][v] == null) return new HashSet<>();
		List<Integer> p = new ArrayList<Integer>();
		p.add(u);
		while (!u.equals(v)) {
			u = next[u][v];
			p.add(u);
		}
		Set<Integer> ids = new HashSet<Integer>();
		for(int i = 0; i + 1 < p.size(); i++) {
			ids.addAll(steinerGraph.edges.get(new SortedPair(p.get(i), p.get(i+1))).getID());
		}
		return ids;
	}
	
	/**
	 * Computes a list of all subsets.
	 * @param set Input set
	 * @return A list of all subsets sorted by length, then lexicographically
	 */
	private List<Set<Integer>> powerSet(Set<Integer> set) {
	    List<Set<Integer>> powSet = new ArrayList<Set<Integer>>();
	    if (set.isEmpty()) {
	    	powSet.add(new HashSet<Integer>());
	        return powSet;
	    }
	    List<Integer> list = new ArrayList<Integer>(set);
	    Set<Integer> rest = new HashSet<Integer>(list.subList(1, list.size())); 
	    for (Set<Integer> s : powerSet(rest)) {
	        Set<Integer> newSet = new HashSet<Integer>();
	        newSet.add(list.get(0));
	        newSet.addAll(s);
	        powSet.add(newSet);
	        powSet.add(s);
	    }
	    powSet.sort(new Comparator<Set<Integer>>() {
			public int compare(Set<Integer> s1, Set<Integer> s2) {
				if(s1.size() != s2.size()) return s1.size()-s2.size();
				List<Integer> l1 = new ArrayList<Integer>(s1);
				List<Integer> l2 = new ArrayList<Integer>(s2);
				int i = 0;
				while(i<l1.size()) {
					if(l1.get(i).compareTo(l2.get(i)) != 0) {
						return l1.get(i).compareTo(l2.get(i));
					}
					i++;
				}
				return 0;
			}
		});
	    return powSet;
	}
	
	public int getWeight() {
		return optimalTreeWeight;
	}
	
	public List<Integer> getEdges() {
		return steinerEdges;
	}
}
