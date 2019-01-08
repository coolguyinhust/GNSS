package signal;

import function.Chart_FourParas;
import function.Map_Parameters;
import function.Picture_save;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.*;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

/**
 * Created by kaixin on 2018/12/13.
 */
/**
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
    private double br=24;//br是BOC的限制带宽，如果用户没有输入限定带宽，默认是GPS信号接收的带宽


    public double getFc() {
        return fc;
    }

    public BOC(double x, double y, double br) {
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
     * 绘制归一化的自相关函数；
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

        lineChart.setTitle("BOC("+x+","+y+")自相关函数图像");
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
        lineChart.getData().add(series);
        Picture_save picture_save=new Picture_save(lineChart,"BOC_self_correlation.png");
    }

    //计算自相关函数主峰与第一副峰间的时延,与自相关函数第一副峰与主峰幅度平方之比
    public Double[] delay_SelfCorrelation(){
        double a=0,i=0.05;
        double x_min=0,y_min=0;//分别代表副峰的横坐标和纵坐标
        double y_max=0;//y_max是主峰的幅度
        if((2 * fs / fc) % 2 == 0){

            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                y_max = y_max + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }

            for(double t = -500;t <0 ; t = t + 0.2){

                for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t * 0.001) * i * 1000000;
                }
                if(a<y_min){
                    y_min=a;
                    x_min=t;
                }

                a = 0;
            }
        }
        else{
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                y_max = y_max + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }

            for(double t = -500;t <0 ; t = t + 0.2){

                for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t * 0.001) * i * 1000000;
                }
                if(a<y_min){
                    y_min=a;
                    x_min=t;
                }

                a = 0;
            }
        }
        return new Double[]{x_min,pow(y_min/y_max,2)};
    }

    //计算带限剩余功率
    public double getLimitedBandWidth(){
        double a = 0;
        //i为小矩形的宽
        double i=0.05;
        if(n % 2 == 0) {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) * i ;
            }
        } else {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) * i;
            }
        }
        return a;
    }

    //计算均方根带宽
    public double getRMSBand() {
        double a=0;
        double i = 0.05;
        if (n % 2 == 0) {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + f * f * fc * pow(getLimitedBandWidth(), -1) * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i * pow(10, 12);
            }
            a = pow(a, 0.5) / 1000000;
        }else{
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                a = a + f * f * fc * pow(getLimitedBandWidth(), -1) * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) * i  * pow(10,12);
            }
            a = pow(a , 0.5) / 1000000;
        }
        return a;
    }

    //计算与自身的频谱隔离系数
    public double getFrequencyIsolationFactor(){
        double a=0,b=0,i=0.05;
        if(n%2==0) {
            for (double f = -15; f >= -15 && f <= 15;f = f + i) {
              a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                      * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) * i ;
            }
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                b = b + pow(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                      * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2), 2) * i * 1e-6;
            }
            b = b / a;
            b = 10 * log10(b);
        }
        else{
            for(double f = -15; f >= -15 && f <= 15; f = f + i){
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) * i ;
            }
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                b = b + pow(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) ,2)* i * 1e-6;
            }
            b = b / a;
            b = 10 * log10(b);
        }
        return  b;
    }

    //计算与BPSK1的频谱隔离系数
    public double getFrequencyIsolationFactorBPSK1(){
        double a=0,b=0,i=0.05,power_bpsk=0;
        for(double f = -15; f >= -15 && f <= 15; f = f + i){
            power_bpsk = power_bpsk + 1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2))  * i ;
        }
        if(n%2==0) {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                b = b + fc * pow(Math.tan(Math.PI * f / (2 * fs)) * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                        (1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2))  )*i* 1e-6  ;
            }
            b = b /power_bpsk ;
            b = 10 * log10(b);
        }
        else{
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                b = b + fc * pow(Math.tan(Math.PI * f / (2 * fs)) * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) *
                        (1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2)) )*i* 1e-6 ;
            }
            b = b / power_bpsk;
            b = 10 * log10(b);
        }
        return  b;
    }


    //计算与BOC105的频谱隔离系数,fs=10*1.023,fc=5*1.023
    public double getFrequencyIsolationFactorBOC105() {
        double value=0,i=0.05;
        double a=0;
        for (double f = -15; f >= -15 && f <= 15; f = f + i) {
            value = value + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                    * Math.sin(Math.PI * f / 5.115), 2)  / pow(Math.PI * f, 2) * i ;
        }
        if(n%2==0){
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                        * Math.sin(Math.PI * f / 5.115), 2)  / pow(Math.PI * f, 2) *
                        fc * pow(Math.tan(Math.PI * f / (2*fs) )
                        * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) * i *1e-6;
            }
        }
        else{
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                        * Math.sin(Math.PI * f / 5.115), 2)  / pow(Math.PI * f, 2) *
                        fc * pow(Math.tan(Math.PI * f / (2*fs) )
                        * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) * i *1e-6;
            }
        }
        a=a/value;
        a=10*log10(a);
        return a;
    }

    //计算有效矩形带宽
    public double getRectBand(){
        double b=0;
        if(n%2==0) {
            b = fc * pow(Math.tan(Math.PI * get_Max_PowerSpectrum()[0] / (2 * fs))
                    * Math.sin(Math.PI * get_Max_PowerSpectrum()[0] / fc), 2) * pow(10, -6) / pow(Math.PI * (get_Max_PowerSpectrum()[0]), 2);
            b = getLimitedBandWidth() / b;
            b = b / 1000000;
        }
        else{
            //计算有效矩形带宽
            b = fc * pow(Math.tan(Math.PI * get_Max_PowerSpectrum()[0] / (2 * fs))
                    * Math.cos(Math.PI * get_Max_PowerSpectrum()[0] / fc), 2) * pow(10, -6) / pow(Math.PI * get_Max_PowerSpectrum()[0], 2);
            b = getLimitedBandWidth() / b;
            b = b / 1000000;
        }
        return b;
    }

    //计算BOC四种重要性能参数
    public void four_parameters(){
        Chart_FourParas p = new Chart_FourParas(this.getLimitedBandWidth(),this.getRMSBand(),
                this.getFrequencyIsolationFactor(),this.getRectBand());
        TableView<Map_Parameters> table = p.getTable();
        Picture_save picture_save=new Picture_save(table,"BOC_four_parameters.png");
    }


    public void paint_time(int[] array){
        int k=array.length;
        double Ts=1/(2*fs);
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(0,k+0.5,0.5);
        final NumberAxis yAxis = new NumberAxis(-1.5,1.5,0.5);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码片 chips");
        yAxis.setLabel("幅度");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BOC("+x+","+y+")调制时域图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("time graph with number of "+k+"code element \n"+ Arrays.toString(array));

        for(double t = 0;  t <= k; t = t + 0.001){
            series.getData().add(new XYChart.Data(t , (array[(int)Math.floor(t)])*
                    ((int)Math.floor(t*n)%2==0 ? 1: -1)));
        }
        lineChart.getData().add(series);
        Picture_save picture_save=new Picture_save(lineChart,"BOC_time_domain.png");
    }

    public void paint_frequency(){
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-17.5,17.5,2.5);
        final NumberAxis yAxis = new NumberAxis(-180,-60,15);
        //设置横轴和纵轴的标签
        xAxis.setLabel("频率（MHz）");
        yAxis.setLabel("功率谱密度（dBW/Hz）");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BOC("+x+","+y+")调制功率谱");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("frequency within 15MHz");

        if((2 * fs / fc) % 2 == 0){
            for(double f = -15; f >= -15 && f <= 15; f = f + 0.01){
                series.getData().add(new XYChart.Data(f , 10* log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2))));
            }

        }
        else{
            for(double f = -15; f >= -15 && f <= 15; f = f + 0.01){
                series.getData().add(new XYChart.Data(f , 10* log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2))));
            }
        }
        lineChart.getData().add(series);
        Picture_save picture_save=new Picture_save(lineChart,"BOC_frequency_domain.png");
    }


    //计算BOC调制信号的最大功率谱密度，找到一个（x,y），使x对应的y值最大
    public Double[] get_Max_PowerSpectrum(){
        double max_power=0;
        Map<Double,Double> map=new HashMap<>();
        if((2 * fs / fc) % 2 == 0){
            for(double f = -br/2;  f <0; f = f + 0.001){
                map.put(f , 10* log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2)));
            }

        }
        else{
            for(double f = -br/2;  f <0; f = f + 0.001){
                map.put(f , 10* log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2)));
            }
        }
        //这里将map.entrySet()转换成list
        List<Map.Entry<Double,Double>> list = new ArrayList<Map.Entry<Double,Double>>(map.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<Double,Double>>() {
            //升序排序
            public int compare(Map.Entry<Double, Double> o1,
                               Map.Entry<Double, Double> o2) {
                if(o1.getValue()-o2.getValue()>0)
                    return -1;
                else if(o1.getValue()-o2.getValue()==0){
                    return 0;
                }
                else {
                    return 1;
                }
            }
        });
        return new Double[]{list.get(0).getKey(), list.get(0).getValue()};
    }



    //计算90%功率的带宽band_n（90％功率的带宽指对于卫星发射的带宽为30MHz的信号，
    //通过其90％的功率所需要的带宽）；
    // 利用功率谱的对称性，要对band_n做积分=0.9,即从-15，到band_n/2做积分等于0.05
    public double getNinetyPercentBand(){
        //从-fi到fi上对功率谱积分应该等于0.9，band_n=2*fi
        double fi=-15;
        double power=0;
        double value;
        for(;fi<=15;fi+=0.005){
            if((2 * fs / fc) % 2 == 0){
                value = fc * pow(Math.tan(Math.PI * fi / (2 * fs))
                        * Math.sin(Math.PI * fi / fc), 2)  / pow(Math.PI * fi, 2);
            }
            else{
                value = fc * pow(Math.tan(Math.PI * fi / (2 * fs))
                        * Math.cos(Math.PI * fi / fc), 2) / pow(Math.PI * fi, 2);
            }
            power+=value*0.005;
            if(power>power_sender()*(1-0.9)/2){
                break;
            }
        }
        return 2*Math.abs(fi);
    }

    //求归一化（无限带宽上功率为1）后的30MHz频带内的发送功率
    private double power_sender(){
        double value;
        double power_sender=0;
        if((2 * fs / fc) % 2 == 0) {
            for (double f = -15; f <= 15; f += 0.005) {
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2);
                power_sender+=value*0.005;
            }
        }
        else {
            for (double f = -15; f <= 15; f += 0.005) {
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2);
                power_sender+=value*0.005;
            }

        }

        return  power_sender;
    }


    //带外损失功率（dB）的定为为：
    //- 10 Log10[带内功率（归一化的）/1]  (dB)
    //发射带宽为30MHz,接收带宽为24MHz损失的功率
    public double band_loss(){
        double value;
        double power_receiver=0;

        for(double f=-12;f<=12;f+=0.005){
            if((2 * fs / fc) % 2 == 0){
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2);
            }
            else{
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2);
            }
            power_receiver+=value*0.005;
        }

        //band_in是带内的损失；
        double band_in= power_receiver/power_sender();
        //带内损失转化为带外损失
        double band_out=-10* log10(band_in/1);
        return band_out;
    }

    //码跟踪误差的计算,分析不同条件下BOC调制信号码跟踪精度的影响
    public double[] trackerError(double t,double bl,double sCN0){
        double[] result = new double[2];
        double temp1, temp2, temp3, temp4;
        double result1, result2;
        double sum = 0;
        double delta = 0.1;
        if ((2 * this.x / this.y) % 2 == 0) {
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.pow(Math.sin(Math.PI * f * t * 1e-3), 2) * delta;
            }
            temp1 = sum;
            sum = 0;
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.pow(Math.cos(Math.PI * f * t * 1e-3), 2) * delta;
            }
            temp2 = sum;
            sum = 0;
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += f * fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.sin(Math.PI * f * t * 1e-3) * delta * 1e6;
            }
            temp3 = 4 * Math.PI * Math.PI * sum * sum;
            sum = 0;
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.cos(Math.PI * f * t * 1e-3) * delta;
            }
            temp4 = sum * sum;
            result1 = Math.sqrt(bl * (1 - 0.25 * bl * 0.005) * temp1
                    / sCN0 / temp3
                    * (1 + temp2 / (0.005 * sCN0 * temp4))) * 3e8;
            result2 = Math.sqrt(bl * (1 - 0.25 * bl * 0.02) * temp1
                    / sCN0 / temp3
                    * (1 + temp2 / (0.02 * sCN0 * temp4))) * 3e8;
            result[0] = result1;
            result[1] = result2;
        }else {
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.pow(Math.sin(Math.PI * f * t * 1e-3), 2) * delta;
            }
            temp1 = sum;
            sum = 0;
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.pow(Math.cos(Math.PI * f * t * 1e-3), 2) * delta;
            }
            temp2 = sum;
            sum = 0;
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += f * fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.sin(Math.PI * f * t * 1e-3) * delta * 1e6;
            }
            temp3 = 4 * Math.PI * Math.PI * sum * sum;
            sum = 0;
            for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
                sum += fc * Math.pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2)
                        / Math.pow(Math.PI * f, 2)
                        * Math.cos(Math.PI * f * t * 1e-3) * delta;
            }
            temp4 = sum * sum;
            result1 = Math.sqrt(bl * (1 - 0.25 * bl * 0.005) * temp1
                    / sCN0 / temp3
                    * (1 + temp2 / (0.005 * sCN0 * temp4))) * 3e8;
            result2 = Math.sqrt(bl * (1 - 0.25 * bl * 0.02) * temp1
                    / sCN0 / temp3
                    * (1 + temp2 / (0.02 * sCN0 * temp4))) * 3e8;
            result[0] = result1;
            result[1] = result2;
        }
        return result;
    }

    public void errorInterval(){
            Stage stage = new Stage();
            //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            //设置横轴和纵轴的标签
            xAxis.setLabel("码片 chips");
            yAxis.setLabel("幅度");
            //creating the chart
            final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
            lineChart.setCreateSymbols(false);

            lineChart.setTitle("BOC("+x+","+y+")码跟踪误差");
            //defining a series
            XYChart.Series series1 = new XYChart.Series();
            XYChart.Series series2 = new XYChart.Series();
            series1.setName("50bps");
            series2.setName("50bps");
        for (double t = 20;t<=80;t+=0.1){
            double[] result = this.trackerError(t,1,1000);
            series1.getData().add(new XYChart.Data(t,result[0]));
            series2.getData().add(new XYChart.Data(t,result[1]));
        }
        lineChart.getData().addAll(series1,series2);
        Picture_save picture_save=new Picture_save(lineChart,"errorboc");
    }
}
