import java.util.ArrayList;

public record ClassificationValues(ArrayList<Double> maxPricingCurve,
                                   ArrayList<Double> minPricingCurve) {

    public Double getMinPricingCurveValue(int i) {
        return minPricingCurve.get(i);
    }

    public Double getMaxMinDifferent(int i) {
        return maxPricingCurve.get(i) - minPricingCurve.get(i);
    }
}
