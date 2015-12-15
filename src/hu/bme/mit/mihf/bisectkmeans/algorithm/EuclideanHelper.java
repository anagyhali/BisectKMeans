package hu.bme.mit.mihf.bisectkmeans.algorithm;


import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

public class EuclideanHelper {

    public static DataModel.GraphInfo getMax(DataModel model) {
        DataModel.GraphInfo retVal = new DataModel.GraphInfo();

        for (int i = 0; i < retVal.numberOfVertices.length; i++) {
            for (DataModel.GraphInfo point: model) {
                retVal.numberOfVertices[i] = retVal.numberOfVertices[i] < point.numberOfVertices[i] ? point.numberOfVertices[i] : retVal.numberOfVertices[i];
            }
        }

        return retVal;
    }

    public static DataModel.GraphInfo calculateCentroid(DataModel model) {
        DataModel.GraphInfo centroid = new DataModel.GraphInfo();

        for (DataModel.GraphInfo p : model) {
            for (int i = 0; i < centroid.numberOfVertices.length; i++) {
                centroid.numberOfVertices[i] += p.numberOfVertices[i];
            }
        }

        for (int i = 0; i < centroid.numberOfVertices.length; i++) {
            centroid.numberOfVertices[i] /= model.size();
        }

        return centroid;
    }

    public static double sumOfSquaredErrors(DataModel model, MetricsInterface metrics) {
        double sse = 0;
        for (DataModel.GraphInfo p : model) {
            sse += metrics.distance(model.getCentroid(), p);
        }
        return sse;
    }

    public static DataModel.GraphInfo opposite(DataModel.GraphInfo origin, DataModel.GraphInfo p1) {
        DataModel.GraphInfo p2 = new DataModel.GraphInfo();

        for (int i = 0; i < origin.numberOfVertices.length; i++) {
            p2.numberOfVertices[i] = 2 * origin.numberOfVertices[i] - p1.numberOfVertices[i];
        }
        return p2;
    }
}
