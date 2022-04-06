package com.github.coderodde.tsp;

import com.github.coderodde.tsp.AbstractGeneticTSPSolver.Solution;
import com.github.coderodde.tsp.impl.GeneticTSPSolverV1;
import java.util.List;

public final class Demo {

    private static final int NUMBER_OF_GENERATIONS = 10;
    private static final int POPULATION_SIZE = 8;
    
    public static void main(String[] args) {
        Node n1 = new Node("1");
        Node n2 = new Node("2");
        Node n3 = new Node("3");
        Node n4 = new Node("4");
        Node n5 = new Node("5");
        Node n6 = new Node("6");
        
        /*
        Graph:
        
        n1    n4
          \  /  \
           n3    n6
          /  \  /
        n2    n5
        
        */
        
        n1.addNeighbor(n3, 1.0);
        n2.addNeighbor(n3, 2.0);
        n3.addNeighbor(n4, 3.0);
        n3.addNeighbor(n5, 4.0);
        n6.addNeighbor(n4, 1.0);
        n6.addNeighbor(n5, 5.0);
        
        AbstractGeneticTSPSolver solver = new GeneticTSPSolverV1();
        
        long startTime = System.currentTimeMillis();
        
        Solution solution = 
                solver.findTSPSolution(
                        n6, 
                        NUMBER_OF_GENERATIONS, 
                        POPULATION_SIZE);
        
        long endTime = System.currentTimeMillis();
        
        AllPairsShortestPathData data = solution.getData();
        
        System.out.println("Tour:");
        
        List<Node> tour = Utils.getExactTour(solution.getTour(), data);
        
        for (Node node : tour) {
            System.out.println(node);
        }
        
        System.out.println(
                "\nCost: " + Utils.getTourCost(solution.getTour(), data));
    }
}