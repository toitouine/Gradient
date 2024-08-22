import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class ReaderFrame extends AppFrame {
  private Graph graph;
  private String abs = "XAUFS";
  private JButton toggle2Button, toggleXButton;
  private JButton toggleFracButton;

  public ReaderFrame(Gradient app, File csvFile) {
    super(app, "Lecteur de CSV");
    graph = new Graph(csvFile.getName(), "Temps (s)", "Absorbance", "2AUFS", "XAUFS");
    setSize((int)(defaultWidth*1.75), (int)(defaultHeight*1.75));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    // Lecture du CSV
    processCSV(csvFile);

    // GUI
    setLayout(new BorderLayout());
    JPanel top = new JPanel();
    JPanel topLeft = new JPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    top.setLayout(new GridBagLayout());
    topLeft.setLayout(new GridBagLayout());
    add(top, BorderLayout.NORTH);
    add(graph.getPanel(), BorderLayout.CENTER);
    gbc.gridx = 0;
    gbc.gridy = 0;
    top.add(topLeft, gbc);

    toggle2Button = new JButton("Masquer 2AUFS");
    toggle2Button.setFocusable(false);
    toggle2Button.addActionListener(e -> toggle2AUFS());
    toggle2Button.setMargin(new Insets(5, 5, 5, 5));
    toggle2Button.setFont(new Font("", Font.PLAIN, 16));
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    topLeft.add(toggle2Button, gbc);

    toggleXButton = new JButton("Masquer " + abs);
    toggleXButton.setFocusable(false);
    toggleXButton.addActionListener(e -> toggleXAUFS());
    toggleXButton.setMargin(new Insets(5, 5, 5, 5));
    toggleXButton.setFont(new Font("", Font.PLAIN, 16));
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    topLeft.add(toggleXButton, gbc);

    toggleFracButton = new JButton("Masquer les fractions");
    toggleFracButton.setFocusable(false);
    toggleFracButton.addActionListener(e -> toggleFrac());
    toggleFracButton.setMargin(new Insets(5, 5, 5, 5));
    toggleFracButton.setFont(new Font("", Font.PLAIN, 16));
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    topLeft.add(toggleFracButton, gbc);

    JButton helpButton = new JButton("Ouvrir l'aide");
    helpButton.setFocusable(false);
    helpButton.addActionListener(e -> application.openHelp());
    helpButton.setMargin(new Insets(5, 5, 5, 5));
    helpButton.setFont(new Font("", Font.PLAIN, 16));
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    top.add(helpButton, gbc);
  }

  private void toggle2AUFS() {
    if (graph.isSeriesShowed(0)) graph.hideSeries(0);
    else graph.showSeries(0);

    boolean is2Displayed = graph.isSeriesShowed(0);
    toggle2Button.setText((is2Displayed ? "Masquer" : "Afficher") + " 2AUFS");
  }

  private void toggleXAUFS() {
    if (graph.isSeriesShowed(1)) graph.hideSeries(1);
    else graph.showSeries(1);

    boolean isXDisplayed = graph.isSeriesShowed(1);
    toggleXButton.setText((isXDisplayed ? "Masquer " : "Afficher ") + abs);
  }

  private void toggleFrac() {
    if (graph.areMarkersHidden()) graph.showMarkers();
    else graph.hideMakers();

    boolean markerDisplayed = !graph.areMarkersHidden();
    toggleFracButton.setText((markerDisplayed ? "Masquer" : "Afficher") + " les fractions");
  }

  private void processCSV(File file) {
    try {
      // Récupère les données du fichier
      List<List<String>> records = new ArrayList<>();
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        records.add(getRecordFromLine(scanner.nextLine()));
      }

      // Récupère l'absorbance pleine échelle
      abs = records.get(0).get(2);
      graph.setSeries2Name(abs);

      // Affiche les données
      int numberOfTubes = 0;
      double lastTube = 0;
      for (int i = 1; i < records.size(); i++) {
        List<String> line = records.get(i);
        boolean changement = line.get(3).equals("oui");

        if (changement) {
          // Si c'est un changement de tube, on affiche les markers
          double value = Double.valueOf(line.get(0));
          if (numberOfTubes != 0) {
            graph.addTextMark((value + lastTube)/2d, String.valueOf(numberOfTubes));
          }
          graph.addMark(value);
          numberOfTubes++;
          lastTube = value;
        }
        else {
          // Sinon on enregistre les données
          double valueAbs2 = Double.valueOf(line.get(1));
          double valueAbsX = Double.valueOf(line.get(2));
          graph.add(Double.valueOf(line.get(0)), valueAbs2, valueAbsX);
        }
      }

    } catch (Exception e) {
      abs = "XAUFS";
      graph.setSeries2Name(abs);
      JOptionPane.showMessageDialog(null, "Erreur dans la lecture du fichier. Vérifiez qu'il s'agit bien d'un csv gradient.", "Erreur", JOptionPane.ERROR_MESSAGE);
    }
  }

  private List<String> getRecordFromLine(String line) {
    List<String> values = new ArrayList<String>();
    try (Scanner rowScanner = new Scanner(line)) {
      rowScanner.useDelimiter(";");
      while (rowScanner.hasNext()) {
        values.add(rowScanner.next());
      }
    }
    return values;
  }
}
