public class Timer {
  private long timeAtStartNs;

  public Timer() {
  }

  public void start() {
    timeAtStartNs = System.nanoTime();
  }

  public long getTimeNs() {
    return System.nanoTime() - timeAtStartNs;
  }

  public double getTimeMs() {
    return getTimeNs() / 1000000d;
  }
}
