package steiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastKernel extends Kernel {
	private Map<Integer, DijkstraCache> dijkstraCache;
	public FastKernel(SteinerGraph steinerGraph) {
		super(steinerGraph);
		computeRatios(steinerGraph.vertices.keySet());
		dijkstraCache = new HashMap<>();
		for(Integer terminal : steinerGraph.terminals) {
			addTerminalToCache(terminal);
		}
	}
	/**
	 * Compute metric closure and contract incrementally
	 */
	public void contract() {
		int L = 1;
		computeClosure(L);
		while(steinerGraph.terminals.size()!=1 && L != Integer.MAX_VALUE-1) {

			while(!contractBestStar() && L != Integer.MAX_VALUE-1) {
				if (L == 1073741824) {
					L = Integer.MAX_VALUE-1;
				}
				else if (L != Integer.MAX_VALUE){
					L *= 2;
				}
				computeClosure(L);
			}
			addTerminalToCache(steinerGraph.vertexCount-1);
			saves.add(new SteinerGraph(steinerGraph));
		}
	}

	/**
	 * Completes all missing edges up to a threshold where at least one endpoint is terminal
	 * @param threshold Upper bound on edges to be added
	 */
	private void computeClosure(int threshold) {
		for(Integer source : steinerGraph.terminals) {
			DijkstraCache cache = dijkstraCache.get(source);
			Integer u;
			while((u = cache.queue.poll())!=null) {
				if (cache.dist.get(u)>=threshold) {
					cache.queue.add(u);
					break;
				}
				for(Integer v : steinerGraph.vertices.get(u).neighbours) {
					SteinerGraphEdge newEdge = steinerGraph.edges.get(new SortedPair(u, v));
					int newDist = cache.dist.get(u) + newEdge.getWeight();
					if (newDist < cache.dist.get(v)) {
						cache.dist.put(v, newDist);
						List<Integer> newID = new ArrayList<Integer>(cache.pathID.get(u));
						newID.addAll(newEdge.getID());
						cache.pathID.put(v, newID);
						cache.queue.remove(v);
						cache.queue.add(v);
					}
				}
			}
			for(Integer v : cache.dist.keySet()) {
				if (v == source) continue;
				if (cache.dist.get(v) < threshold) {
					steinerGraph.addEdge(source, v, cache.dist.get(v), cache.pathID.get(v));
				}
			}
		}
		computeRatios(steinerGraph.vertices.keySet());
	}
	private void addTerminalToCache(Integer terminal) {
		for(DijkstraCache cache : dijkstraCache.values()) {
			cache.dist.put(terminal, Integer.MAX_VALUE);
			cache.pathID.put(terminal, new ArrayList<Integer>());
		}
		dijkstraCache.put(terminal, new DijkstraCache(terminal));
		DijkstraCache newCache = dijkstraCache.get(terminal);
		for(Integer v : steinerGraph.vertices.keySet()) {
			newCache.dist.put(v, Integer.MAX_VALUE);
			newCache.pathID.put(v, new ArrayList<Integer>());
		}
		newCache.dist.put(terminal, 0);
		newCache.queue.addAll(steinerGraph.vertices.keySet());
	}
	
}
