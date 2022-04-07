package com.github.coderodde.tsp;

import com.github.coderodde.tsp.TSPSolver.Solution;
import com.github.coderodde.tsp.impl.BruteForceTSPSolver;
import com.github.coderodde.tsp.impl.GeneticTSPSolverV1;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public final class Demo extends Application {

    private static final int NUMBER_OF_GENERATIONS = 50;
    private static final int POPULATION_SIZE = 500;
    private static final int GRAPH_SIZE = 11;
    private static final int NUMBER_OF_EDGES = 50;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("GA4TSP");
        Group root = new Group();
        Canvas canvas = new Canvas(300.0, 300.0);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        List<Node> nodeList = createNodeList(GRAPH_SIZE, NUMBER_OF_EDGES);
        
        long startTime = System.currentTimeMillis();
        
        // solution1 - approximated.
        Solution solution1 = 
                new GeneticTSPSolverV1(NUMBER_OF_GENERATIONS,
                                       POPULATION_SIZE)
                        .findTSPSolution(
                                nodeList.get(0));
        
        long endTime = System.currentTimeMillis();
        System.out.println(
                "GA duration: " + (endTime - startTime) + " ms.");
        
        // solution2 - optimal.
        startTime = System.currentTimeMillis();
        
        Solution solution2 = 
                new BruteForceTSPSolver().findTSPSolution(nodeList.get(0));
        
        endTime = System.currentTimeMillis();
        System.out.println(
                "Brute-force duration: " + (endTime - startTime) + " ms.");
        
        System.out.println("---");
        double costApproximated;
        double costBruteForced;
        
        System.out.println(
                "GA cost: " 
                        + (costApproximated = Utils.getTourCost(
                                solution1.getTour(),
                                solution1.getData())));
        
        System.out.println(
                "Brute-force cost: " 
                        + (costBruteForced = Utils.getTourCost(
                                solution2.getTour(),
                                solution2.getData())));
        
        System.out.println(
                "Cost ratio: " + (costApproximated / costBruteForced));
        
        // approximated.
        double[][] nodeCoordinates1 = 
                getTourNodeCoordinates(solution1.getTour());
        
        // optimal.
        double[][] nodeCoordinates2 = 
                getTourNodeCoordinates(solution2.getTour());
        
        drawTour(gc, nodeCoordinates1, nodeCoordinates2);
        
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
    private static void drawTour(GraphicsContext gc,
                                 double[][] nodeCoordinates1, // approximated.
                                 double[][] nodeCoordinates2) {
        
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(6.0);
        
        gc.strokePolyline(nodeCoordinates1[0], 
                          nodeCoordinates1[1], 
                          nodeCoordinates1[0].length);
        
        // Finish the tour:
        double endX = nodeCoordinates1[0][nodeCoordinates1[0].length - 1];
        double endY = nodeCoordinates1[1][nodeCoordinates1[1].length - 1];
        
        gc.strokeLine(
                nodeCoordinates1[0][0], 
                nodeCoordinates1[1][0],
                endX,
                endY);
        
        gc.setLineWidth(3.0);
        gc.setStroke(Color.BLUE);
        
        gc.strokePolyline(nodeCoordinates2[0], 
                          nodeCoordinates2[1], 
                          nodeCoordinates2[0].length);
        
        // Finish the tour:
        endX = nodeCoordinates2[0][nodeCoordinates2[0].length - 1];
        endY = nodeCoordinates2[1][nodeCoordinates2[1].length - 1];
        
        gc.strokeLine(
                nodeCoordinates2[0][0], 
                nodeCoordinates2[1][0],
                endX,
                endY);
        
        for (int nodeIndex = 0; 
                nodeIndex < nodeCoordinates1[0].length; 
                nodeIndex++) {
            double x = nodeCoordinates1[0][nodeIndex];
            double y = nodeCoordinates1[1][nodeIndex];
            
            gc.fillRect(x - 2.0, y - 2.0, 4.0, 4.0);
        }
    }
    
    private static List<Node> createNodeList(int graphSize, int numberOfEdges) {
        Random random = new Random();
        List<Node> nodeList = new ArrayList<>(graphSize);
        
        for (int i = 0; i < graphSize; ++i) {
            Node node = 
                    new Node(
                            "" + i, 
                            300.0 * random.nextDouble(), 
                            300.0 * random.nextDouble());
            
            nodeList.add(node);
        }
        
        for (int edge = 0; edge < numberOfEdges; ++edge) {
            Node node1 = nodeList.get(random.nextInt(nodeList.size()));
            Node node2 = nodeList.get(random.nextInt(nodeList.size()));
            node1.addNeighbor(node2);
        }
        
        return nodeList;
    }
    
    private static double[][] getTourNodeCoordinates(List<Node> nodeList) {
        double[][] coordinateArray = new double[2][nodeList.size()];
        int index = 0;
        
        for (Node node : nodeList) {
            coordinateArray[0][index] = node.getX();
            coordinateArray[1][index] = node.getY();
            index++;
        }
        
        return coordinateArray;
    }
}