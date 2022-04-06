package com.github.coderodde.tsp;

import com.github.coderodde.tsp.GeneticTSPSolver.Solution;
import java.util.List;

public final class Demo {

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
        
        Solution solution = GeneticTSPSolver.findTSPSolution(n3, 3);
        AllPairsShortestPathData data = solution.data;
        
        System.out.println("Tour:");
        
        List<Node> tour = Utils.getExactTour(solution.tour, data);
        
        for (Node node : tour) {
            System.out.println(node);
        }
        
        System.out.println("\nCost: " + Utils.getTourCost(solution.tour, data));
    }
}