import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.util.Arrays;

/* Esta clase contiene métodos para ver el grado de correlación entre 2
 * atributos, ya sea entre 2 numéricos o 2 nominales, regresa los índices de
 * los atributos que vale la pena graficar juntos porque están fuertemente
 * correlacionados
 */

class Redundancy {
  private String file;

  Redundancy(String file) {
    this.file = file;
  }

    /**
     * Descubre una correlación entre 2 atributos nominales a través de la prueba
     * de Chi cuadrada, si chi cuadrada es mayor que el valor de la tabla,
     * entonces decimos que los 2 atributos son dependientes y que son útiles
     * @param r Objeto para leer los datos del archivo
     * @param attr1 Atributo 1 a comparar
     * @param attr2 Atributo 2 a comparar
     * @return -1 si uno de los atributos es numérico, el valor de chi cuadrada
     *         si los 2 atributos son nominales
     */
    private double chiSquared(DataReader r, int attr1, int attr2) {
        // Primero checamos si los atributos son nominales
        if(r.attrType[attr1] > 0 && r.attrType[attr2] > 0) {
            int attr1N = r.attrType[attr1]; // numero de posibles valores
            int attr2N = r.attrType[attr2];
            String line;
            int v1, v2; // valores de los atributos
            int[] fLine = new int[r.nAttr];
            // Matriz de las observaciones
            int[][] obs = new int[attr1N][attr2N];

            // Se llena la matriz con las observaciones
            while((line = r.readLine()) != null) {
                fLine = r.formatInt(line);
                v1 = fLine[attr1];
                v2 = fLine[attr2];
                obs[v1][v2]++;
            }

            // Suma de todos los valores de cada fila -> totalAttr1
            // Suma de todos los valores de cada columna -> totalAttr2
            int[] totalAttr1 = new int[attr1N];
            int[] totalAttr2 = new int[attr2N];

            for(int i = 0; i < attr1N; i++)
                for(int j = 0; j < attr2N; j++)
                    totalAttr1[i] += obs[i][j];

            for(int j = 0; j < attr2N; j++)
                for(int i = 0; i < attr1N; i++)
                    totalAttr2[j] += obs[i][j];

            // Se calculan los valores esperados
            double nDataTuples = attr1N * attr2N;
            double[][] exp = new double[attr1N][attr2N];
            for (int i = 0; i < attr1N; i++)
                for (int j = 0; j < attr2N; j++)
                    exp[i][j] = totalAttr1[i] * totalAttr2[j] / nDataTuples;

            // Calculamos chi cuadrada
            double num;
            double chiSquared = 0;
            // Grado de libertad
            // int df = (attr1N-1)*(attr2N-1);
            // double chiHyp = getChiValue(df);
            for (int i = 0; i < attr1N; i++)
                for (int j = 0; j < attr2N; j++)
                    if(exp[i][j] != 0) {    // si el esperado es 0 no se cuenta
                      num = obs[i][j] - exp[i][j];
                      chiSquared += num * num / exp[i][j];
                    }

            // Volvemos al inicio del archivo
            r.reStart(true);
            return chiSquared;
        }

        return -1.0;
    }

    /**
     * Decide cuales son los atributos nominales mas relevantes
     * @param bestN Numero de mejores pares de atributos
     * @return Una matriz con los indices de los pares de atributos relevantes
     *         para graficar
     */
    public int[][] getRelevantNominal(int bestN) {
      DataReader r = new DataReader(file);
      double[] best = new double[bestN];
      int[][] bestPair = new int[bestN][2];
      double chi = 0;
      for (int i = 0; i < r.nAttr; i++)
        for (int j = 0; j < r.nAttr; j++)
          if(i < j) {
              chi = chiSquared(r, i, j);
              if(best[bestN - 1] < chi) {
                for(int p = 0; p < bestN; p++)
                  if(best[p] < chi) {
                    best[p] = chi;
                    bestPair[p][0] = i;
                    bestPair[p][1] = j;
                    break;
                  }
              }
          }

      // for(int i = 0; i < bestN; i++) {
      //   for(int j = 0; j < 2; j++)
      //     System.out.print(bestPair[i][j] + " ");
      //   System.out.print(best[i]);
      //   System.out.println();
      // }

      return bestPair;
    }

    /**
     * Descubre una correlación entre 2 atributos numéricos a través de un
     * coeficiente de correlación
     * @param bestN Numero de mejores pares de atributos
     * @return Una matriz con los indices de los pares de atributos relevantes
     *         para graficar. Si no aparecen algunos pares, es porque su
     *         coeficiente no es mayor a 0
     */
    public int[][] correlationCoeff(int bestN) {
      DataReader r = new DataReader(file);
      String line;
      double[] fLine = new double[r.nAttr];
      double[] variance = new double[r.nAttr];
      double[] stdDev = new double[r.nAttr];
      double[] correlationCoeff = new double[r.nAttr];
      double[] mean = meanNum();
      while((line = r.readLine()) != null) {  // Cálculo de varianza
        fLine = r.formatDouble(line);
        for(int i = 0; i < r.nAttr; i++)
          if(r.attrType[i] == 0)
            variance[i] += (fLine[i] - mean[i]) * (fLine[i] - mean[i]);
      }

      for(int i = 0; i < r.nAttr; i++)
        if(variance[i] != 0)
          variance[i] /= r.nData;

      for(int i = 0; i < r.nAttr; i++)      // Cálculo de la desviación estándar
        if(variance[i] != 0)
          stdDev[i] = Math.sqrt(variance[i]);

      r.reStart(true);    // Leer de nuevo para calcular el coeficiente

      double acc;
      double nAB;     // n * media de atributo A * media de atributo B
      int[][] bestPair = new int[bestN][2];
      double[] best = new double[bestN];

      for(int i = 0; i < bestPair.length; i++) {  // Inicializar los valores en -1
        for(int j = 0; j < bestPair[i].length; j++)
          bestPair[i][j] = -1;
      }

      for(int i = 0; i < r.nAttr; i++) {
        for(int j = 0; j < r.nAttr; j++) {
          if(i < j) {
            if(r.attrType[i] == 0 && r.attrType[j] == 0) {
              acc = 0;
              nAB = r.nData * mean[i] * mean[j];
              while((line = r.readLine()) != null) {
                fLine = r.formatDouble(line);
                acc += fLine[i] * fLine[j];
              }
              acc -= nAB;
              acc /= r.nData * stdDev[i] * stdDev[j];
              // System.out.printf("i:%d j:%d -> %f\n", i, j, acc);
              if(acc > 0) {
                if(best[bestN - 1] < acc) {
                  for(int p = 0; p < bestN; p++) {
                    if(best[p] < acc) {
                      best[p] = acc;
                      bestPair[p][0] = i;
                      bestPair[p][1] = j;
                      break;
                    }
                  }
                }

              }
            }
          }
        }
      }

      return bestPair;
    }


    /**
     * Obtiene el valor de chi que se necesita para rechazar la
     * hipótesis en el nivel de significancia de 0.001.
     * Este valor se obtiene de un archivo con los valores.
     * @param fd Valor del grado de libertad
     * @return El valor mínimo para rechazar la hipótesis
     */
    private double getChiValue(int fd) {
      Scanner sc;
      String line;
      double res = 0;
      int num;
      try (
        BufferedReader in = new BufferedReader(new FileReader("chidistrib.txt"));
      ) {
        line = in.readLine();
        while((line = in.readLine()) != null) {
          sc = new Scanner(line);
          num = sc.nextInt();
          if(num == fd) {
            res = sc.nextDouble();
            break;
          }
        }
      } catch(IOException e) { e.printStackTrace(); }

      return res;
    }

    /**
     * Obtiene el promedio de cada columna de atributos numéricos
     * @return Un arreglo con los promedios de cada atributo
     */
    private double[] meanNum() {
      DataReader r = new DataReader(file);
      String line;
      double[] mean = new double[r.nAttr];
      double[] fLine = new double[r.nAttr];
      while((line = r.readLine()) != null) {
        fLine = r.formatDouble(line);
        for(int i = 0; i < r.nAttr; i++)
          if(r.attrType[i] == 0)
            mean[i] += fLine[i];
      }

      for(int i = 0; i < r.nAttr; i++)
        mean[i] /= r.nData;

      return mean;
    }

}
