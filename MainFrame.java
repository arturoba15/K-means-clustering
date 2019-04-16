import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainFrame extends JFrame {
    public File dataFile, centroidFile;
    public String centroidOption;

    public MainFrame(String title) {
        super(title);
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());      // Definir el Layout Manager
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        /* Crear componentes de Swift */
        JLabel titleCent = new JLabel("Obtener centroides:");
        JButton dataFileButton = new JButton("Cargar archivo de datos");

        JPanel subLeftPanel = new JPanel();
        GridLayout grid = new GridLayout(9, 2);
        grid.setVgap(10);
        subLeftPanel.setLayout(grid);

        JRadioButton randomCentroid = new JRadioButton("Aleatorios");
        randomCentroid.setSelected(true);       // Aleatorios como la opción default
        JRadioButton textCentroid = new JRadioButton("Por línea de texto");
        JRadioButton fileCentroid = new JRadioButton("Por archivo");
        ButtonGroup centroids = new ButtonGroup();
        centroids.add(randomCentroid); centroids.add(textCentroid); centroids.add(fileCentroid);

        JTextField textCent = new JTextField("1,2,3");
        JButton centroidButton = new JButton("Cargar archivo");

        JFileChooser fc = new JFileChooser();

        /* Añadir componentes al panel */
        subLeftPanel.add(dataFileButton);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(titleCent);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(randomCentroid);
        subLeftPanel.add(new Label(""));    // Celda vacía
        subLeftPanel.add(textCentroid);
        subLeftPanel.add(textCent);
        subLeftPanel.add(fileCentroid);
        subLeftPanel.add(centroidButton);

        content.add(new Label("Hola"), BorderLayout.CENTER);
        content.add(subLeftPanel, BorderLayout.LINE_START);

        /* Añadir comportamiento */
        dataFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showOpenDialog(content);
                if(returnVal == JFileChooser.APPROVE_OPTION)
                    dataFile = fc.getSelectedFile();
            }
        });

        centroidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showOpenDialog(content);
                if(returnVal == JFileChooser.APPROVE_OPTION)
                    centroidFile = fc.getSelectedFile();
            }
        });

        randomCentroid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                centroidOption = "random";
            }
        });

        textCentroid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                centroidOption = "text";
            }
        });

        fileCentroid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                centroidOption = "file";
            }
        });

        setContentPane(content);
    }

}
