import com.fazecast.jSerialComm.SerialPort;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

public class MenuFrame extends AppFrame implements ActionListener {
  private JButton selectFolder;

  private File selectedSaveFolder;
  private JComboBox<SerialPort> selectPort;
  private JComboBox<Double> selectAbs;
  private JSpinner selectFe;

  public MenuFrame(Gradient app) {
    super(app, "Menu");
    setResizable(false);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    initComponents();
  }

  @Override
  public void setVisible(boolean v) {
    if (v) refreshSelectPortList();
    super.setVisible(v);
  }

  private void tryStart() {
    if (selectedSaveFolder == null) {
      JOptionPane.showMessageDialog(null, "Aucun dossier de sauvegarde sélectionné", "Erreur", JOptionPane.ERROR_MESSAGE);
      return;
    }

    SerialPort port = (SerialPort)selectPort.getSelectedItem();
    if (!application.isArduino(port)) {
      JOptionPane.showMessageDialog(null, "Carte Arduino non détectée. Vérifiez le port ou la connexion.", "Erreur", JOptionPane.ERROR_MESSAGE);
      return;
    }

    double absorbance = (double)selectAbs.getSelectedItem();
    int fe = (int)selectFe.getValue();
    application.startAcquisition(selectedSaveFolder, port, absorbance, fe);
  }

  private void initComponents() {
    // Titre
    JPanel titlePanel = new JPanel();
    JLabel title = new JLabel("Gradient");
    GridBagConstraints gbc = new GridBagConstraints();
    titlePanel.setLayout(new GridBagLayout());
    titlePanel.setPreferredSize(new Dimension(defaultWidth, 100));
    title.setHorizontalTextPosition(JLabel.CENTER);
    title.setVerticalTextPosition(JLabel.CENTER);
    title.setFont(new Font("", Font.PLAIN, 50));
    titlePanel.add(title, new GridBagConstraints());
    add(titlePanel, BorderLayout.NORTH);

    // Démarrer l'acquisition
    JPanel startPanel = new JPanel();
    JButton button = new JButton();
    startPanel.setPreferredSize(new Dimension(defaultWidth, 100));
    button.setText("Démarrer l'acquisition");
    button.addActionListener(e -> tryStart());
    button.setFocusable(true);
    button.setFont(new Font("", Font.PLAIN, 20));
    button.setMargin(new Insets(20, 10, 20, 10));
    startPanel.add(button);
    add(startPanel, BorderLayout.SOUTH);

    // Centre
    addCenterElements();
  }

  private void refreshSelectPortList() {
    selectPort.removeAllItems();
    SerialPort[] ports = Serial.getPorts();

    for (int i = 0; i < ports.length; i++) {
      selectPort.addItem(ports[i]);
    }
  }

  private void addCenterElements() {
    JPanel centerPanel = new JPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    centerPanel.setLayout(new GridBagLayout());

    class Adder {
      void add(Component c, int x, int y, int position) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.anchor = position;
        centerPanel.add(c, gbc);
      }
    }
    Adder adder = new Adder();

    // Dossier de sauvegarde (ligne 1)
    selectFolder = new JButton();
    JLabel saveText = new JLabel("Sélectionner un dossier de sauvegarde");
    saveText.setFont(new Font("", Font.PLAIN, 17));
    selectFolder.addActionListener(this);
    selectFolder.setFocusable(true);
    selectFolder.setText("Sélectionner");
    adder.add(saveText, 0, 0, GridBagConstraints.WEST);
    adder.add(selectFolder, 1, 0, GridBagConstraints.CENTER);

    // Port (ligne 2)
    selectPort = new JComboBox<SerialPort>();
    refreshSelectPortList();
    JLabel portText = new JLabel("Port série de la carte Arduino");
    JButton refreshButton = new JButton();
    refreshButton.setText("Actualiser");
    refreshButton.addActionListener(e -> refreshSelectPortList());
    portText.setFont(new Font("", Font.PLAIN, 17));
    selectPort.setFocusable(true);
    selectPort.setPreferredSize(new Dimension(200, 27));
    adder.add(portText, 0, 1, GridBagConstraints.WEST);
    adder.add(selectPort, 1, 1, GridBagConstraints.CENTER);
    adder.add(refreshButton, 2, 1, GridBagConstraints.CENTER);

    // Absorbance pleine échelle (ligne 3)
    Double[] abs = {2.0d, 1.0d, 0.5d, 0.2d, 0.1d, 0.05d, 0.02d, 0.01d};
    selectAbs = new JComboBox<Double>(abs);
    JLabel absText = new JLabel("Absorbance pleine échelle");
    absText.setFont(new Font("", Font.PLAIN, 17));
    selectAbs.setFocusable(true);
    adder.add(absText, 0, 2, GridBagConstraints.WEST);
    adder.add(selectAbs, 1, 2, GridBagConstraints.CENTER);

    // Fréquence d'échantillonage (ligne 4)
    selectFe = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
    JLabel feText = new JLabel("Fréquence d'échantillonage (par seconde)");
    feText.setFont(new Font("", Font.PLAIN, 17));
    selectFe.setFocusable(true);
    adder.add(feText, 0, 3, GridBagConstraints.WEST);
    adder.add(selectFe, 1, 3, GridBagConstraints.CENTER);

    add(centerPanel, BorderLayout.CENTER);
  }

  private String reducePath(String path, int max) {
    if (max <= 4 || path.length() <= max) return path;

    while (path.length() > max - 4) {
      String[] folders = path.split("/");
      path = String.join("/", Arrays.copyOfRange(folders, 1, folders.length));
      if (folders.length == 2) break;
    }

    return ".../" + path;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == selectFolder) {
      JFileChooser folderChooser = new JFileChooser();
      folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int response = folderChooser.showOpenDialog(null);
      if (response == JFileChooser.APPROVE_OPTION) {
        File file = new File(folderChooser.getSelectedFile().getAbsolutePath());
        String path = file.getPath();
        // Raccourcit le chemin pour avoir moins de 20 caractères
        path = reducePath(path, 20);
        selectFolder.setText(path);
        selectedSaveFolder = file;
      }
    }
  }
}
