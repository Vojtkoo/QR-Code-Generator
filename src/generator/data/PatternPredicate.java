package generator.data;

@FunctionalInterface
public interface PatternPredicate {
    boolean get(int a, int b);
}
