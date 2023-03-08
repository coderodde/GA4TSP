package com.github.coderodde.tsp.impl;

import com.github.coderodde.tsp.AllPairsShortestPathData;
import com.github.coderodde.tsp.Node;
import com.github.coderodde.tsp.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import com.github.coderodde.tsp.TSPSolver;

/**
 * This class implements the genetic (unoptimized) TSP solver.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 29, 2022)
 * @since 1.6 (Mar 29, 2022)
 */
public final class GeneticTSPSolverV1 implements TSPSolver {
    
    // The number of parents of each individual (tour). This is trisexual.
    protected static final int NUMBER_OF_PARENTS = 3;
    
    // The minimum number of generations. If 1 is passed, only a randomly 
    // (initial) generation will be returned.
    private static final int MINIMUM_NUMBER_OF_GENERATIONS = 1;
    
    // The minimum population size.
    private static final int MINIMUM_POPULATION_SIZE = 5;
    
    // The minimum number of vertices in the input graph. We need this in order
    // to make sure that we have sufficient amount of individuals in each 
    // generation.
    private static final int MINIMUM_GRAPH_SIZE = 4;
    
    private static final int DEFAULT_NUMBER_OF_GENERATIONS = 10;
    private static final int DEFAULT_POPULATION_SIZE = 10;
    
    private final int numberOfGenerations;
    private final int populationSize;
    
    /**
     * Constructs a genetic solver with given parameters.
     * 
     * @param numberOfGenerations the number of generations.
     * @param populationSize      the population size at each generation.
     */
    public GeneticTSPSolverV1(int numberOfGenerations,
                              int populationSize) {
        
        checkNumberOfGenerations(numberOfGenerations);
        checkPopulationSize(populationSize);
        
        this.numberOfGenerations = numberOfGenerations;
        this.populationSize = populationSize;
    }
    
    public GeneticTSPSolverV1() {
        this(DEFAULT_NUMBER_OF_GENERATIONS,
             DEFAULT_POPULATION_SIZE);
    }
    
    /**
     * Returns an (approximate) solution to the TSP problem via genetic 
     * algorithm.
     * 
     * @param seedNode the seed node of the graph.
     * @return an approximate solution to the TSP problem instance.
     */
    public Solution findTSPSolution(Node seedNode) {
        Objects.requireNonNull(seedNode, "The seed node is null.");
        
        List<Node> reachableNodes =
                GraphExpander.computeReachableNodesFrom(seedNode);
        
        checkGraphSize(reachableNodes.size());
        
        Random random = new Random(1);
        AllPairsShortestPathData allPairsData = 
                AllPairsShortestPathSolver.solve(reachableNodes);
        
        List<List<Node>> population = 
                computeInitialGeneration(reachableNodes,
                                         populationSize, 
                                         random);
        for (int generationNumber = 1;
                generationNumber < numberOfGenerations; 
                generationNumber++) {
                
            List<List<Node>> nextPopulation = 
                    evolvePopulation(population, allPairsData, random);
            
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
    
    private static List<List<Node>> 
        evolvePopulation(List<List<Node>> population,
                         AllPairsShortestPathData data,
                         Random random) {
            
        List<List<Node>> nextPopulation = new ArrayList<>(population.size());
        
        while (nextPopulation.size() < population.size()) {
            List<Node>[] parents = getParents(population, data, random);
            nextPopulation.add(breedTour(parents, random));
        }
        
        return nextPopulation;
    }
        
    private static List<Node> breedTour(List<Node>[] parents, Random random) {
        
        int tourLength = parents[0].size();
        int totalGeneLength = tourLength / NUMBER_OF_PARENTS;
        int gene1Length = totalGeneLength;
        int gene2Length = totalGeneLength;
        int gene3Length = tourLength - gene1Length - gene2Length;
        
        List<Node> tour = new ArrayList<>(totalGeneLength);
        List<Node> genes1 = new ArrayList<>(gene1Length);
        List<Node> genes2 = new ArrayList<>(gene2Length);
        List<Node> genes3 = new ArrayList<>(gene3Length);
        Set<Node> nodeSet = new HashSet<>();
        
        for (int i = 0; i < gene1Length; ++i) {
            Node node = parents[0].get(i);
            nodeSet.add(node);
            genes1.add(node); 
        }
        
        int index = 0;
        
        while (genes2.size() < gene2Length) {
            Node node = parents[1].get(index++);
            
            if (!nodeSet.contains(node)) {
                nodeSet.add(node);
                genes2.add(node);
            }
        }
        
        index = 0;
        
        while (genes3.size() < gene3Length) {
            Node node = parents[2].get(index++);
            
            if (!nodeSet.contains(node)) {
                nodeSet.add(node);
                genes3.add(node);
            }
        }
        
        List<List<Node>> genes = new ArrayList<>(NUMBER_OF_PARENTS);
        
        genes.add(genes1);
        genes.add(genes2);
        genes.add(genes3);
        
        Collections.<List<Node>>shuffle(genes, random);
        
        tour.addAll(genes.get(0));
        tour.addAll(genes.get(1));
        tour.addAll(genes.get(2));
        
        return tour;
    }
        
    private static double getMaximumTourCost(List<List<Node>> population,
                                             AllPairsShortestPathData data) {
        double fittestTourCost = -1.0;
        
        for (List<Node> tour : population) {
            double tentativeTourCost = Utils.getTourCost(tour, data);
            
            if (fittestTourCost < tentativeTourCost) {
                fittestTourCost = tentativeTourCost;
            }
        }
        
        return fittestTourCost;
    }
    
    private static List<Node>[] getParents(List<List<Node>> population,
                                           AllPairsShortestPathData data,
                                           Random random) {
        ProbabilityDistribution<List<Node>> distribution = 
                new ProbabilityDistribution<>(random);
        
        double maximumTourCost = getMaximumTourCost(population, data);
        
        for (List<Node> tour : population) {
            double tourCost = Utils.getTourCost(tour, data);
            double tourWeight = 1.2 * maximumTourCost - tourCost;
            distribution.addElement(tour, tourWeight);
        }
        
        List<Node>[] parents = new List[NUMBER_OF_PARENTS];
        Set<List<Node>> parentFilter = new HashSet<>();
        int index = 0;
        
        while (parentFilter.size() < NUMBER_OF_PARENTS) {
            List<Node> parent = distribution.sampleElement();
            
            if (!parentFilter.contains(parent)) {
                parentFilter.add(parent);
                distribution.removeElement(parent);
                parents[index++] = parent;
            }
        }
        
        return parents;
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