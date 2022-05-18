import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class ManipulationDetection {
    public static void main(String[] args) {
        try {
            ArrayList<PricingCurve> trainingPricingCurves = readTrainingData();
            ArrayList<PricingCurve> testingPricingCurves = readTestingData();

            // Classification
            readInputData();
            checkTrainingAccuracy(20, trainingPricingCurves);
            calculateKNearestNeighbours(20, trainingPricingCurves, testingPricingCurves);
            printResults(testingPricingCurves);

            // Linear Programming
            createTestingLPs();
            simplexSolver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read in the training data
    private static ArrayList<PricingCurve> readTrainingData() throws Exception {
        File trainingFile = new File("TrainingData.txt");
        Scanner trainingReader = new Scanner(trainingFile);
        ArrayList<PricingCurve> trainingPricingCurves = new ArrayList<>();

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
        return trainingPricingCurves;
    }

    // Read in the testing data
    private static ArrayList<PricingCurve> readTestingData() throws Exception {
        File testingFile = new File("TestingData.txt");
        Scanner testingReader = new Scanner(testingFile);
        ArrayList<PricingCurve> testingPricingCurves = new ArrayList<>();

        // For each line, create a pricing curve object from the testing data
        while (testingReader.hasNextLine()) {
            ArrayList<String> stringValues = new ArrayList<>(Arrays.asList(testingReader.nextLine().split(",")));

            ArrayList<Double> doubleValues = new ArrayList<>();
            stringValues.stream().mapToDouble(Double::parseDouble).forEach(doubleValues::add);

            testingPricingCurves.add(new PricingCurve(doubleValues));
        }

        testingReader.close();
        return testingPricingCurves;
    }

    // Check the accuracy of the classification
    private static void checkTrainingAccuracy(int k, ArrayList<PricingCurve> trainingPricingCurves) {
        ArrayList<PricingCurve> trainingAccuracyCurves = new ArrayList<>();
        for (int i = 0; i < trainingPricingCurves.size(); i++) {
            trainingAccuracyCurves.add(i, new PricingCurve(trainingPricingCurves.get(i).getPricingValues()));
        }

        for (int i = 0; i < 10; i++) {
            ArrayList<PricingCurve> trainingPricingCurvesBag = new ArrayList<>();
            for (int j = i; j < trainingPricingCurves.size(); j += 10) {
                trainingPricingCurvesBag.add(new PricingCurve(trainingPricingCurves.get(j).getPricingValues()));
            }

            for (PricingCurve trainingAccuracyCurve : trainingAccuracyCurves) {
                ArrayList<DistancePair> distances = new ArrayList<>();
                int count = i;

                for (PricingCurve trainingPricingCurve : trainingPricingCurvesBag) {
                    double distanceSquared = 0.0;

                    for (int m = 0; m < 24; m++) {
                        distanceSquared += Math.pow((trainingAccuracyCurve.getPricingValues().get(m) - trainingPricingCurve.getPricingValues().get(m)), 2);
                    }

                    distances.add(new DistancePair(distanceSquared, count));
                    count += 10;
                }

                distances.sort(Comparator.comparingDouble(DistancePair::getDistanceSquared));
                int normalCount = 0;

                for (int n = 0; n < k; n++) {
                    if (trainingPricingCurves.get(distances.get(i).getIndex()).isNormal()) {
                        normalCount++;
                    }
                }

                trainingAccuracyCurve.addNormal(normalCount > ((k - 1) / 2));
            }
        }

        for (PricingCurve trainingAccuracyCurve : trainingAccuracyCurves) {
            int normalCount = 0;

            for (boolean normal : trainingAccuracyCurve.getNormalBag()) {
                if (normal) {
                    normalCount++;
                }
            }
            trainingAccuracyCurve.setNormal(normalCount >= 5);
        }

        int correctCount = 0;

        for (int i = 0; i < trainingAccuracyCurves.size(); i++) {
            if ((trainingPricingCurves.get(i).isNormal() && trainingAccuracyCurves.get(i).isNormal()) || (!trainingPricingCurves.get(i).isNormal() && !trainingAccuracyCurves.get(i).isNormal())) {
                correctCount++;
            }
        }
        System.out.println("Accuracy = " + correctCount + " / " + trainingAccuracyCurves.size() + " = " + (double) correctCount / (double) trainingAccuracyCurves.size());
    }

    // Calculate the k nearest neighbours
    private static void calculateKNearestNeighbours(int k, ArrayList<PricingCurve> trainingPricingCurves, ArrayList<PricingCurve> testingPricingCurves) {
        for (int i = 0; i < 10; i++) {
            ArrayList<PricingCurve> trainingPricingCurvesBag = new ArrayList<>();
            for (int j = i; j < trainingPricingCurves.size(); j += 10) {
                trainingPricingCurvesBag.add(new PricingCurve(trainingPricingCurves.get(j).getPricingValues()));
            }

            for (PricingCurve testingPricingCurve : testingPricingCurves) {
                ArrayList<DistancePair> distances = new ArrayList<>();
                int count = i;

                for (PricingCurve trainingPricingCurve : trainingPricingCurvesBag) {
                    double distanceSquared = 0.0;

                    for (int m = 0; m < 24; m++) {
                        distanceSquared += Math.pow((testingPricingCurve.getPricingValues().get(m) - trainingPricingCurve.getPricingValues().get(m)), 2);
                    }

                    distances.add(new DistancePair(distanceSquared, count));
                    count += 10;
                }

                distances.sort(Comparator.comparingDouble(DistancePair::getDistanceSquared));
                int normalCount = 0;

                for (int n = 0; n < k; n++) {
                    if (trainingPricingCurves.get(distances.get(i).getIndex()).isNormal()) {
                        normalCount++;
                    }
                }

                testingPricingCurve.addNormal(normalCount > ((k - 1) / 2));
            }
        }

        for (PricingCurve testingPricingCurve : testingPricingCurves) {
            int normalCount = 0;

            for (boolean normal : testingPricingCurve.getNormalBag()) {
                if (normal) {
                    normalCount++;
                }
            }
            testingPricingCurve.setNormal(normalCount >= 5);
        }
    }

    // Read the data from the input spreadsheet (that I've moved to a txt file)
    private static void readInputData() throws Exception {
        File input = new File("Input.txt");
        Scanner inputReader = new Scanner(input);

        FileWriter outputWriter = new FileWriter("Output.txt");
        StringBuilder minimiseBuilder = new StringBuilder(), constraintsBuilderA = new StringBuilder(), constraintsBuilderB = new StringBuilder();
        ArrayList<String> tasks = new ArrayList<>();

        // For each line, create a pricing curve object from the training data
        while (inputReader.hasNextLine()) {
            String[] line = inputReader.nextLine().split("\t");

            String userTaskIds = line[0];
            int readyTime = Integer.parseInt(line[1]);
            int deadline = Integer.parseInt(line[2]);
            int maxEnergy = Integer.parseInt(line[3]);
            int energyDemand = Integer.parseInt(line[4]);

            for (int i = readyTime; i <= deadline; i++) {
                if (i > readyTime) {
                    constraintsBuilderA.append("+");
                }

                constraintsBuilderA.append(userTaskIds).append("_time").append(i);

                if (i == deadline) {
                    constraintsBuilderA.append("=").append(energyDemand).append(";\n");
                }

                constraintsBuilderB.append("0<=").append(userTaskIds).append("_time").append(i).append("<=").append(maxEnergy).append(";\n");
                tasks.add(userTaskIds + "_time" + i);
            }
        }

        // Sort the tasks by time
        tasks.sort((a, b) -> {
            ArrayList<String> splitIdsA = new ArrayList<>(List.of(a.split("_")));
            ArrayList<String> splitIdsB = new ArrayList<>(List.of(b.split("_")));
            return Integer.parseInt(splitIdsA.get(2).substring(4)) - Integer.parseInt(splitIdsB.get(2).substring(4));
        });

        minimiseBuilder.append("c=");
        int newCount = 0;
        boolean first = true;

        for (String userTaskId : tasks) {
            if (!first) {
                minimiseBuilder.append("+");
            } else {
                first = false;
            }
            if (newCount < Integer.parseInt(userTaskId.substring(userTaskId.indexOf("time") + 4))) {
                newCount++;
            }
            minimiseBuilder.append("Q*").append(userTaskId);
        }

        minimiseBuilder.append(";\n");

        // Write the result of the constraints to a file
        outputWriter.write(minimiseBuilder.append(constraintsBuilderA.append(constraintsBuilderB)).toString());
        outputWriter.close();
        inputReader.close();
    }

    // Write the results to a file
    private static void printResults(ArrayList<PricingCurve> testingPricingCurves) throws Exception {
        FileWriter dataWriter = new FileWriter("NeighboursTestingData.txt");
        StringBuilder line = new StringBuilder();

        int normalCount = 0, abnormalCount = 0;
        for (PricingCurve testingPricingCurve : testingPricingCurves) {
            if (testingPricingCurve.isNormal()) {
                normalCount++;
            } else {
                abnormalCount++;
            }

            for (int j = 0; j < 24; j++) {
                line.append(testingPricingCurve.getPricingValues().get(j)).append(",");
            }

            line.append(testingPricingCurve.isNormal() ? 0 : 1).append("\n");
        }

        dataWriter.write(line.toString());
        dataWriter.close();

        System.out.println("Normal: " + normalCount + ", Abnormal: " + abnormalCount);
    }

    // Create LP files for each of the abnormal testing pricing curves
    private static void createTestingLPs() throws Exception {
        File dataFile = new File("NeighboursTestingData.txt");
        Scanner dataReader = new Scanner(dataFile);

        int count = 0;

        while (dataReader.hasNextLine()) {
            File outputFile = new File("Output.txt");
            Scanner outputReader = new Scanner(outputFile);
            FileWriter programWriter;

            String[] stringValues = dataReader.nextLine().split(",");
            String stringOutput = outputReader.nextLine();

            if (stringValues[stringValues.length - 1].equals("0")) {
                programWriter = new FileWriter("normal_programs/program" + count + ".lp");
            } else {
                programWriter = new FileWriter("abnormal_programs/program" + count + ".lp");
            }

            for (int i = 0; i < stringOutput.length(); i++) {
                if (stringOutput.charAt(i) == 'Q') {
                    String userTaskId;
                    try {
                        userTaskId = stringOutput.substring(i, stringOutput.indexOf("+", i));
                    } catch (Exception e) {
                        userTaskId = stringOutput.substring(i, stringOutput.length() - 1);
                    }

                    String time = userTaskId.substring(userTaskId.length() - 2);

                    if (time.charAt(0) == 'e') {
                        time = time.substring(1);
                    }

                    stringOutput = stringOutput.substring(0, i) + stringValues[Integer.parseInt(time)] + stringOutput.substring(i + 1);
                }
            }

            programWriter.write("/* Objective function */\n");
            programWriter.write("min: c;\n\n");
            programWriter.write(stringOutput + "\n");

            while (outputReader.hasNextLine()) {
                programWriter.write(outputReader.nextLine() + "\n");
            }

            programWriter.write("\n/* Variable bounds */");

            programWriter.close();
            outputReader.close();
            count++;
        }
    }

    // Solve the lp problems using simplex
    private static void simplexSolver() throws Exception {
        Scanner testingReader = new Scanner(new File("NeighboursTestingData.txt"));
        int count = 0;

        while (testingReader.hasNextLine()) {
            String testingData = testingReader.nextLine();

            if (testingData.endsWith("1")) {
                Tableau tableau = new Tableau(testingData);
                System.out.print("LP" + count + "   |   ");
                tableau.solve();
            }
            count++;
        }
    }
}
