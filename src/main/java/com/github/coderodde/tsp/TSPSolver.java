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
public interface TSPSolver {
    
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
    
    public Solution findTSPSolution(Node seedNode);
}
