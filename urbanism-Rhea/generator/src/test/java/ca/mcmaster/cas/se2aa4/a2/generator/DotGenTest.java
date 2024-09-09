package ca.mcmaster.cas.se2aa4.a2.generator;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DotGenTest {

    @Test
    public void meshIsNotNull() {
        MeshGen generator = new MeshGen();
        Structs.Mesh aMesh = generator.generate();
        assertNotNull(aMesh);
    }
    @Test
    public void negativeMeshes() {
        IrregMeshGen generator = new IrregMeshGen();
        Structs.Mesh aMesh = generator.generate(-1,2);
        assertNotNull(aMesh);
    }

    @Test
    public void largeMeshes() {
        IrregMeshGen generator = new IrregMeshGen();
        Structs.Mesh aMesh = generator.generate(100,2);
        assertNotNull(aMesh);
    }
    @Test
    public void negativeRelaxation() {
        IrregMeshGen generator = new IrregMeshGen();
        Structs.Mesh aMesh = generator.generate(10,-2);
        assertNotNull(aMesh);
    }

}
