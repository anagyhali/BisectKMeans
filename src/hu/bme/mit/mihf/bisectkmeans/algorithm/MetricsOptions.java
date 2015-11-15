package hu.bme.mit.mihf.bisectkmeans.algorithm;

public enum MetricsOptions {
    EUCLIDEAN("Euklideszi"),
    COSINUS("Koszinusz");

    private String label;

    MetricsOptions(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
