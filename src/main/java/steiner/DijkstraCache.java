package steiner;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A Dijkstra run is frozen and its state is saved in an instance of steiner.DijkstraCache.
 * @author Kemeny Tamas
 */
public class DijkstraCache {
	public Integer id;
	public Map<Integer, Integer> dist;
	public Map<Integer, List<Integer>> pathID;
	public Queue<Integer> queue;
	public DijkstraCache(Integer i) {
		id = i;
		dist = new HashMap<>();
		pathID = new HashMap<>();
		queue = new PriorityQueue<Integer>(new Comparator<Integer>() {
			public int compare(Integer i, Integer j) {
				if (dist.get(i)-dist.get(j)!=0) {
					return dist.get(i)-dist.get(j);
				}
				return i-j;
			}
		});
	}
}
