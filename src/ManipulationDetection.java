import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class ManipulationDetection {
    public static void main(String[] args) {
        try {
            ArrayList<PricingCurve> trainingPricingCurves = readTrainingData();
            ArrayList<PricingCurve> testingPricingCurves = readTestingData();

            // calculateCentroidValues(trainingPricingCurves, testingPricingCurves); // alternate training algorithm
            calculateKNearestNeighbours(20, trainingPricingCurves, testingPricingCurves);
            // readInputData(); // only need to be run once to create base file
            printResults(testingPricingCurves);
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

    // Calculate the centroid values of the normal and abnormal curves
    private static void calculateCentroidValues(ArrayList<PricingCurve> trainingPricingCurves, ArrayList<PricingCurve> testingPricingCurves) {
        ArrayList<Double> normalCumulativeValues = new ArrayList<>(24), abnormalCumulativeValues = new ArrayList<>(24);
        ArrayList<Double> normalCentroidValues = new ArrayList<>(24), abnormalCentroidValues = new ArrayList<>(24);

        for (PricingCurve trainingPricingCurve : trainingPricingCurves) {
            if ((normalCumulativeValues.size() == 0 && trainingPricingCurve.isNormal()) || (abnormalCumulativeValues.size() == 0 && trainingPricingCurve.isNormal())) {
                for (int i = 0; i < 24; i++) {
                    if (trainingPricingCurve.isNormal()) {
                        normalCumulativeValues.add(trainingPricingCurve.getPricingValues().get(i));
                    } else {
                        abnormalCumulativeValues.add(trainingPricingCurve.getPricingValues().get(i));
                    }
                }
            } else {
                for (int i = 0; i < 24; i++) {
                    if (trainingPricingCurve.isNormal()) {
                        normalCumulativeValues.set(i, normalCumulativeValues.get(i) + trainingPricingCurve.getPricingValues().get(i));
                    } else {
                        abnormalCumulativeValues.set(i, abnormalCumulativeValues.get(i) + trainingPricingCurve.getPricingValues().get(i));
                    }
                }
            }
        }

        for (int i = 0; i < 24; i++) {
            normalCentroidValues.add(normalCumulativeValues.get(i) / 5000);
            abnormalCentroidValues.add(abnormalCumulativeValues.get(i) / 5000);
        }

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
        }
    }

    // Calculate the k nearest neighbours
    private static void calculateKNearestNeighbours(int k, ArrayList<PricingCurve> trainingPricingCurves, ArrayList<PricingCurve> testingPricingCurves) {
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
        File lpFile = new File("data/COMP3217-example3.lp");
        Scanner lpReader = new Scanner(lpFile);

        // Read the minimise function
        lpReader.nextLine(); lpReader.nextLine(); lpReader.nextLine();
        String minimiseFunctionLine = lpReader.nextLine();
        String[] minimiseVariables = minimiseFunctionLine.substring(2, minimiseFunctionLine.length() - 1).split("\\+");

        ArrayList<Double> minimiseFunction = new ArrayList<>();
        ArrayList<String> variableNames = new ArrayList<>();

        minimiseFunction.add(1.0);
        variableNames.add("c");

        for (String minimiseVariable : minimiseVariables) {
            String minimiseConstant = minimiseVariable.split("\\*")[0];
            minimiseFunction.add(Double.valueOf(minimiseConstant));

            String minimiseValue = minimiseVariable.split("\\*")[1];
            variableNames.add(minimiseValue);
        }
        variableNames.add("total");

        // Read the constraints
        ArrayList<ArrayList<Double>> constraints = new ArrayList<>();

        while (lpReader.hasNextLine()) {
            String line = lpReader.nextLine();

            if (line.contains("=") && !line.contains("<=")) {
                String[] constraintVariables = line.split("=")[0].split("\\+");
                ArrayList<Double> constraint = new ArrayList<>(Collections.nCopies(variableNames.size() + 1, 0.0));

                String constraintValue = line.split("=")[1];
                constraintValue = constraintValue.substring(0, constraintValue.length() - 1);

                for (String constraintVariable : constraintVariables) {
                    constraint.set(variableNames.indexOf(constraintVariable), 1.0);
                }
                constraint.set(variableNames.size(), Double.valueOf(constraintValue));
                constraints.add(constraint);
            }
        }

        SimplexTableau simplexTableau = new SimplexTableau(variableNames, minimiseFunction, constraints);
    }
}
