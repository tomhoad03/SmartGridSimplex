import java.util.ArrayList;

public class PricingCurve {
    private final ArrayList<Double> pricingValues;
    private boolean isNormal;
    private ArrayList<Boolean> normalBag = new ArrayList<>(10);

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

    public void setNormal(boolean normal) {
        isNormal = normal;
    }

    public ArrayList<Boolean> getNormalBag() {
        return normalBag;
    }

    public void addNormal(boolean normal) {
        normalBag.add(normal);
    }
}
