import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Rectangle;

public class Graph {

  final private JFreeChart chart;
  final private XYSeries series1, series2;
  final private ChartPanel panel;
  final private JPanel jpanel;

  private float strokeWeight = 1.25f;
  private Color markerColor = Color.BLACK;

  public Graph(String title, String abscisses, String ordonnees, String valueName1, String valueName2) {
    series1 = new XYSeries(valueName1);
    series2 = new XYSeries(valueName2);
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(series1);
    dataset.addSeries(series2);
    chart = ChartFactory.createXYLineChart(title, abscisses, ordonnees, dataset, PlotOrientation.VERTICAL, true, true, true);
    setStrokeWeight(strokeWeight);
    panel = new ChartPanel(chart);
    panel.setDomainZoomable(true);
    panel.setRangeZoomable(false);
    panel.setMouseWheelEnabled(true);
    chart.getXYPlot().setDomainPannable(true);

    jpanel = new JPanel();
    jpanel.setLayout(new BorderLayout());
    jpanel.add(panel, BorderLayout.CENTER);

    showSeries(0);
    showSeries(1);
  }

  public boolean isSeriesShowed(int index) {
    if (index != 0 && index != 1) return false;
    return chart.getXYPlot().getRenderer().isSeriesVisible(index);
  }

  public void showSeries(int index) {
    if (index != 0 && index != 1) return;
    chart.getXYPlot().getRenderer().setSeriesVisible(index, true);
  }

  public void hideSeries(int index) {
    if (index != 0 && index != 1) return;
    chart.getXYPlot().getRenderer().setSeriesVisible(index, false);
  }

  public void setSeries2Name(String str) {
    series2.setKey(str);
  }

  public void setStrokeWeight(float sw) {
    strokeWeight = sw;
    chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(strokeWeight));
    chart.getXYPlot().getRenderer().setSeriesStroke(1, new BasicStroke(strokeWeight));
  }

  public void clear() {
    series1.clear();
    series2.clear();
    chart.getXYPlot().clearDomainMarkers();
  }

  public void add(double x, double y1, double y2) {
    series1.add(x, y1);
    series2.add(x, y2);
  }

  public void addMark(double x, Color mColor) {
    ValueMarker marker = new ValueMarker(x);
    marker.setPaint(mColor);
    marker.setStroke(new BasicStroke(strokeWeight/2f));
    chart.getXYPlot().addDomainMarker(marker);
  }

  public void addMark(double x) {
    addMark(x, markerColor);
  }

  public JPanel getPanel() {
    return jpanel;
  }
}
