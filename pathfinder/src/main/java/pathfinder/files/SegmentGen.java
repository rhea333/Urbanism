package pathfinder.files;

import ca.mcmaster.cas.se2aa4.a2.io.Structs.Segment;

public class SegmentGen {

    int v1;
    int v2;

    public SegmentGen(int vertex1, int vertex2) {
        vertex1 = v1;
        vertex2 = v2;

    }

    public Segment segmentBuilder() {
        Segment v = Segment.newBuilder().setV1Idx(v1).setV2Idx(v2).build();
        return v;
    }
}
