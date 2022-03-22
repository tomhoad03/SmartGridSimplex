import java.util.ArrayList;

public class PricingCurve {
    private final ArrayList<Double> pricingValues;
    private boolean isNormal;

    public PricingCurve(ArrayList<Double> pricingValues, int normal) {
        this.pricingValues = pricingValues;
        this.isNormal = normal == 0;
    }

    public PricingCurve(ArrayList<Double> pricingValues) {
        this.pricingValues = pricingValues;
    }

    public ArrayList<Double> getPricingValues() {
        return pricingValues;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public void setNormal() {
        isNormal = true;
    }
}
