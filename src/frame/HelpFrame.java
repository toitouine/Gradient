import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class HelpFrame extends AppFrame {
  public HelpFrame(Gradient app) {
    super(app, "Aide pour le graphique");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setResizable(false);

    setLayout(new BorderLayout());
    JPanel titlePanel = new JPanel();
    JLabel title = new JLabel("Aide pour le graphique");
    GridBagConstraints gbc = new GridBagConstraints();
    titlePanel.setLayout(new GridBagLayout());
    titlePanel.setPreferredSize(new Dimension(defaultWidth, 100));
    title.setHorizontalTextPosition(JLabel.CENTER);
    title.setVerticalTextPosition(JLabel.CENTER);
    title.setFont(new Font("", Font.PLAIN, 50));
    titlePanel.add(title, gbc);
    add(titlePanel, BorderLayout.NORTH);

    JPanel centerPanel = new JPanel();
    add(centerPanel, BorderLayout.CENTER);
    initTexts(centerPanel);

    setMinimumSize(new Dimension((int)(defaultWidth*1.1), (int)(defaultHeight/1.15)));
    setSize((int)(defaultWidth*1.1), (int)(defaultHeight/1.15));
  }

  private void initTexts(JPanel panel) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    panel.setLayout(new GridBagLayout());

    class Adder {
      void add(Component c, int x, int y, int position) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.anchor = position;
        panel.add(c, gbc);
      }
    }
    Adder adder = new Adder();

    JLabel q1 = new JLabel("Glisser clic gauche vers la droite");
    q1.setFont(new Font("", Font.PLAIN, 17));
    adder.add(q1, 0, 0, GridBagConstraints.WEST);

    JLabel q2 = new JLabel("Glisser clic gauche vers la gauche");
    q2.setFont(new Font("", Font.PLAIN, 17));
    adder.add(q2, 0, 1, GridBagConstraints.WEST);

    JLabel q3 = new JLabel("Clic droit > Échelle auto. > Les deux axes");
    q3.setFont(new Font("", Font.PLAIN, 17));
    adder.add(q3, 0, 2, GridBagConstraints.WEST);

    JLabel q4 = new JLabel("Clic droit");
    q4.setFont(new Font("", Font.PLAIN, 17));
    adder.add(q4, 0, 3, GridBagConstraints.WEST);

    JLabel q5 = new JLabel("Molette");
    q5.setFont(new Font("", Font.PLAIN, 17));
    adder.add(q5, 0, 4, GridBagConstraints.WEST);

    JLabel q6 = new JLabel("CTRL + Glisser clic gauche (Windows)");
    q6.setFont(new Font("", Font.PLAIN, 17));
    adder.add(q6, 0, 5, GridBagConstraints.WEST);

    JLabel q7 = new JLabel("Glisser clic molette (Mac)");
    q7.setFont(new Font("", Font.PLAIN, 17));
    adder.add(q7, 0, 6, GridBagConstraints.WEST);


    JLabel r1 = new JLabel("Zoomer sur la partie sélectionnée");
    r1.setFont(new Font("", Font.PLAIN, 17));
    adder.add(r1, 1, 0, GridBagConstraints.EAST);

    JLabel r2 = new JLabel("Rétablit la vue par défaut");
    r2.setFont(new Font("", Font.PLAIN, 17));
    adder.add(r2, 1, 1, GridBagConstraints.EAST);

    JLabel r3 = new JLabel("Rétablit la vue par défaut");
    r3.setFont(new Font("", Font.PLAIN, 17));
    adder.add(r3, 1, 2, GridBagConstraints.EAST);

    JLabel r4 = new JLabel("Menu déroulant du graphique");
    r4.setFont(new Font("", Font.PLAIN, 17));
    adder.add(r4, 1, 3, GridBagConstraints.EAST);

    JLabel r5 = new JLabel("Zoomer ou dézoomer à l'endroit de la souris");
    r5.setFont(new Font("", Font.PLAIN, 17));
    adder.add(r5, 1, 4, GridBagConstraints.EAST);

    JLabel r6 = new JLabel("Déplacer le graphique horizontalement");
    r6.setFont(new Font("", Font.PLAIN, 17));
    adder.add(r6, 1, 5, GridBagConstraints.EAST);

    JLabel r7 = new JLabel("Déplacer le graphique horizontalement");
    r7.setFont(new Font("", Font.PLAIN, 17));
    adder.add(r7, 1, 6, GridBagConstraints.EAST);
  }
}
