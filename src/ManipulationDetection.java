import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ManipulationDetection {
    public static void main(String[] args) {
        ArrayList<PricingCurve> trainingPricingCurves = new ArrayList<>();
        ArrayList<PricingCurve> testingPricingCurves = new ArrayList<>();

        try {
            // Read in the training data
            File trainingFile = new File("TrainingData.txt");
            Scanner trainingReader = new Scanner(trainingFile);

            while (trainingReader.hasNextLine()) {
                ArrayList<String> stringValues = new ArrayList<>(Arrays.asList(trainingReader.nextLine().split(",")));

                int normal = Integer.parseInt(stringValues.get(stringValues.size() - 1));
                stringValues.remove(stringValues.size() - 1);

                ArrayList<Double> doubleValues = new ArrayList<>();
                stringValues.stream().mapToDouble(Double::parseDouble).forEach(doubleValues::add);

                trainingPricingCurves.add(new PricingCurve(doubleValues, normal));
            }
            trainingReader.close();

            // Read in the testing data
            File testingFile = new File("TestingData.txt");
            Scanner testingReader = new Scanner(testingFile);

            while (testingReader.hasNextLine()) {
                ArrayList<String> stringValues = new ArrayList<>(Arrays.asList(testingReader.nextLine().split(",")));

                ArrayList<Double> doubleValues = new ArrayList<>();
                stringValues.stream().mapToDouble(Double::parseDouble).forEach(doubleValues::add);

                testingPricingCurves.add(new PricingCurve(doubleValues));
            }
            trainingReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
