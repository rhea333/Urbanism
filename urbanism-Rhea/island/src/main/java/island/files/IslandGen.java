package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Polygon;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Segment;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Vertex;

import ca.mcmaster.cas.se2aa4.a2.io.Structs.Mesh;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import pathfinder.files.PathGen;


abstract class IslandSeed{
    //instantiate abstract class variables
    int islandShape;
    int altType;
    int altStartIdx;
    boolean isSeed = false;
    int maxLakes;
    int lakeNum;
    int lakeStartIdx;
    int riverNum;
    int riverStartIdx;
    int aquaNum;
    int aquaStartIdx;
    int soilMoisture;
    int biome;
    int cities;

    double defaultBlockElev;
    double defaultHumidity;
}

public class IslandGen extends IslandSeed {
    List<Polygon> polygonList;
    List<Segment> segmentList;
    List<Vertex> vertexList;
    List<Double> elevations;
    List<Double> humidity;
    List<Double> vertexHeights;

    List <Integer> lakeIdxs = new ArrayList<>();

    String islandColor = "253,255,208,255";
    List<Integer> islandBlocks = new ArrayList<>();
    List<Integer> heightPoints = new ArrayList<>();
    List<Integer> islandVertices = new ArrayList<>();

    DecimalFormat precision  = new DecimalFormat("0.00");

    double soilPercent;


    //If the user inputs a seed then the island attributes are extracted from it
    private void seedDecoder(String seed){
        isSeed = true;
        //adjust seed length if it is not correct
        if (seed.length() < 18){
            //if seed is too short then add 0's to the end
            seed += "0".repeat(18-seed.length());
        }
        if (seed.length() > 18) {
            //if seed is too long, cut it off at 18 digits
            seed = seed.substring(0,18);
        }

        //Get island details from seed
        String[] seedDetails = seed.split("");
        islandShape = (Integer.parseInt(seedDetails[0]))%5; //modulo 5 as there are only 5 island shapes
        System.out.println(islandShape);
        altType = (Integer.parseInt(seedDetails[1]))%3;//modulo 3 as there are only elevation types
        altStartIdx = Integer.parseInt(seedDetails[2]+seedDetails[3]);
        maxLakes = Integer.parseInt(seedDetails[4] + seedDetails[5]);
        lakeNum = Integer.parseInt(seedDetails[4] + seedDetails[5]);
        lakeStartIdx = Integer.parseInt(seedDetails[6]+seedDetails[7]);
        riverNum = Integer.parseInt(seedDetails[8]+seedDetails[9]);
        riverStartIdx = Integer.parseInt(seedDetails[10]+seedDetails[11]);
        aquaNum = Integer.parseInt(seedDetails[12]+seedDetails[13]);
        aquaStartIdx = Integer.parseInt(seedDetails[14]+seedDetails[15]);
        soilMoisture = (Integer.parseInt(seedDetails[16]))%3;//modulo 3 as there are only 3 soil profiles
        biome = (Integer.parseInt(seedDetails[17]))%8;//modulo 8 as there are only 8 biomes

    }

    //If the user did not input a seed then get a
    private void getIslandShape(String shape){
        Random rand = new Random();
        //If the user did not input an Island shape in the cmd line then randomize one
        if (shape.equals("")){
            islandShape = (rand.nextInt(0, 5));
        }
        //if the user did input an island shape, find the corresponding integer
        else{
            HashMap<String, Integer> islandShapes = new HashMap<String, Integer>();
            islandShapes.put("Circle", 0);
            islandShapes.put("Oval", 1);
            islandShapes.put("Moon", 2);
            islandShapes.put("Cross", 3);
            islandShapes.put("Heart", 4);

            islandShape = islandShapes.get(shape);
        }

    }

    HashMap<Integer, String> numToShapes = new HashMap<Integer, String>();
    HashMap<Integer, String> numToElevate = new HashMap<Integer, String>();

    public void initalMaps(){
        numToShapes.put(0,"Circle");
        numToShapes.put(1,"Oval");
        numToShapes.put(2,"Moon");
        numToShapes.put(3,"Cross");
        numToShapes.put(4,"Heart");

        numToElevate.put(0,"Mountain");
        numToElevate.put(1,"Flat");
        numToElevate.put(2,"Hill");
    }

    //If user did not input a seed, get the elevation type attribute
    private void getElevationType(String elevType){
        Random rand = new Random();
        //if the user did not input an elevation type in the cmd line randomize one
        if (elevType.equals("")){
            altType = (rand.nextInt(0, 3));
        }

        //if the user did input an elevation type then get the corresponding integer value
        else{
            HashMap<String, Integer> elevationTypes = new HashMap<String, Integer>();
            elevationTypes.put("Mountain", 0);
            elevationTypes.put("Flat", 1);
            elevationTypes.put("Hill", 2);
            altType = elevationTypes.get(elevType);
        }

    }

    //get the polygon start index for elevation formations
    private void getElevationStartIdx(String elevationStartIdx){
        Random rand = new Random();
        int maxIdx = heightPoints.size()-1;

        //if the user did not input a seed and the starting index is empty
        if (elevationStartIdx.equals("")){
            //randomize the starting index, upperbound for the starting index is 100
            altStartIdx = rand.nextInt(0, maxIdx%100+1);//add one in case maxIdx is 0, preventing any bound errors
        }

        //If the user did input a seed
        else{
            //Ensuring that the starting index is not greater than the max possible index, if it is then set it to the max index
            int startIdx = Integer.parseInt(elevationStartIdx);
            if (startIdx>maxIdx){
                altStartIdx = maxIdx;
            }
            else{
                altStartIdx = startIdx;
            }
        }
    }

    //getting the max number of lakes
    private void getLakeNum(String maxNumLakes){
        Random rand = new Random();
        int maxLand = islandBlocks.size();
        //If user did provide a value for the number of lakes(seed) or max number of lakes (cmd line args)
        if (maxNumLakes.equals("")){
            //Randomize the maxLakes value, with an upperbound of 21
             maxLakes = rand.nextInt(1, maxLand%20+2);//min value of 1 so that there isn't an issue with randomizing the number of lakes, as this will be the upper bounds
        }

        //if the number of lakes (seed) or max number of lakes (cmd line args) was provided then ensure it is not greater than the number of land polygons
        else{
            maxLakes = Integer.parseInt(maxNumLakes);
            if (maxLakes>=maxLand){
                maxLakes = maxLand;
            }
        }
    }

    //Get starting index for the lake
    private void getLakeStartIdx(String lakeStartingIdx) {
        Random rand = new Random();
        int maxIdx = islandBlocks.size();

        //if the user did not input a value for the lakes starting index in the polygon list
        if (lakeStartingIdx.equals("")) {
            //randomize the starting index, with an upperbound of 100
            lakeStartIdx = rand.nextInt(0, maxIdx%100+1);//add one in case maxIdx is 0, preventing any bound errors
        }

        //if the user did provide a starting index for the lakes ensure that it is not greater than the max index of the list containing all the island polygons
        else {
            int startIdx = Integer.parseInt(lakeStartingIdx);
            if (startIdx > maxIdx) {
                lakeStartIdx = maxIdx -1;
            } else {
                lakeStartIdx = startIdx;
            }
        }
    }


    //Get the number of rivers
    private void getRiverNum(String numRivers){
        Random rand = new Random();
        int maxRivers = islandVertices.size();

        //if the user did not input a value for the number of rivers
        if (numRivers.equals("")){
            //randomize the number of rivers, with an upperbound of 20
            riverNum = rand.nextInt(0, maxRivers%20+1);//add one in case maxRivers is 0, preventing any bound errors
        }

        //if the user did input a value for the number of rivers then ensure that it is not greater than the number of island vertices
        else{
            int rivers = Integer.parseInt(numRivers);
            if (rivers>maxRivers){
                riverNum = maxRivers;
            }
            else{
                riverNum = rivers;
            }
        }
    }

    //get the starting index for the rivers
    private void getRiverStartIdx(String riverIdx) {
        Random rand = new Random();
        int maxIdx = islandVertices.size();

        //if the user did not provide a starting index for the rivers
        if (riverIdx.equals("")) {
            //randomize the starting index, with an upperbound of 100
            riverStartIdx = rand.nextInt(0, maxIdx%100+1);//add one in case maxIdx is 0, preventing any bound errors
        }

        //if the user did provide a starting index for the rivers (through the seed) then ensure that it is not greater than the number of island vertices
        else {
            int startIdx = Integer.parseInt(riverIdx);
            if (startIdx > maxIdx) {
                riverStartIdx = maxIdx -1;
            }

            else {
                riverStartIdx = startIdx;
            }
        }
    }

    //get the number of aquifers
    private void getAquiferNum(String maxNumAquifer){
        Random rand = new Random();
        int maxLand = islandBlocks.size();

        //if the user did not provide a value for the number of aquifers
        if (maxNumAquifer.equals("")){
            //randomize the number of aquifers, with an upperbound of 20
            aquaNum = rand.nextInt(0, maxLand%20+1);
        }

        //If the user did provide a value, ensure that it is not greater than the total amount of land
        else{
            int maxAqua = Integer.parseInt(maxNumAquifer);
            if (maxAqua>maxLand/20){
                aquaNum = maxLand/20;
            }
            else{
                aquaNum = maxAqua;
            }

        }
    }


    //get starting index for aquifer locations
    private void getAquiferStartIdx(String aquiferStartingIdx) {
        Random rand = new Random();
        int maxIdx = islandBlocks.size();

        //If the user did not provide a starting index then randomize it
        if (aquiferStartingIdx.equals("")) {
            aquaStartIdx = rand.nextInt(0, maxIdx%100+1);
        }

        //If the user did provide a starting index then ensure that it is not greater than the number of island polygons
        else {
            int startIdx = Integer.parseInt(aquiferStartingIdx);
            if (startIdx > maxIdx) {
                aquaStartIdx = maxIdx;
            } else {
                aquaStartIdx = startIdx;
            }
        }
    }

    //get the soil type
    private void getSoil( String soilType){
        Random rand = new Random();

        //if the user did not provide a soil type then randomize it
        if (soilType.equals("")){
            soilMoisture = (rand.nextInt(0, 3));
        }

        //if the user did provide a soil type then find the corresponding integer value
        else{
            HashMap<String, Integer> soilTypes = new HashMap<String, Integer>();
            soilTypes.put("Dry", 0);
            soilTypes.put("Average", 1);
            soilTypes.put("Wet", 2);
            soilMoisture = soilTypes.get(soilType);
        }
    }

    //Get the soil profile based on the soil type
    private void soilProfile(){
        //Soil will absorb different amounts of moisture depending on the soil type
        //Soil percent is the percent that the moisture will increase by
        if (soilMoisture == 0)
            soilPercent = 1.5;
        else if (soilMoisture == 1)
            soilPercent = 1.0;
        else if (soilMoisture == 2)
            soilPercent = 0.5;
    }



    public void defaultValues(Mesh aMesh){
        // Get old mesh details
        polygonList = new ArrayList<>(aMesh.getPolygonsList());
        segmentList = new ArrayList<>(aMesh.getSegmentsList());
        vertexList = new ArrayList<>(aMesh.getVerticesList());

        // Set new Stats
        int nPolygons = polygonList.size();
        int nVertices = vertexList.size();

        elevations = new ArrayList<Double>(Collections.nCopies(nPolygons, 0.0));
        humidity = new ArrayList<Double>(Collections.nCopies(nPolygons, defaultHumidity));
        vertexHeights = new ArrayList<>(Collections.nCopies(nVertices, 0.0));
        initalMaps();
    }

    public Mesh generate(Mesh aMesh,String seedInput, String shape, String elevType, String elevationStartIdx,String maxNumLakes, String lakeStartingIdx, String rivers, String riverStartingIdx, String aquifers, String aquiferStartingIdx, String soilSelect, String biomeSelect, String map, String numCities){

        if (!numCities.equals("")){
            cities =Integer.parseInt(numCities);
        }
        else{
            cities = 6;
        }

        //Create new island
        Biomes biomeGen = new Biomes();
        //If user input a seed
        if (!seedInput.equals("")){
            //Get the island attributes from the seed and then pass them through the methods to ensure that they are all in bounds
            seedDecoder(seedInput);
            String val = biomeGen.numToBiome(biome);
            defaultBlockElev = biomeGen.BiomeElevation(val);
            defaultHumidity = biomeGen.BiomeHumidity(val);
            defaultValues(aMesh);
            IslandShapes island = new IslandShapes();
            island.islandSelector(islandShape, aMesh, vertexList, polygonList);
            polygonList = island.polygonList;
            vertexList = island.vertexList;
            getIslandBlocks();
            getElevationStartIdx(String.valueOf(altStartIdx));
            getLakeStartIdx(String.valueOf(lakeStartIdx));
            getLakeNum(String.valueOf(maxLakes));
            getRiverNum(String.valueOf(riverNum));
            getRiverStartIdx(String.valueOf(riverStartIdx));
            getAquiferNum(String.valueOf(aquaNum));
            getAquiferStartIdx(String.valueOf(aquaStartIdx));
        }
        //If user did not input seed
        else{
            //Get all the details for the island attributes
            //If the user has provided some then ensure that they are all in bounds
            //Island attribute details not provided by the user will be randomized

            //Get the biome to get the default elevation and humidity for the island
            biomeSelect = biomeGen.biomeCheck(biomeSelect);
            defaultBlockElev = biomeGen.BiomeElevation(biomeSelect);
            defaultHumidity = biomeGen.BiomeHumidity(biomeSelect);
            defaultValues(aMesh);
            biome = biomeGen.biome;
            getIslandShape(shape);
            IslandShapes island = new IslandShapes();
            island.islandSelector(islandShape, aMesh, vertexList, polygonList);
            polygonList = island.polygonList;
            vertexList = island.vertexList;
            getIslandBlocks();
            getElevationStartIdx(elevationStartIdx);
            getElevationType(elevType);
            getLakeStartIdx(lakeStartingIdx);
            getLakeNum(maxNumLakes);
            getRiverNum(rivers);
            getRiverStartIdx(riverStartingIdx);
            getAquiferNum(aquifers);
            getAquiferStartIdx(aquiferStartingIdx);
            getSoil(soilSelect);
        }

        //Display General Island Info
        System.out.println("-----------------ISLAND INFO------------------");
        System.out.println("Biome: "+biomeGen.numToBiome(biome));
        System.out.println("Elevation: "+ numToElevate.get(altType));
        System.out.println("Shape: "+ numToShapes.get(islandShape));

        generateAttributes(biomeGen);
        seedOutput(seedInput);


        // Cities
        PathGen newCities = new PathGen();
        newCities.generate(islandVertices, vertexList, segmentList, cities);
        vertexList = newCities.vList;
        segmentList = newCities.sList;


        // Creating Heatmap if the User wants the map to be displayed as one
        Heatmaps heatMap = new Heatmaps();
        heatMap.selectMap(polygonList,humidity,elevations,islandBlocks,map);
        polygonList = heatMap.polygonList;
        return Mesh.newBuilder().addAllVertices(vertexList).addAllSegments(segmentList).addAllPolygons(polygonList).build();
    }


    private void generateAttributes(Biomes biomeGen){
        //Generate Cities and roads


        // Generate Elevation
        Elevation elevate = new Elevation();
        elevate.generate(altType,elevations,altStartIdx,heightPoints,polygonList,islandBlocks);
        elevations = elevate.elevations;
        altStartIdx = elevate.altStartIdx;

        soilProfile();
        //Lakes
        Lake lakes = new Lake();
        lakes.generateLakes(soilPercent,islandBlocks,isSeed,maxLakes,lakeStartIdx,lakeNum,humidity,heightPoints,polygonList);
        humidity = lakes.humidity;
        lakeIdxs = lakes.lakeIdxs;
        lakeNum = lakes.lakeNum;
        polygonList = lakes.polygonList;

        //Rivers
        Rivers river = new Rivers();
        river.generate(soilPercent,riverNum,riverStartIdx,polygonList,segmentList,vertexList,elevations,vertexHeights,islandVertices,islandBlocks,humidity);
        segmentList = river.segmentList;
        vertexList = river.vertexList;
        humidity = river.humidity;

        //Aquifers
        Aquifers aquifer = new Aquifers();
        aquifer.generate(heightPoints, aquaStartIdx, polygonList, humidity, soilPercent, aquaNum);
        humidity = aquifer.humidity;

        //Biomes
        biomeGen.generate(elevations, islandBlocks, lakeIdxs, humidity, polygonList);
        polygonList = biomeGen.polygonList;

    }
    private void seedOutput(String seedInput){
        //Seed generator

        //Create Lists for all the attribute values that can be double digits
        ArrayList<Integer> attributes = new ArrayList<Integer>();
        ArrayList<String> attributesStr = new ArrayList<String >();
        attributes.add(altStartIdx);
        attributes.add(lakeNum);
        attributes.add(lakeStartIdx);
        attributes.add(riverNum);
        attributes.add(riverStartIdx);
        attributes.add(aquaNum);
        attributes.add(aquaStartIdx);

        //if the seed was provided then output the same seed
        if (isSeed){
            System.out.println("-----------------SEED VALUE-------------------");
            System.out.println("SEED: "+seedInput);
            System.out.println("----------------------------------------------");

        }

        // If no seed provided
        else{
            //Go through double digit attribute list and add a leading 0 to all single digit numbers, storing them all in a List as Strings
            for (int i = 0; i < attributes.size(); i++){
                if (attributes.get(i) < 10){
                    attributesStr.add("0"+(attributes.get(i)));
                }
                else{
                    attributesStr.add(String.valueOf(attributes.get(i)));
                }
            }

            //Creating an 18 digit seed based on the island attributes that can reproduce the exact same island
            String seed = (String.valueOf(islandShape) + String.valueOf(altType) + attributesStr.get(0) + attributesStr.get(1) + attributesStr.get(2) + attributesStr.get(3)+ attributesStr.get(4) + attributesStr.get(5) + attributesStr.get(6) + String.valueOf(soilMoisture) + String.valueOf(biome));

            //Output the Seed
            System.out.println("-----------------SEED VALUE-------------------");
            System.out.println("SEED: "+seed);
            System.out.println("----------------------------------------------");
        }
    }

    //Gets all the polygons that make up the island
    private void getIslandBlocks(){
        for (int i = 0; i < polygonList.size();i++){
            Polygon poly = polygonList.get(i);
            //If the polygon is coloured as land
            if (extractColorString(poly.getPropertiesList()).equals(islandColor)){
                islandBlocks.add(i);
                elevations.set(i,defaultBlockElev);//give that polygon an elevation based on biome
            }
        }

        Collections.shuffle(islandBlocks,new Random(2));
        generateInnerIsland();
    }

    /**
     * This method extracts Vertices and returns as a List of Integer
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

    /**
     * This generates the inner island and this is useful as we can constrain river growth, lake growth & elevation growth to be NOT right beside the ocean polygons
     *
     */
    private void generateInnerIsland(){
        // heightPoint Island Blocks
        Set<Integer> verticesInIsland = new HashSet<>();
        for (Integer polyIdx : islandBlocks){       //  For each polygon get the neighbours and check if their neightbours are all soil
            boolean allNeighbourIslands = true;
            Polygon poly = polygonList.get(polyIdx);
            List<Integer> neighbourList = poly.getNeighborIdxsList();
            for (Integer j : neighbourList){
                if (!islandBlocks.contains(j)){     // If not a soil block, flag it.
                    allNeighbourIslands = false;
                }
            }
            if (allNeighbourIslands){       // Only if it is a valid Inner Island, add it to "heightPoints"
                heightPoints.add(polyIdx);
                List<Integer> islandVertexList = extractVertices(poly.getPropertiesList());
                if (islandVertexList != null)
                    for (Integer i : islandVertexList){     // For all the vertices in the valid polygon, add it to the islandVertice list
                        if (!verticesInIsland.contains(i)){     // To keep track of all the vertices as there might be duplicates, I use a set.
                            islandVertices.add(i);
                            verticesInIsland.add(i);
                        }
                    }
            }
        }
        Collections.shuffle(islandVertices,new Random(2));  // As we go in order adding polygons to the list, we want to avoid polygons exactly next to eachother, so we randomize it but the same seed.
    }

    /**
     * Extracts the colours from a property.
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
