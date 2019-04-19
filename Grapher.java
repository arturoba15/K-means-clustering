import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Clase para hacer gráficas de dispersión
 */

class Grapher extends JFrame{
  private XYSeriesCollection dataset;
  private XYSeries[] d;
  private String xLabel;
  private String yLabel;
  private String titlew;
  private JFreeChart chart;

  /**
   * Constructor de la clase
   */
  Grapher(String title, int attr1, int attr2, int k) {
    super(title);
    xLabel = "Atributo "  + attr1;
    yLabel = "Atributo "  + attr2;
    titlew = title;
    dataset = new XYSeriesCollection();
    d = new XYSeries[k];
    for(int i = 0; i < d.length; i++)
      d[i] = new XYSeries(i);
  }

  /**
   * Añade una tupla de datos al set 1 (corresponde a 2 atributos)
   */
  public void addTuple(double val1, double val2, int index) { d[index].add(val1, val2); }

  /**
   * Marca que ya no se registrarán mas datos y crea una imagen con la gráfica de dispersión
   */
  public void endData() {
    for(XYSeries i : d)
      dataset.addSeries(i);
    chart = ChartFactory.createScatterPlot(titlew, xLabel, yLabel, dataset);
    try {
      ChartUtilities.saveChartAsJPEG(new File("plot2.jpg"), chart, 800, 600);
    } catch (IOException e) {};

  }
}
