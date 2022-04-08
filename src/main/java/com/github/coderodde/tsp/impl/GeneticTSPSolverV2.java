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
 * This class implements the genetic (optimized) TSP solver.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 8, 2022)
 * @since 1.6 (Apr 8, 2022)
 */
public final class GeneticTSPSolverV2 implements TSPSolver {
    
    private static final class Tour {
        final List<Node> nodes;
        final double cost;
        private final int hashCode;
        
        Tour(List<Node> nodes, double cost) {
            this.nodes = nodes;
            this.cost = cost;
            this.hashCode = nodes.hashCode();
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Tour)) {
                return false;
            }
            
            Tour other = (Tour) o;
            return nodes.equals(other.nodes);
        }
    }
    
    // The number of parents of each individual (tour). This is trisexual.
    private static final int NUMBER_OF_PARENTS = 3;
    
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
    public GeneticTSPSolverV2(int numberOfGenerations,
                              int populationSize) {
        
        checkNumberOfGenerations(numberOfGenerations);
        checkPopulationSize(populationSize);
        
        this.numberOfGenerations = numberOfGenerations;
        this.populationSize = populationSize;
    }
    
    public GeneticTSPSolverV2() {
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
    public TSPSolver.Solution findTSPSolution(Node seedNode) {
        Objects.requireNonNull(seedNode, "The seed node is null.");
        
        List<Node> reachableNodes =
                GraphExpander.computeReachableNodesFrom(seedNode);
        
        checkGraphSize(reachableNodes.size());
        
        Random random = new Random();
        AllPairsShortestPathData allPairsData = 
                AllPairsShortestPathSolver.solve(reachableNodes);
        
        List<Tour> population = 
                computeInitialGeneration(reachableNodes,
                                         populationSize, 
                                         random);
        for (int generationNumber = 1;
                generationNumber < numberOfGenerations; 
                generationNumber++) {
                
            List<Tour> nextPopulation = 
                    evolvePopulation(population, allPairsData, random);
            
            population = nextPopulation;
        }
    
        Tour fittestTour = getFittestTour(population, allPairsData);
        return new TSPSolver.Solution(fittestTour.nodes, allPairsData);
    }
    
    private static Tour getFittestTour(List<Tour> population,
                                       AllPairsShortestPathData data) {
        Tour fittestTour = null;
        double fittestTourCost = Double.MAX_VALUE;
        
        for (Tour tour : population) {
            double tourCost = tour.cost;
            
            if (fittestTourCost > tourCost) {
                fittestTourCost = tourCost;
                fittestTour = tour;
            }
        }
        
        return fittestTour;
    }
    
    private static List<Tour> 
        evolvePopulation(List<Tour> population,
                         AllPairsShortestPathData data,
                         Random random) {
            
        List<Tour> nextPopulation = new ArrayList<>(population.size());
        
        while (nextPopulation.size() < population.size()) {
            Tour[] parents = getParents(population, data, random);
            nextPopulation.add(breedTour(parents, random, data));
        }
        
        return nextPopulation;
    }
        
    private static Tour breedTour(Tour[] parents,
                                  Random random,
                                  AllPairsShortestPathData data) {
        int tourLength = parents[0].nodes.size();
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
            Node node = parents[0].nodes.get(i);
            nodeSet.add(node);
            genes1.add(node); 
        }
        
        int index = 0;
        
        while (genes2.size() < gene2Length) {
            Node node = parents[1].nodes.get(index++);
            
            if (!nodeSet.contains(node)) {
                nodeSet.add(node);
                genes2.add(node);
            }
        }
        
        index = 0;
        
        while (genes3.size() < gene3Length) {
            Node node = parents[2].nodes.get(index++);
            
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
        
        return new Tour(tour, Utils.getTourCost(tour, data));
    }
        
    private static double getMaximumTourCost(List<Tour> population,
                                             AllPairsShortestPathData data) {
        double fittestTourCost = -1.0;
        
        for (Tour tour : population) {
            fittestTourCost = Math.max(fittestTourCost, tour.cost);
        }
        
        return fittestTourCost;
    }
    
    private static Tour[] getParents(List<Tour> population,
                                     AllPairsShortestPathData data,
                                     Random random) {
        ProbabilityDistribution<Tour> distribution = 
                new ProbabilityDistribution<>(random);
        
        double maximumTourCost = getMaximumTourCost(population, data);
        
        for (Tour tour : population) {
            double tourWeight = 1.2 * maximumTourCost - tour.cost;
            distribution.addElement(tour, tourWeight);
        }
        
        Tour[] parents = new Tour[NUMBER_OF_PARENTS];
        Set<Tour> parentFilter = new HashSet<>();
        int index = 0;
        
        while (parentFilter.size() < NUMBER_OF_PARENTS) {
            Tour parent = distribution.sampleElement();
            
            if (!parentFilter.contains(parent)) {
                parentFilter.add(parent);
                distribution.removeElement(parent);
                parents[index++] = parent;
            }
        }
        
        return parents;
    }    
        
    private static List<Tour> 
                computeInitialGeneration(List<Node> reachableNodes,
                                         int populationSize, 
                                         Random random) {
        List<Tour> initialGeneration = new ArrayList<>(populationSize);
        
        for (int i = 0; i < populationSize; ++i) {
            List<Node> nodes = new ArrayList<>(reachableNodes);
            Collections.shuffle(nodes, random);
            Tour tour = new Tour(nodes, 0.0);
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