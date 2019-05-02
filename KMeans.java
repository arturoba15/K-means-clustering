import org.jfree.chart.JFreeChart;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.Random;
import java.util.Arrays;
import java.lang.Math;

class KMeans {
    private File file;
    private int seed;
    private int k, attr1, attr2;
    private double[][] means;   // Matriz que contiene los valores de los centroides
    private int[][] mode;   // Matriz que contiene arreglos usados por cada cluster para determinar la moda
    private int[] belongsTo; // Arreglo que indica a que cluster pertenece cada item
    private int[] clusterSizes;
    private int iterations;

    /**
     * Constructor
     * Inicializa los centroides con valores del archivo escogidos aleatoriamente
     *
     * @param file Nombre del archivo de datos
     * @param seed Semilla de generación de números aleatorios
     * @param k Número de centroides
     */
    KMeans(File file, int seed, int k, int attr1, int attr2, int maxIterations) {
        DataReader r = new DataReader(file);
        this.file = file;
        this.seed = seed;
        this.k = k;
        belongsTo = new int[r.nData];
        Arrays.fill(belongsTo, -1);
        clusterSizes = new int[k];

        if(r.attrType[attr1] == 0 && r.attrType[attr2] == 0) {
            this.attr1 = attr1;
            this.attr2 = attr2;
            means = getRandomCentroids(this.attr1, this.attr2);
            calculateMeans(maxIterations);
        } else {
            if(r.attrType[attr1] == 0 && r.attrType[attr2] > 0) {
                this.attr1 = attr1;
                this.attr2 = attr2;
            } else if(r.attrType[attr1] > 0 && r.attrType[attr2] == 0) {
                this.attr1 = attr2;
                this.attr2 = attr1;
            }
            mode = new int[k][r.attrType[this.attr2]];
            means = getRandomCentroids(this.attr1, this.attr2);
            calculateMeansMix(maxIterations);
        }
    }

    /**
     * Constructor
     * Inicializa los centroides con valores del archivo que se proporciona
     *
     * @param file Nombre del archivo de datos
     * @param centroids Arreglo con las líneas de los centroides en el archivo de datos
     */
    KMeans(File file, int[] centroids, int attr1, int attr2, int maxIterations) {
        DataReader r = new DataReader(file);
        this.file = file;
        this.k = centroids.length;
        this.attr1 = attr1;
        this.attr2 = attr2;
        belongsTo = new int[r.nData];
        Arrays.fill(belongsTo, -1);
        clusterSizes = new int[k];
        means = initCentroids(centroids, attr1, attr2);

        if(r.attrType[attr1] == 0 && r.attrType[attr2] == 0) {
            this.attr1 = attr1;
            this.attr2 = attr2;
            calculateMeans(maxIterations);
        } else {
            if(r.attrType[attr1] == 0 && r.attrType[attr2] > 0) {
                this.attr1 = attr1;
                this.attr2 = attr2;
            } else if(r.attrType[attr1] > 0 && r.attrType[attr2] == 0) {
                this.attr1 = attr2;
                this.attr2 = attr1;
            }
            mode = new int[k][r.attrType[this.attr2]];
            calculateMeansMix(maxIterations);
        }
    }

    /**
     * Sirve para inicializar los centroides
     * Escoje la linea de donde se tomarán los centroides de cada cluster, desde 1 al número de datos
     * Usa el atributo K de esta clase para saber cuantos centorides se necesitan
     *
     * @return Matriz con los valores de los centroides
     */
    private double[][] getRandomCentroids(int attr1, int attr2) {
        int count = 0, line;
        boolean flag = true;
        Random rand = new Random(seed);
        DataReader r = new DataReader(file);
        int[] lineNum = new int[k];
        double[][] centXY = new double[k][2];
        double[] fLine;
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

        /* Llena la matriz con los números de línea generados */
        count = 0;
        for(int ln = 0; ln < r.nData && count < k; ln++) {
            fLine = r.formatDouble(r.readLine());
            if(lineNum[count] == ln) {      // Si nos encontramos en la línea indicada
                centXY[count][0] = fLine[attr1];
                centXY[count][1] = fLine[attr2];
                count++;
            }
        }

        return centXY;
    }

    /**
     * Sirve para inicializar los centroides a los del arreglo que se le pasa
     *
     * @param lines Cadena con las líneas a buscar como centroides
     * @return Matriz con los valores de los centroides
     */
    private double[][] initCentroids(int[] lines, int attr1, int attr2) {
        DataReader r = new DataReader(file);
        this.k = lines.length;

        double[][] centXY = new double[k][2];
        double[] fLine;
        int count = 0;
        for(int ln = 0; ln < r.nData && count < k; ln++) {
            fLine = r.formatDouble(r.readLine());
            if(lines[count] == ln + 1) {      // Si nos encontramos en la línea indicada (contando desde 1)
                centXY[count][0] = fLine[attr1];
                centXY[count][1] = fLine[attr2];
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
        return Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    }

    /**
     * Actualiza la posición de los clusters a el promedio de todos sus puntos asignados
     * @param n Número de elementos en el cluster
     * @param meanIndex Índice del punto mean que se va a actualizar
     * @param item Arreglo con los valores X y Y del elemento
     *
     */
    private void updateMean(int n , int meanIndex, double[] item) {
      double m;
      for(int i = 0; i < 2; i++) {
          m = means[meanIndex][i];
          m = m + (item[i] - m) / n;
          means[meanIndex][i] = m;
      }
    }


    /**
     * Decide el mejor centroide para un punto de datos (el más cercano)
     * Toma en cuenta la matriz de means que pertenece a la clase
     * @param dpointX Punto de datos, valor X
     * @param dpointY Punto de datos, valor Y
     * @return El índice del centroide que mas cerca este del punto
     *
     */
    private int classify(double dpointX, double dpointY) {
        double dis;
        int index = -1;
        double min = Double.MAX_VALUE;
        for(int i = 0; i < k; i++) {
            dis = distance(means[i][0], means[i][1], dpointX, dpointY);
            if(dis < min) {
                min = dis;
                index = i;
            }
        }

        return index;
    }


    /**
     * Lee cada uno de los datos y lo clasifica en "belongsTo" según los means
     * @param maxIterations Número máximo de iteraciones a correr
     *
     */
    private void calculateMeans(int maxIterations) {
        DataReader r = new DataReader(file);
        boolean noChange;
        double[] item = new double[2];
        double[] fLine;
        int index;
        for(int i = 0; i < maxIterations; i++) {
            noChange = true;
            iterations = i + 1;
            for(int j = 0; j < r.nData; j++) {
                fLine = r.formatDouble(r.readLine());
                item[0] = fLine[attr1];
                item[1] = fLine[attr2];
                index = classify(item[0], item[1]);             // Clasifica item en un cluster
                if(index != belongsTo[j])                       // Si no cambió de cluster, su tamaño se mantiene
                    clusterSizes[index]++;
                updateMean(clusterSizes[index], index, item);   // Actualiza el mean con base en el nuevo item

                if(index != belongsTo[j]) {       // Si el item cambió de cluster
                    noChange = false;
                    if(belongsTo[j] != -1)      // Si el elemento pertenece a algún cluster, reducimos su cluster en 1
                        clusterSizes[belongsTo[j]]--;
                }

                belongsTo[j] = index;
            }
            r.reStart(true);          // Regresar al inicio del archivo
            if(noChange)                      // Si ningún cluster cambió, terminamos
                break;
        }

        // Se crea el archivo de salida
        try (
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("./genFiles/res-" + (attr1+1) + "-" + (attr2+1) + "-" + file.getName())))
                ) {
            writer.print("Archivo de resultados\n\n");
            writer.print("Atributos " + (attr1+1) + " y " + (attr2+1) + "\n\n");
            writer.print("Número de iteraciones: " + iterations + "\n\n");

            writer.print("Centroides obtenidos:\n");
            for(int i = 0; i < means.length; i++) {
                writer.print("Centroide " + (i+1) + ": ");
                for(int j = 0; j < means[i].length; j++)
                    writer.print(means[i][j] + " ");
                writer.print("\n");
            }

            writer.print("\nDistrbución de elementos:\n");
            for(int i = 0; i < k; i++)
                writer.print("Cluster " + (i+1) + ": " + clusterSizes[i] + "\n");

            writer.print("\nDetalles del cluster:\n");
            for(int i = 0; i < k; i++) {
                writer.print("Cluster " + (i+1) + ": ");
                for(int j = 0; j < belongsTo.length; j++) {
                    if(belongsTo[j] == i)
                        writer.print(j + ", ");
                }
                writer.print("\n");
            }

        } catch(IOException e) {e.printStackTrace();}
    }

    /**
     * Obtiene el valor máximo de una columna
     * @param attr Número del atributo (columna)
     * @return El valor máximo de la columna
     */
    private double getMax(int attr) {
        DataReader r = new DataReader(file);
        double max = 0;
        double val;
        for(int i = 0; i < r.nData; i++) {
            val = r.formatDouble(r.readLine())[attr];
            if(val > max)
                max = val;
        }
        return max;
    }

    /**
     * Obtiene el valor mínimo de una columna
     * @param attr Número del atributo (columna)
     * @return El valor mínimo de la columna
     */
    private double getMin(int attr) {
        DataReader r = new DataReader(file);
        double min = Double.MAX_VALUE;
        double val;
        for(int i = 0; i < r.nData; i++) {
            val = r.formatDouble(r.readLine())[attr];
            if(val < min)
                min = val;
        }
        return min;
    }


    /**
     * Genera un valor que determina que tan lejos esta un punto de otro
     * Usado para atributos de diferente tipo
     * @param num1 Valor numérico del atributo 1
     * @param nom1 Valor nominal del atributo 1
     * @param num2 Valor numérico del atributo 2
     * @param nom2 Valor nominal del atributo 2
     * @return Un número que representa la lejanía de los 2 puntos
     *
     * RECORDAR QUE ATTR1 SIEMPRE DEBE SER EL NUMERICO
     */
    private double distanceMix(double num1, int nom1, double num2, int nom2, double max, double min) {
        double nomres = 0;
        if(nom1 != nom2)
            nomres = 1;

        return Math.abs(num1 - num2) / (max - min) + nomres;
    }


    /**
     * Actualiza el valor del centroide con datos mezclados
     * @param n Tamaño del cluster
     * @param meanIndex Índice del cluster
     * @param item Punto que se añade al cluster
     * @param mode Arreglo con los contadores para determinar la moda
     */
    private void updateMeanMix(int n, int meanIndex, double[] item, int[] mode) {
        double m;
        int mod = 0;
        m = means[meanIndex][0];
        m = m + (item[0] - m) / n;
        means[meanIndex][0] = m;

        for(int i = 0; i < mode.length; i++)
            if(mode[i] > mod)
                mod = i;
        means[meanIndex][1] = mod;
        mode[(int)item[1]]++;
    }

    /**
     * Encuentra al índice del cluster que está mas cerca del valor
     * @param numeric Valor numérico
     * @param nominal Valor nominal
     * @return El índice del cluster seleccionado
     */
    private int classifyMix(double numeric, double nominal, double maxV, double minV) {
        double dis;
        int index = -1;
        double min = Double.MAX_VALUE;
        for(int i = 0; i < k; i++) {
            dis = distanceMix(means[i][0], (int)means[i][1], numeric, (int)nominal, maxV, minV);
            if(dis < min) {
                min = dis;
                index = i;
            }
        }

        return index;
    }


    private void calculateMeansMix(int maxIterations) {
        DataReader r = new DataReader(file);
        boolean noChange;
        double[] item = new double[2];
        double[] fLine;
        int index;
        for(int i = 0; i < maxIterations; i++) {
            noChange = true;
            iterations = i + 1;

            for(int b = 0; b < mode.length; b++)            // Reiniciar la matriz de modas
                for(int v = 0; v < mode[b].length; v++)
                    mode[b][v] = 0;

            double max = getMax(attr1);
            double min = getMin(attr1);
            for(int j = 0; j < r.nData; j++) {
                fLine = r.formatDouble(r.readLine());
                item[0] = fLine[attr1];
                item[1] = fLine[attr2];
                index = classifyMix(item[0], item[1], max, min);             // Clasifica item en un cluster
                if(index != belongsTo[j])                       // Si no cambió de cluster, su tamaño se mantiene
                    clusterSizes[index]++;
                updateMeanMix(clusterSizes[index], index, item, mode[index]);   // Actualiza el mean con base en el nuevo item

                if(index != belongsTo[j]) {       // Si el item cambió de cluster
                    noChange = false;
                    if(belongsTo[j] != -1)      // Si el elemento pertenece a algún cluster, reducimos su cluster en 1
                        clusterSizes[belongsTo[j]]--;
                }

                belongsTo[j] = index;
            }

            r.reStart(true);          // Regresar al inicio del archivo
            if(noChange)                      // Si ningún cluster cambió, terminamos
                break;
        }

        // Se crea el archivo de salida
        try (
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("./genFiles/res-" + (attr1+1) + "-" + (attr2+1) + "-" + file.getName())))
        ) {
            writer.print("Archivo de resultados\n\n");
            writer.print("Atributos " + (attr1+1) + " y " + (attr2+1) + "\n\n");
            writer.print("Número de iteraciones: " + iterations + "\n\n");

            writer.print("Centroides obtenidos:\n");
            for(int i = 0; i < means.length; i++) {
                writer.print("Centroide " + (i+1) + ": ");
                for(int j = 0; j < means[i].length; j++)
                    writer.print(means[i][j] + " ");
                writer.print("\n");
            }

            writer.print("\nDistrbución de elementos:\n");
            for(int i = 0; i < k; i++)
                writer.print("Cluster " + (i+1) + ": " + clusterSizes[i] + "\n");

            writer.print("\nDetalles del cluster:\n");
            for(int i = 0; i < k; i++) {
                writer.print("Cluster " + (i+1) + ": ");
                for(int j = 0; j < belongsTo.length; j++) {
                    if(belongsTo[j] == i)
                        writer.print(j + ", ");
                }
                writer.print("\n");
            }

        } catch(IOException e) {e.printStackTrace();}
    }


    /**
     * Crea una gráfica a partir de los centroides y puntos
     * @return Una JFreeChart para poder usarla en un ChartPanel
     */
    public JFreeChart graph() {
        Grapher g = new Grapher("", attr1, attr2, k);
        DataReader r = new DataReader(file);
        double[] fLine;

        for(int i = 0; i < k; i++)      // Añadimos los means primero
            g.addTuple(means[i][0], means[i][1], i);

        for(int i = 0; i < r.nData; i++) {
            fLine = r.formatDouble(r.readLine());
            g.addTuple(fLine[attr1], fLine[attr2], belongsTo[i]);
        }

        return g.endData();
    }
}
