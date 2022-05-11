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

    public void setCoefficients(ArrayList<Double> coefficients) {
        this.coefficients = coefficients;
    }

    public void setVariables(ArrayList<String> variables) {
        this.variables = variables;
    }
}
