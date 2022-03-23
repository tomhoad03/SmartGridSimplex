import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class InputReader {
    public static void main(String[] args) {
        try {
            File input = new File("Input.txt");
            Scanner inputReader = new Scanner(input);

            FileWriter outputWriter = new FileWriter("Output.txt");
            StringBuilder lineBuilder = new StringBuilder();

            // For each line, create a pricing curve object from the training data
            while (inputReader.hasNextLine()) {
                String[] line = inputReader.nextLine().split("\t");

                String userTaskIds = line[0];
                int readyTime = Integer.parseInt(line[1]);
                int deadline = Integer.parseInt(line[2]);
                int maxEnergy = Integer.parseInt(line[3]);
                int energyDemand = Integer.parseInt(line[4]);

                for (int i = readyTime; i <= deadline; i++) {
                    lineBuilder.append("0<=").append(userTaskIds).append("_time").append(i).append("<=").append(maxEnergy).append("\n");
                }
            }

            outputWriter.write(lineBuilder.toString());
            outputWriter.close();
            inputReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
