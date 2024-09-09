import ca.mcmaster.cas.se2aa4.a2.io.MeshFactory;
import ca.mcmaster.cas.se2aa4.a2.io.Structs;
import ca.mcmaster.cas.se2aa4.a2.visualizer.GraphicRenderer;
import ca.mcmaster.cas.se2aa4.a2.visualizer.MeshDump;
import ca.mcmaster.cas.se2aa4.a2.visualizer.SVGCanvas;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.awt.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        // Extracting command line parameters
        String input = args[0];
        String output = args[1];

        //Command Line Parsing
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("X", false, "Toggles Debug Mode");
        options.addOption("h", "help",false, "Help Menu, lists possible commands");

        //Defaults to Debug being off
        String debug = "debugOff";

        try{
            CommandLine commandline = parser.parse(options, args);
            if (commandline.hasOption("X")){
                debug = "debugOn";
            }
            if (commandline.hasOption("h")){
                System.out.println("You've reached the help menu!");
                System.out.println("Enter these commands to personalize the mesh you would like to visualize! \n" +
                        "If no commands are entered, the default mesh will appear.");
                System.out.println("-X ~~ Toggle debug mode. Works for both regular and irregular mesh. \n");
            }
        } catch (ParseException e) {
            System.out.println("----------------------------------------------ERROR MESSAGE----------------------------------------------\n");
            System.out.println("Please Re-Enter. ERROR RECEIVED: \""+e.getMessage()+"\". Use --help/-h");
            System.out.println("-X ~~ Toggle debug mode. Works for both regular and irregular mesh. \n");
            System.out.println("----------------------------------------------ERROR MESSAGE----------------------------------------------\n");
            System.exit(1);
        }

        // Getting width and height for the canvas
        Structs.Mesh aMesh = new MeshFactory().read(input);
        double max_x = Double.MIN_VALUE;
        double max_y = Double.MIN_VALUE;
        for (Structs.Vertex v: aMesh.getVerticesList()) {
            max_x = (Double.compare(max_x, v.getX()) < 0? v.getX(): max_x);
            max_y = (Double.compare(max_y, v.getY()) < 0? v.getY(): max_y);
        }
        // Creating the Canvas to draw the mesh
        Graphics2D canvas = SVGCanvas.build((int) 500, (int) 500);
        GraphicRenderer renderer = new GraphicRenderer();
        // Painting the mesh on the canvas
        renderer.render(aMesh, canvas, debug);
        // Storing the result in an SVG file
        SVGCanvas.write(canvas, output);
        // Dump the mesh to stdout
        MeshDump dumper = new MeshDump();
        dumper.dump(aMesh);
    }
}
// java -jar visualizer.jar ../island/island.mesh sample.svg
