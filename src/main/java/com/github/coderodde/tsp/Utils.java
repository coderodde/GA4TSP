package com.github.coderodde.tsp;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 ()
 * @since 1.6 ()
 */
public final class Utils {
    
    public static double getTourCost(List<Node> tour, 
                                     AllPairsShortestPathData data) {
        double cost = 0.0;
        
        for (int i = 0; i < tour.size(); ++i) {
            int index1 = i;
            int index2 = (i + 1) % tour.size();
            Node node1 = tour.get(index1);
            Node node2 = tour.get(index2);
            cost += data.getShortestPathCost(node1, node2);
        }
        
        return cost;
    }
    
    public static List<Node> getExactTour(List<Node> basicTour,
                                          AllPairsShortestPathData data) {
        List<Node> tour = new ArrayList<>();
        
        for (int i = 0; i < basicTour.size(); ++i) {
            int index1 = i;
            int index2 = (i + 1) % basicTour.size();
            Node node1 = basicTour.get(index1);
            Node node2 = basicTour.get(index2);
            List<Node> path = data.getPath(node1, node2);
            path.remove(path.size() - 1);
            tour.addAll(path);
        }
        
        return tour;
    }
}
