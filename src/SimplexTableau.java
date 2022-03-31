import java.util.ArrayList;

public class SimplexTableau {
    private final ArrayList<String> variableNames;
    private ArrayList<Double> minimiseFunction;
    private ArrayList<ArrayList<Double>> constraints;

    public SimplexTableau(ArrayList<String> variableNames, ArrayList<Double> minimiseFunction, ArrayList<ArrayList<Double>> constraints) {
        this.variableNames = variableNames;
        this.minimiseFunction = minimiseFunction;
        this.constraints = constraints;
    }

    public ArrayList<String> getVariableNames() {
        return variableNames;
    }

    public ArrayList<Double> getMinimiseFunction() {
        return minimiseFunction;
    }

    public ArrayList<ArrayList<Double>> getConstraints() {
        return constraints;
    }

    public void setMinimiseFunction(ArrayList<Double> minimiseFunction) {
        this.minimiseFunction = minimiseFunction;
    }

    public void setConstraints(ArrayList<ArrayList<Double>> constraints) {
        this.constraints = constraints;
    }
}
