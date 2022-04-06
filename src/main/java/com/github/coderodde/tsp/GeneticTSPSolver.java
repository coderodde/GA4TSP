package com.github.coderodde.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * This class implements the genetic TSP solver.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 29, 2022)
 * @since 1.6 (Mar 29, 2022)
 */
public final class GeneticTSPSolver {
    
    public static final class Solution {
        public List<Node> tour;
        public AllPairsShortestPathData data;
        
        Solution(List<Node> tour, AllPairsShortestPathData data) {
            this.tour = tour;
            this.data = data;
        }
    }

    public static Solution findTSPSolution(Node seedNode, 
                                           int generations,
                                           int populationSize) {
        Objects.requireNonNull(seedNode, "The seed node is null.");
        checkNumberOfGenerations(generations);
        checkPopulationSize(populationSize);
        
        Random random = new Random();
        
        List<Node> reachableNodes =
                GraphExpander.computeReachableNodesFrom(seedNode);
        
        AllPairsShortestPathData allPairsData = 
                AllPairsShortestPathSolver.solve(reachableNodes);
        
        List<List<Node>> population = 
                computeInitialGeneration(reachableNodes,
                                         populationSize, 
                                         random);
        for (int generationNumber = 1;
                generationNumber < generations; 
                generationNumber++) {
                
            List<List<Node>> nextPopulation = 
                    evolvePopulation(population, random);
            
            population = nextPopulation;
        }
    
        List<Node> fittestTour = getFittestTour(population, allPairsData);
        return new Solution(fittestTour, allPairsData);
    }
    
    private static List<Node> getFittestTour(List<List<Node>> population,
                                             AllPairsShortestPathData data) {
        List<Node> fittestTour = null;
        double fittestTourCost = Double.MAX_VALUE;
        
        for (List<Node> tour : population) {
            double tentativeCost = Utils.getTourCost(tour, data);
            
            if (fittestTourCost > tentativeCost) {
                fittestTourCost = tentativeCost;
                fittestTour = tour;
            }
        }
        
        return fittestTour;
    }
    
    private static List<Node> breedIndividual(List<List<Node>> population,
                                              Random random) {
        int[] indices = new int[population.get(0).size()];
        
        for (int i = 0; i < indices.length; ++i) {
            indices[i] = i;
        }
        
        shuffle(indices, random);
    }
    
    private static void shuffle(int[] arr, Random random) {
        for (int i = arr.length - 1; i > 0; --i) {
            int j = random.nextInt(i + 1);
            swap(arr, i, j);
        }
    }
    
    private static void swap(int[] arr, int index1, int index2) {
        int tmp = arr[index1];
        arr[index1] = arr[index2];
        arr[index2] = tmp;
    }
    
    private static List<List<Node>> 
        evolvePopulation(List<List<Node>> population,
                         Random random) {
            
        List<List<Node>> nextPopulation = new ArrayList<>(population.size());
        
        while (nextPopulation.size() < population.size()) {
            nextPopulation.add(breedIndividual(population, random));
        }
        
        return nextPopulation;
    }
    
    private static List<List<Node>> 
                computeInitialGeneration(List<Node> reachableNodes,
                                         int populationSize, 
                                         Random random) {
        List<List<Node>> initialGeneration = new ArrayList<>(populationSize);
        
        for (int i = 0; i < populationSize; ++i) {
            List<Node> tour = new ArrayList<>(reachableNodes);
            Collections.shuffle(tour, random);
            initialGeneration.add(tour);
        }
        
        return initialGeneration;
    }
                
    private static void checkNumberOfGenerations(int numberOfGenerations) {
        if (numberOfGenerations < 1) {
            throw new IllegalArgumentException(
                    "Number of generations is too small: " 
                            + numberOfGenerations 
                            + ". Must be at least 1.");
        }
    }
               
    private static void checkPopulationSize(int populationSize) {
        if (populationSize < 3) {
            throw new IllegalArgumentException(
                    "Population size is too small: " 
                            + populationSize 
                            + ". Must be at least 3.");
        }
    }
}