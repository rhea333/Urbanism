package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.text.DecimalFormat;
import java.util.*;

/**
 * This elevation Class is responsible for all of the elevation type processes.
 */
public class Elevation {
    int altStartIdx;
    List<Integer> heightPoints;
    List<Structs.Polygon> polygonList;
    List<Double> elevations;
    List<Integer> islandBlocks;
    int altType;
    DecimalFormat precision  = new DecimalFormat("0.00");

    /**
     * This method is the main parent being the only public to call generate to call the internal methods that only Elevation needs.
     * @param type
     */
    public void generate(int type, List<Double> elevate,int startIdx, List<Integer> hPoints, List<Structs.Polygon> pList, List<Integer> iBlocks){
        altStartIdx = startIdx;
        elevations = elevate;
        heightPoints = hPoints;
        polygonList = pList;
        islandBlocks = iBlocks;
        altType = type;
        // SELECTION OF ELEVATION BASED ON USER INPUT/SEED
        selectElevation(altType);
    }

    /**
     * This method chooses which method to run depending on the user input.
     * @param elevationNum
     */
    private void selectElevation(int elevationNum){
        if (elevationNum == 0){
            mountain(altStartIdx);
        }
        else if (elevationNum == 2){
            generateHilly(altStartIdx);
        }
    }

    /**
     * This method generates a Hilly terrain where there are hills and it
     * @param startIdx
     */
    private void generateHilly(int startIdx){
        // Have it incrementally do it with the seed
        for (int i = 0; i < heightPoints.size()/4; i++){
            Deque<Integer> deque = new ArrayDeque<>();
            Set<Integer> visited = new HashSet<>();
            int growthPoint = (startIdx + i) % heightPoints.size();
            int polyIdx = heightPoints.get(growthPoint);
            double hillHeight = 20.0;       // Size of top of the hill
            visited.add(polyIdx);
            deque.add(polyIdx);
            // Using a Breadth First Search, have a growth point and start sloping down as you move down neighbouring island Blocks
            while (!deque.isEmpty()){
                int idxVal = deque.removeFirst();
                Structs.Polygon poly = polygonList.get(idxVal);
                double val = elevations.get(idxVal) + hillHeight; // Get current value and add the hillHeight
                elevations.set(idxVal,Double.parseDouble(precision.format(val)));
                List<Integer> neighbourList = poly.getNeighborIdxsList();       // Using the neighbourlist, check if it is a valid island Block and if not visited
                for (Integer idx : neighbourList){
                    if (!visited.contains(idx) && islandBlocks.contains(idx)){  // If Valid islandBlock and have not been seen, add to Queue.
                        visited.add(idx);
                        deque.add(idx);
                    }
                }
                // Decrease Height every iteration to layer the hill levels.
                if (hillHeight > 0){
                    hillHeight = Double.parseDouble(precision.format(hillHeight-5.0));
                }
            }
        }
    }

    /**
     * This method generates the mountain terrain and starts at a pretty high elevation and basically acts like a very big hill with different slopes.
     * @param startIdx
     */
    private void mountain(int startIdx){
        Deque<Integer> deque = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        int polyIdx = heightPoints.get(startIdx);
        double mountainHeight = 100.0;
        visited.add(polyIdx);
        deque.add(polyIdx);
        // Using a Breadth First Search, have a growth point and start sloping down as you move down neighbouring island Blocks
        while (!deque.isEmpty()){
            int idxVal = deque.removeFirst();
            Structs.Polygon poly = polygonList.get(idxVal);
            elevations.set(idxVal,elevations.get(idxVal)+mountainHeight); // Get current value and add the mountainHeight
            List<Integer> neighbourList = poly.getNeighborIdxsList();   // Using the neighbourlist, check if it is a valid island Block and if not visited
            for (Integer idx : neighbourList){
                if (!visited.contains(idx) && islandBlocks.contains(idx)){ // If Valid islandBlock and have not been seen, add to Queue.
                    visited.add(idx);
                    deque.add(idx);
                }
            }
            // Decrease Height every iteration to layer the mountain levels.
            if (mountainHeight >0)
                mountainHeight = Double.parseDouble(precision.format(mountainHeight-0.2));
        }
    }
}
