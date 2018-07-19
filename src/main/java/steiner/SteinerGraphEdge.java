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
        this.id = new ArrayList<Integer>();
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
        start = parent.start.intValue();
        end = parent.end.intValue();
        weight = parent.weight.intValue();
        id = new ArrayList<Integer>();
        for (Integer i : parent.id) {
            id.add(i.intValue());
        }
    }

    public int hashCode() {
        int hashOne = start != null ? start.hashCode() : 0;
        int hashTwo = end != null ? end.hashCode() : 0;
        int hashThree = id != null ? id.hashCode() : 0;
        int hashFour = weight != null ? weight.hashCode() : 0;

        return (hashOne + hashTwo) * hashThree
                + (hashOne + hashThree) * hashTwo
                + (hashTwo + hashThree) * hashOne
                % hashFour;
    }

    public boolean equals(Object other) {
        if (other instanceof SteinerGraphEdge) {
            SteinerGraphEdge otherEdge = (SteinerGraphEdge) other;
            return (this.id == otherEdge.id);
        }
        return false;
    }

    public String toString() {
        return id + "w(" + start + "," + end + ")=" + weight;
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