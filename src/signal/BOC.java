package signal;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static java.lang.Math.pow;

/**
 * Created by kaixin on 2018/12/13.
 */
/*
 * BOC调制有两个参数，记为BOC(α,β);
 * 副载波频率fs=α*f0,扩频码速率fc=β*f0;f0为基频，默认为1.023MHz
 * n=2fs/fc=2α/β
 * 由时域表达式，Ct压载波周期为2Ts,Ts=1/(2fs),
 */
public class BOC{
    private double x;
    private double y;
    private double fs;
    private double fc;
    private int n;
    //br是BOC的限制带宽，如果带宽为-1，默认为无限制带宽
    private double br=-1;
    public BOC(double x, double y,double br) {
        this.x = x;
        this.y = y;
        this.fs = x*1.023;
        this.fc = y*1.023;
        this.n=(int)(2*fs/fc);
        this.br=br;
    }
    public BOC(double x, double y) {
        this.x = x;
        this.y = y;
        this.fs = x*1.023;
        this.fc = y*1.023;
        this.n=(int)(2*fs/fc);
    }
    /*
     *
     */
    public void self_correlation(){
        double p=x/y;
        int k;
        double a=0;
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

        lineChart.setTitle("BOC自相关函数图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("self correlation function graph");
        if(br==-1) {
            for (double t = -1; t <= 1; t = t + 0.001) {
                k= (int) Math.ceil(2 * p * Math.abs(t));
                series.getData().add(new XYChart.Data(t, Math.pow(-1, k + 1) *
                        ((1 / p) * (-k * k + 2 * p * k + k - p) - (4 * p - 2 * k + 1) * Math.abs(t))));
            }
            for (double t = -1.2; t <= -1; t = t + 0.001) {
                series.getData().add(new XYChart.Data(t, 0));
            }
            for (double t = 1; t <= 1.2; t = t + 0.001) {
                series.getData().add(new XYChart.Data(t, 0));
            }
        }
        else if(n % 2 == 0){
            double value0=0;
            for(double f = -(br/2);  f <= (br/2); f = f + 0.1){
                value0 = value0 + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * 0.1 * 1000000;
            }
            for(double t = -1; t <= 1; t = t + 0.001){
                for(double f = -(br/2);  f <= (br/2); f = f + 0.1){
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t/fc ) * 0.1 * 1000000;
                }
                a /= value0;
                series.getData().add(new XYChart.Data(t, a));
                a = 0;
            }
        }
        else{
            //求出┏=0时候的值，以便归一化
            double value0=0;
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.1){
                value0 = value0 + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * 0.1 * 1000000;
            }
            for(double t = -1; t <= 1; t = t + 0.001){
                for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.1){
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t/fc ) * 0.1 * 1000000;
                }
                a /= value0;
                series.getData().add(new XYChart.Data(t, a));
                a = 0;
            }
        }
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
        stage.setScene(scene);
        stage.show();
    }

    public void four_parameters(){
        double a = 0;
        double b = 0;
        double c = 0;
        double label1=0;
        double label2=0;
        double label3=0;
        double label4=0;
        //i为小矩形的宽
        double i=0.05;
        if(n % 2 == 0){
            //计算带限剩余功率////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }
            label1=a;
            //计算均方根带宽////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                b = b + f * f * fc * pow(a, -1) * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000 * pow(10,12);
            }
            b = pow(b , 0.5) / 1000000;
            label2=b;
            b = 0;

            //计算频谱隔离系数////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////
            for(double f = -15; f >= -15 && f <= 15; f = f + i){
                c = c + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                b = b + pow(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) ,2)* i * 1000000;
            }
            b = b / c;
            b = 10 * Math.log10(b);
            label3=b;
            //计算有效矩形带宽/////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////////////////
            b = fc * pow(Math.tan(Math.PI * (fs * 0.95) / (2 * fs))
                    * Math.sin(Math.PI * (fs * 0.95) / fc), 2) * pow(10, -6) / pow(Math.PI * (fs * 0.95), 2);
            b = a / b;
            b = b / 1000000;
            label4=b;

        }
        else{

            //计算带限剩余功率////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }
            label1=a;

            //计算均方根带宽////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                b = b + f * f * fc * pow(a, -1) * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000 * pow(10,12);
            }
            b = pow(b , 0.5) / 1000000;
            label2=b;

            b = 0;
            //计算频谱隔离系数////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////
            for(double f = -15; f >= -15 && f <= 15; f = f + i){
                c = c + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                b = b + pow(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) ,2)* i * 1000000;
            }
            b = b / c;
            b = 10 * Math.log10(b);
            label3=b;

            //计算有效矩形带宽/////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////////////////
            b = fc * pow(Math.tan(Math.PI * (fs * 0.95) / (2 * fs))
                    * Math.cos(Math.PI * (fs * 0.95) / fc), 2) * pow(10, -6) / pow(Math.PI * (fs * 0.95), 2);
            b = a / b;
            b = b / 1000000;
            label4=b;
        }
        Stage stage = new Stage();
        GridPane gridPane= new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.add(new Label("四种重要性能参数"), 0, 0);
        gridPane.add(new Label("值"),1,0);
        gridPane.add(new Label("带限之后剩余功率"), 0, 1);
        gridPane.add(new Label(String.format("%.1f", label1)),1,1);
        gridPane.add(new Label("均方根（RMS）带宽"), 0, 2);
        gridPane.add(new Label(String.format("%.1f" + "(MHz)", label2)),1,2);
        gridPane.add(new Label("频谱隔离系数"), 0, 3);
        gridPane.add(new Label(String.format("%.1f" + "(dB/Hz)", label3)),1,3);
        gridPane.add(new Label("功率谱密度的有效矩形带宽"), 0, 4);
        gridPane.add(new Label(String.format("%.1f" + "(MHz)", label4)),1,4);
        gridPane.setAlignment(Pos.CENTER);
        Scene scene=new Scene(gridPane,500,500);
        stage.setScene(scene);
        stage.show();
    }


    public void paint_time(int[] array){
        int k=array.length;
        double Ts=1/(2*fs);
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

        lineChart.setTitle("BOC调制时域图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("time graph with number of "+k+"code element \n"+ Arrays.toString(array));

        for(double t = 0;  t <= k; t = t + 0.001){
            series.getData().add(new XYChart.Data(t , (array[(int)Math.floor(t)])*
                    ((int)Math.floor(t*n)%2==0 ? 1: -1)));
        }
        VBox vBox= new VBox();
        vBox.setSpacing(40);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(lineChart);
        Scene scene  = new Scene(vBox,800,600);
        lineChart.getData().add(series);
        stage.setScene(scene);
        stage.show();


        Button bt = new Button("save");

        bt.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                WritableImage snapShot = scene.snapshot(null);

                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File("test.png"));
                }catch (IOException ex){
                    ex.printStackTrace();
                }

            }
        });
        Button bt2 = new Button("save as");
        bt2.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Image");
                WritableImage snapShot = scene.snapshot(null);
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(snapShot,
                                null), "png", file);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        });
        vBox.getChildren().addAll(bt,bt2);
    }

    public void paint_frequency(){
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-17.5,17.5,2.5);
        final NumberAxis yAxis = new NumberAxis(-300,0,25);
        //设置横轴和纵轴的标签
        xAxis.setLabel("频率（MHz）");
        yAxis.setLabel("功率谱密度（dBW/Hz）");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BOC调制功率谱");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("frequency within 15MHz");

        if((2 * fs / fc) % 2 == 0){
            for(double f = -15; f >= -15 && f <= 15; f = f + 0.01){
                series.getData().add(new XYChart.Data(f , 10*Math.log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2))));
            }
        }
        else{
            for(double f = -15; f >= -15 && f <= 15; f = f + 0.01){
                series.getData().add(new XYChart.Data(f , 10*Math.log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2))));
            }
        }
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
        stage.setScene(scene);
        stage.show();
    }
}
