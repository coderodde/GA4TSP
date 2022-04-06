package com.github.coderodde.tsp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides the method for computing the all-pairs shortest paths via
 * Floyd-Warshall algorithm.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 29, 2022)
 * @since 1.6 (Mar 29, 2022)
 */
public final class AllPairsShortestPathSolver {

    public static AllPairsShortestPathData solve(List<Node> graph) {
        Map<Node, Map<Node, Double>> costMatrix = new HashMap<>(graph.size());
        Map<Node, Map<Node, Node>> parentMatrix = new HashMap<>(graph.size());
        
        for (Node node : graph) {
            Map<Node, Double> costMatrixRow = new HashMap<>();
            Map<Node, Node> parentMatrixRow = new HashMap<>();
            
            for (Node node2 : graph) {
                costMatrixRow.put(node2, Double.POSITIVE_INFINITY);
                parentMatrixRow.put(node2, null);
            }
            
            costMatrix.put(node, costMatrixRow);
            parentMatrix.put(node, parentMatrixRow);
        }
        
        for (Node tailNode : graph) {
            for (Node headNode : tailNode.getNeighbors()) {
                costMatrix.get(tailNode)
                    .put(headNode, tailNode.getWeightTo(headNode));
                
                costMatrix.get(headNode)
                    .put(tailNode, tailNode.getWeightTo(headNode));
                
                parentMatrix.get(tailNode).put(headNode, headNode);
            }
        }
        
        for (Node node : graph) {
            costMatrix.get(node).put(node, 0.0);
            parentMatrix.get(node).put(node, node);
        }
        
        for (Node node1 : graph) {
            for (Node node2 : graph) {
                for (Node node3 : graph) {
                    double tentativeCost = costMatrix.get(node2).get(node1) +
                                           costMatrix.get(node1).get(node3);
                    
                    if (costMatrix.get(node2).get(node3) > tentativeCost) {
                        costMatrix.get(node2).put(node3, tentativeCost);
                        parentMatrix.get(node2)
                                    .put(node3, parentMatrix.get(node2)
                                                            .get(node1));
                    }
                }
            }
        }
        
        return new AllPairsShortestPathData(costMatrix, parentMatrix);
    }
}