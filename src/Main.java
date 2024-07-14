public class Main {
  public static void main(String[] args) {
    // Démarre l'application
    Gradient application = new Gradient();
    application.goToMenu();

    // À l'arrêt du programme, s'assure que l'acquisition est arrêtée
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        if (application.inAcquisition) {
          application.stopAcquisition();
        }
      }
    }, "Shutdown-thread"));
  }
}
