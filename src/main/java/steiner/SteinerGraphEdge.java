package steiner;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing an edge of a steinerGraph
 *
 * @author Kemeny Tamas
 */
public class SteinerGraphEdge {
    private Integer start;
    private Integer end;
    private List<Integer> id;
    private Integer weight;

    public SteinerGraphEdge(int i, int start, int end, int weight) {
        this.id = new ArrayList<>();
        this.id.add(i);
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public SteinerGraphEdge(int start, int end, int weight, List<Integer> i) {
        this.id = i;
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public SteinerGraphEdge(SteinerGraphEdge parent) {
        start = parent.start;
        end = parent.end;
        weight = parent.weight;
        id = new ArrayList<>();
        id.addAll(parent.id);
    }

    public String toString(){
        return "{" + start + "," + end + "}";
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(int s) {
        start = s;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(int e) {
        end = e;
    }

    public List<Integer> getID() {
        return id;
    }

    public void setID(List<Integer> i) {
        this.id = i;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(int w) {
        weight = w;
    }
}