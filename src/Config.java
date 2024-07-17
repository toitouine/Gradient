import java.nio.file.Paths;
import java.nio.file.Files;

public class Config {
  private Config() {}

  // Chemin (absolu) vers le dossier de sauvegarde par défaut
  public static String defaultSaveDirectory = "";

  static {
    // Vérifie que le chemin est valide
    if (!defaultSaveDirectory.equals("")) {
      if (!Files.exists(Paths.get(defaultSaveDirectory))) {
        defaultSaveDirectory = "";
        System.out.println("Fichier de sauvegarde par défaut invalide !");
      }
    }
  }
}
