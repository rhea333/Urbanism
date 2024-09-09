package ca.mcmaster.cas.se2aa4.a2.generator;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Mesh;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.*;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Polygon;

//FROM JTS
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.awt.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


// This is Part 2 using a more Object-Oriented Approach

/**
 * This abstract class serves as the base to a Mesh. This is a General Mesh containing all the nessasary components and attributes
 */
abstract class GeneralMesh {
    //THESE DATASETS ARE FOR REGULAR MESH
    public int WIDTH = 500;
    public int HEIGHT = 500;
    Set<Vertex> vertices = new HashSet<>();
    Set<Segment> segments = new HashSet<>();

    List<Vertex> vertexList = new ArrayList<Vertex>();
    List<Segment> segmentList = new ArrayList<Segment>();
    List<Polygon> polygonList = new ArrayList<Polygon>();
    List<Vertex> centroidList = new ArrayList<>();
    List<Segment> neighbourConnectionList = new ArrayList<>();
    DecimalFormat precision  = new DecimalFormat("0.00");
}

/**
 * is the MeshGen Class where it contains the method generate() to generate the Mesh needed for the Mesh square grid.
 */
public class MeshGen extends GeneralMesh{
    public final int SQUARE_SIZE = 20;

    // This method includes creating grids, which incrementally calls makePolygon for each polygon

    /**
     * makeVertex will create the grid shape and call the makePolygon method to define each square as a polygon
     */
    private void makeVertex(){
        for(int x = 0; x < WIDTH; x += SQUARE_SIZE) {
            for(int y = 0; y < HEIGHT; y += SQUARE_SIZE) {
                // Here I replicate a square with vertices
                Vertex v1 = Vertex.newBuilder().setX(Double.parseDouble(precision.format(x))).setY(Double.parseDouble(precision.format(y))).build();      // Top left
                Vertex v2 = Vertex.newBuilder().setX(Double.parseDouble(precision.format(x + SQUARE_SIZE))).setY(Double.parseDouble(precision.format(y))).build();    // Top Right
                Vertex v3 = Vertex.newBuilder().setX(Double.parseDouble(precision.format(x))).setY(Double.parseDouble(precision.format(y + SQUARE_SIZE))).build();    // Bottom Left
                Vertex v4 = Vertex.newBuilder().setX( Double.parseDouble(precision.format(x+ SQUARE_SIZE))).setY(Double.parseDouble(precision.format(y + SQUARE_SIZE))).build();    // Bottom Right

                // This list is made so I can conveniently run through each vertex in the square
                List<Vertex> square = new ArrayList<Vertex>(Arrays.asList(v1,v2,v3,v4));
                for (Vertex v : square){
                    if (!vertices.contains(v)){     // If it's not in the SET, add it and also add it to the Iterable List vertexList
                        vertices.add(v);            // The SET will prevent duplicates
                        vertexList.add(v);
                    }
                }
                // This function will create the segments and define the square shape as a polygon on the vertexes made here
                makePolygon(vertexList.indexOf(v1),vertexList.indexOf(v2),vertexList.indexOf(v3),vertexList.indexOf(v4));
            }
        }
    }

    /**
     * After creating all the vertices and layout as a grid, here we load all the neighbour relations between all the polygons
     * Also add the neighbour relations to the segmentList and neighbourConnectionsList
     */
    private void loadNeighbourRelations(){
        // After all the polygons are made, here is where we make neighbour relations
        Set<Integer> neighboursList = new HashSet<>();
        // FOR EACH POLYGON
        for(Polygon poly: polygonList){
            // SINCE WE ONLY ADDED ONE EXTRA PROPERTY, get(0) gets the vertices relations
            String val = poly.getPropertiesList().get(0).getValue();
            String[] raw = val.split(",");
            // Runs through the polygonList to find a neighbour match utilizing i as the neighbour ID
            for(int i = 0; i<polygonList.size(); i++){
                // Get the comparing polygon as an object
                Polygon polyCompare = polygonList.get(i);
                String valCompare = polyCompare.getPropertiesList().get(0).getValue();  // Get containing vertexes
                String[] rawCompare = valCompare.split(",");
                // Since we know they have 4 vertices since its a grid, run through all 4
                for (int j = 0; j<4; j++){
                    int vertex = Integer.parseInt(raw[j]);
                    for (int y = 0; y<4; y++){
                        int vertexCompare = Integer.parseInt(rawCompare[y]);
                        // If the centroids are not the same and they both share a vertex, they are neighbours.
                        if (vertex == vertexCompare && (poly.getCentroidIdx() != polyCompare.getCentroidIdx())){
                            neighboursList.add(i);  // Add the polygon ID to the neighbour list of the current Polygon
                        }
                    }
                }
            }
            // CREATE A NEW POLYGON THAT CONTAINS THE NEIGHBOUR LIST
            Polygon polyNew = Polygon.newBuilder(poly).addAllNeighborIdxs(neighboursList).build();
            polygonList.set(polygonList.indexOf(poly), polyNew);    // Set the polygon to the new polygon with neighbours
            neighboursList.clear();     // Clear for next iteration
        }

        // Here we will create the segment objects to connect the neighbour relations to visualize
        for (Polygon poly:polygonList){
            for (Integer i : poly.getNeighborIdxsList()){
                // Segment object which is the actual neighbour relation
                Segment neighbourConnection = Segment.newBuilder().setV1Idx(poly.getCentroidIdx()).setV2Idx(polygonList.get(i).getCentroidIdx()).build();

                // If it not is already in the segmentList, add it
                if (!segmentList.contains(neighbourConnection)){
                    segmentList.add(neighbourConnection);
                    neighbourConnectionList.add(neighbourConnection);
                }
            }
        }
    }


    /**
     * Given 4 parameters of vertex IDs, we can create polygons and segments.
     * Polygons created will also be updated on the polygonList data so that we can use it for other features
     * @param v1Id
     * @param v2Id
     * @param v3Id
     * @param v4Id
     */
    private void makePolygon(int v1Id,int v2Id,int v3Id,int v4Id){
        // Created Segments here based on the identifier of the vertexes to make a square shape
        Segment s1 = Segment.newBuilder().setV1Idx(v1Id).setV2Idx(v2Id).build();
        Segment s2 = Segment.newBuilder().setV1Idx(v2Id).setV2Idx(v4Id).build();
        Segment s3 = Segment.newBuilder().setV1Idx(v3Id).setV2Idx(v4Id).build();
        Segment s4 = Segment.newBuilder().setV1Idx(v1Id).setV2Idx(v3Id).build();

        // This list is made so I can conviently run through each segment in the square
        List<Segment> square = new ArrayList<Segment>(Arrays.asList(s1,s2,s3,s4));
        for (Segment s : square){
            if (!segments.contains(s)){     // If it's not in the SET, add it and also add it to the Iterable List
                segments.add(s);            // The SET will prevent duplicates
                segmentList.add(s);
            }
        }
        // Here I created an integer list of all the segment identifiers, to make up the square. It is added in a consecutive order aswell
        List<Integer> squarelist = new ArrayList<Integer>(Arrays.asList(segmentList.indexOf(s1),segmentList.indexOf(s2),segmentList.indexOf(s3),segmentList.indexOf(s4)));


        // Getting the vertexes to reference to calculate the centroid location
        Vertex vertex1 = vertexList.get(v1Id);
        Vertex vertex2 = vertexList.get(v2Id);
        Vertex vertex3 = vertexList.get(v3Id);

        // Get the coordinate of the centroid X and Y values
        double centroidIdx = Double.parseDouble(precision.format((vertex1.getX()+vertex2.getX())/2));
        double centroidIdy = Double.parseDouble(precision.format((vertex1.getY()+vertex3.getY())/2));
        // Create a CENTROID as a Vertex object to be visualized later
        Vertex centroid = Vertex.newBuilder().setX(centroidIdx).setY(centroidIdy).build();
        vertexList.add(centroid);
        // Add to a CENTROID List to be used for colouring
        centroidList.add(centroid);
        Property vertices = Property.newBuilder().setKey("vertices").setValue(v1Id + "," + v2Id + "," + v3Id + "," + v4Id).build();
        // Creates a polygon object, adds the list of integers of segments to the object
        Polygon poly = Polygon.newBuilder().addAllSegmentIdxs(squarelist).setCentroidIdx(vertexList.indexOf(centroid)).addProperties(vertices).build();
        polygonList.add(poly);      // Add the polygon object into the polygonList
    }



    public Mesh generate() {

        // This will run the program to create the grid and fill in the vertices, segments and polygon
        makeVertex();
        loadNeighbourRelations();

        Random bag = new Random();
        for (Vertex v : vertexList) {
            // If not a centroid, color randomly
            if (!centroidList.contains(v)){
                int red = bag.nextInt(255);
                int green = bag.nextInt(255);
                int blue = bag.nextInt(255);
                int alpha = 255;
                colorVertex(v,red,green,blue, alpha);
            }
            else{
                // If centroid, colour red
                colorVertex(v, 255,0,0,255);
            }
        }

        //Coloring the Segments
        for (Segment s : segmentList) {
            // If segment not a neighbour relation, colour based on the two vertices it is attached to
            if (!neighbourConnectionList.contains(s)){
                Color v1Color = extractColor(vertexList.get(s.getV1Idx()).getPropertiesList());
                Color v2Color = extractColor(vertexList.get(s.getV2Idx()).getPropertiesList());
                int red = (v1Color.getRed() + v2Color.getRed()) / 2;
                int green = (v1Color.getGreen() + v2Color.getGreen()) / 2;
                int blue = (v1Color.getBlue() + v2Color.getBlue()) / 2;
                int alpha = 255;
                colorSegment(s,red,green,blue,alpha );
            }
            else{
                // If the segment is a neighbour relation, colour it grey
                colorSegment(s, 169,169,169,255);
            }
        }
        // RETURN MESH OBJECT TO BE VISUALIZED
        return Mesh.newBuilder().addAllVertices(vertexList).addAllSegments(segmentList).addAllPolygons(polygonList).build();
    }

    /**
     * Called when given a vertex, and replaces the vertex with added colour
     * @param vertex
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    private void colorVertex(Vertex vertex, int red, int green, int blue, int alpha){
        Random bag = new Random();
        String colorCode = red + "," + green + "," + blue + "," + alpha;
        Property color = Property.newBuilder().setKey("rgb_color").setValue(colorCode).build();
        Vertex colored = Vertex.newBuilder(vertex).addProperties(color).build();
        vertexList.set(vertexList.indexOf(vertex), colored);
    }

    /**
     * Called when given a segment, and replaces the segment with added colour
     * @param seg
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    private void colorSegment(Segment seg, int red, int green, int blue, int alpha){
        Property color = Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Segment colored = Segment.newBuilder(seg).addProperties(color).build();
        segmentList.set(segmentList.indexOf(seg), colored);
    }


    /**
     * Give a property list, return back the colour of the Vertex/Segment Object
     * @param properties
     * @return
     */
    private Color extractColor(List<Property> properties) {
        String val = null;
        for(Property p: properties) {
            if (p.getKey().equals("rgb_color")) {
                val = p.getValue();
            }
        }
        if (val == null)    // If colour not found, return colour black
            return Color.BLACK;
        String[] raw = val.split(",");
        int red = Integer.parseInt(raw[0]);
        int green = Integer.parseInt(raw[1]);
        int blue = Integer.parseInt(raw[2]);
        int alpha = Integer.parseInt(raw[3]);
        return new Color(red, green, blue, alpha);

    }
}

