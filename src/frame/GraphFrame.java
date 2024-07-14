import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;

public class GraphFrame extends AppFrame {
  private Graph graph;
  private JButton menuButton;
  private JButton stopButton;

  public GraphFrame(Gradient app) {
    super(app, "Acquisition");
    graph = new Graph("", "Temps (s)", "Absorbance", "2AUFS", "XAUFS");
    setSize((int)(defaultWidth*1.75), (int)(defaultHeight*1.75));
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    initComponents();
    setCloseConfirmation();
  }

  public void setNameXAUFS(double abs) {
    graph.setSeries2Name(abs + "AUFS");
  }

  @Override
  public void setVisible(boolean v) {
    super.setVisible(v);
    updateButtons();
  }

  public void sendData(double x, double y1, double y2) {
    graph.add(x, y1, y2);
  }

  public void addMark(double x, Color c) {
    graph.addMark(x, c);
  }

  public void addMark(double x) {
    graph.addMark(x);
  }

  private void setCloseConfirmation() {
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        tryExit();
      }
    });
  }

  private void tryExit() {
    // Confirmation pour quitter le programme
    Object[] options = {"Oui", "Annuler"};
    int response = JOptionPane.showOptionDialog(null, "Voulez-vous vraiment quitter ?",
                                                "Quitter le programme",
                                                JOptionPane.DEFAULT_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null, options, options[1]);
    if (response == JOptionPane.YES_OPTION) {
      application.quit();
    }
  }

  public void clear() {
    graph.clear();
  }

  private void togglePause() {
    application.togglePause();
    updateButtons();
  }

  private void updateButtons() {
    boolean paused = application.acquisitionPaused();
    menuButton.setEnabled(paused);
    stopButton.setText((paused ? "Reprendre" : "Suspendre") + " l'acquisition");
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    JPanel top = new JPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    top.setLayout(new GridBagLayout());

    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.weightx = 1;
    gbc.weighty = 1;

    menuButton = new JButton("Retour au menu");
    menuButton.setFocusable(false);
    menuButton.setEnabled(false);
    menuButton.addActionListener(e -> application.goToMenu());
    menuButton.setMargin(new Insets(5, 5, 5, 5));
    menuButton.setFont(new Font("", Font.PLAIN, 16));
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    top.add(menuButton, gbc);

    stopButton = new JButton("Suspendre l'acquisition");
    stopButton.setFocusable(false);
    stopButton.addActionListener(e -> togglePause());
    stopButton.setMargin(new Insets(5, 5, 5, 5));
    stopButton.setFont(new Font("", Font.PLAIN, 16));
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    top.add(stopButton, gbc);

    JButton exportButton = new JButton("Exporter en CSV");
    exportButton.setFocusable(false);
    exportButton.addActionListener(e -> application.saveToCSV());
    exportButton.setMargin(new Insets(5, 5, 5, 5));
    exportButton.setFont(new Font("", Font.PLAIN, 16));
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    top.add(exportButton, gbc);

    add(top, BorderLayout.NORTH);
    add(graph.getPanel(), BorderLayout.CENTER);
  }
}
