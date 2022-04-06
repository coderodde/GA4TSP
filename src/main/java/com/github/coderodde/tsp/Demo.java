package com.github.coderodde.tsp;

import com.github.coderodde.tsp.AbstractGeneticTSPSolver.Solution;
import com.github.coderodde.tsp.impl.GeneticTSPSolverV1;
import java.util.List;

public final class Demo {

    private static final int NUMBER_OF_GENERATIONS = 20;
    private static final int POPULATION_SIZE = 10;
    
    public static void main(String[] args) {
        miniDemo();
    }
    
    private static void miniDemo() {
        Node n1 = new Node("1", 0.0, 10.0);
        Node n2 = new Node("2", 5.5, 8.5);
        Node n3 = new Node("3", 2.3, 6.7);
        Node n4 = new Node("4", 2.8, 5.0);
        Node n5 = new Node("5", 4.1, 7.2);
        Node n6 = new Node("6", 1.0, 5.8);
        
        /*
        Graph:
        
        n1    n4
          \  /  \
           n3    n6
          /  \  /
        n2    n5
        
        */
        
        n1.addNeighbor(n3);
        n2.addNeighbor(n3);
        n3.addNeighbor(n4);
        n3.addNeighbor(n5);
        n6.addNeighbor(n4);
        n6.addNeighbor(n5);
        
        AbstractGeneticTSPSolver solver = new GeneticTSPSolverV1();
        
        long startTime = System.currentTimeMillis();
        
        Solution solution = 
                solver.findTSPSolution(
                        n6, 
                        NUMBER_OF_GENERATIONS, 
                        POPULATION_SIZE);
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Duration: " + (endTime - startTime) + " ms.");
        
        AllPairsShortestPathData data = solution.getData();
        
        System.out.println("Tour:");
        
        List<Node> tour = Utils.getExactTour(solution.getTour(), data);
        
        for (Node node : tour) {
            System.out.println(node);
        }
        
        System.out.println(
                "\nCost: " + Utils.getTourCost(solution.getTour(), data));
        
        System.out.println("Data:");
        
        for (Node node : solution.getTour()) {
            System.out.println(
                    node.toString() 
                            + ", x = " 
                            + node.getX() 
                            + ", y = " 
                            + node.getY());
        }
    }
}