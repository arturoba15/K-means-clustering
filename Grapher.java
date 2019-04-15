import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Clase para hacer gráficas de dispersión
 */

class Grapher extends JFrame{
  private XYSeriesCollection dataset;
  private XYSeries d;
  private String xLabel;
  private String yLabel;
  private String titlew;
  private JFreeChart chart;

  /**
   * Constructor de la clase
   */
  Grapher(String title, int attr1, int attr2) {
    super(title);
    xLabel = "Atributo "  + attr1;
    yLabel = "Atributo "  + attr2;
    titlew = title;
    dataset = new XYSeriesCollection();
    d = new XYSeries(".");
  }

  /**
   * Añade una tupla de datos (corresponde a 2 atributos)
   */
  public void addTuple(double val1, double val2) {
    d.add(val1, val2);
  }

  /**
   * Marca que ya no se registrarán mas datos y crea una imagen con la gráfica de dispersión
   */
  public void endData() {
    dataset.addSeries(d);
    chart = ChartFactory.createScatterPlot(titlew, xLabel, yLabel, dataset);
    try {
      ChartUtilities.saveChartAsJPEG(new File("plot2.jpg"), chart, 800, 600);
    } catch (IOException e) {};

  }
}
