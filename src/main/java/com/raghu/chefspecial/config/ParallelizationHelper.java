package com.raghu.chefspecial.config;

public class ParallelizationHelper {
    
    public static <T> ParallelizationHelperStream<T> stream(final Iterable<T> input) {
        return new ParallelizationHelperStream<T>(input);
    }
    
}
