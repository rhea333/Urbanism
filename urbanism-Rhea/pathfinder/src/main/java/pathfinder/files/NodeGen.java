package pathfinder.files;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Vertex;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

public class NodeGen {

    double x;
    double y;
    double e;

    //create 1 object and create for loop and call builder method in a for loop with the vertexes from islandgen
    public NodeGen(double xCoord, double yCoord){
        xCoord = x;
        yCoord = y;
        //elevation = e;

    }

    public Vertex vertexBuilder(){
        Vertex v = Vertex.newBuilder().setX(x).setY(y).build();
        return v;
    }

}
