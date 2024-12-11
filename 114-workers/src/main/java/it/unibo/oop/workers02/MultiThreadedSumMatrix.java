package it.unibo.oop.workers02;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * This is an implementation using streams.
 * 
 */
@SuppressWarnings("CPD-START")
public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int threads;

    /**
     * 
     * @param threads numebr of thread performing the sum.
     */
    public MultiThreadedSumMatrix(final int threads) {
        this.threads = threads;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startPosition;
        private final int elements;
        private double result;

        /**
         * Build a new worker.
         * 
         * @param matrix        the matrix to sum
         * @param startPosition the initial position for this worker
         * @param elements      the number of elements to sum up for this worker
         */
        Worker(final double[][] matrix, final int startPosition, final int elements) {
            super();
            this.matrix = Arrays.copyOf(matrix, matrix.length);
            this.startPosition = startPosition;
            this.elements = elements;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public void run() {
            System.out.println("Working from row " + startPosition + " to row " + (startPosition + elements - 1));

            this.result = IntStream.range(startPosition, Math.min(matrix.length, startPosition + elements))
                    .mapToDouble(i -> {
                        return Arrays.stream(matrix[i]).sum();
                    }).sum();
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public double getResult() {
            return this.result;
        }

    }

    @SuppressWarnings("PMD.AvoidPrintStackTrace")
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public double sum(final double[][] matrix) {
        final int rowsPerThread = matrix.length % this.threads + matrix.length / this.threads;

        return IntStream
                .iterate(0, start -> start + rowsPerThread)
                .limit(threads)
                .mapToObj(start -> new Worker(matrix, start, rowsPerThread))
                // Start threads
                .peek(Thread::start)
                // Join them
                .peek(MultiThreadedSumMatrix::joinUninterruptibly)
                // Get their result and sum
                .mapToDouble(Worker::getResult)
                .sum();
    }
}
