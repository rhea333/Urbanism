package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class IslandShapes implements IslandColour{
    //instantiate attributes of class
    Structs.Mesh aMesh;
    List<Structs.Polygon> polygonList;
    List<Structs.Vertex> vertexList;

    //Method that generates the correct island shape based on the users shape input/the seed/the randomized island shape
    public void islandSelector(int shapeSeed, Structs.Mesh mesh, List<Structs.Vertex> vertexes, List<Structs.Polygon> polygons){

        //Gets the attributes passed from IslandGen and sets the class's attributes to them
        aMesh = mesh;
        vertexList = vertexes;
        polygonList = polygons;

        //The various island shapes were assigned different numbers
        //Based on what the shape input number is, a different shape will generate
        if (shapeSeed == 0){
            circleIsland(aMesh);
        }
        else if (shapeSeed == 1){
            ovalIsland(aMesh);
        }

        else if (shapeSeed ==2){
            moonIsland(aMesh);
        }
        else if (shapeSeed == 3){
            crossIsland(aMesh);
        }
        else{
            HeartIsland(aMesh);
        }
    }

    //Generates a circle shaped Island
    private void circleIsland(Structs.Mesh aMesh){
        //iterates through all polygons
        for (int i =0; i< aMesh.getPolygonsCount(); i++){
            Structs.Polygon poly = polygonList.get(i);
            Structs.Vertex centroid = vertexList.get(poly.getCentroidIdx());
            //Determines the distance from the centroid to the center of the map
            double x = centroid.getX();
            double y = centroid.getY();
            double distance = Math.sqrt(Math.pow(x-255,2)+Math.pow(y-255,2));

            //If the polygon centroid is within a radius of 200 units from the center, colour it as land
            if (distance < 200){
                colorPolygon(253, 255,208,255, i);
                new Color(253,255,208,255);
            }

            //If not then colour it blue for the ocean
            else{
                colorPolygon( 35, 85,138,255, i);
            }
        }
    }

    //generates a cross shaped island
    private void crossIsland(Structs.Mesh aMesh){
        //iterates through all the polygons in the mesh
        for (int i =0; i< aMesh.getPolygonsCount(); i++){
            Structs.Polygon poly = polygonList.get(i);
            Structs.Vertex centroid = vertexList.get(poly.getCentroidIdx());
            //gets the x and y coordinates of the polygon
            double x = centroid.getX();
            double y = centroid.getY();

            //Determines the distance from the centroid to the center of the map
            double distance = Math.sqrt(Math.pow(x-250,2)+Math.pow(y-250,2));

            //Creates a circle shapes island first
            if (distance < 200){
                colorPolygon(253, 255,208,255, i);
            }
            else{
                colorPolygon( 35, 85,138,255, i);
            }

            //Cuts out various oval shapes from the circle island to create a cross
            for (int j = 100; j<= 400; j+=150) {
                //Each If statement represents an oval shape to be removed from the top, bottom, left, and right of the circle
                //Each iteration, the oval to be removed gets shifted either to the right (for ovals being removed from the top or bottom) or down (for ovals being removed from the left or right)

                //If the centroid exists within any of the ovals then it is coloured blue for the ocean, essentially "cutting out" the oval from the island

                //Oval Shape to be removed from the top of the circle
                if (inOval(50, 100, j, 50, x, y) < 0) {
                    colorPolygon( 35, 85, 138, 255, i);
                }

                //Oval Shape to be removed from the bottom of the circle
                if (inOval(50, 100, j, 450, x, y) < 0) {
                    colorPolygon(35, 85, 138, 255, i);
                }

                //Oval Shape to be removed from the left of the circle
                if (inOval(100, 50, 50, j, x, y) < 0) {
                    colorPolygon( 35, 85, 138, 255, i);
                }

                //Oval Shape to be removed from the right of the circle
                if (inOval(100, 50, 450, j, x, y) < 0) {
                    colorPolygon(35, 85, 138, 255, i);
                }

            }

        }
    }

    /*
    Checks if a polygon is within an oval
    a represents the width of the circle, b represents the height of the circle
    offsetX is the x value of the center of the oval
    offsetY is the y value of the center of the oval
    x and y are the coordinates of the polygon's centroid
    */
    private double inOval(int a,int b,int offsetX, int offsetY, double x, double y){
        //Checks if the centroid is within the oval created by the parameters a, b, offsetX, and offsetY
        //if result is less than 0 then the polygon is inside the oval
        double result = Math.pow(((x-offsetX)/a),2) + Math.pow(((y-offsetY)/b),2) -1;
        return result;
    }

    //Creates a moon shape island
    private void moonIsland(Structs.Mesh aMesh){
        //iterates through the polygons in the mesh
        for (int i =0; i< aMesh.getPolygonsCount(); i++){
            Structs.Polygon poly = polygonList.get(i);
            Structs.Vertex centroid = vertexList.get(poly.getCentroidIdx());

            //gets the x and y coordinates of the polygon
            double x = centroid.getX();
            double y = centroid.getY();

            //Determines the distance from two circles
            //The first circle is centered in the middle and is the Island that will be initially created
            //The second circle is the one that will be cut out from the original circle to create a moon shaped island
            double distance = Math.sqrt(Math.pow(x-250,2)+Math.pow(y-250,2));
            double distance1 = Math.sqrt(Math.pow(x-380,2)+Math.pow(y-250,2));

            //If the centroid is within the first circle but outside the second one the colour it as land
            if (distance < 200 && distance1 > 100){
                colorPolygon(253, 255,208,255, i);
            }
            //If not then colour it blue for the ocean
            else{
                colorPolygon(35, 85,138,255, i);
            }
        }
    }

    //Creates and oval shaped island
    private void ovalIsland(Structs.Mesh aMesh){
        int a = 200;//width of oval
        int b = 100;//height of oval

        //iterates through all the polygons in the mesh
        for (int i =0; i< aMesh.getPolygonsCount(); i++){
            Structs.Polygon poly = polygonList.get(i);
            Structs.Vertex centroid = vertexList.get(poly.getCentroidIdx());
            //gets the coordinates of the polygon
            double x = centroid.getX();
            double y = centroid.getY();
            //checks if polygon is inside the oval
            double result = Math.pow(((x-250)/a),2) + Math.pow(((y-250)/b),2) -1;

            //if result is less than 0 then the polygon is inside the oval and should be coloured as land
            if (result < 0) {
                colorPolygon(253, 255, 208, 255, i);
            }

            //if not colour it blue for the ocean
            else{
                colorPolygon(35, 85,138,255, i);
            }
        }
    }


    //creates a heart shaped island
    private void HeartIsland(Structs.Mesh aMesh){

        //iterates through the polygons in the mesh
        for (int i =0; i< aMesh.getPolygonsCount(); i++){
            Structs.Polygon poly = polygonList.get(i);
            Structs.Vertex centroid = vertexList.get(poly.getCentroidIdx());

            //The heart is created through created multiple smaller circles that overlap to create a heart shape
            //determines the distance between the polygon and all the smaller circles
            double x = centroid.getX();
            double y = centroid.getY();
            double distance = Math.sqrt(Math.pow(x-250,2)+Math.pow(y-300,2));
            double distance3 = Math.sqrt(Math.pow(x-250,2)+Math.pow(y-390,2));

            double distance1 = Math.sqrt(Math.pow(x-185,2)+Math.pow(y-230,2));
            double distance2 = Math.sqrt(Math.pow(x-315,2)+Math.pow(y-230,2));
            double distance4 = Math.sqrt(Math.pow(x-330,2)+Math.pow(y-290,2));
            double distance5 = Math.sqrt(Math.pow(x-160,2)+Math.pow(y-300,2));

            //if the polygon lies within any of the circles colour it as land
            if (distance < 100 | distance1 < 80 | distance2 < 80 | distance3 < 40 | distance4 < 40 | distance5 < 30){
                colorPolygon(253, 255,208,255, i);
            }

            //if not then colour the polygon blue for the ocean
            else{
                colorPolygon(35, 85,138,255, i);
            }
        }

    }


    //Interface method to colour the polygon
    @Override
    public void colorPolygon(int red, int green, int blue, int alpha, int index) {
        //colours the polygon and replaces it within the polygon list
        Structs.Polygon poly = polygonList.get(index);
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Structs.Polygon colored = Structs.Polygon.newBuilder(poly).addProperties(color).build();
        polygonList.set(index, colored);
    }



}
