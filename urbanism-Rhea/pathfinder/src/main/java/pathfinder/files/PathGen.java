package pathfinder.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Polygon;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Segment;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Vertex;

import ca.mcmaster.cas.se2aa4.a2.io.Structs.Mesh;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class PathGen implements ShortestPath, ColourCity {

    public List<Vertex> vList;
    private List<Vertex> vList2;
    public List<Segment> sList;
    List<Double> elevations;
    List<Integer> cityNodeList = new ArrayList<>();
    private List<Integer> IslandVerts;
    private List<List<Integer>> adjList = new ArrayList<>();

    private ArrayList<ArrayList<Integer>> starNetwork = new ArrayList<>();
    private int startNode;


    int numCities;

    //develops the graph
    public void generate(List<Integer> IslandVertices, List<Vertex> vertexList, List<Segment> segmentList, int numOfCities){
        IslandVerts = IslandVertices;
        vList = vertexList;
        sList = segmentList;
        numCities = numOfCities;


        Graph();
        adjList();
        StarNetwork();
        colourRoads();
        colourCityNodes();

    }

    public void Graph(){
        for (Integer i : IslandVerts){
            Vertex v = vList.get(i);
            double x = v.getX();
            double y = v.getY();
            NodeGen vertex = new NodeGen(x,y);
            Vertex newNode = vertex.vertexBuilder();
            vList.add(newNode);
        }
        for (int j = 0; j < vList.size() - 1; j++) {
            SegmentGen segment = new SegmentGen(j, j + 1);
            Segment newSegment = segment.segmentBuilder();
            if (!sList.contains(newSegment)) {
                sList.add(newSegment);
            }
        }
    }

    @Override
    //run through island vertices, run through segments, if v1 segment is current island vertex, store v2 in adjacency list
    public void adjList(){
        //create empty lists for all vertices so neighbour lists can later be set to the right index
        for (int k = 0; k < vList.size(); k++){
            List<Integer> emptyNeighbours = new ArrayList<>();
            adjList.add(emptyNeighbours);
        }

        for (Integer i : IslandVerts){
            List<Integer> neighbours = new ArrayList<>();
            for (Segment s : sList){
                if (s.getV1Idx() == i){
                    neighbours.add(s.getV2Idx());
                }
                else if (s.getV2Idx() == i){
                    neighbours.add(s.getV1Idx());
                }
            }
            //index where neighbours are stored matches vertex id
            adjList.set(i,neighbours);
        }
    }



    //islandvertices is integer list whose elements correspond to vertexlist indexes.
    @Override
    public ArrayList<Integer> findShortestPath(int startNode) {
        ArrayList<Integer> shortestPathNodes = new ArrayList<Integer>();
        HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();
        int numNodes = IslandVerts.size();
        Random num2 = new Random();
        int endNodeIdx = num2.nextInt(numNodes);
        //get vertex id for end nodes
        int endNode = IslandVerts.get(endNodeIdx);
        cityNodeList.add(endNode);

        Queue<Integer> q = new LinkedList<Integer>();
        Stack<Integer> nodePathStack = new Stack<Integer>();

        q.add(startNode);
        nodePathStack.add(startNode);
        visited.put(startNode, true);

        while (!q.isEmpty()){
            int head = q.poll();
            List<Integer> neighbours = adjList.get(head);

            for(int n : neighbours){
                if(!visited.containsKey(n)){
                    q.add(n);
                    visited.put(n, true);
                    nodePathStack.add(n);
                    if(head == endNode)
                        break;

                }
            }
        }

        int node, currentNode = endNode;
        shortestPathNodes.add(endNode);
        while (!nodePathStack.isEmpty()){
            node = nodePathStack.pop();

            //go backwards from last node to start node
            if (adjList.get(currentNode).contains(node)){
                shortestPathNodes.add(node);
                currentNode = node;
                if (node == startNode){
                    break;
                }
            }

        }

        return shortestPathNodes;
    }

    @Override
    public ArrayList<ArrayList<Integer>> StarNetwork() {
        int numNodes = IslandVerts.size();
        Random num = new Random();
        int startNodeIdx = num.nextInt(numNodes);
        startNode = IslandVerts.get(startNodeIdx);
        cityNodeList.add(startNode);

        for (int i = 0; i < numCities; i++){
            ArrayList<Integer> minPath = findShortestPath(startNode);
            starNetwork.add(minPath);
        }
        return starNetwork;

    }


    @Override
    public void colourCityNodes() {
        System.out.println(cityNodeList.toString());
        for (int i : cityNodeList){
            System.out.println("City:"+i);
            Vertex cityNode = vList.get(i);
            colorVertex(cityNode,255,0,255,255);
            new Color(255, 0, 255);
            increaseThickness(vList.get(i));


        }
    }

    @Override
    public void colourRoads() {
        for (ArrayList<Integer> sp: starNetwork)
            for (int i = 0; i < sp.size() - 1; i++) {
                for (Segment s : sList) {
                    if (s.getV1Idx() == sp.get(i) & s.getV2Idx() == sp.get(i + 1)) {
                        colorSegment(s, 0, 0, 0, 255);
                        new Color(0, 0, 0);

                    } else if (s.getV2Idx() == sp.get(i) & s.getV1Idx() == sp.get(i + 1)) {
                        colorSegment(s, 0, 0, 0, 255);

                    }

                }

            }
    }

    private void increaseThickness(Vertex vertex){
        double previousThickness = extractThickness(vertex,vertex.getPropertiesList());
        Random rand = new Random();
        double thickMultiplier = rand.nextDouble((2 - 1) + 1) + 1;
        double val = previousThickness*thickMultiplier;
        Structs.Property thickness = Structs.Property.newBuilder().setKey("cityThickness").setValue(String.valueOf(val)).build();
        Vertex thickened = Vertex.newBuilder(vertex).addProperties(thickness).build();
        vList.set(vList.indexOf(vertex), thickened);


    }

    private Double extractThickness(Vertex vertex, List<Structs.Property> properties){
        String val = null;
        for(Structs.Property p: properties) {
            // TRY TO FIND THE RGB COLOR
            if (p.getKey().equals("cityThickness")) {
                val = p.getValue();
            }
        }
        if (val == null){       // IF no thickness, add thinkness
            return 3.0;
        }
        double valInt = Double.parseDouble(val);
        return valInt;
    }


    private void colorVertex(Structs.Vertex vertex, int red, int green, int blue, int alpha){
        String colorCode = red + "," + green + "," + blue + "," + alpha;
        // Create new Property with "rgb_color" key and the rgb value as the value
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(colorCode).build();
        Structs.Vertex colored = Structs.Vertex.newBuilder(vertex).addProperties(color).build();
        // Set the old vertex in the list as the new one with color property
        vList.set(vList.indexOf(vertex), colored);
    }

    private void colorSegment(Structs.Segment seg, int red, int green, int blue, int alpha){
        // Create new Property with "rgb_color" key and the rgb value as the value
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Structs.Segment colored = Structs.Segment.newBuilder(seg).addProperties(color).build();
        // Set the old segment in the list as the new one with color property
        sList.set(sList.indexOf(seg), colored);
    }

    /**
     * Get the Color value of a property in a String form to compare to another String.
     * @param properties
     * @return
     */
    private String extractColorString(List<Structs.Property> properties){
        String val = null;
        for(Structs.Property p: properties) {
            // TRY TO FIND THE RGB COLOR
            if (p.getKey().equals("rgb_color")) {
                val = p.getValue();
            }
        }
        if (val == null){       // IF THE RGB COLOR PROPERTY DOESNT EXIST, COVER THAT CASE BY MAKING IT BLACK
            return "0,0,0,0"; // COVERING CASE IF KEY RGB_COLOR DOESN'T EXIST
        }
        return val;
    }
}
