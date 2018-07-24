package steiner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Reduction method that completes metric closure at once
 */
public class SlowKernel extends Kernel {

	public SlowKernel(SteinerGraph steinerGraph) {
		super(steinerGraph);
	}

	/**
	 * Compute metric closure then contract
	 */
	public void contract() {
		computeClosure();
		while(contractBestStar()) {
			saves.add(new SteinerGraph(steinerGraph));
		}
    }

	/**
	 * Completes all missing edges where at least one endpoint is terminal
	 */
	public void computeClosure() {
		for(Integer source : steinerGraph.terminals) {
			Map<Integer, Integer> dist = new HashMap<>();
			Map<Integer, List<Integer>> pathID = new HashMap<>();
			Queue<Integer> queue = new PriorityQueue<Integer>(new Comparator<Integer>() {
				public int compare(Integer i, Integer j) {
					if (dist.get(i)-dist.get(j)!=0) {
						return dist.get(i)-dist.get(j);
					}
					return i-j;
				}
			});
			for(Integer i : steinerGraph.vertices.keySet()) {
				dist.put(i, Integer.MAX_VALUE);
				pathID.put(i, new ArrayList<Integer>());
			}
			dist.put(source, 0);
			queue.addAll(steinerGraph.vertices.keySet());
			Integer u;
			while((u = queue.poll())!=null) {
				for(Integer v : steinerGraph.vertices.get(u).neighbours) {
					SteinerGraphEdge newEdge = steinerGraph.edges.get(new SortedPair(u, v));
					int newDist = dist.get(u) + newEdge.getWeight();
					if (newDist < dist.get(v)) {
						dist.put(v, newDist);
						List<Integer> newID = new ArrayList<Integer>(pathID.get(u));
						newID.addAll(newEdge.getID());
						pathID.put(v, newID);
						queue.remove(v);
						queue.add(v);
					}
				}
			}
			for(Integer v : dist.keySet()) {
				if(dist.get(v) == Integer.MAX_VALUE) continue;
				steinerGraph.addEdge(source, v, dist.get(v), pathID.get(v));
			}
		}
		computeRatios(steinerGraph.vertices.keySet());
	}

}
