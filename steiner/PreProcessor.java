package steiner;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Preprocessing algorithm that removes Steiner Vertices of degree at most 2
 */
public class PreProcessor {

    private SteinerGraph graph;
    private List<Integer> degreeZeroSVs;
    private List<Integer> degreeOneSVs;
    private List<Integer> degreeTwoSVs;

    public PreProcessor(SteinerGraph graph){
        degreeZeroSVs = new ArrayList<>();
        degreeOneSVs = new ArrayList<>();
        degreeTwoSVs = new ArrayList<>();
        this.graph = graph;
    }
    /**
     * Performs pre-processing step
     */
    public void run() {
        removeIsolated();
        removeLeaves();
        undoSubdivisions();
    }

    /**
     * Vertices with degree 2 may be seen as a subdivision of a single edge
     */
    private void undoSubdivisions() {
        Stack<Integer> stack = new Stack<Integer>();
        stack.addAll(graph.vertices.keySet());
        while(!stack.isEmpty()) {
            Integer middle = stack.pop();
            if((graph.vertices.get(middle).neighbours.size()!=2)||graph.vertices.get(middle).isTerminal) continue;
            Integer neighbourOne = graph.vertices.get(new ArrayList<>(graph.vertices.get(middle).neighbours).get(0)).id;
            Integer neighbourTwo = graph.vertices.get(new ArrayList<>(graph.vertices.get(middle).neighbours).get(1)).id;
            SteinerGraphEdge edgeOne = graph.edges.get(new SortedPair(middle, neighbourOne));
            SteinerGraphEdge edgeTwo = graph.edges.get(new SortedPair(middle, neighbourTwo));
            List<Integer> tempID = new ArrayList<>();
            tempID.addAll(edgeOne.getID());
            tempID.addAll(edgeTwo.getID());
            graph.addEdge(neighbourOne, neighbourTwo, edgeOne.getWeight() + edgeTwo.getWeight(), tempID);
            graph.removeVertex(middle);
            degreeTwoSVs.add(middle);
        }
    }

    /**
     * Removes all Steiner vertices that are isolated
     */
    private void removeIsolated() {
        List<Integer> steinerIsolated = new ArrayList<>();
        for(SteinerGraphVertex v : graph.vertices.values()) {
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
            graph.removeVertex(i);
        }
        degreeZeroSVs = steinerIsolated;
    }

    /**
     * Removes all Steiner vertices that are leaves
     */
    private void removeLeaves() {
        Stack<Integer> stack = new Stack<Integer>();
        stack.addAll(graph.vertices.keySet());
        while(!stack.isEmpty()) {
            Integer leaf = stack.pop();
            if((graph.vertices.get(leaf).neighbours.size()!=1)||graph.vertices.get(leaf).isTerminal) continue;
            Integer neighbour = graph.vertices.get(new ArrayList<>(graph.vertices.get(leaf).neighbours).get(0)).id;
            graph.removeEdge(leaf, neighbour);
            graph.vertices.remove(leaf);
            degreeOneSVs.add(leaf);
            stack.remove(neighbour);
            stack.add(neighbour);
        }
    }

    public SteinerGraph getGraph() {
        return graph;
    }

    public List<Integer> getDegreeZeroSVs() {
        return degreeZeroSVs;
    }

    public List<Integer> getDegreeOneSVs() {
        return degreeOneSVs;
    }

    public List<Integer> getDegreeTwoSVs() {
        return degreeTwoSVs;
    }

}
