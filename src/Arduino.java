import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortDataListener;

final public class Arduino {
  final private Gradient application;
  final public SerialPort port;
  final public int frequence;

  public Arduino(Gradient app, SerialPort p, int fe) {
    application = app;
    port = p;
    frequence = fe;
    port.setBaudRate(115200);
  }

  public boolean tryConnect() {
    return Serial.open(port);
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
        String str = Serial.received(port, event);
        if (str.charAt(0) == 'm') application.addMark(Double.parseDouble(str.substring(1)));
        else if (str.charAt(0) == 'd') {
          String data = str.substring(1);
          double valAbs2 = Double.parseDouble(data.split("/")[0]);
          double valAbsX = Double.parseDouble(data.split("/")[1]);
          application.acquisition(valAbs2, valAbsX);
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
    String msg = "s" + frequence;
    Serial.write(port, msg);
  }
}
