import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MainFrame extends JFrame {
    public File dataFile, centroidFile; // Archivo de datos, archivo con los centroides (opcional)
    public int normOption, centroidOption, seed;          // Tipo de normalización 0-zscore 1-minmax 2-decimal 3-no
                                                             // centroidOption -> 0-Aleatorio, 1-Texto, 2-Archivo
    private JFreeChart[] charts;

    public MainFrame(String title) {
        super(title);
        JPanel content = new JPanel();  // content -> Panel principal, cubre toda la ventana
        content.setLayout(new BorderLayout());      // Definir el Layout Manager
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        /* Crear componentes de Swift */
        JButton dataFileButton = new JButton("Cargar archivo de datos");
        JLabel warningText = new JLabel("");

        JPanel subLeftPanel = new JPanel();
        GridLayout grid = new GridLayout(0, 2);
        grid.setVgap(10);
        subLeftPanel.setLayout(grid);

        ButtonGroup centroids = new ButtonGroup();  // Botones de selección de centroides
        JRadioButton randomCentroid = new JRadioButton("Aleatorios");
        JRadioButton textCentroid = new JRadioButton("Por línea de texto");
        JRadioButton fileCentroid = new JRadioButton("Por archivo");
        randomCentroid.setSelected(true);
        centroids.add(randomCentroid); centroids.add(textCentroid); centroids.add(fileCentroid);

        ButtonGroup normalization = new ButtonGroup();  // Botones de selección de normalización
        JRadioButton zScore = new JRadioButton("Z-score");
        JRadioButton minMax = new JRadioButton("Min Max");
        JRadioButton decimal = new JRadioButton("Decimal");
        JRadioButton noNorm = new JRadioButton("Sin normalizar");
        zScore.setSelected(true);
        normalization.add(zScore); normalization.add(minMax); normalization.add(decimal); normalization.add(noNorm);

        JTextField textCent = new JTextField("1,2,3");
        JTextField kCentroids = new JTextField("K");
        JButton centroidButton = new JButton("Cargar archivo");
        JButton run = new JButton("Iniciar");
        JList<String> toggleAttributes = new JList<>();         // Lista con los atributos a seleccionar para la gráfica
        toggleAttributes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toggleAttributes.setFixedCellHeight(20);

        JFileChooser fc = new JFileChooser();
        JFreeChart useless = new JFreeChart(new Plot() {
            @Override
            public String getPlotType() {
                return null;
            }

            @Override
            public void draw(Graphics2D graphics2D, Rectangle2D rectangle2D, Point2D point2D, PlotState plotState, PlotRenderingInfo plotRenderingInfo) {}
        });
        ChartPanel chart = new ChartPanel(useless);
        chart.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


        /* Añadir componentes al panel */
        subLeftPanel.add(dataFileButton);
        subLeftPanel.add(warningText);
        subLeftPanel.add(new JLabel("Obtener centroides:"));
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(randomCentroid);
        subLeftPanel.add(kCentroids);
        subLeftPanel.add(textCentroid);
        subLeftPanel.add(textCent);
        subLeftPanel.add(fileCentroid);
        subLeftPanel.add(centroidButton);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(new Label("Normalizar datos:"));
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(zScore);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(minMax);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(decimal);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(noNorm);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(run);

        content.add(subLeftPanel, BorderLayout.LINE_START);
        content.add(chart, BorderLayout.CENTER);
        content.add(toggleAttributes, BorderLayout.LINE_END);

        /* Añadir comportamiento */
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File dir = new File("./genFiles/");
                dir.mkdir();
                File[] files = dir.listFiles();
                for(File file : files)
                    file.delete();

                if(dataFile != null) {               // Checar que el archivo haya sido cargado
                    warningText.setText("");
                    if(randomCentroid.isSelected()) {  // Checar que se de el número de centroides con la semilla
                        if(kCentroids.getText().matches("(\\d+,\\d+){1}")) {
                            warningText.setText("");
                            String[] numstring = kCentroids.getText().split(",");
                            int[] kSeed = new int[2];
                            kSeed[0] = Integer.parseInt(numstring[0]);
                            kSeed[1] = Integer.parseInt(numstring[1]);
                            DataReader dr = new DataReader(dataFile);
                            Redundancy r = new Redundancy(dataFile);
                            Normalizer n = new Normalizer(dataFile);
                            String[] list;
                            File kmeansFile = new File(".");

                            int len = 0;
                            for(int i = 0; i < r.pairs.length; i++)
                                if(dr.attrType[r.pairs[i][0]] == 0)
                                    len++;

                            list = new String[len];
                            charts = new JFreeChart[len];
                            int index = 0;
                            for(int i = 0; i < r.pairs.length; i++) {
                                if(dr.attrType[r.pairs[i][0]] == 0) {
                                    list[index] = (r.pairs[i][0] + 1) + "-" + (r.pairs[i][1] + 1);
                                    index++;
                                }
                            }

                            toggleAttributes.setListData(list);

                            switch (normOption) {
                                case 0:
                                    kmeansFile = n.zScore();
                                    break;
                                case 1:
                                    kmeansFile = n.minMax();
                                    break;
                                case 2:
                                    kmeansFile = n.decimalScaling();
                                    break;
                                case 3:
                                    kmeansFile = dataFile;
                                    break;
                            }
                            index = 0;
                            boolean flag = true;
                            for (int i = 0; i < r.attrs.length; i++)
                                for (int j = 0; j < r.attrs.length; j++) {
                                    if (i < j) {
                                        if (!(dr.attrType[r.attrs[i]] > 0 && dr.attrType[r.attrs[j]] > 0)) {
                                            if (dr.attrType[r.attrs[i]] == 0 && dr.attrType[r.attrs[j]] == 0) {
                                                for(int y = 0; y < r.pairs.length; y++) {
                                                    if(r.pairs[y][0] == r.attrs[i] && r.pairs[y][1] == r.attrs[j]) {
                                                        charts[index] = new KMeans(kmeansFile, kSeed[1], kSeed[0], r.attrs[i], r.attrs[j], 20).graph();
                                                        index++;
                                                        flag = false;
                                                        break;
                                                    }
                                                }

                                                if(flag) {
                                                    new KMeans(kmeansFile, kSeed[1], kSeed[0], r.attrs[i], r.attrs[j], 20);
                                                    flag = true;
                                                }
                                            } else {
                                                new KMeans(kmeansFile, kSeed[1], kSeed[0], r.attrs[i], r.attrs[j], 20);
                                            }
                                        }
                                    }
                                }

                            if(charts.length > 0) {
                                chart.setChart(charts[0]);
                                toggleAttributes.setSelectedIndex(0);
                            }
                        }
                        else
                            warningText.setText("   Formato: <k>, <semilla>");
                    } else if(textCentroid.isSelected()) {             // Checar que la cadena tenga el formato correcto
                        if(textCent.getText().matches("(\\d+,)+\\d+")) {
                            warningText.setText("");
                            String[] strCentroids = textCent.getText().split(",");
                            int[] centroids = new int[strCentroids.length];
                            for(int i = 0; i < centroids.length; i++)
                                centroids[i] = Integer.parseInt(strCentroids[i]); // Las líneas se cuentan desde 1

                            Arrays.sort(centroids);

                            DataReader dr = new DataReader(dataFile);
                            Redundancy r = new Redundancy(dataFile);
                            Normalizer n = new Normalizer(dataFile);
                            String[] list;
                            File kmeansFile = new File(".");

                            int len = 0;
                            for(int i = 0; i < r.pairs.length; i++)
                                if(dr.attrType[r.pairs[i][0]] == 0)
                                    len++;

                            list = new String[len];
                            charts = new JFreeChart[len];
                            int index = 0;
                            for(int i = 0; i < r.pairs.length; i++) {
                                if(dr.attrType[r.pairs[i][0]] == 0) {
                                    list[index] = (r.pairs[i][0] + 1) + "-" + (r.pairs[i][1] + 1);
                                    index++;
                                }
                            }

                            toggleAttributes.setListData(list);

                            switch (normOption) {
                                case 0:
                                    kmeansFile = n.zScore();
                                    break;
                                case 1:
                                    kmeansFile = n.minMax();
                                    break;
                                case 2:
                                    kmeansFile = n.decimalScaling();
                                    break;
                                case 3:
                                    kmeansFile = dataFile;
                                    break;
                            }

                            index = 0;
                            boolean flag = true;
                            for (int i = 0; i < r.attrs.length; i++)
                                for (int j = 0; j < r.attrs.length; j++) {
                                    if (i < j) {
                                        if (!(dr.attrType[r.attrs[i]] > 0 && dr.attrType[r.attrs[j]] > 0)) {
                                            if (dr.attrType[r.attrs[i]] == 0 && dr.attrType[r.attrs[j]] == 0) {
                                                for(int y = 0; y < r.pairs.length; y++) {
                                                    if(r.pairs[y][0] == r.attrs[i] && r.pairs[y][1] == r.attrs[j]) {
                                                        charts[index] = new KMeans(kmeansFile, centroids, r.attrs[i], r.attrs[j], 20).graph();
                                                        index++;
                                                        flag = false;
                                                        break;
                                                    }
                                                }

                                                if(flag) {
                                                    new KMeans(kmeansFile, centroids, r.attrs[i], r.attrs[j], 20);
                                                    flag = true;
                                                }
                                            } else {
                                                new KMeans(kmeansFile, centroids, r.attrs[i], r.attrs[j], 20);
                                            }
                                        }
                                    }
                                }

                            if(charts.length > 0) {
                                chart.setChart(charts[0]);
                                toggleAttributes.setSelectedIndex(0);
                            }


                        } else
                            warningText.setText("   Formato: 1,2,3...,12");
                    } else if(fileCentroid.isSelected()) { // Checar que el archivo con los centroides haya sido cargado
                        if(centroidFile != null) {
                            warningText.setText("");
                            try (
                                    BufferedReader buff = new BufferedReader(new FileReader(centroidFile.getAbsolutePath()));
                                    ) {
                                String centroidLine = buff.readLine();
                                if(centroidLine.matches("(\\d+,)+\\d+")) {
                                    String[] strCentroids = centroidLine.split(",");
                                    int[] centroids = new int[strCentroids.length];
                                    for(int i = 0; i < centroids.length; i++)
                                        centroids[i] = Integer.parseInt(strCentroids[i]); // Las líneas se cuentan desde 1

                                    Arrays.sort(centroids);

                                    DataReader dr = new DataReader(dataFile);
                                    Redundancy r = new Redundancy(dataFile);
                                    Normalizer n = new Normalizer(dataFile);
                                    String[] list;
                                    File kmeansFile = new File(".");

                                    int len = 0;
                                    for(int i = 0; i < r.pairs.length; i++)
                                        if(dr.attrType[r.pairs[i][0]] == 0)
                                            len++;

                                    list = new String[len];
                                    charts = new JFreeChart[len];
                                    int index = 0;
                                    for(int i = 0; i < r.pairs.length; i++) {
                                        if(dr.attrType[r.pairs[i][0]] == 0) {
                                            list[index] = (r.pairs[i][0] + 1) + "-" + (r.pairs[i][1] + 1);
                                            index++;
                                        }
                                    }

                                    toggleAttributes.setListData(list);

                                    switch (normOption) {
                                        case 0:
                                            kmeansFile = n.zScore();
                                            break;
                                        case 1:
                                            kmeansFile = n.minMax();
                                            break;
                                        case 2:
                                            kmeansFile = n.decimalScaling();
                                            break;
                                        case 3:
                                            kmeansFile = dataFile;
                                            break;
                                    }

                                    index = 0;
                                    boolean flag = true;
                                    for (int i = 0; i < r.attrs.length; i++)
                                        for (int j = 0; j < r.attrs.length; j++) {
                                            if (i < j) {
                                                if (!(dr.attrType[r.attrs[i]] > 0 && dr.attrType[r.attrs[j]] > 0)) {
                                                    if (dr.attrType[r.attrs[i]] == 0 && dr.attrType[r.attrs[j]] == 0) {
                                                        for(int y = 0; y < r.pairs.length; y++) {
                                                            if(r.pairs[y][0] == r.attrs[i] && r.pairs[y][1] == r.attrs[j]) {
                                                                charts[index] = new KMeans(kmeansFile, centroids, r.attrs[i], r.attrs[j], 20).graph();
                                                                index++;
                                                                flag = false;
                                                                break;
                                                            }
                                                        }

                                                        if(flag) {
                                                            new KMeans(kmeansFile, centroids, r.attrs[i], r.attrs[j], 20);
                                                            flag = true;
                                                        }
                                                    } else {
                                                        new KMeans(kmeansFile, centroids, r.attrs[i], r.attrs[j], 20);
                                                    }
                                                }
                                            }
                                        }

                                    if(charts.length > 0) {
                                        chart.setChart(charts[0]);
                                        toggleAttributes.setSelectedIndex(0);
                                    }
                                } else
                                    warningText.setText("   Formato: 1,2,3...,12");


                            } catch(IOException e) { e.printStackTrace(); }
                        } else
                            warningText.setText("    Elija archivo de centroides");
                    }
                }
                else
                    warningText.setText("   Cargue un archivo");
            }
        });

        toggleAttributes.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if(toggleAttributes.getSelectedIndex() >= 0)
                    chart.setChart(charts[toggleAttributes.getSelectedIndex()]);
            }
        });

        dataFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showOpenDialog(content);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    dataFile = fc.getSelectedFile();
                    dataFileButton.setText(dataFile.getName());
                }
            }
        });

        centroidButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showOpenDialog(content);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    centroidFile = fc.getSelectedFile();
                    fileCentroid.setSelected(true);
                    if(centroidFile.getName().length() > 20)
                        centroidButton.setText(centroidFile.getName().substring(0, 20) + "...");
                    else
                        centroidButton.setText(centroidFile.getName());

                }
            }
        });

        textCent.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent actionEvent) {
                    textCentroid.setSelected(true);
            }

            public void focusLost(FocusEvent actionEvent) {}
        });

        kCentroids.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent actionEvent) {
                randomCentroid.setSelected(true);
                kCentroids.setText("");
                Random r = new Random();
                seed = r.nextInt(500);
            }

            public void focusLost(FocusEvent actionEvent) {}
        });

        randomCentroid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                centroidOption = 0;
            }
        });

        textCentroid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                centroidOption = 1;
            }
        });

        fileCentroid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                centroidOption = 2;
            }
        });

        zScore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                normOption = 0;
            }
        });

        minMax.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                normOption = 1;
            }
        });

        decimal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                normOption = 2;
            }
        });

        noNorm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                normOption = 3;
            }
        });

        setContentPane(content);
    }

}
