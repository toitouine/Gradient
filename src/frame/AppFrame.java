import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

public abstract class AppFrame extends JFrame {
  protected final Gradient application;
  protected final int defaultWidth, defaultHeight;

  public AppFrame(Gradient app, String title) {
    application = app;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    defaultWidth = (int)screenSize.getWidth()/2;
    defaultHeight = (int)screenSize.getHeight()/2;
    setLocationByPlatform(true);
    if (title != null) setTitle("Gradient - " + title);
    else setTitle("Gradient");
    setSize(defaultWidth, defaultHeight);
    setMinimumSize(new Dimension(defaultWidth, defaultHeight));
  }

  public AppFrame(Gradient app) {
    this(app, null);
  }

  public void setVisible(boolean v) {
    super.setVisible(v);
  }
}
