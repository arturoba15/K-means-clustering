import java.util.regex.Pattern;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Métodos para leer y dar formato al archivo */
class DataReader {
  private File file;
  public int[] attrType;      // 0-> numerico  mayor a 0 ->nominal
  public int nData, nAttr;    // Numero de datos y atributos
  public int nNum;            // Numero de datos numéricos
  private BufferedReader in;
  private static Pattern row = Pattern.compile(",");

  /**
   * Constructor de la clase.
   * @param filename Nombre del archivo
   */
  DataReader(String filename) {
    file = new File(filename);
    readHead();
    nNum = countItem(attrType, 0);
    try {
        in = new BufferedReader(new FileReader(file));
        ignoreHead();
    } catch(IOException e) {e.printStackTrace();}
  }

  /**
   * Lee los encabezados y guarda la informacion en los atributos de la clase
   */
  private void readHead() {
    try {
        in = new BufferedReader(new FileReader(file));
        nData = Integer.parseInt(in.readLine());
        nAttr = Integer.parseInt(in.readLine());
        attrType = formatInt(in.readLine());
        in.close();
    } catch(IOException e) {e.printStackTrace();}
  }

  /**
   * Lee la cabecera del archivo simn guardarla para comenzar a leer datos
   */
  private void ignoreHead() {
    try {
        for(int i = 0; i < 3; i++)
            in.readLine();
    } catch(IOException e) {e.printStackTrace();}
  }

  /**
   * Lee una linea del archivo
   * @return Una linea del archivo como cadena
   */
  public String readLine() {
    String line = null;
    try {
        line = in.readLine();
    } catch(IOException e) {}

    return line;
  }

  /**
   * Cierra el stream y lo vuelve a abrir para comenzar a leer el archivo
   * desde el inicio
   * @param ignoreHead Si es true, ignora la cabecera, si es false se comienza a
   *                   leer el archivo desde la cabecera
   */
  public void reStart(boolean ignoreHead) {
    try {
        in.close();
        in = new BufferedReader(new FileReader(file));
        if(ignoreHead)
          ignoreHead();
    } catch(IOException e) {e.printStackTrace();}
  }

  /**
   * Le da formato a la linea convirtiéndola en datos manejables
   * @param line La cadena de la línea leída del archivo
   * @return Un arreglo con los datos en formato int
   */
  public int[] formatInt(String line) {
    int[] res = new int[nAttr];
    Scanner sc = new Scanner(line);
    sc.useDelimiter(row);
    for (int i = 0; i < nAttr; i++)
        res[i] = Integer.parseInt(sc.next());

    return res;
  }

  /**
   * Le da formato a la linea convirtiéndola en datos manejables
   * @param line La cadena de la línea leída del archivo
   * @return Un arreglo con los datos en formato double
   */
  public double[] formatDouble(String line) {
    double[] res = new double[nAttr];
    Scanner sc = new Scanner(line);
    sc.useDelimiter(row);
    for (int i = 0; i < nAttr; i++)
        res[i] = Double.parseDouble(sc.next());

    return res;
  }

  /**
  * Cuenta el número de veces que aparece un número
  * en un arreglo. Se usa para contar el número de atributos de un tipo
  * @param attrType Arreglo con los tipos de atributos
  * @param num Número a buscar en el arreglo
  * @return Número de veces que aparece un número en el arreglo
  */
  public int countItem(int[] attrType, int num) {
    int count = 0;
    for(int x : attrType)
      if(x == num)
        count++;

    return count;
  }

}
