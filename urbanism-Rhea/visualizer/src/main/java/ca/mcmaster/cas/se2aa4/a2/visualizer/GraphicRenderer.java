package ca.mcmaster.cas.se2aa4.a2.visualizer;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Mesh;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Vertex;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Property;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Segment;


import java.awt.*;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.geom.Line2D;
import java.util.Random;

/**
 * This Graphic Renderer is the core of rendering the mesh, colouring all polygons
 */
public class GraphicRenderer {

    private static final int THICKNESS = 3;

    /**
     * Renders all Segments
     * @param aMesh
     * @param canvas
     * @param debug
     */
    private void renderSegments(Mesh aMesh, Graphics2D canvas, String debug){
        // FOR EACH SEGMENT IN THE MESH FILE, GET THE VERTEXES BASED ON THE V1Idx and V2Idx
        for (Segment s: aMesh.getSegmentsList()){
            Vertex vertex = aMesh.getVerticesList().get(s.getV1Idx());
            Vertex vertex2 = aMesh.getVerticesList().get(s.getV2Idx());
            // USING THE TWO VERTEXES, GET THE X AND Y VALUES OF BOTH
            double x1 = vertex.getX();
            double y1 = vertex.getY();
            double x2 = vertex2.getX();
            double y2 = vertex2.getY();
            boolean isNeighbour = false;
            for (Property p : s.getPropertiesList()) {
                // TRY TO FIND THE RGB COLOR
                if (p.getKey().equals("rgb_color")) {
                    if (p.getValue().equals("169,169,169,255")){
                        isNeighbour = true;
                    }
                }
            }
            double val = extractThickness(s.getPropertiesList());
            // If the debugMode is Off and the segment is not a neighbour relation
            if (debug.equals("debugOff") && !isNeighbour) {
                Color old = canvas.getColor();
                Stroke oldStroke = canvas.getStroke();
                canvas.setColor(extractColor(s.getPropertiesList()));
                Line2D seg = new Line2D.Double(x1, y1, x2, y2);
                Stroke stroke1 = new BasicStroke((float)val);
                canvas.setStroke(stroke1);
                canvas.draw(seg);
                canvas.setStroke(oldStroke);
                canvas.setColor(old);
            }
            // If debug is on, show the neighbour relations
            else if (debug.equals("debugOn")){
                Color old = canvas.getColor();
                Stroke oldStroke = canvas.getStroke();

                if (isNeighbour){       // Colour the Neighbour Relations Grey
                    canvas.setColor(new Color(169,169,169,50));
                } else{     // Colour the Segments Black
                    canvas.setColor(Color.BLACK);
                }
                Line2D seg = new Line2D.Double(x1, y1, x2, y2);
                Stroke stroke1 = new BasicStroke(1);
                canvas.setStroke(stroke1);
                canvas.draw(seg);
                canvas.setStroke(oldStroke);
                canvas.setColor(old);
            }
        }
    }

    /**
     * Renders all Vertices
     * @param aMesh
     * @param canvas
     * @param debug
     * @param isIrreg
     */
    private void renderVertices(Mesh aMesh, Graphics2D canvas, String debug, boolean isIrreg){
        // FOR EACH VERTEX GET THE X AND Y VALUE, GET THE COLOR AND CREATE AN ELLIPSE2D VISUALIZED DOT
        for (Vertex v: aMesh.getVerticesList()) {
            double thickness = extractCityThickness(v.getPropertiesList());
            double centre_x = v.getX() - (thickness/2.0d);
            double centre_y = v.getY() - (thickness/2.0d);
            boolean isCentroid = false;
            boolean isWater = false;
            for (Property p : v.getPropertiesList()) {
                // TRY TO FIND THE RGB COLOR
                if (p.getKey().equals("rgb_color")) {
                    if (p.getValue().equals("255,0,0,255")){    // If a vertex is red, it is a centroid
                        isCentroid = true;
                    }
                    if (p.getValue().equals("0,0,255,255")){    // If a vertex is red, it is a centroid
                        isWater = true;
                    }
                }
            }
            // If it is a irregular mode, show the centroids even without debug
            if (isIrreg){
                // If it is in debug mode, get all vertices colours
                Color old = canvas.getColor();
                canvas.setColor(extractColor(v.getPropertiesList()));
                Ellipse2D point = new Ellipse2D.Double(centre_x, centre_y, thickness, thickness);
                canvas.fill(point);
                canvas.setColor(old);
            }
            else {
                // Regular Mode/Grid Mode
                if (debug.equals("debugOff") && !isCentroid) {
                    // Debug Mode should not show centroids for Grid Mode
                    Color old = canvas.getColor();
                    if (isWater){
                        canvas.setColor(Color.BLUE);
                    }
                    else{
                        canvas.setColor(extractColor(v.getPropertiesList()));
                    }
                    Ellipse2D point = new Ellipse2D.Double(centre_x, centre_y, thickness, thickness);
                    canvas.fill(point);
                    canvas.setColor(old);

                }
                if (debug.equals("debugOn")) {
                    // DEBUG MODE ON
                    Color old = canvas.getColor();
                    if (isCentroid) {   // Show Centroid
                        canvas.setColor(Color.RED);
                    } else {    // Colour the Vertices Black
                        canvas.setColor(Color.BLACK);
                    }
                    Ellipse2D point = new Ellipse2D.Double(centre_x, centre_y, thickness, thickness);
                    canvas.fill(point);
                    canvas.setColor(old);
                }
            }
        }
    }

    private void renderPolygon(Mesh aMesh, Graphics2D canvas, String debug){
        for (Structs.Polygon poly : aMesh.getPolygonsList()){
            Color old = canvas.getColor();

            canvas.setColor(extractColor(poly.getPropertiesList()));
            Polygon polyFill = new Polygon();
            List<Segment> segmentList = aMesh.getSegmentsList();
            List<Vertex> vertexList = aMesh.getVerticesList();
            List<Integer> polyVertices = extractVertices(poly.getPropertiesList());

            for (int i =0; i< polyVertices.size(); i++){
                double x = vertexList.get(polyVertices.get(i)).getX();
                double y = vertexList.get(polyVertices.get(i)).getY();
                polyFill.addPoint((int)x,(int)y);
            }
            //Get color of polygon, set canvas color to color of polygon
            Color color = extractColor(poly.getPropertiesList());
            canvas.setColor(color);
            canvas.fillPolygon(polyFill);
            canvas.setColor(old);
        }
    }
    private Double extractThickness(List<Structs.Property> properties){
        String val = null;
        for(Structs.Property p: properties) {
            // TRY TO FIND THE RGB COLOR
            if (p.getKey().equals("riverThickness")) {
                val = p.getValue();
            }
        }
        if (val == null){       // IF no thickness, add thinkness lol
            return 1.0;
        }
        double valInt = Double.parseDouble(val);
        return valInt;
    }
    private Double extractCityThickness(List<Structs.Property> properties){
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
    private List<Integer> extractVertices(List<Property> properties){
        String val = null;
        for(Property p: properties) {
            // TRY TO FIND THE RGB COLOR
            if (p.getKey().equals("vertices")) {
                val = p.getValue();
            }
        }
        if (val == null){       // IF THE RGB COLOR PROPERTY DOESNT EXIST, COVER THAT CASE BY MAKING IT BLACK
            System.out.println("NO VERTEX PROPERTY");
            return null;
        }
        String[] raw = val.split(",");
        List<Integer> rawInts = new ArrayList<>();
        for (int i =0; i< raw.length;i++){
            Integer value = Integer.parseInt(raw[i]);
            rawInts.add(value);
        }
        return rawInts;
    }
    public void render(Mesh aMesh, Graphics2D canvas, String debug) {
        canvas.setColor(Color.BLACK);
        Stroke stroke = new BasicStroke(0.5f);
        canvas.setStroke(stroke);
        // Since the Irregular Mesh is made with the Voronoi Library, it doesn't have a Structs.Polygon mesh. Here is how we differentiate the two
        boolean isIrreg = (aMesh.getPolygonsCount() == 0);
        renderPolygon(aMesh,canvas,debug);
        renderSegments(aMesh,canvas,debug);
        renderVertices(aMesh,canvas,debug,isIrreg);
    }

    /**
     * This function will extract the colour from the property rgb_color and if there isn't a property of gb_colour, color it black
     * @param properties
     * @return
     */
    private Color extractColor(List<Property> properties) {
        String val = null;
        // EXTRACTCOLOR GOES THROUGH ALL THE PROPERTIES OF THE OBJECT
        for(Property p: properties) {
            // TRY TO FIND THE RGB COLOR
            if (p.getKey().equals("rgb_color")) {
                val = p.getValue();
            }
        }
        if (val == null){       // IF THE RGB COLOR PROPERTY DOESNT EXIST, COVER THAT CASE BY MAKING IT BLACK
            return Color.BLACK; // COVERING CASE IF KEY RGB_COLOR DOESN'T EXIST
        }
        // IF RGB PROPERTY EXIST, GET VALUES OF EACH PARAMETER
        String[] raw = val.split(",");
        int red = Integer.parseInt(raw[0]);
        int green = Integer.parseInt(raw[1]);
        int blue = Integer.parseInt(raw[2]);
        int alpha = Integer.parseInt(raw[3]);
        // RETURN AS COLOR OBJECT
        return new Color(red, green, blue, alpha);
    }
}
