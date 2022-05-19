import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Tableau {
    private final Constraint minimiseFunction;
    private final ArrayList<Constraint> constraints = new ArrayList<>();
    private int equalCount = 0;

    public Tableau(String testingData) throws Exception {
        Scanner outputReader = new Scanner(new File("output.txt"));

        String[] splitString = outputReader.nextLine().split(";")[0].split("=")[1].split("\\+");
        String[] splitData = testingData.split(",");

        ArrayList<Double> coefficients = new ArrayList<>();
        ArrayList<String> variables = new ArrayList<>();

        for (String split : splitString) {
            coefficients.add(Double.valueOf(splitData[Integer.parseInt(split.split("\\*")[1].split("_")[2].substring(4))]));
            variables.add(split.split("\\*")[1]);
        }

        this.minimiseFunction = new Constraint(coefficients, variables);

        while (outputReader.hasNextLine()) {
            String constraintString = outputReader.nextLine();

            if (!constraintString.contains("<")) {
                this.constraints.add(new Constraint(constraintString, 1.0));
                this.constraints.add(new Constraint(constraintString, -1.0));
                equalCount++;
            } else {
                this.constraints.add(new Constraint(constraintString, false));
            }
        }
    }

    public void solve() {
        int y = 0, x = 0;
        int numberOfVariables = minimiseFunction.getVariables().size(), // 398
            numberOfConstraints = constraints.size(), // 498
            numberOfBoth = numberOfConstraints + numberOfVariables; // 896

        // Create an array representing the tableau
        Double[][] tableau = new Double[numberOfConstraints + 2][numberOfBoth + 1];

        // Add the constraints to the tableau
        for (Constraint constraint : constraints) {
            for (String variable : minimiseFunction.getVariables()) {
                if (constraint.getVariables().contains(variable)) {
                    tableau[y][x] = 1.0;
                } else {
                    tableau[y][x] = 0.0;
                }
                x++;
            }
            for (int i = numberOfVariables; i < numberOfBoth; i++) {
                if (constraint.isEquals()) {
                    if (i - numberOfVariables + equalCount == y) {
                        tableau[y][i] = 1.0;
                    } else {
                        tableau[y][i] = 0.0;
                    }
                }
                else {
                    if (i - numberOfVariables == y) {
                        tableau[y][i] = -1.0;
                    } else if (i - numberOfBoth - equalCount == y) {
                        tableau[y][i] = 1.0;
                    } else {
                        tableau[y][i] = 0.0;
                    }
                }
            }
            tableau[y][numberOfBoth] = constraint.getValue();
            y++;
            x = 0;
        }

        // Add the minimisation function to the tableau
        for (int i = 0; i < numberOfVariables; i++) {
            tableau[numberOfConstraints][i] = minimiseFunction.getCoefficients().get(i);
        }
        for (int i = numberOfVariables; i < numberOfBoth; i++) {
            tableau[numberOfConstraints][i] = 0.0;
        }
        tableau[numberOfConstraints][numberOfBoth] = minimiseFunction.getValue();

        // Create the function to minimise artificial variables
        Double[] rowSum = new Double[numberOfBoth + 1];

        for (int i = 0; i < numberOfConstraints - numberOfVariables; i++) {
            Double[] row = tableau[i];

            for (int j = 0; j < numberOfBoth - (numberOfConstraints - numberOfVariables); j++) {
                try {
                    rowSum[j] -= row[j];
                } catch (Exception ignored) {
                    rowSum[j] = 0.0 - row[j];
                }
            }
            for (int j = numberOfBoth - (numberOfConstraints - numberOfVariables); j < numberOfBoth; j++) {
                rowSum[j] = 0.0;
            }
            try {
                rowSum[numberOfBoth] -= row[numberOfBoth];
            } catch (Exception ignored) {
                rowSum[numberOfBoth] = 0.0 - row[numberOfBoth];
            }
        }
        tableau[numberOfConstraints + 1] = rowSum;

        // Perform the first phase of simplex
        boolean isSolved = false;
        while (!isSolved) {
            int pivotCol = 0;
            for (int i = 0; i < numberOfBoth; i++) {
                if (tableau[numberOfConstraints + 1][i] < tableau[numberOfConstraints + 1][pivotCol]) {
                    pivotCol = i;
                }
            }

            if (tableau[numberOfConstraints + 1][pivotCol] == 0) {
                isSolved = true;
            } else {
                int pivotRow = 0;
                for (int i = 0; i < numberOfConstraints + 1; i++) {
                    if ((tableau[i][numberOfBoth] / tableau[i][pivotCol]) < (tableau[pivotRow][numberOfBoth] / tableau[pivotRow][pivotCol]) && (tableau[i][numberOfBoth] / tableau[i][pivotCol] > 0)) {
                        pivotRow = i;
                    }
                }

                double pivotValue = tableau[pivotRow][pivotCol];
                for (int i = 0; i < numberOfBoth + 1; i++) {
                    tableau[pivotRow][i] = tableau[pivotRow][i] / pivotValue;
                }

                for (int i = 0; i < numberOfConstraints + 2; i++) {
                    if (i != pivotRow) {
                        double operationValue = tableau[i][pivotCol];

                        for (int j = 0; j < numberOfBoth + 1; j++) {
                            tableau[i][j] = tableau[i][j] - (operationValue * tableau[pivotRow][j]);
                        }
                    }
                }
            }
        }
        System.out.print("First Stage: " + -tableau[numberOfConstraints][numberOfBoth]);

        // Perform the second phase of simplex
        isSolved = false;
        while (!isSolved) {
            int pivotCol = 0;
            for (int i = 0; i < numberOfConstraints; i++) {
                if (tableau[numberOfConstraints][i] < tableau[numberOfConstraints][pivotCol]) {
                    pivotCol = i;
                }
            }

            if (tableau[numberOfConstraints][pivotCol] == 0) {
                isSolved = true;
            } else {
                int pivotRow = 0;
                for (int i = 0; i < numberOfConstraints; i++) {
                    if ((tableau[i][numberOfBoth] / tableau[i][pivotCol]) < (tableau[pivotRow][numberOfBoth] / tableau[pivotRow][pivotCol])  && (tableau[i][numberOfBoth] / tableau[i][pivotCol] > 0)) {
                        pivotRow = i;
                    }
                }

                double pivotValue = tableau[pivotRow][pivotCol];
                for (int i = 0; i < numberOfBoth + 1; i++) {
                    tableau[pivotRow][i] = tableau[pivotRow][i] / pivotValue;
                }

                for (int i = 0; i < numberOfConstraints + 1; i++) {
                    if (i != pivotRow) {
                        double operationValue = tableau[i][pivotCol];
                        for (int j = 0; j < numberOfBoth + 1; j++) {
                            tableau[i][j] = tableau[i][j] - (operationValue * tableau[pivotRow][j]);
                        }
                    }
                }
            }
        }
        System.out.println("   |   Second Stage: " + -tableau[numberOfConstraints][numberOfBoth]);
    }
}
