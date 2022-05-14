import java.util.ArrayList;

public class Constraint {
    private ArrayList<Double> coefficients = new ArrayList<>();
    private ArrayList<String> variables = new ArrayList<>();
    private final double value;

    public Constraint(String constraintString) {
        String[] splitString = constraintString.split("=")[0].split("\\+");

        for (String split : splitString) {
            if (split.contains("*")) {
                this.coefficients.add(Double.valueOf(split.split("\\*")[0]));
                this.variables.add(split.split("\\*")[1]);
            } else {
                this.coefficients.add(1.0);
                this.variables.add(split);
            }
        }

        this.value = Double.parseDouble(constraintString.split(";")[0].split("=")[1]);
    }

    public Constraint(String constraintString, boolean ignored) {
        this.coefficients.add(1.0);
        this.variables.add(constraintString.split("<=")[1]);
        this.value = 1.0;
    }

    public Constraint(ArrayList<Double> coefficients, ArrayList<String> variables) {
        this.coefficients = coefficients;
        this.variables = variables;
        this.value = 0.0;
    }

    public ArrayList<Double> getCoefficients() {
        return coefficients;
    }

    public ArrayList<String> getVariables() {
        return variables;
    }

    public double getValue() {
        return value;
    }
}
