import java.util.ArrayList;

public class PricingCurve {
    private final ArrayList<Double> pricingValues;
    private final boolean isTesting;
    private boolean isNormal;

    public PricingCurve(ArrayList<Double> pricingValues, int normal) {
        this.pricingValues = pricingValues;
        this.isNormal = normal == 0;
        this.isTesting = false;
    }

    public PricingCurve(ArrayList<Double> pricingValues) {
        this.pricingValues = pricingValues;
        this.isTesting = true;
    }

    public ArrayList<Double> getPricingValues() {
        return pricingValues;
    }

    public boolean isTesting() {
        return isTesting;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public void setNormal(boolean normal) {
        if (isTesting) {
            isNormal = normal;
        }
    }
}
