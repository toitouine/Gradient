public final class Data {
  public final double time, valueAbs2, valueAbsX;

  public Data(double time, double v2, double vx) {
    this.time = time;
    this.valueAbs2 = v2;
    this.valueAbsX = vx;
  }

  @Override
  public String toString() {
    return time + " / " + valueAbs2 + " / " + valueAbsX;
  }
}
