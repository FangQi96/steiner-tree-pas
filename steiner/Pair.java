package steiner;

/**
 * A pair data structure parameterized by two generic types.
 * @author Kemeny Tamas
 * @param <A> First type parameter
 * @param <B> Second type parameter
 */
public class Pair<A, B> {
    private A first;
    private B second;
    
    public Pair(A first, B second) {
        super();
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

}