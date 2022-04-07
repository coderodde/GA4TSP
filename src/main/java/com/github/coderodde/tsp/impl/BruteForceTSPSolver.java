package com.github.coderodde.tsp.impl;

import com.github.coderodde.tsp.AbstractTSPSolver;
import com.github.coderodde.tsp.AllPairsShortestPathData;
import com.github.coderodde.tsp.Node;
import com.github.coderodde.tsp.Utils;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a method for solving the Travelling salesman problem via
 * exhaustive, brute-force search. It generates all possible graph tours and 
 * returns the shortest one. The running time is of order {@code O(n! * n)}.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 7, 2022)
 * @since 1.6 (Apr 7, 2022)
 */
public final class BruteForceTSPSolver extends AbstractTSPSolver {
    
    @Override
    public Solution findTSPSolution(Node seedNode, 
                                    int generations,
                                    int populationSize) {
        
        Objects.requireNonNull(seedNode, "The seed node is null.");
        
        checkNumberOfGenerations(generations);
        checkPopulationSize(populationSize);
        
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
}
