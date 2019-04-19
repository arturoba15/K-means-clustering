import javax.swing.*;

public class ClusterP {
  public static void main(String[] args) {
    // Paso 1. Descubrir relevancia de atributos (atributos mas correlacionados)
    // Paso 2. A partir de esa relevancia, generar un nuevo archivo con los atributos importantes
    // Paso 3. Dar opción de normalizar esos atributos importantes
    // Paso 4. Hacer kmeans a los datos normalizados
    /*Redundancy r = new Redundancy("FlagData.txt");
    r.printMatrix(r.pairs);*/
    //JFrame frame = new MainFrame("Kmeans");
    KMeans k = new KMeans("bestAttrFlagData.txt", 124, 5, 0, 1, 10);

    /*SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      }
    });*/
  }
}
