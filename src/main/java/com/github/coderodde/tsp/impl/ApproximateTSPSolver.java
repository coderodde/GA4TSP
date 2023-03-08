package com.github.coderodde.tsp.impl;

import com.github.coderodde.tsp.AllPairsShortestPathData;
import com.github.coderodde.tsp.Node;
import com.github.coderodde.tsp.TSPSolver;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This class implements a greedy approximate algorithm for solving TSP. First,
 * it selects a shortest edge in the graph and adds it to the tentative tour. 
 * Then, it repeatedly selects the closest node to some of the tour's end 
 * points, and continues so until the tour becomes complete.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 8, 2023)
 * @see 1.6 (Mar 8, 2023)
 */
public final class ApproximateTSPSolver implements TSPSolver {

    private static final class Pair<F, S> {
        F first;
        S second;
        
        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
    
    private static final int MINIMUM_GRAPH_SIZE = 3;
    
    @Override
    public Solution findTSPSolution(Node seedNode) {
        Objects.requireNonNull(seedNode, "The seed node is null.");
        
        List<Node> reachableNodes = 
                GraphExpander.computeReachableNodesFrom(seedNode);
        
        checkGraphSize(reachableNodes.size());
        
        AllPairsShortestPathData allPairsData = 
                AllPairsShortestPathSolver.solve(reachableNodes);
        
        Deque<Node> tentativeTour = new ArrayDeque<>();
        
        Pair<Node, Node> initialEdge = getInitialEdge(reachableNodes,
                                                      allPairsData);
        
        tentativeTour.addLast(initialEdge.first);
        tentativeTour.addLast(initialEdge.second);
        
        Set<Node> visitedSet = new HashSet<>();
        
        visitedSet.add(initialEdge.first);
        visitedSet.add(initialEdge.second);
        
        while (tentativeTour.size() < reachableNodes.size()) {
            Node head = tentativeTour.getFirst();
            Node tail = tentativeTour.getLast();
            
            Node nearestToHead = getNearestNeighborOf(head, 
                                                      visitedSet,
                                                      reachableNodes,
                                                      allPairsData);
            
            Node nearestToTail = getNearestNeighborOf(tail, 
                                                      visitedSet, 
                                                      reachableNodes,
                                                      allPairsData);
            
            if (allPairsData.getShortestPathCost(head, nearestToHead) <
                allPairsData.getShortestPathCost(tail, nearestToTail)) {
                tentativeTour.addFirst(nearestToHead);
                visitedSet.add(nearestToHead);
            } else {
                tentativeTour.addLast(nearestToTail);
                visitedSet.add(nearestToTail);
            }
        } 
        
        return new Solution(
                tentativeTourToResultTour(tentativeTour), 
                allPairsData);
    }
    
    private static Node 
        getNearestNeighborOf(
                Node node, 
                Set<Node> visitedSet,
                List<Node> graphNodes,
                AllPairsShortestPathData allPairsData) {
        
        double tentativeCost = Double.POSITIVE_INFINITY;
        Node nearestNeighbor = null;
        
        for (Node neighbor : graphNodes) {
            if (visitedSet.contains(neighbor)) {
                continue;
            }
            
            double cost = 
                    allPairsData.getShortestPathCost(
                            node,
                            neighbor);

            if (tentativeCost > cost) {
                tentativeCost = cost;
                nearestNeighbor = neighbor;
            }
        }
        
        return nearestNeighbor;
    }
    
    private static Pair<Node, Node> 
        getInitialEdge(List<Node> reachableNodes,
                       AllPairsShortestPathData allPairsData) {
        double tentativeCost = Double.POSITIVE_INFINITY;
        Node tentativeNode1 = null;
        Node tentativeNode2 = null;
        
        for (Node node : reachableNodes) {
            for (Node neighbor : node.getNeighbors()) {
                if (neighbor.equals(node)) {
                    continue;
                }
                
                double cost = 
                        allPairsData.getShortestPathCost(
                                node,
                                neighbor);
                
                if (tentativeCost > cost) {
                    tentativeCost = cost;
                    tentativeNode1 = node;
                    tentativeNode2 = neighbor;
                }
            }
        }
        
        return new Pair<>(tentativeNode1, tentativeNode2);
    }
    
    private static List<Node> tentativeTourToResultTour(Deque<Node> nodeDeque) {
        return new ArrayList<>(nodeDeque);
    }
    
    private static void checkGraphSize(int numberOfNodes) {
        if (numberOfNodes < MINIMUM_GRAPH_SIZE) {
            throw new IllegalArgumentException(
                    "Too little graph nodes: " 
                            + numberOfNodes 
                            + ". Must be at least " 
                            + MINIMUM_GRAPH_SIZE
                            + ".");
        }
    }
}
