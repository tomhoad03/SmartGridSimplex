import java.util.ArrayList;

public class PricingCurve {
    private ArrayList<Double> pricingValues;
    private boolean isNormal;
    private ArrayList<Boolean> normalBag = new ArrayList<>(10);
    private double weighting;

    public PricingCurve(ArrayList<Double> pricingValues, int normal, double weighting) {
        this.pricingValues = pricingValues;
        this.isNormal = normal == 0;
        this.weighting = weighting;
    }

    public PricingCurve(ArrayList<Double> pricingValues, double weighting) {
        this.pricingValues = pricingValues;
        this.weighting = weighting;
    }

    public ArrayList<Double> getPricingValues() {
        return pricingValues;
    }

    public void setPricingValues(ArrayList<Double> pricingValues) {
        this.pricingValues = pricingValues;
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

    public double getWeighting() {
        return weighting;
    }

    public void setWeighting(double weighting) {
        this.weighting = weighting;
    }
}
