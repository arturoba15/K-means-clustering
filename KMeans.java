// @TODO opcion para eliminar outliers con zscore
// @TODO generar graficas e irlas guardando para mostrarlas cuando se cambien los atributos comparados
// @TODO los atributos mas relevantes para graficar son los mas correlacionados
// @TODO Cambiar tipos de variables si se refiere a valor nominal
// @TODO Arreglo para mostrar a que centroide está asignado cada dato
// @TODO no se pueden graficar puntos nominales SOLO GRAFICAR ATRIBUTOS NOMINALES
// @TODO funcion overlap para mezclar nominales y numericos
// @TODO haciendo interfaz gráfica ver que no haya errores por cualquier entrada de datos
// Dar a elegir: cuantos centroides aleatorios
//               numero de los elementos (linea) con los que quieres iniciar
// Primero, ver cuales vale la pena
// con respecto a los resultados, comenzamos a procesar lo demás

import java.util.Random;
import java.util.Arrays;
import java.lang.Math;

class KMeans {
  private String file;
  private int seed;
  private int k;

  KMeans(String file, int seed, int k) {
    this.file = file;
    this.seed = seed;
    this.k = k;
  }

  KMeans(String file, int seed) {
    this.file = file;
    this.seed = seed;
  }

  /**
   * Sirve para inicializar los centroides
   * Escoje la linea de donde se tomarán los centroides
   * de cada cluster, desde 1 al número de datos
   * Regresa las lineas en un arreglo ordenado
   * Usa el atributo K de esta clase para saber cuantos centorides se necesitan
   * @return Matriz con los valores de los centroides
   */
  private double[][] getRandomCentroids() {
    int count = 0, line;
    boolean flag = true;
    Random rand = new Random(seed);
    DataReader r = new DataReader(file);
    int[] lineNum = new int[k];
    double[][] centXY = new double[k][r.nAttr];
    double[] fLine = new double[r.nAttr];
    while(count < k) {                  // Llena lineNum con numeros aleatorios
      line = rand.nextInt(r.nData);
      for(int i = 0; i < lineNum.length && flag; i++)
        if(lineNum[i] == line)
          flag = false;
      if(flag) {                     // Si no está repetido, lo añade al arreglo
        lineNum[count] = line;
        count++;
      }
      flag = true;
    }
    Arrays.sort(lineNum);

    count = 0;
    /* Llena la matriz con los números de línea generados */
    for(int ln = 0; ln < r.nData && count < k; ln++) {
      fLine = r.formatDouble(r.readLine());
      if(lineNum[count] == ln) {      // Si nos encontramos en la línea indicada
        for(int i = 0; i < r.nAttr; i++)
          centXY[count][i] = fLine[i];
        count++;
      }
    }

    return centXY;
  }

  /**
   * Sirve para inicializar los centroides (inicializa k)
   * Recibe una cadena con las líneas de los centroides
   * Se llama cuando el constructor no recibe los centroides
   * @param lines Cadena con las líneas a buscar como centroides
   * @return Matriz con los valores de los centroides
   */
  private double[][] initCentroids(String lines) {
    DataReader r = new DataReader(file);
    String[] lineS = lines.split(",");
    int[] lineNum = new int[lineS.length];
    this.k = lineNum.length;

    for(int i = 0; i < lineS.length; i++)
      lineNum[i] = Integer.parseInt(lineS[i]);

    double[][] centXY = new double[k][r.nAttr];
    double[] fLine = new double[r.nAttr];
    int count = 0;
    for(int ln = 0; ln < r.nData && count < k; ln++) {
      fLine = r.formatDouble(r.readLine());
      if(lineNum[count] == ln) {      // Si nos encontramos en la línea indicada
        for(int i = 0; i < r.nAttr; i++)
          centXY[count][i] = fLine[i];
        count++;
      }
    }

    return centXY;
  }

  /**
   * Calcula la distancia entre 2 puntos
   * @return Distancia entre 2 "puntos"
   */
  private double distance(double x1, double y1, double x2, double y2) {
    return Math.abs(Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) ));
  }

  /**
   * Decide el mejor centroide para un punto de datos (el más cercano)
   * @param cents Arreglo que contiene los centroides
   * @param attr1 Índice del atributo 1 que se va a comparar
   * @param attr2 Índice del atributo 2 que se va a comparar
   * @param dpointX Punto de datos, valor X
   * @param dpointY Punto de datos, valor Y
   * @return El índice del centroide que mas cerca este del punto
   */
  private int clasify(double[][] cents, int attr1, int attr2, double dpointX, double dpointY) {
    // Checamos la diferencia entre todos los centroides
    double dis;
    int centIndex = -1;
    double min = Double.MAX_VALUE;
    for(int i = 0; i < k; i++) {
      dis = distance(cents[i][attr1], cents[i][attr2], dpointX, dpointY);
      if(dis < min) {
        min = dis;
        centIndex = i;
      }
    }

    return centIndex;
   }

  /**
   * Extrae 2 atributos de un arreglo de valores
   * @param arr Arreglo del que se van a extraer los atributos
   * @param attr1 Atributo 1 a extraer
   * @param attr2 Atributo 2 a extraer
   * @return Regresa un arreglo con los 2 atributos
   */
  private double[] getAttr(double[] arr, int attr1, int attr2) {
    return new double[] {arr[attr1], arr[attr2]};
  }
}
