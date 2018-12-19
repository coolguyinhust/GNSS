package signal;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.Arrays;

/**
 * Created by kaixin on 2018/12/13.
 */

/*
 * PSK-R具体信号通常表示为PSk-R(m)的形式，m表示扩频码的速率为1.023MHz的m倍
 * 所以扩频码fc=1.023*m;角频率wc=2*3.14*fc;
 *
 */
public class BPSK {
    private int m;
    //n是一个载波周期内的码片数；即n=载波周期Tc/码片宽度Ts
    private double n;
    private double fc;
    private double wc;
    public BPSK(int m,double n) {
        this.m = m;
        this.fc=m*1.023;
        this.wc=fc*2*Math.PI;
        this.n=n;
    }
    public BPSK(int m) {
        this.m = m;
        this.fc=m*1.023;
        this.wc=fc*2*Math.PI;
    }
    //绘制功率谱密度
    public void paint_frequency(){
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-17.5,17.5,2.5);
        final NumberAxis yAxis = new NumberAxis(-120,0,25);
        //设置横轴和纵轴的标签
        xAxis.setLabel("频率（MHz）");
        yAxis.setLabel("功率谱密度（W/Hz）");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BPSK调制功率谱");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("frequency within 15MHz");
        for(double f = -15; f >= -15 && f <= 15; f = f + 0.01){
            series.getData().add(new XYChart.Data(f ,10*Math.log10( (1/fc)*Math.pow((Math.sin(Math.PI*f/fc))/(Math.PI*f/fc),2) )));
        }
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
        stage.setScene(scene);
        stage.show();
    }

    public void paint_time(int[] array){
        int k=array.length;
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(0,k+0.5,0.5);
        final NumberAxis yAxis = new NumberAxis(-1.5,1.5,0.5);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码片 chips");
        yAxis.setLabel("幅度");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BPSK码元时域图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("time graph with number of "+k+"code element \n"+ Arrays.toString(array));

        for(double t = 0;  t <= k; t = t + 0.001){
            series.getData().add(new XYChart.Data(t , (array[(int)Math.floor(t)])*
                    Math.sin(2*Math.PI*n*t)));
        }
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
        stage.setScene(scene);
        stage.show();
    }

    public void paint_self_correlation(){
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-1.2,1.2,0.2);
        final NumberAxis yAxis = new NumberAxis(-1,1,0.5);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码片 chips");
        yAxis.setLabel("幅度");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BPSK自相关函数图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("self correlation function graph");

        for(double t = -1;  t <= 1; t = t + 0.001){

        }
        for(double t = -1.2;  t <= -1; t = t + 0.001) {
            series.getData().add(new XYChart.Data(t ,0));
        }
        for(double t = 1;  t <= 1.2; t = t + 0.001) {
            series.getData().add(new XYChart.Data(t ,0));
        }

        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
        stage.setScene(scene);
        stage.show();
    }
}
