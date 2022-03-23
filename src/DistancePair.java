public record DistancePair(double distanceSquared, int index) {
    public double getDistanceSquared() {
        return distanceSquared;
    }

    public int getIndex() {
        return index;
    }
}
