package hu.bme.mit.mihf.bisectkmeans.algorithm;

import hu.bme.mit.mihf.bisectkmeans.algorithm.StartVectorInterface;
import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

import java.util.Random;

public enum StartVectorOptions implements StartVectorInterface {
    RANDOM("Véletlen") {
        @Override
        public DataModel.GraphInfo getStartVector(DataModel model) {

            DataModel.GraphInfo max = EuclideanHelper.getMax(model);
            DataModel.GraphInfo retVal = new DataModel.GraphInfo();

            Random rnd = new Random();

            for (int i = 0; i < max.numberOfVertices.length; i++) {
                retVal.numberOfVertices[i] = rnd.nextFloat() * max.numberOfVertices[i];
            }

            return retVal;
        }
    },
    RANDOM_FROM_DATA("Véletlenül a tesztadatokból") {
        @Override
        public DataModel.GraphInfo getStartVector(DataModel model) {
            return model.get((new Random()).nextInt(model.size()));
        }
    },
    CENTER_OF_DATA("A tesztadatok befoglaló hiperkockájának közepe") {
        @Override
        public DataModel.GraphInfo getStartVector(DataModel model) {
            DataModel.GraphInfo retVal = new DataModel.GraphInfo();
            DataModel.GraphInfo max = EuclideanHelper.getMax(model);
            for (int i = 0; i < max.numberOfVertices.length; i++) {
                retVal.numberOfVertices[i] = max.numberOfVertices[i] / 2;
            }
            return retVal;
        }
    };

    private String label;

    StartVectorOptions(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }

}
