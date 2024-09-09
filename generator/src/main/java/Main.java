import ca.mcmaster.cas.se2aa4.a2.generator.IrregMeshGen;
import ca.mcmaster.cas.se2aa4.a2.generator.MeshGen;
import ca.mcmaster.cas.se2aa4.a2.io.MeshFactory;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Mesh;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        options.addOption("P", true, "Indicates number of Polygons");
        options.addOption("I", false, "Toggles Irregular Mesh");
        options.addOption("h", "help",  false, "Command information");
        options.addOption("R", true, "Indicates Number of times to Relax Irregular Mesh");

        //defaults to the regular mesh
        String regOrNot = "Irreg";
        //Default debug mode toggle state
        String debug = "debugOff";
        //Default number of Polygons
        String numPoly = "1000";
        //Default relaxations
        String defaultRelaxTimes = "200";


        // Default Values
        int userRelaxRequests = Integer.parseInt(defaultRelaxTimes);;
        int numOfPolygons = Integer.parseInt(numPoly);
        try {
            CommandLine commandline = parser.parse(options, args);
            // IRREGULAR MESH OPTION
            if (commandline.hasOption("I")) {
                regOrNot = "Irreg";
                if (commandline.hasOption("P")) {   // CUSTOM POLYGON AMOUNT OPTION
                    numPoly = commandline.getOptionValue("P");
                }
                if (commandline.hasOption("R")){    // CUSTOM RELAXATION AMOUNT OPTION
                    defaultRelaxTimes = commandline.getOptionValue("R");
                }
            }
            else if ((commandline.hasOption("P") || commandline.hasOption("R")) && !commandline.hasOption("I")){
                throw new ParseException("Tried to use P/R option without -I");   // THROW EXCEPTION TO SHOW ERROR/HELP MENU
            }
            if (commandline.hasOption("h")){    // HELP OPTION
                System.out.println("----------------------------------------------HELP MENU----------------------------------------------\n");
                System.out.println("Enter these commands to personalize the mesh you would like to create! \n" +
                                    "If no commands are entered, the default colourful mesh will appear.");
                System.out.println("-I ~~ This creates the default irregular mesh with 100 polygons.  \n" +
                        "-P xx ~~ (-I Option Needed) In place of xx enter a INTEGER to choose how many POLYGONS are created in the irregular mesh \n" +
                        "-R xx ~~ (-I Option Needed) In place of xx enter an INTEGER to choose the LEVEL OF RELAXATION of the irregular mesh. \n\n" +
                        " NOTE: To toggle debug mode for either type of Mesh, use -X in the visualizer command line!");
                System.exit(1);
            }
            try {   // THIS CATCHES ANY INPUT THATS NOT A INTEGER
                numOfPolygons = Integer.parseInt(numPoly);
                if (numOfPolygons < 0)
                    numOfPolygons = 0;
            } catch(Exception e) {
                throw new ParseException("P value is not an Integer");   // THROW EXCEPTION TO SHOW ERROR/HELP MENU
            }
            try {   // THIS CATCHES ANY INPUT THATS NOT A INTEGER
                userRelaxRequests = Integer.parseInt(defaultRelaxTimes);
                if (userRelaxRequests < 0)
                    userRelaxRequests = 0;
            } catch(Exception e) {
                throw new ParseException("R value is not an Integer");   // THROW EXCEPTION TO SHOW ERROR/HELP MENU
            }
        } catch (ParseException e) {
            System.out.println("----------------------------------------------ERROR MESSAGE----------------------------------------------\n");
            System.out.println("Please Re-Enter. An ERROR occured with your user input \""+e.getMessage()+"\". Use --help/-h");
            System.out.println("NOTE: To toggle debug mode for either type of Mesh, use -X in the visualizer command line!\n");
            System.out.println("----------------------------------------------ERROR MESSAGE----------------------------------------------\n");
        }

        //convert the number of polygons from string to int
        System.out.println("Please Standby... There are "+numOfPolygons+" Polygons being generated...");
        // Running the Irregular Mesh or Default Grid Mesh if given no option

        if (regOrNot.equals("Irreg")) {
            IrregMeshGen gen = new IrregMeshGen();
            Mesh myMesh = gen.generate(numOfPolygons, userRelaxRequests);
            MeshFactory factory = new MeshFactory();
            factory.write(myMesh, args[0]);
        } else {
            MeshGen gen = new MeshGen();
            Mesh myMesh = gen.generate();
            MeshFactory factory = new MeshFactory();
            factory.write(myMesh, args[0]);
        }

        // Print Out Current Generator
        System.out.println("---------------------------------------CURRENT MESH GENERATED--------------------------------------------");
        System.out.print("Mode: "+regOrNot+"\t\t\t"+"Number of Polygons: "+numOfPolygons);
        if (regOrNot.equals("Irreg")){
            System.out.println("\t\t\tNumber of Relaxations: "+userRelaxRequests);
        } else {
            System.out.println();
        }
    }

}

// java -jar generator.jar sample.mesh