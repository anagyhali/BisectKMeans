package hu.bme.mit.mihf.bisectkmeans.algorithm;


import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

public interface StartVectorInterface {

    public DataModel.GraphInfo getStartVector(DataModel model);
}
