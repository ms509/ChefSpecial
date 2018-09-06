package com.raghu.chefspecial.config;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ParallelizationHelperStream<T> {

    private static final int PARALLELIZATION_THRESHHOLD = 50;
    private static final ForkJoinPool threadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2);

    private Spliterator<T> spliterator;
    protected ExecutionMode executionMode;

    protected ParallelizationHelperStream(final ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    /**
     * Creates a deferred execution stream from the specified iterable. The execution mode of the stream
     * is determined based on the size of the iterable.
     */
    public ParallelizationHelperStream(final Iterable<T> iterable) {
        this.spliterator = iterable.spliterator();

        if (spliterator.hasCharacteristics(Spliterator.SIZED) && spliterator.estimateSize() < PARALLELIZATION_THRESHHOLD) {
            this.executionMode = ExecutionMode.SEQUENTIAL;
        } else {
            this.executionMode = ExecutionMode.PARALLEL;
        }
    }

    /**
     * Filter this stream with the specified filter function
     */
    public ParallelizationHelperStream<T> filter(final Predicate<? super T> filterFunc) {
        return new FilteredStream<T>(this, this.executionMode, filterFunc);
    }

    /**
     * Map this stream with the specified map function
     */
    public <R> ParallelizationHelperStream<R> map(final Function<? super T, ? extends R> mapFunc) {
        return new MappedStream<T, R>(this, this.executionMode, mapFunc);
    }

    /**
     * Executes the operations of this stream with the appropriate execution mode.
     * Failed parallel execution falls back to sequential execution
     */
    public List<T> execute() {
        if (this.executionMode == ExecutionMode.SEQUENTIAL) {
            return this.executeSequential();
        }

        try {
            return this.executeParallel();
        } catch (ExecutionException | InterruptedException e) {
            return this.executeSequential();
        }
    }

    /**
     * Executes this stream sequentially
     */
    private List<T> executeSequential() {
        return this.stream(false).collect(Collectors.toList());
    }

    /**
     * Executes this stream in parallel
     */
    private List<T> executeParallel()
            throws ExecutionException, InterruptedException
    {
        return threadPool
            .submit(() -> this.stream(true).collect(Collectors.toList()))
            .get();
    }

    /**
     * Converts this stream into a Stream<T> with the specified parallel flag
     */
    protected Stream<T> stream(final boolean parallel) {
        return StreamSupport.stream(this.spliterator, parallel);
    }

    /**
     * Whether a stream should be executed sequentially or in parallel
     */
    private static enum ExecutionMode {
        SEQUENTIAL,
        PARALLEL;
    }

    /**
     * Encapsulates a stream that was just modified with filter()
     */
    private static class FilteredStream<T> extends ParallelizationHelperStream<T> {

        private ParallelizationHelperStream<T> parent;
        private Predicate<? super T> filterFunc;

        private FilteredStream(final ParallelizationHelperStream<T> parent, final ExecutionMode executionMode, final Predicate<? super T> filterFunc) {
            super(executionMode);

            this.parent = parent;
            this.filterFunc = filterFunc;
        }

        @Override
        protected Stream<T> stream(final boolean parallel) {
            return this.parent
                .stream(parallel)
                .filter(item -> this.filterFunc.test(item));
        }

    }

    /**
     * Encapsulates a stream that was just modified with map()
     */
    private static class MappedStream<T, R> extends ParallelizationHelperStream<R> {

        private ParallelizationHelperStream<T> parent;
        private Function<? super T, ? extends R> mapFunc;

        private MappedStream(final ParallelizationHelperStream<T> parent, final ExecutionMode executionMode, final Function<? super T, ? extends R> mapFunc) {
            super(executionMode);

            this.parent = parent;
            this.mapFunc = mapFunc;
        }

        @Override
        protected Stream<R> stream(final boolean parallel) {
            return this.parent
                .stream(parallel)
                .map(item -> this.mapFunc.apply(item));
        }

    }

}
