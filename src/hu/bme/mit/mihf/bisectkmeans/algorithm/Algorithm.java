package hu.bme.mit.mihf.bisectkmeans.algorithm;

import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

import java.util.ArrayList;
/**
 * Created by Tamás on 2015.11.15..
 */
public class Algorithm extends Thread {
    private MetricsOptions metrics;
    private StartVectorOptions startVector;
    private int numberOfClusters;
    private String stopStatement;
    private boolean stepMode;
    private DataModel initialModel;

    private AlgorithmObserver observer;

    private Object syncObject = new Object();

    private ArrayList<DataModel> clusters = new ArrayList<DataModel>();

    public Algorithm(MetricsOptions metrics, StartVectorOptions startVector, int numberOfClusters, String stopStatement, boolean stepMode, DataModel initialModel, AlgorithmObserver observer) {
        this.metrics = metrics;
        this.startVector = startVector;
        this.numberOfClusters = numberOfClusters;
        this.stopStatement = stopStatement;
        this.stepMode = stepMode;
        this.initialModel = initialModel;
        this.observer = observer;
    }

    @Override
    public void run() {
        try {
            synchronized (syncObject) {
                //TODO initialize

                // TODO stop statement
                while (clusters.size() < numberOfClusters) {

                    //TODO logic
                    Thread.sleep(1000);

                    clusters.add(initialModel);

                    // TODO stop statement
                    if (stepMode && clusters.size() < numberOfClusters) {
                        observer.algorithmPaused(clusters);
                        syncObject.wait();
                    }
                }

                observer.algorithmFinished(clusters);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            observer.errorOccurred("Algorithm Interrupted");
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

