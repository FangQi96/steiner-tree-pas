package steiner;

/**
 * A pair of sorted integers 
 * @author Kemeny Tamas
 */
public class SortedPair {
	private Integer first;
    private Integer second;
    
    public SortedPair(Integer first, Integer second) {
    	if(first > second) {
    		this.first = second;
    		this.second = first;
    	}
    	else {
	        this.first = first;
	        this.second = second;
    	}
	}
    
    public SortedPair(SortedPair p) {
    	this(p.getFirst(), p.getSecond());
    }

    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
        if (other instanceof SortedPair) {
        	SortedPair otherPair = (SortedPair) other;
            return
                    ((  this.first == otherPair.first ||
                            ( this.first != null && otherPair.first != null &&
                                    this.first.equals(otherPair.first))) &&
                            (  this.second.equals(otherPair.second) ||
                                    ( this.second != null && otherPair.second != null &&
                                            this.second.equals(otherPair.second))) );
        }

        return false;
    }

    public String toString()
    {
        return "[" + first + " "  + second + "]";
    }

    public Integer getFirst() {
        return first;
    }

    public Integer getSecond() {
        return second;
    }

}