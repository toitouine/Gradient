import com.fazecast.jSerialComm.SerialPort;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.io.File;
import java.io.PrintWriter;
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
  private int numberOfTubes = 0;
  private double lastTube = 0;
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

  public void readCSV(File file) {
    ReaderFrame reader = new ReaderFrame(this, file);
    reader.setVisible(true);
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
    numberOfTubes = 0;
    lastTube = 0;

    graph.clear();
    graph.setNameXAUFS(abs);
    menu.setVisible(false);
    graph.setVisible(true);

    timer = new Timer();
    arduino = new Arduino(this, port, fe, abs);
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
    numberOfTubes = 0;
    lastTube = 0;
    startTime = null;
    saveFolder = null;
    inAcquisition = false;
  }

  public void addMark(double retardMs) {
    double time = timer.getTimeMs()/1000d;
    double retard = retardMs/1000d;
    double value = time - retard;
    if (numberOfTubes != 0) {
      graph.addTextMark((value + lastTube)/2d, String.valueOf(numberOfTubes));
    }
    graph.addMark(value);
    markerTimes.add(value);
    numberOfTubes++;
    lastTube = value;
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

    // Demande le chemin vers le csv
    String fullPath = "";
    do {
      JFileChooser folderChooser = new JFileChooser();
      folderChooser.setCurrentDirectory(saveFolder);
      int response = folderChooser.showSaveDialog(null);

      if (response != JFileChooser.APPROVE_OPTION) return;

      fullPath = folderChooser.getSelectedFile().getAbsolutePath();

      if (!fullPath.endsWith(".csv")) {
        fullPath += ".csv";
      }

      if (Files.exists(Paths.get(fullPath))) {
        JOptionPane.showMessageDialog(null, "Ce fichier existe déjà.", "Erreur", JOptionPane.ERROR_MESSAGE);
      }
    } while (Files.exists(Paths.get(fullPath)));

    // Copie les données
    ArrayList<Data> datasCopy = new ArrayList<Data>();
    ArrayList<Double> markersCopy = new ArrayList<Double>();
    datasCopy.addAll(datas);
    markersCopy.addAll(markerTimes);

    // Prépare et trie les données
    ArrayList<String[]> entries = new ArrayList<String[]>();

    for (Data data : datasCopy) {
      String[] entry = {String.valueOf(data.time), String.valueOf(data.valueAbs2), String.valueOf(data.valueAbsX), "non"};
      entries.add(entry);
    }
    for (int i = 0; i < markersCopy.size(); i++) {
      String[] entry = {String.valueOf(markersCopy.get(i)), "", "", "oui"};
      entries.add(entry);
    }

    Collections.sort(entries, new Comparator<String[]>() {
      @Override
      public int compare(String[] arr1, String[] arr2) {
        return Double.valueOf(arr1[0]).compareTo(Double.valueOf(arr2[0]));
      }
    });

    // Ajoute les titres
    String[] titles = {"Temps (s)", "2AUFS", absorbancePe + "AUFS", "Changement de tube"};
    entries.add(0, titles);

    // Écrit le fichier en csv
    try {
      File output = new File(fullPath);
      PrintWriter pw = new PrintWriter(output);
      for (String[] entry : entries) {
        List<String> entryList = Arrays.asList(entry);
        String line = entryList.stream().collect(Collectors.joining(";"));
        pw.println(line);
      }
      pw.close();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Une erreur est survenue pendant la sauvegarde", "Erreur", JOptionPane.ERROR_MESSAGE);
      return;
    }

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

    // Copie les données
    ArrayList<Data> datasCopy = new ArrayList<Data>();
    ArrayList<Double> markersCopy = new ArrayList<Double>();
    datasCopy.addAll(datas);
    markersCopy.addAll(markerTimes);

    // Crée les lignes du fichier
    List<String> lines = new ArrayList<String>();
    lines.add("Collecteur de fractions (s) :");
    for (int i = 0; i < markersCopy.size(); i++) {
      lines.add(markersCopy.get(i).toString());
    }
    lines.add("");

    lines.add("Données (s / 2AUFS / " + absorbancePe + "AUFS) :");
    for (int i = 0; i < datasCopy.size(); i++) {
      lines.add(datasCopy.get(i).toString());
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
