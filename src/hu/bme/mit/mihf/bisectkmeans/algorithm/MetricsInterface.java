package hu.bme.mit.mihf.bisectkmeans.algorithm;


import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

public interface MetricsInterface{
    public double distance(DataModel.GraphInfo p1, DataModel.GraphInfo p2);
}
