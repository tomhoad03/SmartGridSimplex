import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Tableau {
    private Constraint minimiseFunction;
    private ArrayList<Constraint> constraints = new ArrayList<>();

    public Tableau(String testingData) throws Exception {
        Scanner outputReader = new Scanner(new File("output.txt"));

        String minimiseString = outputReader.nextLine();
        String[] splitString = minimiseString.split(";")[0].split("=")[1].split("\\+");
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
            }
        }
    }

    public void solve() {
        Double[][] tableau = new Double[51][449];

        int y = 0, x = 0;

        for (Constraint constraint : constraints) {
            for (String variable : minimiseFunction.getVariables()) {
                if (constraint.getVariables().contains(variable)) {
                    double value = constraint.getCoefficients().get(constraint.getVariables().indexOf(variable));
                    tableau[y][x] = value;
                } else {
                    tableau[y][x] = 0.0;
                }
                x++;
            }
            for (int i = 398; i < 448; i++) {
                if (i - 398 == y) {
                    tableau[y][i] = 1.0;
                } else {
                    tableau[y][i] = 0.0;
                }
            }
            tableau[y][448] = constraint.getValue();
            y++;
            x = 0;
        }

        for (int i = 0; i < 398; i++) {
            tableau[50][i] = 0 - minimiseFunction.getCoefficients().get(i);
        }
        for (int i = 398; i < 447; i++) {
            tableau[50][i] = 0.0;
        }
        tableau[50][448] = minimiseFunction.getValue();

        boolean isSolved = false;

        while (!isSolved) {
            int pivotCol = 0;

            for (int i = 0; i < 397; i++) {
                if (tableau[50][i] < tableau[50][pivotCol]) {
                    pivotCol = i;
                }
            }

            if (pivotCol == 307) {
                System.out.println("Test");
            }

            if (tableau[50][pivotCol] >= 0) {
                isSolved = true;
            } else {
                int pivotRow = 0;

                for (int i = 0; i < 49; i++) {
                    if ((tableau[i][398] / tableau[i][pivotCol]) < (tableau[pivotRow][398] / tableau[pivotRow][pivotCol])) {
                        pivotRow = i;
                    }
                }

                double pivotValue = tableau[pivotRow][pivotCol];

                for (int i = 0; i < 398; i++) {
                    tableau[pivotRow][i] = tableau[pivotRow][i] / pivotValue;
                }

                for (int i = 0; i < 51; i++) {
                    if (i != pivotRow) {
                        double operationValue = tableau[i][pivotCol];

                        for (int j = 0; j < 398; j++) {
                            tableau[i][j] = tableau[i][j] - (operationValue * tableau[pivotRow][j]);
                        }
                    }
                }
                System.out.println(pivotCol + ", " + tableau[50][pivotCol] + ", " + pivotRow + ", " + tableau[pivotRow][pivotCol]);
            }
        }
    }

    public Constraint getMinimiseFunction() {
        return minimiseFunction;
    }

    public ArrayList<Constraint> getConstraints() {
        return constraints;
    }
}
