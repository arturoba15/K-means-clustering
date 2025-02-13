import javax.swing.*;

public class ClusterP {
  public static void main(String[] args) {
    // Paso 1. Descubrir relevancia de atributos (atributos mas correlacionados)
    // Paso 2. A partir de esa relevancia, generar un nuevo archivo con los atributos importantes
    // Paso 3. Dar opción de normalizar esos atributos importantes
    // Paso 4. Hacer kmeans a los datos normalizados
    JFrame frame = new MainFrame("Kmeans");

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        frame.setSize(1100, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      }
    });
  }
}