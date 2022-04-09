package com.github.coderodde.tsp.impl;

import com.github.coderodde.tsp.AllPairsShortestPathData;
import com.github.coderodde.tsp.Node;
import com.github.coderodde.tsp.TSPSolver;
import com.github.coderodde.tsp.Utils;
import static com.github.coderodde.tsp.impl.GeneticTSPSolverV2.checkGraphSize;
import static com.github.coderodde.tsp.impl.GeneticTSPSolverV2.checkNumberOfGenerations;
import static com.github.coderodde.tsp.impl.GeneticTSPSolverV2.checkPopulationSize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * This class implements the parallel genetic algorithm for solving the 
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">
 * Travelling salesman problem</a>.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 8, 2022)
 * @since 1.6 (Apr 8, 2022)
 */
public final class ParallelGeneticTSPSolver implements TSPSolver {

    // The tour/individual:
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
    
    // This class implements the GA thread for solving the TSP:
    private final class TSPAlgorithmThread extends Thread {
        
        private final Random random;
        private final List<Node> reachableNodes;
        private final AllPairsShortestPathData data;
        private final int numberOfGenerations;
        private final int populationSize;
        private Tour result;
        
        TSPAlgorithmThread(Random random, 
                           List<Node> reachableNodes,
                           AllPairsShortestPathData data,
                           int numberOfGenerations,
                           int populationSize) {
            this.random = random;
            this.reachableNodes = reachableNodes;
            this.data = data;
            this.numberOfGenerations = numberOfGenerations;
            this.populationSize = populationSize;
        }
        
        @Override
        public void run() {
            List<Tour> population = 
                    computeInitialGeneration(reachableNodes);
            
            for (int generationNumber = 1;
                    generationNumber < numberOfGenerations;
                    generationNumber++) {
                
                List<Tour> nextPopulation = evolvePopulation(population);
            
                population = nextPopulation;
            }
            
            result = getMinimalTour(population);
        }
        
        Tour getResult() {
            return result;
        }
        
        Tour getMinimalTour(List<Tour> population) {
            Tour minimalTour = null;
            double minimalTourCost = Double.POSITIVE_INFINITY;
            
            for (Tour tour : population) {
                double tentativeTourCost = tour.cost;
                
                if (minimalTourCost > tentativeTourCost) {
                    minimalTourCost = tentativeTourCost;
                    minimalTour = tour;
                }
            }
            
            return minimalTour;
        }
    
        private List<Tour> 
            evolvePopulation(List<Tour> population) {

            List<Tour> nextPopulation = new ArrayList<>(population.size());

            while (nextPopulation.size() < population.size()) {
                Tour[] parents = getParents(population);
                nextPopulation.add(breedTour(parents));
            }

            return nextPopulation;
        }
            
        private Tour[] getParents(List<Tour> population) {
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
        
        private double getMaximumTourCost(List<Tour> population,
                                          AllPairsShortestPathData data) {
            double fittestTourCost = -1.0;

            for (Tour tour : population) {
                fittestTourCost = Math.max(fittestTourCost, tour.cost);
            }

            return fittestTourCost;
        }
            
        private Tour breedTour(Tour[] parents) {
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
        
        private List<Tour> 
                    computeInitialGeneration(List<Node> reachableNodes) {
            List<Tour> initialGeneration = 
                    new ArrayList<>(populationSize);

            for (int i = 0; i < populationSize; ++i) {
                List<Node> nodes = new ArrayList<>(reachableNodes);
                Collections.shuffle(nodes, random);
                Tour tour = new Tour(nodes, 0.0);
                initialGeneration.add(tour);
            }

            return initialGeneration;
        }
    }
    
    // The number of parents of each individual (tour). This is trisexual.
    private static final int NUMBER_OF_PARENTS = 3;
    
    private static final int MINIMUM_THREAD_COUNT = 2;
    
    private final Random random;
    private final int numberOfGenerations;
    private final int populationSize;
    private final int numberOfThreads;
    
    public ParallelGeneticTSPSolver(Random random,
                                    int numberOfGenerations,
                                    int populationSize,
                                    int numberOfThreads) {
        Objects.requireNonNull(random, "The input Random is null.");
        
        checkNumberOfGenerations(numberOfGenerations);
        checkPopulationSize(populationSize);
        checkNumberOfThreads(numberOfThreads);
        
        this.random = random;
        this.numberOfGenerations = numberOfGenerations;
        this.populationSize = populationSize;
        this.numberOfThreads = numberOfThreads;
    } 
    
    public ParallelGeneticTSPSolver(Random random,
                                    int numberOfGenerations,
                                    int populationSize) {
        this(random,
             numberOfGenerations,
             populationSize,
             Runtime.getRuntime().availableProcessors());
    }
    
    public ParallelGeneticTSPSolver(int numberOfGenerations,
                                    int populationSize) {
        this(new Random(),
             numberOfGenerations,
             populationSize,
             Runtime.getRuntime().availableProcessors());
    }
    
    @Override
    public Solution findTSPSolution(Node seedNode) {
        Objects.requireNonNull(seedNode, "The seed node is null.");
        
        List<Node> reachableNodes =
                GraphExpander.computeReachableNodesFrom(seedNode);
        
        checkGraphSize(reachableNodes.size());
        
        AllPairsShortestPathData allPairsData = 
                AllPairsShortestPathSolver.solve(reachableNodes);
        
        TSPAlgorithmThread[] threads = new TSPAlgorithmThread[numberOfThreads];
        
        for (int i = 0; i < numberOfThreads; ++i) {
            threads[i] = 
                    new TSPAlgorithmThread(
                            new Random(random.nextLong()), 
                            reachableNodes, 
                            allPairsData,
                            numberOfGenerations, 
                            populationSize);
        }
    
        for (TSPAlgorithmThread thread : threads) {
            thread.start();
        }
        
        for (TSPAlgorithmThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                // Should not get here.
            }
        }
        
        Tour bestTour = getBestTour(threads);
        return new Solution(bestTour.nodes, allPairsData);
    }
    
    private static Tour 
        getBestTour(TSPAlgorithmThread[] threads) {
        
        double minimumCost = Double.POSITIVE_INFINITY;
        Tour bestTour = null;
        
        for (TSPAlgorithmThread thread : threads) {
            Tour tour = thread.getResult();
            double tourCost = tour.cost;
            
            if (minimumCost > tourCost) {
                minimumCost = tourCost;
                bestTour = tour;
            }
        }
        
        return bestTour;
    }

    private static void checkNumberOfThreads(int threadCount) {
        if (threadCount < MINIMUM_THREAD_COUNT) {
            throw new IllegalArgumentException(
                    "The input thread count is too small (" + threadCount + 
                            "). Must be at least " + MINIMUM_THREAD_COUNT +
                            ".");
        }
    }
}
