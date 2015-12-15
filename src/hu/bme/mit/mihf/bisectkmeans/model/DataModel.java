package hu.bme.mit.mihf.bisectkmeans.model;

import hu.bme.mit.mihf.bisectkmeans.algorithm.MetricsInterface;

import java.util.ArrayList;


public class DataModel extends ArrayList<DataModel.GraphInfo> {
    public DataModel(GraphInfo centroid) {
        this.centroid = centroid;
    }

    public DataModel() {
        centroid = new GraphInfo();
    }

    public static class GraphInfo {
        public int pointID;
        // the array's index is the degree
        public float[] numberOfVertices = new float[13];
    }

    private GraphInfo centroid;

    public GraphInfo getCentroid() {
        return centroid;
    }

    public void setCentroid(GraphInfo centroid) {
        this.centroid = centroid;
    }
}
