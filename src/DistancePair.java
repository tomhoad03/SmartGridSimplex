public class DistancePair<Double, Integer> {
    private final double distanceSquared;
    private final int index;

    public DistancePair(double distanceSquared, int index) {
        this.distanceSquared = distanceSquared;
        this.index = index;
    }

    public double getDistanceSquared() {
        return distanceSquared;
    }

    public int getIndex() {
        return index;
    }
}
