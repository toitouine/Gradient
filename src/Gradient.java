import com.fazecast.jSerialComm.SerialPort;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final public class Gradient {
  private final MenuFrame menu;
  private final GraphFrame graph;

  // Acquisition
  public boolean inAcquisition = false;
  private boolean paused = false;
  private Timer timer;
  private Arduino arduino;
  private File saveFolder;
  private double absorbancePe;
  private ArrayList<Data> datas;
  private ArrayList<Double> markerTimes;
  private double lastSauvegarde = 0;
  private final double sauvegardeWaitTime = 20000;
  private LocalDateTime startTime = null;

  public Gradient() {
    menu = new MenuFrame(this);
    graph = new GraphFrame(this);
    datas = new ArrayList<Data>();
    markerTimes = new ArrayList<Double>();
  }

  public void goToMenu() {
    stopAcquisition();

    graph.setVisible(false);
    menu.setVisible(true);
  }

  public void quit() {
    if (inAcquisition) stopAcquisition();
    menu.dispose();
    graph.dispose();
    System.exit(0);
  }

  public boolean isArduino(SerialPort port) {
    return Serial.detectArduino(port);
  }

  public void startAcquisition(File folder, SerialPort port, double abs, int fe) {
    startTime = LocalDateTime.now();
    absorbancePe = abs;
    saveFolder = folder;
    inAcquisition = true;

    graph.clear();
    menu.setVisible(false);
    graph.setVisible(true);

    timer = new Timer();
    arduino = new Arduino(this, port, fe);
    timer.start();

    if (!arduino.tryConnect()) {
      String[] options = {"Réessayer", "Retour au menu"};
      int ans = JOptionPane.showOptionDialog(null, "Connexion perdue", "Erreur",
                  JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                  null, options, options[0]);
      if (ans == 1 || ans == JOptionPane.CLOSED_OPTION) {
        goToMenu();
      } else {
        stopAcquisition();
        startAcquisition(folder, port, abs, fe);
      }

      return;
    }
    arduino.startAcquisition();
  }

  public void togglePause() {
    paused = !paused;
  }

  public boolean acquisitionPaused() {
    return paused;
  }

  public void stopAcquisition() {
    if (!inAcquisition) return;

    arduino.stopAcquisition();

    if (datas.size() != 0 || markerTimes.size() != 0) sauvegarde();
    datas.clear();
    markerTimes.clear();

    arduino = null;
    timer = null;
    absorbancePe = -1;
    paused = false;
    lastSauvegarde = 0;
    startTime = null;
    saveFolder = null;
    inAcquisition = false;
  }

  public void addMark(double retardMs) {
    double time = timer.getTimeMs()/1000d;
    double retard = retardMs/1000d;
    graph.addMark(time - retard);
    markerTimes.add(time - retard);
    trySave();
  }

  public void acquisition(double valueAbs2, double valueAbsX) {
    if (!inAcquisition || paused) return;

    double time = timer.getTimeMs()/1000d;
    graph.sendData(time, valueAbs2, valueAbsX);
    Data data = new Data(time, valueAbs2, valueAbsX);
    datas.add(data);
    trySave();
  }

  public void saveToCSV() {
    if (!inAcquisition) return;

    JFileChooser folderChooser = new JFileChooser();
    folderChooser.setCurrentDirectory(saveFolder);
    int response = folderChooser.showSaveDialog(null);

    if (response != JFileChooser.APPROVE_OPTION) return;

    String fullPath = folderChooser.getSelectedFile().getAbsolutePath();
    if (!fullPath.endsWith(".csv")) {
      fullPath += ".csv";
    }

    System.out.println("TODO");
    System.out.println("\u001B[36mDonnées exportées en CSV : " + fullPath + "\u001B[0m");
  }

  private void trySave() {
    if (!inAcquisition) return;

    if (timer.getTimeMs() - lastSauvegarde > sauvegardeWaitTime) {
      new Thread(this::sauvegarde).start();
      lastSauvegarde = timer.getTimeMs();
    }
  }

  public void sauvegarde() {
    if (!inAcquisition) return;

    // Crée le chemin vers le fichier
    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyy_HH'h'mm'm'ss's'");
    String fileName = "GradientSave-" + startTime.format(format) + ".txt";
    String pathStr = saveFolder.getAbsolutePath() + File.separator + fileName;

    // Crée les lignes du fichier
    List<String> lines = new ArrayList<String>();
    lines.add("Collecteur de fractions (s) :");
    for (int i = 0; i < markerTimes.size(); i++) {
      lines.add(markerTimes.get(i).toString());
    }
    lines.add("");

    lines.add("Données (s / 2AUFS / XAUFS) :");
    for (int i = 0; i < datas.size(); i++) {
      lines.add(datas.get(i).toString());
    }

    // Sauvegarde le fichier
    try {
      Path path = Paths.get(pathStr);
      Files.write(path, lines, StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("\u001B[31mErreur pendant la sauvegarde des données\u001B[0m");
      return;
    }
    System.out.println("\u001B[36mDonnées sauvegardées : " + pathStr + "\u001B[0m");
  }
}
