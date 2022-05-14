import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Tableau {
    private final Constraint minimiseFunction;
    private final ArrayList<Constraint> constraints = new ArrayList<>();

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
                this.constraints.add(new Constraint(constraintString));
            } else {
                this.constraints.add(new Constraint(constraintString, false));
            }
        }
    }

    public void solve() {
        int y = 0, x = 0;
        int numberOfConstraints = constraints.size(), // 448
            numberOfVariables = minimiseFunction.getVariables().size(), // 398
            numberOfBoth = numberOfConstraints + numberOfVariables; // 846

        Double[][] tableau = new Double[numberOfConstraints + 1][numberOfBoth + 1];

        for (Constraint constraint : constraints) {
            for (String variable : minimiseFunction.getVariables()) {
                if (constraint.getVariables().contains(variable)) {
                    tableau[y][x] = constraint.getCoefficients().get(constraint.getVariables().indexOf(variable));
                } else {
                    tableau[y][x] = 0.0;
                }
                x++;
            }
            for (int i = numberOfVariables; i < numberOfBoth; i++) {
                if (i - numberOfVariables == y) {
                    tableau[y][i] = 1.0;
                } else {
                    tableau[y][i] = 0.0;
                }
            }
            tableau[y][numberOfBoth] = constraint.getValue();
            y++;
            x = 0;
        }

        for (int i = 0; i < numberOfVariables; i++) {
            tableau[numberOfConstraints][i] = 0 - minimiseFunction.getCoefficients().get(i);
        }
        for (int i = numberOfVariables; i < numberOfBoth; i++) {
            tableau[numberOfConstraints][i] = 0.0;
        }
        tableau[numberOfConstraints][numberOfBoth] = minimiseFunction.getValue();

        boolean isSolved = false;

        while (!isSolved) {
            int pivotCol = 0;
            for (int i = 0; i < numberOfBoth; i++) {
                if (tableau[numberOfConstraints][i] < tableau[numberOfConstraints][pivotCol]) {
                    pivotCol = i;
                }
            }
            if (tableau[numberOfConstraints][pivotCol] >= 0) {
                isSolved = true;
            } else {
                int pivotRow = 0;
                for (int i = 0; i < numberOfConstraints; i++) {
                    if ((tableau[i][numberOfBoth] / tableau[i][pivotCol]) < (tableau[pivotRow][numberOfBoth] / tableau[pivotRow][pivotCol])) {
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
        System.out.println(tableau[numberOfConstraints][numberOfBoth]);
    }
}
