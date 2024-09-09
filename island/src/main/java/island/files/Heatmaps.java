package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.util.ArrayList;
import java.util.List;

public class Heatmaps implements IslandColour{
    List<Structs.Polygon> polygonList;
    List<Double> humidity;
    List<Double> elevations;
    List<Integer> islandBlocks;

    //Calls the correct heat map generation method based on the user input
    public void selectMap(List<Structs.Polygon> polygons, List<Double> humidityList, List<Double> elevationlist ,List<Integer> islandBlocksList, String map){
        //The needed values to create the heatmap are passed as method parameters and set to the class variables to be accesed by the generation methods
        polygonList = polygons;
        humidity = humidityList;
        elevations = elevationlist;
        islandBlocks = islandBlocksList;

        if (map.equals("Elevation")){
            elevationMap();
        }

        else if(map.equals("Moisture")){
            moistureMap();
        }
    }

    //elevation heatmap
    private void elevationMap(){
        //Iterate through all the island polygons and assign them the correct shade of red depending on how high it is (higher the brighter the red)
        for (int j: islandBlocks){
            double i = elevations.get(j);
            //maps the elevation values from 0 to 300, to rgb values ranging from 0 to 25.5 which will later be multiplied by 10, breaking up the polygons into 10 different elevations brackets, that have a difference of 10 units between them
            int greenBlue = (int)((25.5/300.0)*(i));

            //If an elevation is greater than 300 and becomes greater the max value (25.5, but 25 as it is an integer) than it will be set to the max value
            if (greenBlue>25){
                greenBlue=25;
            }
            //do 255-10*greenBlue for the green and blue values so that the highest elevations are bright red instead of white
            colorPolygon(255, 255-(10*greenBlue), 255-(10*greenBlue), 255, j);
        }
    }

    //moisture heatmap
    private void moistureMap(){
        //Iterate through all the island polygons and assign them the correct shade of blue depending on how high it is (higher the brighter the red)
        for (int j: islandBlocks){
            double i = humidity.get(j);
            //maps the humidity values from 0 to 2000, to rgb values ranging from 0 to 25.5 which will later be multiplied by 10, breaking up the polygons into 10 different moisture brackets, that have a difference of 10 units between them
            int greenRed = (int)((25.5/2000.0)*(i));

            //If a humidity is greater than 2000 and becomes greater the max value (25.5, but 25 as it is an integer) than it will be set to the max value
            if (greenRed>25){
                greenRed=25;
            }

            //do 255-10*greenRed for the green and red values so that the highest elevations are bright blue instead of white
            colorPolygon(255-(10*greenRed), 255-(10*greenRed), 255, 255, j);
        }
    }

    @Override
    public void colorPolygon(int red, int green, int blue, int alpha, int index) {
        Structs.Polygon poly = polygonList.get(index);
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Structs.Polygon colored = Structs.Polygon.newBuilder(poly).addProperties(color).build();
        polygonList.set(index, colored);
    }
}
