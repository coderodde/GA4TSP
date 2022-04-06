package com.github.coderodde.tsp;

import com.github.coderodde.tsp.AbstractGeneticTSPSolver.Solution;
import com.github.coderodde.tsp.impl.GeneticTSPSolverV1;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

public final class Demo extends Application {

    private static final int NUMBER_OF_GENERATIONS = 50;
    private static final int POPULATION_SIZE = 500;
    private static final int GRAPH_SIZE = 10;
    private static final int NUMBER_OF_EDGES = 30;
    
    public static void main(String[] args) {
        miniDemo(args);
    }
    
    private static void miniDemo(String[] args) {
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
        
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("GA4TSP");
        Group root = new Group();
        Canvas canvas = new Canvas(300, 300);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        List<Node> nodeList = createNodeList(GRAPH_SIZE, NUMBER_OF_EDGES);
        
        Solution solution = 
                new GeneticTSPSolverV1()
                        .findTSPSolution(
                                nodeList.get(0), 
                                NUMBER_OF_GENERATIONS, 
                                POPULATION_SIZE);
        
        double[][] nodeCoordinates = getTourNodeCoordinates(solution.getTour());
        
        drawTour(gc, nodeCoordinates);
        
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
    private static void drawTour(GraphicsContext gc,
                                 double[][] nodeCoordinates) {
        
        gc.strokePolyline(nodeCoordinates[0], 
                          nodeCoordinates[1], 
                          nodeCoordinates[0].length);
        
        // Finish the tour:
        double endX = nodeCoordinates[0][nodeCoordinates[0].length - 1];
        double endY = nodeCoordinates[1][nodeCoordinates[1].length - 1];
        
        gc.strokeLine(
                nodeCoordinates[0][0], 
                nodeCoordinates[1][0],
                endX,
                endY);
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