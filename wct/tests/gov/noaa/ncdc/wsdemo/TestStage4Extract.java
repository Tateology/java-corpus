package gov.noaa.ncdc.wsdemo;

import gov.noaa.ncdc.wsdemo.Stage4Extract.AccumulationType;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.junit.Test;

public class TestStage4Extract {
    
    
    public static void main(String[] args) {
        TestStage4Extract t = new TestStage4Extract();
//        t.testStage4Extract();
        try {
            t.graphStage4Extract();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    @Test
    public void testStage4Extract() {
        Stage4Extract st4 = new Stage4Extract();
        
        String begYYYYMMDD = "20070401";
        String endYYYYMMDD = "20070501";
        double lat = 35.0;
        double lon = -95.0;
        st4.getDailyData(System.out, AccumulationType.TYPE_DAILY, lat, lon, begYYYYMMDD, endYYYYMMDD);
    }

    
    
    
    public void graphStage4Extract() throws MalformedURLException, IOException {
        Stage4Extract st4 = new Stage4Extract();
        TimeSeries st4Series = new TimeSeries("Stage-IV NEXRAD MPE", FixedMillisecond.class); 
        TimeSeries st4TotalSeries = new TimeSeries("Running Total", FixedMillisecond.class); 
        TimeSeriesCollection precipDataset = new TimeSeriesCollection();
        TimeSeriesCollection precipTotalDataset = new TimeSeriesCollection();
        
        
        precipDataset.addSeries(st4Series);
        precipTotalDataset.addSeries(st4TotalSeries);

//        String begYYYYMMDD = "20070401";
//        String endYYYYMMDD = "20070631";
//        File outfile = new File("phase1.png");
//        String begYYYYMMDD = "20070601";
//        String endYYYYMMDD = "20070831";
//        File outfile = new File("phase2.png");
        String begYYYYMMDD = "20070801";
        String endYYYYMMDD = "20071031";
        File outfile = new File("phase3.png");
        double lat = 33.36778;
        double lon = -95.68083;
        
        
        
        
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "NCDC Web Services Demo",      // chart title
                "Date",                      // x axis label
                "Precip (in)",                      // y axis label
                precipDataset,                  // data
                //PlotOrientation.VERTICAL,
                true,                     // include legend
                false,                     // tooltips
                false                     // urls
        );



        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        LegendTitle legend = (LegendTitle) chart.getLegend();

        // get a reference to the plot for further customisation...
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(0, precipDataset);
        plot.setDataset(1, precipTotalDataset);
        NumberAxis totalAxis = new NumberAxis("Total (in)");
        plot.setRangeAxis(1, totalAxis);

        
        plot.getRangeAxis(0).setLabelFont(plot.getRangeAxis(0).getLabelFont().deriveFont(Font.BOLD));
        plot.getRangeAxis(1).setLabelFont(plot.getRangeAxis(1).getLabelFont().deriveFont(Font.BOLD));
        plot.getDomainAxis(0).setLabelFont(plot.getDomainAxis(0).getLabelFont().deriveFont(Font.BOLD));

        
//        rangeAxis.setAutoRangeIncludesZero(false);
//        XYItemRenderer renderer2 = new DefaultXYItemRenderer();
//        renderer2.setSeriesStroke(0, new java.awt.BasicStroke(1.0f));
//        renderer2.setSeriesPaint(0, new Color(155, 0, 155));
//        plot.setRenderer(n, renderer2);
////      plot.setRenderer(n, renderer);
//        plot.setRangeAxis(n, rangeAxis);
//        plot.mapDatasetToRangeAxis(n, n);

        
        
        XYItemRenderer renderer1 = plot.getRenderer(0);
        renderer1.setSeriesStroke(0, new java.awt.BasicStroke(3.0f));
        renderer1.setSeriesPaint(0, Color.RED);
        
        XYItemRenderer renderer2 = new DefaultXYItemRenderer();
        renderer2.setSeriesStroke(0, new java.awt.BasicStroke(3.0f));
        renderer2.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(1, renderer2);
        plot.mapDatasetToRangeAxis(1, 1);


        plot.setBackgroundImage(javax.imageio.ImageIO.read(new URL("http://www.ncdc.noaa.gov/img/noaaseagullbkg.jpg")));
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        // change the auto tick unit selection to integer units only...
//        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        

        chart.getXYPlot().getDomainAxis().setLabel(begYYYYMMDD+" - "+endYYYYMMDD);
        chart.setTitle("Precipiation Estimates (Radar & Gauge)\n  ("+lon+" , "+lat+")");


        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);

        st4.getDailyData(st4Series, st4TotalSeries, AccumulationType.TYPE_DAILY, lat, lon, begYYYYMMDD, endYYYYMMDD);
        
        ChartUtilities.saveChartAsPNG(outfile, chart, 800, 700);
    }

}
