package com.github.coderodde.tsp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class defines the graph node type for the traveling salesman problem.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 29, 2022)
 * @since 1.6 (Mar 29, 2022)
 */
public final class Node {

    private final String name;
    private final Map<Node, Double> neighborMap = new HashMap<>();
    
    private final double x;
    private final double y;
    
    public Node(String name, double x, double y) {
        this.name = Objects.requireNonNull(name, "The node name is null.");
        this.x = x;
        this.y = y;
    }
    
    public void addNeighbor(Node node) {
        double dx = x - node.x;
        double dy = y - node.y;
        double weight = Math.sqrt(dx * dx + dy * dy);
        
        neighborMap.put(node, weight);
        node.neighborMap.put(this, weight);
    }
    
    public double getWeightTo(Node node) {
        return neighborMap.get(node);
    }
    
    public Collection<Node> getNeighbors() {
        return neighborMap.keySet();
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) {
            return false;
        }
        
        Node other = (Node) o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    @Override
    public String toString() {
        return "[Node, name = " + name + "]";
    }
}
