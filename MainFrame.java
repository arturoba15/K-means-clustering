import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Random;

public class MainFrame extends JFrame {
    public File dataFile, centroidFile; // Archivo de datos, archivo con los centroides (opcional)
    public int normOption, centroidOption, k, seed;          // Tipo de normalización 0-zscore 1-minmax 2-decimal 3-no
                                                             // centroidOption -> 0-Aleatorio, 1-Texto, 2-Archivo

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

        ButtonGroup centroids = new ButtonGroup();
        JRadioButton randomCentroid = new JRadioButton("Aleatorios");
        JRadioButton textCentroid = new JRadioButton("Por línea de texto");
        JRadioButton fileCentroid = new JRadioButton("Por archivo");
        randomCentroid.setSelected(true);           // Aleatorios como la opción default
        centroids.add(randomCentroid); centroids.add(textCentroid); centroids.add(fileCentroid);

        ButtonGroup normalization = new ButtonGroup();
        JRadioButton zScore = new JRadioButton("Z-score");
        JRadioButton minMax = new JRadioButton("Min Max");
        JRadioButton decimal = new JRadioButton("Decimal");
        JRadioButton noNorm = new JRadioButton("Sin normalizar");
        zScore.setSelected(true);                   // Zscore como la opción default
        normalization.add(zScore); normalization.add(minMax); normalization.add(decimal); normalization.add(noNorm);

        JTextField textCent = new JTextField("1,2,3");
        JTextField kCentroids = new JTextField("K");
        JButton centroidButton = new JButton("Cargar archivo");
        JButton run = new JButton("Iniciar");

        JFileChooser fc = new JFileChooser();

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
        content.add(new Label("Hola"), BorderLayout.CENTER);

        /* Añadir comportamiento */
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(dataFile != null) {               // Checar que el archivo haya sido cargado
                    warningText.setText("");
                    if(randomCentroid.isSelected()) {  // Checar que se de el número de centroides con la semilla
                        if(kCentroids.getText().matches("(\\d+,\\d+){1}")) {
                            warningText.setText("");
                            System.out.println(kCentroids.getText());
                            String[] numstring = kCentroids.getText().split(",");
                            int[] kSeed = new int[2];
                            kSeed[0] = Integer.parseInt(numstring[0]);
                            kSeed[1] = Integer.parseInt(numstring[1]);
                            switch (normOption) {
                                case 0:
                                    Redundancy r = new Redundancy(dataFile.getName());
                                    Normalizer n = new Normalizer(dataFile.getName());
                                    String kmeansFile = n.zScore();
                                    for(int i = 0; i < r.pairs.length; i++)
                                        new KMeans(kmeansFile, kSeed[1], kSeed[0], r.pairs[i][0], r.pairs[i][1], 20).graphKMeans();
                                    break;
                            }
                        }
                        else
                            warningText.setText("   Formato: <k>, <semilla>");
                    } else if(textCentroid.isSelected()) {              // Checar que la cadena tenga el formato correcto
                        if(textCent.getText().matches("(\\d+,)+\\d+")) {
                            warningText.setText("");
                            System.out.println(textCent.getText());
                        } else
                            warningText.setText("   Formato: 1,2,3...,12");
                    } else if(fileCentroid.isSelected()) { // Checar que el archivo con los centroides haya sido cargado
                        if(centroidFile != null) {
                            warningText.setText("");
                            System.out.println(centroidFile.getName());
                        } else
                            warningText.setText("    Elija archivo de centroides");
                    }
                }
                else
                    warningText.setText("   Cargue un archivo");
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
