import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class InputReader {
    public static void main(String[] args) {
        try {
            File input = new File("Input.txt");
            Scanner inputReader = new Scanner(input);

            FileWriter outputWriter = new FileWriter("Output.txt");
            StringBuilder constraintsBuilderA = new StringBuilder(), constraintsBuilderB = new StringBuilder();
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
                    constraintsBuilderB.append("0<=").append(userTaskIds).append("_time").append(i).append("<=").append(maxEnergy).append("\n");
                    tasks.add(userTaskIds + "_time" + i);
                }
            }

            outputWriter.write(constraintsBuilderA.append(constraintsBuilderB).toString());
            outputWriter.close();
            inputReader.close();

            // Sort the tasks by time
            tasks.sort((a, b) -> {
                ArrayList<String> splitIdsA = new ArrayList<>(List.of(a.split("_")));
                ArrayList<String> splitIdsB = new ArrayList<>(List.of(b.split("_")));
                return Integer.parseInt(splitIdsA.get(2).substring(4)) - Integer.parseInt(splitIdsB.get(2).substring(4));
            });

            for (String userTaskId : tasks) {
                System.out.println(userTaskId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
