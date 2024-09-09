package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.awt.*;
import java.util.*;
import java.util.List;

//IMPLEMENT ABSTRACT CLASS TO BE ABLE TO COLOUR POLYGONS
public class Biomes implements IslandColour{

    //INITIALIZE LISTS AND VARIABLES TO BE ABLE TO HAVE MULTIPLE USAGES AND AUTO-UPDATE EACH ONE
    List<Double> elevations;
    List<Double> humidities;
    List<Structs.Polygon> polygonList;
    List<Integer> IslandBlocks;
    List<Integer> LakeIdxs;
    int biome;
    public HashMap<Integer,String> biomeMap;

    //if island polygon is not a lake, get the elevation

    //METHOD TO SET ATTRIBUTES TO BIOME OBJECTS CREATED IN OTHER CLASSES
    public void generate(List<Double> elev, List<Integer> IsleBlocks, List<Integer> LakeBlocks, List<Double> humidity, List<Structs.Polygon> polyList){
        elevations = elev;
        IslandBlocks = IsleBlocks;
        LakeIdxs = LakeBlocks;
        humidities = humidity;
        polygonList = polyList;

        BiomeType(elevations, humidities, IslandBlocks, LakeIdxs);

    }

    //SET SEED VALUE OF EACH BIOME USING A HASHMAP
    public String numToBiome(int num){
        biomeMap = new HashMap<>();
        biomeMap.put(0,"Desert");
        biomeMap.put(1,"Savana");
        biomeMap.put(2,"Tropical");
        biomeMap.put(3,"Grassland");
        biomeMap.put(4,"Deciduous");
        biomeMap.put(5,"TemperateRain");
        biomeMap.put(6,"Taiga");
        biomeMap.put(7,"Tundra");

        return biomeMap.get(num);
    }
    public String biomeCheck(String Biome){
        if (Biome.equals("")){
            return numToBiome(new Random().nextInt(0,8));
        }
        return Biome;
    }

    //OFFSETS INITIAL ELEVATION OF THE ISLAND DEPENDANT ON BIOME AS COLOUR DEPENDS ON THIS
    public double BiomeElevation(String Biome){
        //DEFAULT TO ZERO
        double elevation = 0;
        if (Biome.equals("Desert")){
            elevation = 10;
        }
        else if (Biome.equals("Savana")){
            elevation = 25;
        }
        else if (Biome.equals("Tropical")){
            elevation = 25;
        }
        else if (Biome.equals("Grassland")){
            elevation = 50;
        }
        else if (Biome.equals("Deciduous")){
            elevation = 100;
        }
        else if (Biome.equals("TemperateRain")){
            elevation = 100;
        }
        else if (Biome.equals("Taiga")){
            elevation = 150;
        }
        else if (Biome.equals("Tundra")){
            elevation = 200;
        }
    return elevation;

    }

    //OFFSETS HUMIDITY BASED ON CHOSEN BIOME
    public double BiomeHumidity(String Biome){
        double humidity = 0;
        if (Biome.equals("Desert")){
            humidity = 20;
            biome = 0;
        }
        else if (Biome.equals("Savana")){
            humidity = 175;
            biome = 1;
        }
        else if (Biome.equals("Tropical")){
            humidity = 330;
            biome = 2;
        }
        else if (Biome.equals("Grassland")){
            humidity = 70;
            biome = 3;
        }
        else if (Biome.equals("Deciduous")){
            humidity = 140;
            biome = 4;
        }
        else if (Biome.equals("TemperateRain")){
            humidity = 240;
            biome = 5;
        }
        else if (Biome.equals("Taiga")){
            humidity = 100;
            biome = 6;
        }
        else if (Biome.equals("Tundra")){
            humidity = 10;
            biome = 7;

        }
        return humidity;

    }


    //COLOUR POLYGONS BASED ON BIOME TYPE SELECTED
    private void BiomeType(List<Double> elev, List<Double> humidity, List<Integer> IsleBlocks, List<Integer> LakeBlocks){
        //LOOP THROUGH ALL ISLAND BLOCKS
        for (Integer i: IsleBlocks){
            //DO NOT RECOLOUR ISLAND BLOCKS
            if (!LakeBlocks.contains(i)){
                //CLASSIFY POLYGONS INTO BIOMES BASED ON THEIR ELEVATION AND HUMIDITY
                double height = elev.get(i);
                double humid = humidity.get(i);
                //DESERT
                if (0 <= humid && humid <= 50 && 0 <= height && height < 175){
                    colorPolygon(255,255,221,255, i);
                    new Color(255,255,221,255);
                }
                //SAVANA
                else if (50 < humid && humid < 275 && 0 <= height && height < 50) {
                    colorPolygon(138,138,77,255, i);
                    new Color(138, 138, 77,255);
                }
                //TROPICAL RAIN FOREST
                else if (275 <= humid && 0 <= height && height < 50) {
                    colorPolygon(160,255,30,255, i);
                    new Color(160, 255, 30,255);
                }
                //GRASSLAND
                else if (50 < humid && humid < 100 && 50 <= height && height < 175) {
                    colorPolygon(206,112,44,255, i);
                    new Color(206,112,44,255);
                }
                //DECIDUOUS
                else if (100 <= humid && humid <= 225 && 50 <= height && height < 125) {
                    colorPolygon(49,113,79,255, i);
                    new Color(49,113,79,255);
                }
                //TEMPERATE RAIN FOREST
                else if (225 < humid && 50 <= height && height <= 125) {
                    colorPolygon(163,255,181,255, i);
                    new Color(163, 255, 181,255);
                }
                //TAIGA
                else if (50 <= humid && 125 <= height && height <= 175) {
                    colorPolygon(0, 59, 3, 255, i);
                    new Color(0, 59, 3, 255);
                }
                //TUNDRA
                else if (0 <= humid && 150 > humid && 175 <= height) {
                    colorPolygon(238,254,255,255, i);
                    new Color(238, 254, 255,255);
                }
                //TUNDRA ICE
                else if (150 <= humid && 175 <= height) {
                    colorPolygon(184,217,255,255, i);
                    new Color(184, 217, 255,255);
                }
                else{
                    System.out.println("Humidity:"+humid+" Height:"+height);
                    colorPolygon(255,255,255,255,i);
                }


                }


            }

        }



    //COLOUR THE POLYGON
    @Override
    public void colorPolygon(int red, int green, int blue, int alpha, int index){
        Structs.Polygon poly = polygonList.get(index);
        Structs.Property color = Structs.Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue+ "," + alpha).build();
        Structs.Polygon colored = Structs.Polygon.newBuilder(poly).addProperties(color).build();
        polygonList.set(index, colored);
    }

}





