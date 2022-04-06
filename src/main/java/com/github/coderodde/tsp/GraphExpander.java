package com.github.coderodde.tsp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides the method for searching for all nodes reachable from a 
 * "seed" node.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 29, 2022)
 * @since 1.6 (Mar 29, 2022)
 */
public final class GraphExpander {

    public static List<Node> computeReachableNodesFrom(Node seedNode) {
        List<Node> reachableNodes = new ArrayList<>();
        Deque<Node> queue = new ArrayDeque<>();
        Set<Node> visitedSet = new HashSet<>();
        
        queue.addLast(seedNode);
        visitedSet.add(seedNode);
        
        while (!queue.isEmpty()) {
            Node currentNode = queue.removeFirst();
            visitedSet.add(currentNode);
            reachableNodes.add(currentNode);
            
            for (Node childNode : currentNode.getNeighbors()) {
                if (!visitedSet.contains(childNode)) {
                    visitedSet.add(childNode);
                    queue.addLast(childNode);
                }
            }
        }
        
        return reachableNodes;
    }
}
