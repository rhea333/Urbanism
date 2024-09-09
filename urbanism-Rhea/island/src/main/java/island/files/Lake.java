package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * This Class Lake is to create Lakes for the Mesh, while contributing moisture to nearby soil.
 */
public class Lake implements IslandColour {

    int lakeNum;
    int maxLakes;
    List<Double> humidity;
    List<Integer> heightPoints;

    List<Integer> lakeIdxs = new ArrayList<>();
    boolean isSeed;
    List<Structs.Polygon> polygonList;
    List<Integer> islandBlocks = new ArrayList<>();
    DecimalFormat precision  = new DecimalFormat("0.00");
    double soil;

    /**
     *
     * This method is the parent where it initiates values and calls methods needed to generateLakes
     */
    public void generateLakes(double soilPercent, List<Integer> iBlocks,boolean seed, int maximumLakes, int startIndexL, int lN, List<Double> humid,List<Integer> hPoints, List<Structs.Polygon> pList){
        lakeNum = lN;
        maxLakes = maximumLakes;
        humidity = humid;
        heightPoints = hPoints;
        polygonList = pList;
        isSeed = seed;
        islandBlocks = iBlocks;
        soil = soilPercent;

        // Calls the method to create the creates based on seed or randomly generated seed based on the maximumLakes and startIndex.
        createLakes(maximumLakes,startIndexL);
    }

    /**
     * CreateLakes will create lakes with varying sizes!
     * This method will also determine what number to use whether there is a seed or not.
     * @param maximumLakes
     * @param startIndexL
     */
    public void createLakes(int maximumLakes, int startIndexL){
        Random randNum = new Random();
        if(maximumLakes> 0){
            //int startIndexL = randNum.nextInt(heightPoints.size()); //put this in the generator in an if statement
            if (isSeed){    // IF THERE IS A SEED, USE THE NUMBER MATCHING TO THE SEED.
                lakeNum = maxLakes;
            }else {
                lakeNum = randNum.nextInt(maxLakes); // SINCE THIS A USER GIVEN "MAX" Number of Lakes, we can randomize how many based on their input, but save that random num.
            }
            // Look through the heightPoints list to check for valid islandBlocks
            for (int i = 0; i < lakeNum; i ++){
                int polyIndex = (startIndexL + i) % heightPoints.size();    // INDEX CHECKING
                int validPolyId  = heightPoints.get(polyIndex);
                lakeSizes(validPolyId);                                     // THIS WILL CREATE UNIQUE SIZES
            }
        }
    }

    /**
     * This method creates unique sizes of lake shapes by taking in a polygon identifier
     * @param polyIdx
     */
    private void lakeSizes(int polyIdx){
        lakeIdxs.add(polyIdx);      // Add the idx into the lakeIdx list to keep track of what polygons are lakes.
        colorPolygon(79, 156,255,255, polyIdx);     // Colors the polygon as a lake colour
        new Color(79, 156, 255);       // This is just to see what Color we are working with visually.
        addLakeHumidity(polyIdx);               // Add Humidity to the nearby soil
        List<Integer> neighbours;               // Neighbours for the polygon,
        Deque<Integer> stack = new ArrayDeque<>();      // This stack is to create uniques lake shapes, carrying indexes
        stack.add(polyIdx);                             // Add the polyIdx into stack
        int ranInt = 6;                                 // This is what I found to look best
        while (ranInt > 0 && !stack.isEmpty()){         // Looping through the ranInt & if there is still items in the stack
            int idxVal = stack.pop();                   // Current index from the stack
            if (idxVal != polyIdx){                     // Since we already made the growthIndex a lake, I dont want to recolor it
                lakeIdxs.add(idxVal);
                colorPolygon(79, 156,255,255, idxVal);
                addLakeHumidity(idxVal);
            }
            Structs.Polygon polyTest = polygonList.get(idxVal);
            neighbours = polyTest.getNeighborIdxsList();

            for (Integer i : neighbours){               // for all the neighbours of the lake, check to make sure its still a islandBlock
                if (islandBlocks.contains(i)){
                    boolean edge = false;
                    for (Integer j : polygonList.get(i).getNeighborIdxsList()){     // Check if the current block is an edge.
                        // This is to make sure a lake is never connecting to the Ocean, because that wouldnt be a lake
                        if (!islandBlocks.contains(j))
                            edge = true;
                    }
                    if (!edge)          // if it's not an edge block, add it to the stack
                        stack.add(i);   // Add the next polygon if valid lake object.
                }
            }
            // Increment Down
            ranInt -= 1;
        }
    }

    /**
     * This method adds humidity to the lake and to its surroundings.
     * @param lakePoly
     */
    private void addLakeHumidity(int lakePoly){
        Structs.Polygon poly = polygonList.get(lakePoly);       // This is the polygon object to get properties from.
        double humidityValLake = Double.parseDouble(precision.format(humidity.get(lakePoly)+150));  // Increase by 150
        humidity.set(lakePoly, humidityValLake);
        for (Integer n : poly.getNeighborIdxsList()){       // This checks the neighbours of the lake.
            Structs.Polygon neighbourPoly = polygonList.get(n);         // 1 Block away
            double humidityValNeigbours = Double.parseDouble(precision.format(humidity.get(n)+100*soil));   // Increase by 100
            humidity.set(n,humidityValNeigbours);
            for (Integer i : neighbourPoly.getNeighborIdxsList()){      // 2 Blocks away
                double doubleNeighbours = Double.parseDouble(precision.format(humidity.get(i)+50*soil));    // Increase by 50
                humidity.set(i,doubleNeighbours);                       // Sets the humidity the
            }
        }
    }

    /**
     * colorPolygon colors polygons based on the polygon identifier
     */
    @Override
    public void colorPolygon(int red, int green, int blue, int alpha, int index){
        Structs.Polygon poly = polygonList.get(index);
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Structs.Polygon colored = Structs.Polygon.newBuilder(poly).addProperties(color).build();
        polygonList.set(index, colored);
    }
}
