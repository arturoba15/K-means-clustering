import java.awt.Color;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class Grapher extends JFrame{

  public Grapher(String appTitle, String chartTitle, String file, int attr1, int attr2) {
  	super(appTitle);

   	// based on the dataset we create the chart
  	JFreeChart chart = ChartFactory.createScatterPlot(chartTitle, "X-Axis", "Y-Axis", createDataset(file, attr1-1, attr2-1),
  		PlotOrientation.VERTICAL, true, true, true);

  	// Changes background color
  	XYPlot plot = (XYPlot) chart.getPlot();
  	plot.setBackgroundPaint(new Color(255, 228, 196));

  	// Adding chart into a chart panel
  	ChartPanel chartPanel = new ChartPanel(chart);

  	// settind default size
  	chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

  	// add to contentPane
  	setContentPane(chartPanel);
  }

  private XYDataset createDataset(String file, int attr1, int attr2) {
    DataReader r = new DataReader(file);
    String line;
    double[] vals = new double[r.nAttr];
    // create the dataset
    final XYSeriesCollection dataset = new XYSeriesCollection();
    XYSeries xyValues = new XYSeries("ejemplo");
    while((line = r.readLine()) != null) {
      vals = r.formatDouble(line);
      // solo tomamos los 2 que vamos a graficar
      xyValues.add(vals[attr1], vals[attr2]);
    }
    dataset.addSeries(xyValues);

    return dataset;
  }
}
