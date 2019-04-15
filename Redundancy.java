import java.io.*;
import java.util.*;

/**
 * Esta clase contiene métodos para ver el grado de correlación entre 2
 * atributos, ya sea entre 2 numéricos o 2 nominales, regresa los índices de
 * los atributos que vale la pena graficar juntos porque están fuertemente
 * correlacionados
 */

class Redundancy {
    private String file;
    public int[] attrIndex;            // Índice de los atributos en su archivo original
    public int[][] pairs;              // Contiene a las parejas de atributos que están mas fuertemente correlacionados

    /**
     * Constructor
     *
     */
    Redundancy(String file) {
        this.file = file;
        int num = 10;
        int[][] rNominal, rNumeric;
        if(num % 2 != 0) {
            rNominal = getRelevantNominal(num/2);
            rNumeric = getRelevantNumeric(num/2 + 1);
        } else {
            rNominal = getRelevantNominal(num/2);
            rNumeric = getRelevantNumeric(num/2);
        }
        genFile(rNominal, rNumeric);
    }

    /**
     * Descubre una correlación entre 2 atributos nominales a través de la prueba
     * de Chi cuadrada, si chi cuadrada es mayor que el valor de la tabla,
     * entonces decimos que los 2 atributos son dependientes y que son útiles
     *
     * @param r     Objeto para leer los datos del archivo
     * @param attr1 Atributo 1 a comparar
     * @param attr2 Atributo 2 a comparar
     * @return -1 si uno de los atributos es numérico, el valor de chi cuadrada
     * si los 2 atributos son nominales
     *
     */
    private double chiSquared(DataReader r, int attr1, int attr2) {
        if (r.attrType[attr1] > 0 && r.attrType[attr2] > 0) {       // Checa si los atributos son nominales
            int attr1N = r.attrType[attr1];     // numero de posibles valores
            int attr2N = r.attrType[attr2];
            String line;
            int v1, v2;                         // valores de los atributos
            int[] fLine;
            int[][] obs = new int[attr1N][attr2N];      // Matriz de las observaciones

            while ((line = r.readLine()) != null) {     // Se llena la matriz con las observaciones
                fLine = r.formatInt(line);
                v1 = fLine[attr1];
                v2 = fLine[attr2];
                obs[v1][v2]++;
            }

            int[] totalAttr1 = new int[attr1N];     // Suma de todos los valores de cada fila
            int[] totalAttr2 = new int[attr2N];     // Suma de todos los valores de cada columna

            for (int i = 0; i < attr1N; i++)
                for (int j = 0; j < attr2N; j++)
                    totalAttr1[i] += obs[i][j];

            for (int j = 0; j < attr2N; j++)
                for (int i = 0; i < attr1N; i++)
                    totalAttr2[j] += obs[i][j];

            double nDataTuples = attr1N * attr2N;       // Se calculan los valores esperados
            double[][] exp = new double[attr1N][attr2N];
            for (int i = 0; i < attr1N; i++)
                for (int j = 0; j < attr2N; j++)
                    exp[i][j] = totalAttr1[i] * totalAttr2[j] / nDataTuples;

            double num;
            double chiSquared = 0;
            // Grado de libertad @TODO talvez agregar que cheque si pasa el valor requerido
            // int df = (attr1N-1)*(attr2N-1);
            // double chiHyp = getChiValue(df);
            for (int i = 0; i < attr1N; i++)        // Cálculo de chi cuadrada
                for (int j = 0; j < attr2N; j++)
                    if (exp[i][j] != 0) {           // si el esperado es 0, no se cuenta
                        num = obs[i][j] - exp[i][j];
                        chiSquared += num * num / exp[i][j];
                    }

            r.reStart(true);            // Volvemos al inicio del archivo
            return chiSquared;
        }

        return -1.0;
    }

    /**
     * Decide cuales son los atributos nominales mas relevantes
     *
     * @param bestN Numero de mejores pares de atributos
     * @return Una matriz con los indices de los pares de atributos relevantes
     * para graficar
     *
     */
    private int[][] getRelevantNominal(int bestN) {
        DataReader r = new DataReader(file);
        double[] best = new double[bestN];
        int[][] partialBestPair = new int[bestN][2];
        int finalCounter = bestN;
        double chi;
        for (int i = 0; i < r.nAttr; i++)
            for (int j = 0; j < r.nAttr; j++)
                if (i < j) {
                    chi = chiSquared(r, i, j);
                    if (best[bestN - 1] < chi) {
                        for (int p = 0; p < bestN; p++)
                            if (best[p] < chi) {
                                best[p] = chi;
                                partialBestPair[p][0] = i;
                                partialBestPair[p][1] = j;
                                break;
                            }
                    }
                }

        for(int i = 0; i < partialBestPair.length; i++)             // Hacer un nuevo arreglo solo con valores válidos
            if(partialBestPair[i][0] == 0 && partialBestPair[i][1] == 0)
                finalCounter--;

        int[][] bestPair = new int[finalCounter][2];
        for (int i = 0; i < finalCounter; i++) {                    // Crear arreglo final solo con valores válidos
            bestPair[i][0] = partialBestPair[i][0];
            bestPair[i][1] = partialBestPair[i][1];
        }

        return bestPair;
    }

    /**
     * Descubre una correlación entre 2 atributos numéricos a través de un
     * coeficiente de correlación
     *
     * @param bestN Numero de mejores pares de atributos
     * @return Una matriz con los indices de los pares de atributos relevantes
     * para graficar. Si no aparecen algunos pares, es porque su
     * coeficiente no es mayor a 0
     *
     */
    private int[][] getRelevantNumeric(int bestN) {
        DataReader r = new DataReader(file);
        String line;
        double[] fLine;
        double[] variance = new double[r.nAttr];
        double[] stdDev = new double[r.nAttr];
        double[] mean = meanNum();
        double limit = 0.5;

        while ((line = r.readLine()) != null) {     // Cálculo de varianza
            fLine = r.formatDouble(line);
            for (int i = 0; i < r.nAttr; i++)
                if (r.attrType[i] == 0)
                    variance[i] += (fLine[i] - mean[i]) * (fLine[i] - mean[i]);
        }

        for (int i = 0; i < r.nAttr; i++)
            if (variance[i] != 0)
                variance[i] /= r.nData;

        for (int i = 0; i < r.nAttr; i++)           // Cálculo de la desviación estándar
            if (variance[i] != 0)
                stdDev[i] = Math.sqrt(variance[i]);

        r.reStart(true);                 // Regresa a inicio para calcular el coeficiente

        double acc;
        double nAB;                                 // n * media de atributo A * media de atributo B
        int[][] partialBestPair = new int[bestN][2];
        double[] best = new double[bestN];
        int finalCounter = bestN;

        for (int i = 0; i < partialBestPair.length; i++) {  // Inicializar los valores en -1
            for (int j = 0; j < partialBestPair[i].length; j++)
                partialBestPair[i][j] = 0;
        }

        for (int i = 0; i < r.nAttr; i++)
            for (int j = 0; j < r.nAttr; j++)
                if (i < j)
                    if (r.attrType[i] == 0 && r.attrType[j] == 0) {
                        acc = 0;
                        nAB = r.nData * mean[i] * mean[j];
                        while ((line = r.readLine()) != null) {
                            fLine = r.formatDouble(line);
                            acc += fLine[i] * fLine[j];
                        }
                        acc -= nAB;
                        acc /= r.nData * stdDev[i] * stdDev[j];

                        if (Math.abs(acc) >= limit) {               // Checar que el valor sea mayor al límite
                            if (Math.abs(best[bestN - 1]) < Math.abs(acc)) {
                                for (int p = 0; p < bestN; p++) {
                                    if (Math.abs(best[p]) < Math.abs(acc)) {
                                        best[p] = acc;
                                        partialBestPair[p][0] = i;
                                        partialBestPair[p][1] = j;
                                        break;
                                    }
                                }
                            }

                        }
                    }

        for(int i = 0; i < partialBestPair.length; i++)             // Hacer un nuevo arreglo solo con valores válidos
            if(partialBestPair[i][0] == 0 && partialBestPair[i][1] == 0)
                finalCounter--;

        int[][] bestPair = new int[finalCounter][2];
        for (int i = 0; i < finalCounter; i++) {                    // Crear arreglo final solo con valores válidos
            bestPair[i][0] = partialBestPair[i][0];
            bestPair[i][1] = partialBestPair[i][1];
        }

        return bestPair;
    }


    /**
     * Obtiene el valor de chi que se necesita para rechazar la
     * hipótesis en el nivel de significancia de 0.001.
     * Este valor se obtiene de un archivo con los valores.
     *
     * @param fd Valor del grado de libertad
     * @return El valor mínimo para rechazar la hipótesis
     *
     */
    private double getChiValue(int fd) {
        Scanner sc;
        String line;
        double res = 0;
        int num;
        try (
                BufferedReader in = new BufferedReader(new FileReader("chidistrib.txt"))
        ) {
            while ((line = in.readLine()) != null) {
                sc = new Scanner(line);
                num = sc.nextInt();
                if (num == fd) {
                    res = sc.nextDouble();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Obtiene el promedio de cada columna de atributos numéricos
     *
     * @return Un arreglo con los promedios de cada atributo
     *
     */
    private double[] meanNum() {
        DataReader r = new DataReader(file);
        String line;
        double[] mean = new double[r.nAttr];
        double[] fLine;
        while ((line = r.readLine()) != null) {
            fLine = r.formatDouble(line);
            for (int i = 0; i < r.nAttr; i++)
                if (r.attrType[i] == 0)
                    mean[i] += fLine[i];
        }

        for (int i = 0; i < r.nAttr; i++)
            mean[i] /= r.nData;

        return mean;
    }

    /**
     * Crea un nuevo archivo solo con los atributos importantes
     * Este archivo contiene como cabecera:
     *          Número de datos
     *          Índice de cada atributo (del archivo original)
     *          Tipo de los archivos
     *          Datos
     *
     */
    private void genFile(int[][] bestNominal, int[][] bestNumeric) {
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter("bestAttr" + file))
        ) {
            DataReader r = new DataReader(file);
            String arr;
            pairs = mergeMatrices(bestNominal, bestNumeric);
            writer.write(r.nData + "\n");                                   // Número de datos

            attrIndex = getUniqueAttrs(bestNominal, bestNumeric);
            writer.write(attrIndex.length + "\n");                          // Número de atributos relevantes

            int[] attrType = new int[attrIndex.length];
            for(int i = 0; i < attrIndex.length; i++)
                attrType[i] = r.attrType[attrIndex[i]];
            arr = Arrays.toString(attrType);
            arr = arr.substring(1, arr.length()-1);
            arr = arr.replaceAll("\\s+","") + "\n";
            writer.write(arr);                                                  // Tipo de los atributos

            double[] fLine;
            double[] writeArray = new double[attrIndex.length];
            while((arr = r.readLine()) != null) {                               // Datos
                fLine = r.formatDouble(arr);
                for (int i = 0; i < attrIndex.length; i++)
                    writeArray[i] = fLine[attrIndex[i]];

                arr = "";
                for (int i = 0; i < writeArray.length; i++) {   // Copiar nominales sin punto decimal
                    if(attrType[i] != 0)
                        arr += (int)writeArray[i] + ",";
                    else
                        arr += writeArray[i] + ",";
                }

                arr = arr.substring(0, arr.length()-1);       // Quitar la última coma
                writer.write(arr + "\n");
            }
        } catch(IOException e) {e.printStackTrace();}
    }


    /**
     * Imprime los contenidos de una matriz
     *
     */
    public void printMatrix(int[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                System.out.print(m[i][j] +  " ");
            }
            System.out.println();
        }
    }

    /**
     * Obtiene los índices únicos de 2 matrices
     * @param mat1 Matriz 1
     * @param mat2 Matriz 2
     * @return Un arreglo con los índices únicos de atributos
     *
     */
    private int[] getUniqueAttrs(int[][] mat1, int[][] mat2) {
        Set<Integer> index = new HashSet<>();
        for (int[] i : mat1)
            for(int elem : i)
                index.add(elem);

        for (int[] i : mat2)
            for(int elem : i)
                index.add(elem);

        Set<Integer> indexInteger = new TreeSet<>(index);
        Integer[] m = indexInteger.toArray(new Integer[indexInteger.size()]);
        int[] indexes = new int[m.length];

        for (int i = 0; i < m.length; i++)
            indexes[i] = m[i].intValue();

        return indexes;
    }


    /**
     * Combina las 2 matrices que se pasan como parámetro en una
     * @param mat1 Una matriz de números enteros (índices)
     * @param mat2 Una matriz de números enteros (índices)
     * @return Matriz que contiene los elementos de mat1 y mat2
     */
    private int[][] mergeMatrices(int[][] mat1, int[][] mat2) {
        int[][] retMat = new int[mat1.length + mat2.length][2];
        int in = 0;
        for (int i = 0; i < mat1.length; i++) {
            for (int j = 0; j < mat1[i].length; j++)
                retMat[i][j] = mat1[i][j];
            in++;
        }

        for (int i = in; i < retMat.length; i++)
            for (int j = 0; j < mat2[0].length; j++)
                retMat[i][j] = mat2[i - in][j];

        return retMat;
    }
}
