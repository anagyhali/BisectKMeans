package hu.bme.mit.mihf.bisectkmeans.model;

import java.util.ArrayList;

/**
 * Created by Tamás on 2015.11.15..
 */
public class DataModel extends ArrayList<DataModel.GraphInfo> {
    public static class GraphInfo {
        public int pointID;
        // the array's index is the degree
        public int[] numberOfVertices = new int[13];
    }
}
