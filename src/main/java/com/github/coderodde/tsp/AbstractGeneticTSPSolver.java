package com.github.coderodde.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This interface defines the API for genetic algorithms for solving the 
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Travelling salesman problem</a>.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 6, 2022)
 * @since 1.6 (Apr 6, 2022)
 */
public abstract class AbstractGeneticTSPSolver {
    
    // The minimum number of generations. If 1 is passed, only a randomly 
    // (initial) generation will be returned.
    private static final int MINIMUM_NUMBER_OF_GENERATIONS = 1;
    
    // The minimum population size.
    private static final int MINIMUM_POPULATION_SIZE = 5;
    
    // The minimum number of vertices in the input graph. We need this in order
    // to make sure that we have sufficient amount of individuals in each 
    // generation.
    private static final int MINIMUM_GRAPH_SIZE = 4;
    
    // The number of parents of each individual (tour). This is trisexual.
    protected static final int NUMBER_OF_PARENTS = 3;
    
    /**
     * This class holds the information describing the shortest tour in a TSP.
     */
    public static final class Solution {
        
        // The solution tour. Not necessarily optimal.
        private final List<Node> tour = new ArrayList<>();
        
        // The data structure for graph queries.
        private final AllPairsShortestPathData data;
        
        public Solution(List<Node> tour, AllPairsShortestPathData data) {
            this.tour.addAll(
                    Objects.requireNonNull(
                            tour, 
                            "The input tour is null."));
            
            this.data = Objects.requireNonNull(data, "The input data is null.");
        }
        
        public List<Node> getTour() {
            return Collections.<Node>unmodifiableList(tour);
        }
        
        public AllPairsShortestPathData getData() {
            return data;
        }
    }
    
    public abstract Solution findTSPSolution(Node seedNode, 
                                             int generations,
                                             int populations);
    
    protected static void checkNumberOfGenerations(int numberOfGenerations) {
        if (numberOfGenerations < MINIMUM_NUMBER_OF_GENERATIONS) {
            throw new IllegalArgumentException(
                    "Number of generations is too small: " 
                            + numberOfGenerations 
                            + ". Must be at least " 
                            + MINIMUM_NUMBER_OF_GENERATIONS 
                            + ".");
        }
    }
               
    protected static void checkPopulationSize(int populationSize) {
        if (populationSize < MINIMUM_POPULATION_SIZE) {
            throw new IllegalArgumentException(
                    "Population size is too small: " 
                            + populationSize 
                            + ". Must be at least " 
                            + MINIMUM_POPULATION_SIZE 
                            + ".");
        }
    }
    
    protected static void checkGraphSize(int graphSize) {
        if (graphSize < MINIMUM_GRAPH_SIZE) {
            throw new IllegalArgumentException(
                    "The graph size is " 
                            + graphSize
                            + ". Minimum allowed size is " 
                            + MINIMUM_GRAPH_SIZE
                            + "."
            );
        }
    }
}
