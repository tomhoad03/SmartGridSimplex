import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class ManipulationDetection {
    public static void main(String[] args) {
        ArrayList<PricingCurve> trainingPricingCurves = new ArrayList<>(10000);
        ArrayList<PricingCurve> testingPricingCurves = new ArrayList<>(100);

        // Read in the training data
        try {
            File trainingFile = new File("TrainingData.txt");
            Scanner trainingReader = new Scanner(trainingFile);

            // For each line, create a pricing curve object from the training data
            while (trainingReader.hasNextLine()) {
                ArrayList<String> stringValues = new ArrayList<>(Arrays.asList(trainingReader.nextLine().split(",")));

                int normal = Integer.parseInt(stringValues.get(stringValues.size() - 1));
                stringValues.remove(stringValues.size() - 1);

                ArrayList<Double> doubleValues = new ArrayList<>();
                stringValues.stream().mapToDouble(Double::parseDouble).forEach(doubleValues::add);

                trainingPricingCurves.add(new PricingCurve(doubleValues, normal));
            }
            trainingReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Read in the testing data
        try {
            File testingFile = new File("TestingData.txt");
            Scanner testingReader = new Scanner(testingFile);

            // For each line, create a pricing curve object from the testing data
            while (testingReader.hasNextLine()) {
                ArrayList<String> stringValues = new ArrayList<>(Arrays.asList(testingReader.nextLine().split(",")));

                ArrayList<Double> doubleValues = new ArrayList<>();
                stringValues.stream().mapToDouble(Double::parseDouble).forEach(doubleValues::add);

                testingPricingCurves.add(new PricingCurve(doubleValues));
            }
            testingReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Calculate the centroid values of the normal and abnormal values
        ArrayList<Double> normalCumulativeValues = new ArrayList<>(24), abnormalCumulativeValues = new ArrayList<>(24);
        ArrayList<Double> normalCentroidValues = new ArrayList<>(24), abnormalCentroidValues = new ArrayList<>(24);

        for (PricingCurve trainingPricingCurve : trainingPricingCurves) {
            if (trainingPricingCurve.isNormal()) {
                populateCumulativeValues(normalCumulativeValues, trainingPricingCurve);
            } else {
                populateCumulativeValues(abnormalCumulativeValues, trainingPricingCurve);
            }
        }

        for (int i = 0; i < 24; i++) {
            normalCentroidValues.add(normalCumulativeValues.get(i) / 5000);
            abnormalCentroidValues.add(abnormalCumulativeValues.get(i) / 5000);
        }

                /*
        // Classify the testing data
        for (PricingCurve testingPricingCurve : testingPricingCurves) {
            double normalDistanceSquared = 0, abnormalDistanceSquared = 0;

            for (int i = 0; i < 24; i++) {
                normalDistanceSquared += Math.pow((testingPricingCurve.getPricingValues().get(i) - normalCentroidValues.get(i)), 2);
                abnormalDistanceSquared += Math.pow((testingPricingCurve.getPricingValues().get(i) - abnormalCentroidValues.get(i)), 2);
            }

            if (normalDistanceSquared < abnormalDistanceSquared) {
                testingPricingCurve.setNormal();
            }
        }*/

        // Calculate the k nearest neighbours
        int k = 20;

        for (PricingCurve testingPricingCurve : testingPricingCurves) {
            ArrayList<DistancePair> distances = new ArrayList<>();
            int count = 0;

            for (PricingCurve trainingPricingCurve : trainingPricingCurves) {
                double distanceSquared = 0;

                for (int i = 0; i < 24; i++) {
                    distanceSquared += Math.pow((testingPricingCurve.getPricingValues().get(i) - trainingPricingCurve.getPricingValues().get(i)), 2);
                }

                distances.add(new DistancePair(distanceSquared, count));
                count++;
            }

            distances.sort(Comparator.comparingDouble(DistancePair::getDistanceSquared));
            int normalCount = 0;

            for (int i = 0; i < k; i++) {
                if (trainingPricingCurves.get(distances.get(i).getIndex()).isNormal()) {
                    normalCount++;
                }
            }

            if (normalCount > ((k - 1) / 2)) {
                testingPricingCurve.setNormal();
            }
        }

        // Write the results to a file
        try {
            FileWriter myWriter = new FileWriter("NeighboursTestingData.txt");
            StringBuilder line = new StringBuilder();

            int normalCount = 0, abnormalCount = 0;
            for (int i = 0; i < 100; i++) {
                if (testingPricingCurves.get(i).isNormal()) {
                    normalCount++;
                } else {
                    abnormalCount++;
                }

                for (int j = 0; j < 24; j++) {
                    line.append(testingPricingCurves.get(i).getPricingValues().get(j)).append(",");
                }
                line.append(testingPricingCurves.get(i).isNormal() ? 0 : 1).append("\n");
            }

            myWriter.write(line.toString());
            myWriter.close();

            System.out.println("Normal: " + normalCount + ", Abnormal: " + abnormalCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void populateCumulativeValues(ArrayList<Double> cumulativeValues, PricingCurve trainingPricingCurve) {
        if (cumulativeValues.size() == 0) {
            for (int i = 0; i < 24; i++) {
                cumulativeValues.add(trainingPricingCurve.getPricingValues().get(i));
            }
        } else {
            for (int i = 0; i < 24; i++) {
                cumulativeValues.set(i, cumulativeValues.get(i) + trainingPricingCurve.getPricingValues().get(i));
            }
        }
    }
}
