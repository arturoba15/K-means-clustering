import org.jfree.chart.JFreeChart;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Arrays;
import java.lang.Math;

class KMeans {
    private String file;
    private int seed;
    private int k, attr1, attr2;
    private double[][] means;
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
    KMeans(String file, int seed, int k, int attr1, int attr2, int maxIterations) {
        DataReader r = new DataReader(file);
        this.file = file;
        this.seed = seed;
        this.k = k;
        this.attr1 = attr1;
        this.attr2 = attr2;
        belongsTo = new int[r.nData];
        Arrays.fill(belongsTo, -1);
        clusterSizes = new int[k];
        means = getRandomCentroids(attr1, attr2);
        calculateMeans(maxIterations);
    }

    /**
     * Constructor
     * Inicializa los centroides con valores del archivo que se proporciona
     *
     * @param file Nombre del archivo de datos
     * @param centroids Arreglo con las líneas de los centroides en el archivo de datos
     */
    KMeans(String file, int[] centroids, int attr1, int attr2, int maxIterations) {
        DataReader r = new DataReader(file);
        this.file = file;
        this.k = centroids.length;
        this.attr1 = attr1;
        this.attr2 = attr2;
        belongsTo = new int[r.nData];
        for(int i = 0; i < belongsTo.length; i++)
            belongsTo[i] = -1;
        clusterSizes = new int[k];
        means = initCentroids(centroids, attr1, attr2);
        calculateMeans(maxIterations);
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
    private void updateMean(int n, int meanIndex, double[] item) {
        double m;
        for(int i = 0; i < 2; i++) {
            m = means[meanIndex][i];
            m = (m * (n-1) + item[i]) / n;
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
                BufferedWriter writer = new BufferedWriter(new FileWriter("resultados"))
                ) {
            writer.write("Archivo de resultados\n\n");
            writer.write("Atributos " + (attr1+1) + " y " + (attr2+1) + "\n\n");
            writer.write("Número de iteraciones: " + iterations + "\n\n");

            writer.write("Centroides obtenidos:\n");
            for(int i = 0; i < means.length; i++) {
                writer.write("Centroide " + (i+1) + ": ");
                for(int j = 0; j < means[i].length; j++)
                    writer.write(means[i][j] + " ");
                writer.write("\n");
            }

            writer.write("\nDistrbución de elementos:\n");
            for(int i = 0; i < k; i++)
                writer.write("Cluster " + (i+1) + ": " + clusterSizes[i] + "\n");

            writer.write("\nDetalles del cluster:\n");
            for(int i = 0; i < k; i++) {
                writer.write("Cluster " + (i+1) + ": ");
                for(int j = 0; j < belongsTo.length; j++) {
                    if(belongsTo[j] == i)
                        writer.write(j + ", ");
                }
                writer.write("\n");
            }

        } catch(IOException e) {e.printStackTrace();}
    }


    public JFreeChart graph() {
        Grapher g = new Grapher(attr1 + " " + attr2, attr1, attr2, k);
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
