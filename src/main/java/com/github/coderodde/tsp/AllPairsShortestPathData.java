package com.github.coderodde.tsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds the data for the all-pairs shortest paths of a graph.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.61 (Apr 3, 2022)
 * @since 1.61 (Apr 3, 2022)
 */
public final class AllPairsShortestPathData {

    private final Map<Node, Map<Node, Double>> costMatrix = new HashMap<>();
    private final Map<Node, Map<Node, Node>> parentMatrix = new HashMap<>();
    
    public AllPairsShortestPathData(Map<Node, Map<Node, Double>> costMatrix,
                                    Map<Node, Map<Node, Node>> parentMatrix) {
        this.costMatrix.putAll(costMatrix);
        this.parentMatrix.putAll(parentMatrix);
    }
    
    public double getShortestPathCost(Node tail, Node head) {
        return costMatrix.get(tail).get(head);
    }
    
    public List<Node> getPath(Node node1, Node node2) {
        List<Node> path = new ArrayList<>();
        
        if (!this.parentMatrix.containsKey(node1) ||
                !this.parentMatrix.get(node1).containsKey(node2)) {
            return null;
        }
        
        path.add(node1);
        
        while (node1 != node2) {
            node1 = this.parentMatrix.get(node1).get(node2);
            path.add(node1);
        }
        
        return path;
    }
}
