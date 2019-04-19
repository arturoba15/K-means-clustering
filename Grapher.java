import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

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
  private int attr1, attr2;

  /**
   * Constructor de la clase
   * @param title Título de la gráfica
   * @param attr1 Número del atributo que se está representando
   * @param attr2 Número del atributo que se está representando
   * @param k Número de clusters
   *
   */
  Grapher(String title, int attr1, int attr2, int k) {
    super(title);
    xLabel = "Atributo "  + attr1;
    yLabel = "Atributo "  + attr2;
    this.attr1 = attr1;
    this.attr2 = attr2;
    titlew = title;
    dataset = new XYSeriesCollection();
    d = new XYSeries[k];
    for(int i = 0; i < d.length; i++)
      d[i] = new XYSeries(i);
  }

  /**
   * Añade una tupla de datos al set indicado (corresponde a 2 atributos)
   * @param val1 Valor del 1er atributo
   * @param val2 Valor del 2ndo atributo
   * @param index Indica a que cluster pertenece este punto
   *
   */
  public void addTuple(double val1, double val2, int index) { d[index].add(val1, val2); }

  /**
   * Marca que ya no se registrarán mas datos y crea una imagen con la gráfica de dispersión
   */
  public void endData() {
    for(XYSeries i : d)
      dataset.addSeries(i);
    JFreeChart chart = ChartFactory.createScatterPlot(titlew, xLabel, yLabel, dataset);
    XYPlot xyPlot = (XYPlot) chart.getPlot();
    xyPlot.setRenderer(new XYLineAndShapeRenderer(false, true) {  // Hace una cruz en el centroide
      @Override
      public Shape getItemShape(int row, int col) {
        if (col == 0) {
          return ShapeUtilities.createDiagonalCross(8, 2);
        } else {
          return super.getItemShape(row, col);
        }
      }
    });

    try {
      ChartUtilities.saveChartAsJPEG(new File(attr1 + "-" + attr2 + ".jpg"), chart, 800, 600);
    } catch (IOException e) {};

  }
}
