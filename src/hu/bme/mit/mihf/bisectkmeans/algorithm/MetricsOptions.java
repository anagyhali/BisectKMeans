package hu.bme.mit.mihf.bisectkmeans.algorithm;

import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

public enum MetricsOptions implements MetricsInterface {
    EUCLIDEAN("Euklideszi") {
        @Override
        public double distance(DataModel.GraphInfo p1, DataModel.GraphInfo p2) {
            double retVal = 0;
            for (int i = 0; i < p1.numberOfVertices.length; i++) {
                retVal += (p2.numberOfVertices[i] - p1.numberOfVertices[i]) * (p2.numberOfVertices[i] - p1.numberOfVertices[i]);
            }
            return Math.sqrt(retVal);
        }
    },
    COSINUS("Koszinusz") {
        @Override
        public double distance(DataModel.GraphInfo p1, DataModel.GraphInfo p2) {
            double a = 0, b1 = 0, b2 = 0;

            for (int i = 0; i < p1.numberOfVertices.length; i++) {
                a += p2.numberOfVertices[i] * p1.numberOfVertices[i];

                b1 += p2.numberOfVertices[i] * p2.numberOfVertices[i];

                b2 += p1.numberOfVertices[i] * p1.numberOfVertices[i];
            }
            
            if (b1 == 0 && b2 == 0)
                return 0;

            return a / (Math.sqrt(b1) * Math.sqrt(b2));
        }
    };

    private String label;

    MetricsOptions(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
