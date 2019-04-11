import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Contiene métodos para normalizar
 * Solo normaliza los valores numéricos
 */
class Normalizer {
  private static final String dir = "./genFiles/";
  private String file;

  Normalizer(String file) {
    this.file = file;
  }

  /**
   * Normaliza los datos con Min Max y los escribe a
   * un nuevo archivo llamado minmax-<file>
   * Regresa el nombre del archivo como cadena
   * @return Una cadena con el nombre del archivo normalizado
   */
  public String minMax() {
    DataReader r = new DataReader(file);

    // Encuentra maximo y minimo de cada fila
    double[] fLine = new double[r.nAttr];
    double[] max = new double[r.nAttr];
    double[] min = new double[r.nAttr];
    String line = r.readLine();
    fLine = r.formatDouble(line);
    System.arraycopy(fLine, 0, min, 0, fLine.length);
    do {
      fLine = r.formatDouble(line);
      for(int i = 0; i < r.nAttr; i++)
        if(r.attrType[i] == 0) {
          max[i] = max(fLine[i], max[i]);
          min[i] = min(fLine[i], min[i]);
        }
      line = r.readLine();
    } while(line != null);

    // Calcula el valor normalizado y lo escribe a un archivo
    try (
    BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "minmax-" + file));
    ) {
      String[] res = new String[r.nAttr];
      r.reStart(false);
      // Copiar cabeceras
      for(int i = 0; i < 3; i++)
        writer.write(r.readLine() + "\n");

      while((line = r.readLine()) != null) {
        fLine = r.formatDouble(line);
        for (int i = 0; i < r.nAttr; i++) {
          if(r.attrType[i] == 0)
            res[i] = Double.toString((fLine[i] - min[i]) / (max[i] - min[i]));
          else
            res[i] = Integer.toString((int)fLine[i]);
        }
        writer.write(String.join(",", res) + "\n");
      }
    } catch(IOException e) {e.printStackTrace();}

    return dir + "minmax-" + file;
  }

  /**
   * Normaliza los datos con zScore y los escribe a
   * un nuevo archivo llamado zscore-<file>
   * Regresa el nombre del archivo como cadena
   * @return Una cadena con el nombre del archivo normalizado
   */
  public String zScore() {
    DataReader r = new DataReader(file);
    String line;

    // Obtenemos promedio de cada atributo
    double[] meanAcc = new double[r.nAttr];
    double[] mean = new double[r.nAttr];
    double[] fLine = new double[r.nAttr];
    while((line = r.readLine()) != null) {
      fLine = r.formatDouble(line);
      for (int i = 0; i < r.nAttr; i++) {
        if(r.attrType[i] != 0)
          continue;
        meanAcc[i] += fLine[i];
      }
    }

    for(int i = 0; i < r.nAttr; i++) {
      if(r.attrType[i] != 0)
        continue;
      mean[i] = meanAcc[i] / r.nData;
    }

    // Obtenemos varianza
    r.reStart(true);
    double[] vz = new double[r.nAttr];
    while((line = r.readLine()) != null) {
      fLine = r.formatDouble(line);
      for (int i = 0; i < r.nAttr; i++) {
        if(r.attrType[i] != 0)
          continue;
        vz[i] += (fLine[i] - mean[i]) * (fLine[i] - mean[i]);
      }
    }

    // Obtenemos la desviación estándar con la varianza
    double[] stdDev = new double[r.nAttr];
    for (int i = 0; i < r.nAttr; i++) {
      if(r.attrType[i] != 0)
        continue;
      stdDev[i] = Math.sqrt(vz[i]);
    }

    // Calcula el valor normalizado y lo escribe a un archivo
    try (
    BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "zscore-" + file));
    ) {
      String[] res = new String[r.nAttr];
      r.reStart(false);
      // Copiar cabeceras
      for(int i = 0; i < 3; i++)
        writer.write(r.readLine() + "\n");

      while((line = r.readLine()) != null) {
        fLine = r.formatDouble(line);
        for (int i = 0; i < r.nAttr; i++) {
          if(r.attrType[i] != 0)
            res[i] = Integer.toString((int)fLine[i]);
          else
            res[i] = Double.toString((fLine[i] - mean[i]) / stdDev[i]);
        }
        writer.write(String.join(",", res) + "\n");
      }
    } catch(IOException e) {e.printStackTrace();}

    return dir + "zscore-" + file;
  }

  /**
   * Normaliza los datos con decimal scaling y los
   * escribe a un nuevo archivo llamado decimal-<file>
   * Regresa el nombre del archivo como cadena
   * @return Una cadena con el nombre del archivo normalizado
   */
  public String decimalScaling() {
    DataReader r = new DataReader(file);
    String line;

    // Encuentra el valor máximo
    double[] max = new double[r.nAttr];
    double[] fLine = new double[r.nAttr];
    while((line = r.readLine()) != null) {
      fLine = r.formatDouble(line);
      for (int i = 0; i < r.nAttr; i++) {
        if(r.attrType[i] != 0)
          continue;
        if(fLine[i] > max[i])
          max[i] = fLine[i];
      }
    }

    // Cuenta número de dígitos de cada máximo
    // Guarda el denominador por el que divide cada número
    double[] digits = new double[r.nAttr];
    int n = 10;
    for (int i = 0; i < r.nAttr; i++) {
      if(r.attrType[i] != 0)
        continue;
      for (int j = 0; j < ((int)Math.log10(max[i])); j++)
        n *= 10;
      digits[i] = n;
      n = 10;
    }

    // Calcula el valor normalizado y lo escribe a un archivo
    try (
    BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "decimal-" + file));
    ) {
      String[] res = new String[r.nAttr];
      r.reStart(false);
      // Copiar cabeceras
      for(int i = 0; i < 3; i++)
        writer.write(r.readLine() + "\n");

      while((line = r.readLine()) != null) {
        fLine = r.formatDouble(line);
        for (int i = 0; i < r.nAttr; i++) {
          if(r.attrType[i] != 0)
            res[i] = Integer.toString((int)fLine[i]);
          else
            res[i] = Double.toString(fLine[i] / digits[i]);
        }
        writer.write(String.join(",", res) + "\n");
      }
    } catch(IOException e) {e.printStackTrace();}

    return dir + "decimal-" + file;
  }

  /* Métodos de ayuda */
  private double max(double a, double b) {
    if(a > b)
      return a;
    return b;
  }

  private double min(double a, double b) {
    if(a < b)
      return a;
    return b;
  }
}
