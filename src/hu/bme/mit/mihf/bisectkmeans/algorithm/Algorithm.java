package hu.bme.mit.mihf.bisectkmeans.algorithm;

import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

import java.util.ArrayList;

public class Algorithm extends Thread {
    private static final double EPSILON = 0.000001;


    private final MetricsOptions metrics;
    private final StartVectorOptions startVector;
    private final int numberOfClusters;
    private final int minimumIterations;
    private final double maximumCentroidMovement;
    private final boolean stepMode;
    private final DataModel initialModel;

    private DataModel prevCentroids;

    private AlgorithmObserver observer;

    private Object syncObject = new Object();

    private ArrayList<DataModel> clusters = new ArrayList<DataModel>();

    public Algorithm(MetricsOptions metrics, StartVectorOptions startVector, int numberOfClusters, int minimumIterations, double maximumCentroidMovement, boolean stepMode, DataModel initialModel, AlgorithmObserver observer) {
        this.metrics = metrics;
        this.startVector = startVector;
        this.numberOfClusters = numberOfClusters;
        this.minimumIterations = minimumIterations;
        this.maximumCentroidMovement = maximumCentroidMovement + EPSILON;
        this.stepMode = stepMode;
        this.initialModel = initialModel;
        this.observer = observer;
    }

    @Override
    public void run() {
        try {

            clusters.add(initialModel);
            initialModel.setCentroid(EuclideanHelper.calculateCentroid(initialModel));

            boolean firstBisection = true;

            while (clusters.size() < numberOfClusters) {
                synchronized (syncObject) {
                    if (stepMode && !firstBisection) {
                        observer.algorithmPaused(clusters);
                        syncObject.wait();
                    }

                    DataModel biggestSSEcluster = null;
                    double biggestSSE = 0;

                    for (DataModel cluster : clusters) {
                        double currentSSE = EuclideanHelper.sumOfSquaredErrors(cluster, metrics);
                        if (currentSSE > biggestSSE) {
                            biggestSSE = currentSSE;
                            biggestSSEcluster = cluster;
                        }
                    }

                    DataModel.GraphInfo newClusterSeed1 = startVector.getStartVector(biggestSSEcluster);

                    DataModel.GraphInfo newClusterSeed2 = EuclideanHelper.opposite(biggestSSEcluster.getCentroid(), newClusterSeed1);

                    ArrayList<DataModel> prevClusters = clusters;

                    clusters = new ArrayList<DataModel>();

                    clusters.add(new DataModel(newClusterSeed1));
                    clusters.add(new DataModel(newClusterSeed2));

                    for (DataModel cluster : prevClusters) {
                        if (cluster != biggestSSEcluster) {
                            clusters.add(new DataModel(cluster.getCentroid()));
                        }
                    }

                    if (firstBisection) firstBisection = false;

                    double currentCentroidMovement;

                    classifyPoints();
                    currentCentroidMovement = recalculateCentroids();

                    for (int i = 0; i < minimumIterations - 1; i++) {
                        if (stepMode) {
                            observer.algorithmPaused(clusters);
                            syncObject.wait();
                        }
                        prevClusters = clusters;
                        clusters = new ArrayList<DataModel>();
                        for (DataModel cluster : prevClusters) {
                            clusters.add(new DataModel(cluster.getCentroid()));
                        }
                        classifyPoints();
                        currentCentroidMovement = recalculateCentroids();
                    }

                    if (maximumCentroidMovement > 0) {
                        while (currentCentroidMovement > maximumCentroidMovement) {
                            if (stepMode) {
                                observer.algorithmPaused(clusters);
                                syncObject.wait();
                            }
                            prevClusters = clusters;
                            clusters = new ArrayList<DataModel>();
                            for (DataModel cluster : prevClusters) {
                                clusters.add(new DataModel(cluster.getCentroid()));
                            }

                            classifyPoints();
                            currentCentroidMovement = recalculateCentroids();
                        }
                    }
                }
            }

            observer.algorithmFinished(clusters);
        } catch (InterruptedException e) {
            e.printStackTrace();
            observer.errorOccurred("Algorithm Interrupted");
        }
    }

    private double recalculateCentroids() {
        double currentCentroidMovement = 0;

        for (DataModel cluster : clusters) {
            DataModel.GraphInfo newCentroid = EuclideanHelper.calculateCentroid(cluster);

            double centroidDistance = metrics.distance(cluster.getCentroid(), newCentroid);

            currentCentroidMovement = currentCentroidMovement < centroidDistance ? centroidDistance : currentCentroidMovement;

            cluster.setCentroid(newCentroid);
        }
        return currentCentroidMovement;
    }

    private void classifyPoints() {
        for (DataModel.GraphInfo point : initialModel) {
            DataModel closestCluster = clusters.get(0);
            double closestClusterDistance = metrics.distance(closestCluster.getCentroid(), point);

            for (int i = 1; i < clusters.size(); i++) {
                double currentDistance = metrics.distance(clusters.get(i).getCentroid(), point);
                if (closestClusterDistance > currentDistance) {
                    closestCluster = clusters.get(i);
                    closestClusterDistance = currentDistance;
                }
            }

            closestCluster.add(point);
        }
    }

    public void nextStep() {
        synchronized (syncObject) {
            syncObject.notify();
        }
    }

    public interface AlgorithmObserver {
        public void algorithmFinished(ArrayList<DataModel> result);

        public void algorithmPaused(ArrayList<DataModel> partialResult);

        public void errorOccurred(String errorMessage);
    }
}

