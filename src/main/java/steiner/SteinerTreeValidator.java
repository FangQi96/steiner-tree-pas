package steiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SteinerTreeValidator {

	/**
	 * Checks Steiner tree validity, prints error if it isn't valid, otherwise doesn't print anything
	 * @return validity of Steiner tree
	 */
	public boolean validate(SteinerGraph steinerGraph) {
		Stack<Integer> stack = new Stack<Integer>();
		Map<Integer, Boolean> visited = new HashMap<Integer, Boolean>();
		Map<Integer, Integer> parent = new HashMap<Integer, Integer>();
		for(Integer v : steinerGraph.vertices.keySet()) {
			visited.put(v, false);
		}
		for(Integer i : steinerGraph.terminals) {
			stack.push(i);
			parent.put(i, -1);
			break;
		}
		while(!stack.isEmpty()) {
			Integer v = stack.pop();
			visited.put(v, true);
			for(Integer w : steinerGraph.vertices.get(v).neighbours) {
				if(visited.get(w)) {
					if (!parent.get(v).equals(w)) {
						List<Integer> path1 = new ArrayList<Integer>();
						Integer i = v;
						while(i!=-1) {
							path1.add(0, i);
							i = parent.get(i);
						}
						List<Integer> path2 = new ArrayList<Integer>();
						Integer j = w;
						while(j!=-1) {
							path2.add(0, j);
							j = parent.get(j);
						}
						path1.add(w);
						System.out.println(steinerGraph.inputPath + " Error - Steiner tree has a cycle:");
						System.out.println(path1);
						System.out.println(path2);
						return false;
					}
				}
				else if (!stack.contains(w)) {
					stack.push(w);
					parent.put(w, v);
				}

			}
		}
		for(Integer i : steinerGraph.vertices.keySet()) {
			if (!visited.get(i)) {
				System.out.println(steinerGraph.inputPath + " Error - Steiner tree is disconnected: steiner.SteinerGraphVertex " + i + " was not visited");
				return false;
			}
		}
		for(Integer terminal : steinerGraph.terminals) {
			if(!visited.get(terminal)) {
				System.out.println(steinerGraph.inputPath + " Error - Steiner tree does not cover terminal " + terminal);
				return false;
			}
		}
		return true;
	}
}
