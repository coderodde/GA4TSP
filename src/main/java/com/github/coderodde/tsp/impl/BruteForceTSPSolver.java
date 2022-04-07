package com.github.coderodde.tsp.impl;

import com.github.coderodde.tsp.AllPairsShortestPathData;
import com.github.coderodde.tsp.Node;
import com.github.coderodde.tsp.Utils;
import java.util.List;
import java.util.Objects;
import com.github.coderodde.tsp.TSPSolver;

/**
 * This class provides a method for solving the Travelling salesman problem via
 * exhaustive, brute-force search. It generates all possible graph tours and 
 * returns the shortest one. The running time is of order {@code O(n! * n)}.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 7, 2022)
 * @since 1.6 (Apr 7, 2022)
 */
public final class BruteForceTSPSolver implements TSPSolver {
    
    private static final int MINIMUM_GRAPH_SIZE = 2;
    
    @Override
    public Solution findTSPSolution(Node seedNode) {
        
        Objects.requireNonNull(seedNode, "The seed node is null.");
        
        List<Node> reachableNodes =
                GraphExpander.computeReachableNodesFrom(seedNode);
        
        checkGraphSize(reachableNodes.size());
        
        AllPairsShortestPathData allPairsData = 
                AllPairsShortestPathSolver.solve(reachableNodes);
        
        TourIterable tourIterable = new TourIterable(reachableNodes);
        
        List<Node> bestTour = null;
        double bestTourCost = Double.POSITIVE_INFINITY;
        
        for (List<Node> tour : tourIterable) {
            double tentativeTourCost = Utils.getTourCost(tour, allPairsData);
            
            if (bestTourCost > tentativeTourCost) {
                bestTourCost = tentativeTourCost;
                bestTour = tour;
            }
        }
        
        return new Solution(bestTour, allPairsData);
    }
    
    private static void checkGraphSize(int numberOfNodes) {
        if (numberOfNodes < 2) {
            throw new IllegalArgumentException(
                    "Too little graph nodes: " 
                            + numberOfNodes 
                            + ". Must be at least " 
                            + MINIMUM_GRAPH_SIZE
                            + ".");
        }
    }
}
