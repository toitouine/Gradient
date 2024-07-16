import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortDataListener;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class Serial {
  private Serial() {
  }

  public static SerialPort[] getPorts() {
    return SerialPort.getCommPorts();
  }

  public static String[] getPortsName() {
    SerialPort[] ports = getPorts();
    String[] names = new String[ports.length];
		for(int i = 0; i < names.length; i++) {
			names[i] = ports[i].getSystemPortName();
    }
    return names;
  }

  public static void write(SerialPort port, String str) {
    byte[] bytes = str.getBytes();
    System.out.println("\u001B[33m" + "Envoi de " + str + " à " + port + "\u001B[0m");
    port.writeBytes(bytes, bytes.length);
  }

  public static String received(SerialPort port, SerialPortEvent event) {
    byte[] newData = event.getReceivedData();
    String received = "";
    for (int i = 0; i < newData.length; i++) {
      received += (char)newData[i];
    }
    System.out.println("\u001B[33m" + "Réception de " + received + " de " + port + "\u001B[0m");

    return received;
  }

  public static boolean open(SerialPort port) {
    boolean success = port.openPort();

    if (success) System.out.println("\u001B[36mPort série " + port + " ouvert\u001B[0m");
    else System.out.println("\u001B[36mÉchec de l'ouverture du port série " + port + "\u001B[0m");

    return success;
  }

  public static boolean close(SerialPort port) {
    port.removeDataListener();
    boolean success = port.closePort();

    if (success) System.out.println("\u001B[36mPort série " + port + " fermé\u001B[0m");
    else System.out.println("\u001B[36mÉchec de la fermeture du port série " + port + "\u001B[0m");
    System.out.println();

    return success;
  }

  // Envoie un ping sur le port, et attend une réponse.
  // Si il n'y en a pas au bout de 2 secondes, renvoie false
  public static boolean detectArduino(SerialPort port) {
    port.setBaudRate(115200);
    boolean opened = Serial.open(port);
    if (!opened) return false;

    AtomicBoolean gotResponse = new AtomicBoolean(false);

    // Écoute une réponse
    port.addDataListener(new SerialPortDataListener() {
      @Override
      public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
      }

      @Override
      public void serialEvent(SerialPortEvent event) {
        String data = Serial.received(port, event);
        gotResponse.set(data.charAt(0) == 'r');
        synchronized(gotResponse) {
          gotResponse.notify();
        }
      }
    });

    // Timeout
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.schedule(() -> {
      gotResponse.set(false);
      synchronized(gotResponse) {
        gotResponse.notify();
      }
    }, 2, TimeUnit.SECONDS);


    // Ping
    write(port, "p");

    // Attend le retour (timeout ou réponse)
    synchronized (gotResponse) {
      try {
        gotResponse.wait();
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }

    executorService.shutdown();
    Serial.close(port);

    return gotResponse.get();
  }
}
