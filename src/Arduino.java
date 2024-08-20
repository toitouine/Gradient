import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortDataListener;

final public class Arduino {
  final private Gradient application;
  final public SerialPort port;
  final public int frequence;
  final public double pleineEchelle;

  public Arduino(Gradient app, SerialPort p, int fe, double ape) {
    application = app;
    port = p;
    frequence = fe;
    pleineEchelle = ape;
    port.setBaudRate(115200);
  }

  public boolean tryConnect() {
    return Serial.open(port);
  }

  private double map(double value, double min1, double max1, double min2, double max2) {
    return (value - min1) * (max2 - min2) / (max1 - min1) + min2;
  }

  public void startAcquisition() {
    // Se prépare à recevoir les données
    port.addDataListener(new SerialPortDataListener() {
      @Override
      public int getListeningEvents() {
       return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
      }

      @Override
      public void serialEvent(SerialPortEvent event) {
        try {
          String fullMessage = Serial.received(port, event);
          String[] strs = fullMessage.split("(?<=[0-9])(?=[a-zA-Z])");
          for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (str.charAt(0) == 'm') application.addMark(Double.parseDouble(str.substring(1)));
            else if (str.charAt(0) == 'd') {
              String data = str.substring(1);
              double val2 = Double.parseDouble(data.split("/")[0]);
              double valX = Double.parseDouble(data.split("/")[1]);
              double maxValue = Math.pow(2, 16) - 1;
              double valAbs2 = map(val2, 0, maxValue/3.3d, 0, 2);
              double valAbsX = map(valX, 0, maxValue/3.3d, 0, pleineEchelle);
              application.acquisition(valAbs2, valAbsX);
            }
          }
        } catch (Exception e) {
          System.out.println("ERREUR DANS LA LECTURE DE LA DERNIÈRE DONNÉE REÇUE");
        }
      }
    });

    // Indique à la carte de démarrer l'envoi des données
    sendStart();
  }

  public void stopAcquisition() {
    Serial.write(port, "e");
    Serial.close(port);
  }

  public void sendStart() {
    // S'assure que la carte soit en WAITING
    Serial.write(port, "e");

    // Démarre l'acquisition avec la bonne fréquence
    String msg = "s" + frequence;
    Serial.write(port, msg);
  }
}
