package island.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.text.DecimalFormat;
import java.util.List;


public class Aquifers{
    //INITIALIZE LISTS AND VARIABLES SO THAT ONE UPDATE WILL CHANGE ALL USAGES
    List<Structs.Polygon> polygonList;
    List<Integer> heightPoints;
    List<Double> humidity;
    int aquaStartIdx;
    int aquaNum;
    double soilPercent;

    DecimalFormat precision  = new DecimalFormat("0.00");

    //METHOD TO SET ATTRIBUTES TO AQUIFER OBJECTS CREATED IN OTHER CLASSES
    public void generate(List<Integer> hP, int startIdx, List<Structs.Polygon> polygons,  List<Double> humidityList, double soilPer, int numAquifers){
        heightPoints = hP;
        aquaStartIdx = startIdx;
        polygonList = polygons;
        humidity = humidityList;
        soilPercent = soilPer;
        aquaNum = numAquifers;

        createAquifers(aquaNum, aquaStartIdx);

    }

    //SET CERTAIN POLYGONS TO BE AQUIFERS
    private void createAquifers(int aquaNum, int startIndexA){
        //LOOP AS MANY TIMES AS THE NUMBER OF AQUIFERS THE USER WANTS
        for (int i = 0; i < aquaNum; i ++){
            //START WITH THE POLYGON THE USER CHOOSES AND SEQUENTIALLY SET EVERY FOLLOWING POLYGON TO BE AN AQUIFER
            //MODULUS THE INDEX BY THE LENGTH OF THE LIST TO MAKE SURE THAT THE INDEX OF THE POLYGON CHOSEN DOES NOT EXCEED THE LENGTH OF THE LIST
            int polyIndex = (startIndexA + i) % heightPoints.size();
            int validPolyId = heightPoints.get(polyIndex);
//            colorPolygon(102, 178,255,255, validPolyId);
            addAquaHumidity(validPolyId);
        }
        aquaStartIdx = startIndexA;

    }

    //SET HUMIDITY OF AQUIFER AND HOW IT AFFECTS ITS NEIGHBOURING POLYGONS
    private void addAquaHumidity(int aquaPoly){
        Structs.Polygon poly = polygonList.get(aquaPoly);
        //MAKE SURE HUMIDITY IS ALSO AFFECTED BY THE ISLAND'S SOIL TYPE
        double humidityValAqua = Double.parseDouble(precision.format(humidity.get(aquaPoly)+150*soilPercent));
        humidity.set(aquaPoly, humidityValAqua);
        for (Integer n : poly.getNeighborIdxsList()){
            double humidityValNeigbours = Double.parseDouble(precision.format(humidity.get(n)+100*soilPercent));
            //ADD TO HUMIDITY LIST SUCH THAT INDEX IS THE SAME AS THE POLYGON IT IS AFFECTING
            humidity.set(n,humidityValNeigbours);
        }
    }
}
