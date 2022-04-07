package com.github.coderodde.tsp.impl;

import com.github.coderodde.tsp.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class implements an {@code Iterable} returning all possible permutations 
 * of a list.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 14, 2016) :*
 */
final class TourIterable implements Iterable<List<Node>> {

    private final List<Node> allNodes = new ArrayList<>();

    TourIterable(List<Node> allElements) {
        this.allNodes.addAll(allElements);
    }

    @Override
    public Iterator<List<Node>> iterator() {
        return new PermutationIterator(allNodes);
    }

    private static final class PermutationIterator 
    implements Iterator<List<Node>> {

        private List<Node> nextPermutation;
        private final List<Node> allElements = new ArrayList<>();
        private int[] indices;

        PermutationIterator(List<Node> allElements) {
            if (allElements.isEmpty()) {
                nextPermutation = null;
                return;
            }

            this.allElements.addAll(allElements);
            this.indices = new int[allElements.size()];

            for (int i = 0; i < indices.length; ++i) {
                indices[i] = i;
            }

            nextPermutation = new ArrayList<>(this.allElements);
        }

        @Override
        public boolean hasNext() {
            return nextPermutation != null;
        }

        @Override
        public List<Node> next() {
            if (nextPermutation == null) {
                throw new NoSuchElementException("No permutations left.");
            }

            List<Node> ret = nextPermutation;
            generateNextPermutation();
            return ret;
        }

        private void generateNextPermutation() {
            int i = indices.length - 2;

            while (i >= 0 && indices[i] > indices[i + 1]) {
                --i;
            }

            if (i == -1) {
                // No more new permutations.
                nextPermutation = null;
                return;
            }

            int j = i + 1;
            int min = indices[j];
            int minIndex = j;

            while (j < indices.length) {
                if (indices[i] < indices[j] && indices[j] < min) {
                    min = indices[j];
                    minIndex = j;
                }

                ++j;
            }

            swap(indices, i, minIndex);

            ++i;
            j = indices.length - 1;

            while (i < j) {
                swap(indices, i++, j--);
            }

            loadPermutation();
        }

        private void loadPermutation() {
            List<Node> newPermutation = new ArrayList<>(indices.length);

            for (int i : indices) {
                newPermutation.add(allElements.get(i));
            }

            this.nextPermutation = newPermutation;
        }
    }

    private static void swap(int[] array, int a, int b) {
        int tmp = array[a];
        array[a] = array[b];
        array[b] = tmp;
    }
}
