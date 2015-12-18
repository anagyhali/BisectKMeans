package hu.bme.mit.mihf.bisectkmeans.inputloader;

import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

import java.io.*;


public class Loader extends Thread {

    private String inputPath;
    private FinishedEventHandler handler;

    public Loader(String inputPath, FinishedEventHandler handler) {
        this.inputPath = inputPath;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));

            String line = reader.readLine();

            if (line.compareToIgnoreCase("PointID\tV1\tV2\tV3\tV4\tV5\tV6\tV7\tV8\tV9\tV10\tV11\tV12\tV13") != 0) {
                handler.onError("Error: Input file - bad header");
                return;
            }

            DataModel model = new DataModel();

            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] split = line.split("\\s");
                    if (split.length != DataModel.DIMENSION + 1) {
                        handler.onError("Error: Input file - bad line (" + line + ")");
                        return;
                    }

                    DataModel.GraphInfo info = new DataModel.GraphInfo();

                    info.pointID = Integer.parseInt(split[0]);

                    for (int i = 1; i < DataModel.DIMENSION + 1; i++) {
                        info.numberOfVertices[i - 1] = Integer.parseInt(split[i]);
                    }
                    model.add(info);
                }
            }

            reader.close();

            handler.onLoadedSuccessFully(model);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            handler.onError(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            handler.onError(e.getMessage());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            handler.onError(e.getMessage());
        }
    }

    public interface FinishedEventHandler {
        public void onLoadedSuccessFully(DataModel model);
        public void onError(String errorMessage);
    }
}
