package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class is responsible for all the river generation processes
 */
public class Rivers {
    List<Integer> islandBlocks = new ArrayList<>();
    List<Integer> islandVertices = new ArrayList<>();
    List<Double> vertexHeights;
    List<Double> elevations;
    List<Structs.Polygon> polygonList;
    List<Structs.Segment> segmentList;
    List<Structs.Vertex> vertexList;
    List<Double> humidity;
    List<List<Integer>> VTVRelations;
    List<List<Integer>> VTSRelations;
    List<List<Integer>> VTPRelations;
    int riverNum;
    int riverStartIdx;
    double soil;

    /**
     *
     * This generate method is the main parent where it is the only public class to access the generation portion.
     * This generates the vertex relations since polygons are assigned heights, vertexes are needed to be assigned relative to those polygon who share the vertex
     */
    public void generate(double soilPercent,int rM, int rSI, List<Structs.Polygon> pList ,List<Structs.Segment> sList,List<Structs.Vertex> vList, List<Double> e,List<Double> vH, List<Integer> iV, List<Integer> iB,List<Double> humid){
        islandBlocks = iB;
        elevations = e;
        vertexHeights = vH;
        islandVertices = iV;
        vertexList = vList;
        segmentList = sList;
        polygonList = pList;
        riverNum = rM;
        riverStartIdx = rSI;
        humidity = humid;
        soil = soilPercent;

        generateVTPRelation();      // Vertex to Polygon Relationship where it the vertex identifier holds the polygons IDs in a array that its attached to
        getVTVRelation();           // Vertex to Vertex, Meaning the nearby vertexes around it and what their IDs are.
        generateVertexHeights();    // Now since we have all the relations, Generate the heights to each vertex
        riverFlow();                // After we have all the heights, we generate the flow of rivers with riverflow()
    }

    /**
     * generate of vertexHeights will assign a value to each vertex in the island mesh, based on the polygons that share the vertex.
     */
    private void generateVertexHeights(){
        for (Integer vertIdx : islandVertices){     // Each vertexID
            List<Integer> polysAssociated = VTPRelations.get(vertIdx);      // Polygons for the vertex
            Double highestValue = null;
            for (Integer polyIdx : polysAssociated){        // For all the polygons in the list
                Double height = elevations.get(polyIdx);
                if (highestValue == null){                  // Getting the highest value of all the polygons.
                    highestValue = height;
                    continue;
                }
                if (height > highestValue){                 // If there is a higher value, use that one instead.
                    highestValue = height;
                }
            }
            vertexHeights.set(vertIdx,highestValue);        // Setting the values with vertex Heights
        }
    }

    /**
     * This method generate Vertex to Vertex relations, based on the vertexes connected with a segment.
     */
    private void getVTVRelation(){
        VTVRelations = new ArrayList<>(Collections.nCopies(vertexList.size(),new ArrayList<>()));
        VTSRelations = new ArrayList<>(Collections.nCopies(vertexList.size(),new ArrayList<>()));
        for (Structs.Segment s : segmentList){      // For each segment in the list
            int v1Idx = s.getV1Idx();
            int v2Idx = s.getV2Idx();
            int segID = segmentList.indexOf(s);
            // Check if they have been already been connected
            if (!VTVRelations.get(v1Idx).contains(v2Idx) && !VTSRelations.get(v1Idx).contains(segID)){
                // Vertex to Vertex Relations
                List<Integer> sList = new ArrayList<>(VTVRelations.get(v1Idx));
                sList.add(v2Idx);
                VTVRelations.set(v1Idx,sList);

                // Vertex to Segment Relations, With the same ID as the Vertex to match.
                List<Integer> vList = new ArrayList<>(VTSRelations.get(v1Idx));
                vList.add(segID);
                VTSRelations.set(v1Idx,vList);
            }
            if (!VTVRelations.get(v2Idx).contains(v1Idx) && !VTSRelations.get(v2Idx).contains(segID)){
                // Vertex to Vertex Relations
                List<Integer> sList = new ArrayList<>(VTVRelations.get(v2Idx));
                sList.add(v1Idx);
                VTVRelations.set(v2Idx,sList);

                // Vertex to Segment Relations, With the same ID as the Vertex to match.
                List<Integer> vList = new ArrayList<>(VTSRelations.get(v2Idx));
                vList.add(segID);
                VTSRelations.set(v2Idx,vList);
            }
        }
    }

    /**
     * Riverflow is the method that mainly prcoesses the river flow movement, utilzing searches of relations betwen Polygons, Segments and neighbour vertices
     */
    private void riverFlow(){
        Random rand = new Random();
        int minimumLengthRiver = 2; // This is the minimum length of a river, this will be the case of rivers being the same height, to visualize better
        for (int i = 0; i < riverNum; i++){     // Loop through how many rivers there are
            boolean notEnd = true;
            int randIslandVert = (riverStartIdx + i) % islandVertices.size();       // This the core of our unique seed, having the startIdx as the seed
            int id = islandVertices.get(randIslandVert);                // This ID is used to get the vertex in the island.
            int lengthOfRiver = 0;
            while (notEnd) {            // White the river is still flowing!
                List<Integer> vertNeighbours = VTVRelations.get(id);
                Integer nextSeg = null;         // Calculating the Segment to color, if still null that means we are the lowest point.
                Integer nextVert = null;        // Calculating the Vertex we are going to next, if still null that means we are the lowest point.
                double height = vertexHeights.get(id);
                for (int j = 0; j < vertNeighbours.size(); j++) {
                    double compare = vertexHeights.get(vertNeighbours.get(j));
                    // Searching to find if there is a vertex nearby that is lower than the one we are currently looking at.
                    if (vertexHeights.get(vertNeighbours.get(j)) < height) {
                        height = compare;
                        // If it is lower, set the next vertex and segment as the identifier respectfully
                        nextSeg = VTSRelations.get(id).get(j);
                        nextVert = vertNeighbours.get(j);
                    }
                }
                // We are at the lowest point and the minimum length of river is met.
                if (nextSeg == null && lengthOfRiver > minimumLengthRiver) {
                    colorVertex(vertexList.get(id),0,0,255,255);
                    break;
                }
                // Each is to check if we are flowing alongside a ocean tile, if we are we should stop since we are in the ocean already.
                boolean oceanEdge = false;
                List<Integer> polygonsAtVertex = VTPRelations.get(id);
                if (lengthOfRiver <= minimumLengthRiver){
                    int valueToChange = 0;
                    // Checking if nearby is a islandBlock, if its not, mark that it is a ocean Edge.
                    for (int k = 0; k < polygonsAtVertex.size();k++){
                        if (islandVertices.contains(id)){
                            valueToChange = VTSRelations.get(id).get(k);
                            nextVert = VTVRelations.get(id).get(k);
                        }
                        else{
                            oceanEdge = true;       // Found an ocean edge.
                            break;
                        }
                    }
                    // Chosen a segment to go to next.
                    nextSeg = valueToChange;
                }
                if (oceanEdge)
                    break;      // If current vertex is in the ocean, just stop flowing.
                // Get the segment object to start to change its colour
                Structs.Segment seg = segmentList.get(nextSeg);

                // If the segment is ALREADY a river, increase its thickness.
                if (extractColorString(seg.getPropertiesList()).equals("0,0,255,255")){
                    increaseThickness(seg);
                }
                else
                    colorSegment(seg,0,0,255,255);  // Color the segment blue
                new Color(0, 0, 255);       // This Colour here is to just visualize for us.

                // Increasing humidity to surrounding soils
                List<Integer> surroundingPolys = VTPRelations.get(id);
                for (Integer polyIdx : surroundingPolys){
                    humidity.set(polyIdx,humidity.get(polyIdx)+100*soil);       // Based on soil value, increase the humidity around it
                }
                id = nextVert;  // Change the id to the nextVertex ID
                lengthOfRiver += 1;     // Increase the length.
            }
        }
    }

    private void increaseThickness(Structs.Segment segment){
        double previousThickness = extractThickness(segment,segment.getPropertiesList());
        double val = previousThickness*1.5;
        Structs.Property thickness = Structs.Property.newBuilder().setKey("riverThickness").setValue(String.valueOf(val)).build();
        Structs.Segment thickened = Structs.Segment.newBuilder(segment).addProperties(thickness).build();
        segmentList.set(segmentList.indexOf(segment), thickened);
    }
    private Double extractThickness(Structs.Segment segment, List<Structs.Property> properties){
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


    private void colorSegment(Structs.Segment seg, int red, int green, int blue, int alpha){
        // Create new Property with "rgb_color" key and the rgb value as the value
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Structs.Segment colored = Structs.Segment.newBuilder(seg).addProperties(color).build();
        // Set the old segment in the list as the new one with color property
        segmentList.set(segmentList.indexOf(seg), colored);
    }

    private void generateVTPRelation(){
        VTPRelations = new ArrayList<>(Collections.nCopies(vertexList.size(),new ArrayList<>()));
        for (Integer polyIdx : islandBlocks){
            Structs.Polygon poly = polygonList.get(polyIdx);
            List<Integer> vertice = extractVertices(poly.getPropertiesList());
            for (Integer v : vertice){
                List<Integer> vList = VTPRelations.get(v);
                if (!vList.contains(polyIdx)){
                    List<Integer> vList2 = new ArrayList<>(vList);
                    vList2.add(polyIdx);
                    VTPRelations.set(v,vList2);
                }
            }
        }
    }

    /**
     * This returns a List of the vertices relationed to a polygon.
     * @param properties
     * @return
     */
    private List<Integer> extractVertices(List<Structs.Property> properties){
        String val = null;
        for(Structs.Property p: properties) {
            // TRY TO FIND THE RGB COLOR
            if (p.getKey().equals("vertices")) {
                val = p.getValue();
            }
        }
        if (val == null){       //
            System.out.println("NO VERTEX PROPERTY");
            return null;
        }
        // Returned in a Integer List, from a value of "0,1,2,3" in a string.
        String[] raw = val.split(",");
        List<Integer> rawInts = new ArrayList<>();
        for (int i =0; i< raw.length;i++){
            Integer value = Integer.parseInt(raw[i]);
            rawInts.add(value);
        }
        return rawInts;     // Return the Integer List
    }
    /**
     * This function replaces the vertex given as a parameter and adds a rgb color property to it based on rgb value params
     * @param vertex
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    private void colorVertex(Structs.Vertex vertex, int red, int green, int blue, int alpha){
        String colorCode = red + "," + green + "," + blue + "," + alpha;
        // Create new Property with "rgb_color" key and the rgb value as the value
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(colorCode).build();
        Structs.Vertex colored = Structs.Vertex.newBuilder(vertex).addProperties(color).build();
        // Set the old vertex in the list as the new one with color property
        vertexList.set(vertexList.indexOf(vertex), colored);
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
