package ca.mcmaster.cas.se2aa4.a2.generator;
import ca.mcmaster.cas.se2aa4.a2.io.Structs;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.*;


import me.tongfei.progressbar.ProgressBar;
import org.locationtech.jts.geom.*;
//import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.awt.*;

import java.beans.VetoableChangeListener;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

// This is Part 3, stilling using the object-oriented approach but with irregular mesh
public class IrregMeshGen extends GeneralMesh {

    /**
     * This function will take in a Voronoi Diagram, then we would relax the diagram by setting its centroid as the sites
     * @param oldVoronoi
     * @return VoronoiDiagramBuilder Object
     */
    private VoronoiDiagramBuilder relaxation(VoronoiDiagramBuilder oldVoronoi){
        // Relaxed Version
        VoronoiDiagramBuilder relaxVoronoi = new VoronoiDiagramBuilder();

        // Before-Relaxed diagram and polygon
        GeometryFactory makePolygons = new GeometryFactory();
        Geometry polygons = oldVoronoi.getDiagram(makePolygons);
        constrainPoly(polygons);
        List<Coordinate> coords = new ArrayList<Coordinate>();  // Coordinate List to set the sites of the new voronoi

        // Goes through each polygon and gets the centroid, and based on that centroid, set them as my new sites for the relaxed voronoi
        for (int i = 0; i < polygons.getNumGeometries(); i++) {
            Geometry poly = polygons.getGeometryN(i);
            double xCoord = poly.getCentroid().getX();
            double yCoord = poly.getCentroid().getY();
            coords.add(new Coordinate(xCoord,yCoord));
        }
        // Set new coords to the new diagram
        relaxVoronoi.setSites(coords);
        return relaxVoronoi;
    }

    /**
     * This method does the delaunay Triangulation and sets all Polygon's neighbour relations and creates segments based on it
     * @param allPolygons
     * @param allPolygonSegments
     */
    public void delaunayTriangulation(Geometry allPolygons, List<List<Integer>> allPolygonSegments){
        int numPoly = allPolygons.getNumGeometries();
        List<Set<Integer>> neighbourConnectionsList = new ArrayList<>();
        // For all Polygons Find the neighbours for each polygons
        for(int i = 0; i < numPoly; i++){
            Set<Integer> neighboursList = new HashSet<>();
            // POLYGON SEGMENTS
            List<Integer> polygonSegments = allPolygonSegments.get(i);
            // LOOPS through all the polygons to find matches
            for(int j = 0; j<numPoly; j++){
                List<Integer> polygonSegmentsCompare = allPolygonSegments.get(j);
                // FOR EACH SEGMENT THE POLYGON HAS
                for (int x = 0; x<polygonSegments.size(); x++){
                    int segment = polygonSegments.get(x);
                    // COMPARE IT TO THE OTHER POLYGON TO SEE IF THEY HAVE A SHARING SEGMENT
                    for (int y = 0; y<polygonSegmentsCompare.size(); y++){
                        int segmentCompare = polygonSegmentsCompare.get(y);
                        // FOUND SHARING SEGMENTS ADD THE POLYGON ID TO THE NEIGHBOUR LIST
                        if (segment == segmentCompare && i!=j){
                            neighboursList.add(j);
                        }
                    }
                }
            }
            // Added the neighbourList to the neighbour Connection List
            neighbourConnectionsList.add(neighboursList);
            polygonList.set(i, Structs.Polygon.newBuilder(polygonList.get(i)).addAllNeighborIdxs(neighboursList).build());
        }
        // Looping through each Polygon and getting it's neighbours
        for (int i = 0; i <numPoly; i++){
            Set<Integer> neighbours = neighbourConnectionsList.get(i);
            for (Integer j: neighbours){
                // Creating the segmentation and adding the neighbouring segments into the segmentList
                Segment neighbourConnection = Segment.newBuilder().setV1Idx(vertexList.indexOf(centroidList.get(i))).setV2Idx(vertexList.indexOf(centroidList.get(j))).build();
                if (!segmentList.contains(neighbourConnection)){
                    // ADD SEGMENT TO THE SEGMENT LIST AND ADD TO THE NEIGHBOUTCONNECTIONLIST
                    segmentList.add(neighbourConnection);
                    neighbourConnectionList.add(neighbourConnection);
                }
            }

        }
    }

    /**
     * constrainPoly will clip all the polygon shapes into the 500x500 canvas.
     * @param allPoly (Geometry) of the voronoi diagram
     */
    private void constrainPoly(Geometry allPoly){
        for (int i= 0; i < allPoly.getNumGeometries();i++){
            Coordinate[] poly = allPoly.getCoordinates();
            for (Coordinate coord : poly){
                // CONSTRAIN X COORDINATES
                if (coord.getX() < 0)
                    coord.setX(0);
                else if (coord.getX() > 500)
                    coord.setX(500);
                // CONSTRAIN Y COORDINATES
                if (coord.getY() < 0)
                    coord.setY(0);
                else if (coord.getY() > 500)
                    coord.setY(500);
            }
        }
        // ConvexHull to double check any conflicts
        allPoly.convexHull();
    }

    /**
     * This to consistently keep centroidList updated everytime we want to load the updated centroids to centroidList
     * @param polygons
     */
    private void loadCentroids(Geometry polygons){
        // Clears the centroid list to check for any previous loaded centroids
        centroidList.clear();
        // Loop through all the polygons, get their centroids and add it to the list based on their Geometry ID (GeometryN)
        for (int i=0; i < polygons.getNumGeometries();i++){
            Geometry poly = polygons.getGeometryN(i);
            double xCoord = poly.getCentroid().getX();
            double yCoord = poly.getCentroid().getY();
            centroidList.add(Vertex.newBuilder().setX(xCoord).setY(yCoord).build());
        }
    }

    /**
     * This function will generate the Irregular Mesh using VoronoiDiagramBuilder from JTS
     * 1. Randomize points onto mesh - (# of points based on user-input)
     * 2. Relaxation Option - Dependent on User Input - Default 3
     * 3. Create Meshes with vertices and segment lists
     * @param numberPolygons
     */
    private void IrregularMesh(int numberPolygons, int userRelax) {
        VoronoiDiagramBuilder voronoi = new VoronoiDiagramBuilder();
        Envelope env = new Envelope(new Coordinate(0, 0), new Coordinate(WIDTH, HEIGHT));
        voronoi.setClipEnvelope(env);//given diagram size constraint
        voronoi.setTolerance(0.01); // Set Tolerance of Voronoi Diagram
        Random Val = new Random();  // Random Variable to get random points on diagram

        //Takes user input for the number of sites
        int points = numberPolygons;
        List<Coordinate> coords = new ArrayList<Coordinate>();

//         Randomly generates coordinates based on the canvas size
        for (int i = 0; i < points; i++) {
            int xCoord = Val.nextInt(WIDTH);
            int yCoord = Val.nextInt(HEIGHT);
            //ADD SITES TO COORDINATE LIST FOR VORONOI BUILDER
            coords.add(new Coordinate(xCoord,yCoord));
        }

        // SQUARES
//        int SQUARE_SIZE = 25;
//        for(int x = SQUARE_SIZE; x < WIDTH; x += SQUARE_SIZE) {
//            for(int y = SQUARE_SIZE; y < HEIGHT; y += SQUARE_SIZE) {
//                //ADD SITES TO COORDINATE LIST FOR VORONOI BUILDER
//                coords.add(new Coordinate(x,y));
//            }
//        }
//
        // SET THE COORDINATES AS SITES TO THE VORONOI
        voronoi.setSites(coords);
        GeometryFactory makePolygons = new GeometryFactory();

        //allPolygons contains all polygons, to access each polygon, you would have to do
        Geometry allPolygons = voronoi.getDiagram(makePolygons);
        constrainPoly(allPolygons);
        loadCentroids(allPolygons);

        ProgressBar pb = new ProgressBar("Generating Voronoi Mesh", userRelax);
        pb.start();
        // RELAXATION -----------------------
        int relaxAmount = userRelax;        // RELAXATION AMOUNT BASED ON USER INPUT
        for (int i=0; i < relaxAmount; i++){    // Based on user input, how many times to relax
            pb.step();
            voronoi = relaxation(voronoi);      // Sets the voronoi to a new relaxed voronoi
        }
        pb.stop();

        // Adds all the centroids vertexes after relaxation, to the vertexList
        allPolygons = voronoi.getDiagram(makePolygons);
        constrainPoly(allPolygons);     // Contrain to 500x500 after getting the voronoi diagram

        // After loading all Centroids after relaxation or not, add all the centroids in vertexList as a Vertex Object
        loadCentroids(allPolygons);
        vertexList.addAll(centroidList);

        // Amount of polygons in the diagram
        int numPoly = allPolygons.getNumGeometries();
        // This variable will hold the Coordinates of all the polygon shape
        List<Coordinate[]> allPolygonVertices = new ArrayList<>();
        for (int i = 0; i < numPoly; i++) { //will get vertices list of each individual polygon
            Geometry poly = allPolygons.getGeometryN(i); // Indivdiual Polygon N
            allPolygonVertices.add(poly.getCoordinates());  // Add the coordinates of the polygon N into the polygonVertices list
        }

        // For each coordinate, which is apart of a polygon
        List<List<Integer>> allPolygonSegments = new ArrayList<>();
        for (int i =0; i< allPolygonVertices.size(); i++){
            //
            Coordinate[] polyVertices = allPolygonVertices.get(i);
            List<Integer> polyVerticesIdx = new ArrayList<>();
            List<Integer> polySegments = new ArrayList<>();
            // Create Segments between each point connecting all the points in the polygon making a shape
            for (int j = 0; j < polyVertices.length - 1; j++) {
                // Get the two Vertices
                Coordinate vert1 = polyVertices[j];
                Coordinate vert2 = polyVertices[j + 1];
                // Get all the coordinates of V1 and V2
                double x1 = vert1.getX();
                double y1 = vert1.getY();
                double x2 = vert2.getX();
                double y2 = vert2.getY();
                // Make two Vertex with those coordinates
                Vertex v1 = Vertex.newBuilder().setX(Double.parseDouble(precision.format(x1))).setY(Double.parseDouble(precision.format(y1))).build();
                Vertex v2 = Vertex.newBuilder().setX(Double.parseDouble(precision.format(x2))).setY(Double.parseDouble(precision.format(y2))).build();

                // Check if vertex already exist using a SET to double check the vertices
                List<Vertex> segment = new ArrayList<Vertex>(Arrays.asList(v1, v2));
                for (Vertex v : segment) {
                    if (!vertices.contains(v)) {        // IF VERTEX NOT IN THE SET ALREADY, ADD IT
                        vertices.add(v);                // ADD IT TO THE SET
                        vertexList.add(v);              // ADD IT TO THE VERTEX LIST
                    }
                }
                if (j == polyVertices.length - 2){
                    polyVerticesIdx.add(vertexList.indexOf(v1));
                    polyVerticesIdx.add(vertexList.indexOf(v2));
                }
                else{
                    polyVerticesIdx.add(vertexList.indexOf(v1));
                }

                // After checking if ther vertex already exist, we can make the segment and check if the segment already exist or not.
                // We have two segments to double-check if the reversed segment exist too.
                Segment s = Segment.newBuilder().setV1Idx(vertexList.indexOf(v1)).setV2Idx(vertexList.indexOf(v2)).build();
                Segment s2 = Segment.newBuilder().setV1Idx(vertexList.indexOf(v2)).setV2Idx(vertexList.indexOf(v1)).build();
                if (!segmentList.contains(s) && !segmentList.contains(s2)) {        // IF SEGMENT NOT IN THE SET ALREADY, ADD IT
                    segments.add(s);                // ADD IT TO THE SET
                    segmentList.add(s);             // ADD IT TO THE SEGMENT LIST
                }

                // Linking the Polygons to the segments by creating a polygon segment list and adding it to allPolygonSegments List
                // Now we will have a list of segment IDs for that aligns with the current polygon
                if (segmentList.contains(s)){       // Would be either
                    polySegments.add(segmentList.indexOf(s));
                }
                else{
                    polySegments.add(segmentList.indexOf(s2));
                }
            }
            String vertexStrings = "";
            for (int k = 0; k < polyVerticesIdx.size(); k++){
                if (k == polyVerticesIdx.size() -1){
                    vertexStrings = vertexStrings + polyVerticesIdx.get(k);
                    continue;
                }
                vertexStrings = vertexStrings + polyVerticesIdx.get(k) + ",";
            }
            Property vertices = Property.newBuilder().setKey("vertices").setValue(vertexStrings).build();
            polygonList.add(Structs.Polygon.newBuilder().addAllSegmentIdxs(polySegments).addProperties(vertices).setCentroidIdx(vertexList.indexOf(centroidList.get(i))).build());
            // Adds all the segments associated with the polygon into the allPolygonSegments list
            allPolygonSegments.add(polySegments);
        }
        // After finishing the Irregular Mesh, we call the delaunayTriangulation method to make those neighbour relations
        delaunayTriangulation(allPolygons, allPolygonSegments);

        // Converting Polygon to Structs Version
    }

    /**
     * Runs the Irregular Mesh function, creates colors for segments and vertices
     * @return Mesh including all the colored vertexes and segments
     */
    public Mesh generate(int numberPolygons, int userRelax) {
        if (numberPolygons < 0){
            return Mesh.newBuilder().addAllVertices(vertexList).addAllSegments(segmentList).build();
        }
        IrregularMesh(numberPolygons, userRelax);
        //COLOUR SITE VERTEXES AND SEGMENTS
        for (Vertex v : vertexList) {
            int red = 255;
            int green = 0;
            int blue = 0;
            int alpha = 255;
            // IF NOT A CENTROID, MAKE IT INVISIBLE
            if (!centroidList.contains(v)){
                alpha = 0;
            }
            colorVertex(v, red, green, blue, alpha);
        }
        // For each segment in the segmentlist apply a color to it
        for (Segment s : segmentList) {
            int red = 0;
            int green = 0;
            int blue = 0;
            int alpha = 0; // FOR ISLANDS TAKE AWAY SEGMENT COLOURS
            // IF THE SEGMENT IS A NEIGHBOUR, MAKE IT GREY COLOR
            if (neighbourConnectionList.contains(s)){
                red = 169;
                green = 169;
                blue=169;
            }
            colorSegment(s,red,green,blue,alpha );      // COLOUR THE SEGMENTS BLACK
        }
        // RETURNS MESH TO BE READY FOR VISUALIZATION
        return Mesh.newBuilder().addAllVertices(vertexList).addAllSegments(segmentList).addAllPolygons(polygonList).build();
    }

    /**
     * This function replaces the vertex given as a parameter and adds a rgb color property to it based on rgb value params
     * @param vertex
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    private void colorVertex(Vertex vertex, int red, int green, int blue, int alpha){
        Random bag = new Random();
        String colorCode = red + "," + green + "," + blue + "," + alpha;
        // Create new Property with "rgb_color" key and the rgb value as the value
        Property color = Property.newBuilder().setKey("rgb_color").setValue(colorCode).build();
        Vertex colored = Vertex.newBuilder(vertex).addProperties(color).build();
        // Set the old vertex in the list as the new one with color property
        vertexList.set(vertexList.indexOf(vertex), colored);
    }

    /**
     * This function replaces the segment given as a parameter and adds a rgb color property to it based on rgb value params
     * @param seg
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    private void colorSegment(Segment seg, int red, int green, int blue, int alpha){
        // Create new Property with "rgb_color" key and the rgb value as the value
        Property color = Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Segment colored = Segment.newBuilder(seg).addProperties(color).build();
        // Set the old segment in the list as the new one with color property
        segmentList.set(segmentList.indexOf(seg), colored);
    }
}
