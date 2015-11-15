package hu.bme.mit.mihf.bisectkmeans.algorithm;

public enum StartVectorOptions {
    RANDOM("Véletlen"),
    RANDOM_FROM_DATA("Véletlenül a tesztadatokból"),
    CENTER_OF_DATA("A tesztadatok közepe");

    private String label;

    StartVectorOptions(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
