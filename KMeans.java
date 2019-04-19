// @TODO Cambiar tipos de variables si se refiere a valor nominal
// @TODO Arreglo para mostrar a que centroide está asignado cada dato
// @TODO no se pueden graficar puntos nominales SOLO GRAFICAR ATRIBUTOS NOMINALES
// @TODO funcion overlap para mezclar nominales y numericos
// @TODO haciendo interfaz gráfica ver que no haya errores por cualquier entrada de datos
// @TODO Checar que desde la interfaz gráfica, se lea que la línea de centroides no es correcta

import javax.xml.crypto.Data;
import java.util.ArrayList;
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
        clusterSizes = new int[k];
        means = getRandomCentroids(attr1, attr2);
        calculateMeans(maxIterations);
        graphKMeans();
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
        clusterSizes = new int[k];
        means = initCentroids(centroids, attr1, attr2);
        calculateMeans(maxIterations);
        graphKMeans();
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
        double[] fLine = new double[r.nAttr];
        int count = 0;
        for(int ln = 0; ln < r.nData && count < k; ln++) {
            fLine = r.formatDouble(r.readLine());
            if(lines[count] == ln) {      // Si nos encontramos en la línea indicada
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
            for (int p = 0; p < means.length; p++) {
                for (int pp = 0; pp < means[p].length; pp++) {
                    System.out.print(means[p][pp] +  " ");
                }
                System.out.println();
            }
            System.out.println("-----------------");
            noChange = true;
            for(int j = 0; j < r.nData; j++) {
                fLine = r.formatDouble(r.readLine());
                item[0] = fLine[attr1];
                item[1] = fLine[attr2];
                index = classify(item[0], item[1]);             // Clasifica item en un cluster
                clusterSizes[index]++;                          // Actualiza el tamaño del cluster
                updateMean(clusterSizes[index], index, item);   // Actualiza el mean con base en el nuevo item

                if(index != belongsTo[j])        // Si el item cambió de cluster
                    noChange = false;

                belongsTo[j] = index;
            }
            r.reStart(true);          // Regresar al inicio del archivo
            if(noChange)                        // Si ningún cluster cambió, terminamos
                break;
        }
    }


    public void graphKMeans() {
        Grapher g = new Grapher("Ejemplo", attr1, attr2, k);
        DataReader r = new DataReader(file);
        double[] fLine;
        for(int i = 0; i < r.nData; i++) {
            fLine = r.formatDouble(r.readLine());
            g.addTuple(fLine[attr1], fLine[attr2], belongsTo[i]);
        }
        g.endData();
    }
}
