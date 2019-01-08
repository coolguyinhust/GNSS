package signal;

import function.Chart_FourParas;
import function.Map_Parameters;
import function.Picture_save;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.Arrays;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

/**
 * Created by kaixin on 2018/12/13.
 * BPSK类包含了不同参数下多个BPSK调制信号的时域波形、频谱和自相关函数图。
 * 计算出GNSS常用调制信号的特性并生成表格。
 * 其中，PSK-R具体信号通常表示为PSk-R(m)的形式，m表示扩频码的速率为1.023MHz的m倍
 * 所以扩频码fc=1.023*m;角频率wc=2*3.14*fc;
 */

public class BPSK {
    private int m;

    //n是一个载波周期内的码片数；即n=载波周期Tc/码片宽度Ts
    private double n;

    private double fc;
    private double wc;

    //br是BPSk的限制带宽，如果带宽为-1，默认为无限制带宽
    private double br;

    /**
     * 构造函数
     * @param m 扩频码的速率为1.023MHz的m倍
     * @param n 一个载波周期内的码片数
     */
    public BPSK(int m,double n) {
        this.m = m;
        this.fc=m * 1.023;
        this.wc=fc * 2*Math.PI;
        this.n=n;
        this.br=24;
    }

    /**
     * 构造函数
     * @param m 扩频码的速率为1.023MHz的m倍
     */
    public BPSK(int m) {
        this.m = m;
        this.fc=m*1.023;
        this.wc=fc*2*Math.PI;
        this.br=24;
    }

    /**
     * 构造函数
     * @param m 扩频码的速率为1.023MHz的m倍
     * @param br 限制带宽
     */
    public BPSK(double br,int m) {
        this.m = m;
        this.fc=m*1.023;
        this.br=br;
        this.wc=fc*2*Math.PI;
    }

    /**
     * 绘制BPSK的功率谱密度图像
     * <ul>
     *     <li>无需传入参数和返回值，用到的参数从本类中获取
     *     <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性
     *     <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）
     *     <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。
     * </>
     */
    public void paint_frequency(){
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-17.5,17.5,2.5);
        final NumberAxis yAxis = new NumberAxis(-120,-50,25);

        //设置横轴和纵轴的标签
        xAxis.setLabel("频率（MHz）");
        yAxis.setLabel("功率谱密度（W/Hz）");

        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);

        //让图表上的点不会显示在坐标轴上
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BPSK-("+m+")调制功率谱");

        //defining a series
        XYChart.Series<Number,Number> series = new XYChart.Series();
        series.setName("frequency within 15MHz");

        //根据公式向坐标的数据集里面添加元素
        for(double f = -15; f >= -15 && f <= 15; f = f + 0.01){
            series.getData().add(new XYChart.Data(f ,10*Math.log10( (1/fc)* pow((Math.sin(Math.PI*f/fc))/(Math.PI*f/fc),2)*1e-6 )));
        }

        lineChart.getData().add(series);
        Picture_save picture_save=new Picture_save(lineChart,"BPSK_frequency_domain.png");
    }

    /**
     * 计算BPSK主瓣的最大功率谱密度，即零频率时对应的功率谱函数上的值。
     * <ul>
     *     <li>无需传入参数
     *     @return  the maximum value of powerSpectrum
     *     <li>此处有一处需要注意，频率不能直接取0,因为出现在了分母中；
     *     <li>我们只有采取求极限的方式，取一个趋近于0的数</li>
     * </>
     */
    public double get_Max_PowerSpectrum(){
        double f=0.00001;
        double value=10*Math.log10( (1/fc)* pow((Math.sin(Math.PI*f/fc))/(Math.PI*f/fc),2)*1e-6 );
        return value;
    }


    /**
     * 计算90%功率的带宽band_n，其中90％功率的带宽指对于卫星发射的带宽为30MHz的信号，通过其90％的功率所需要的带宽
     * 利用功率谱的对称性，要对band_n做积分=0.9,即从-15，到band_n/2做积分等于0.05
     * <ul>
     *     <li>无需传入参数和返回值
     *     <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性
     *     <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）
     *     <li>由x生成y的具体计算中，需要用到积分，这里我们用微元法，即用小矩形的面积来计算积分的值。
     *     <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。
     * </>
     */
    public double getNinetyPercentBand(){
        //从-fi到fi上对功率谱积分应该等于0.9，band_n=2*fi
        double fi=-15;
        double power=0;
        double value;
        for(;fi<=15;fi+=0.005){
            value=(1/fc)* pow((Math.sin(Math.PI*fi/fc))/(Math.PI*fi/fc),2);
            power+=value*0.005;
            if(power>power_sender()*0.1/2){
                break;
            }
        }
        return 2*Math.abs(fi);
    }

    //求归一化（无限带宽上功率为1）后的30MHz频带内的发送功率
    private double power_sender(){
        double value;
        double power_sender=0;
        for(double f=-15;f<=15;f+=0.005){
            value= (1/fc)* pow((Math.sin(Math.PI*f/fc))/(Math.PI*f/fc),2) ;
            power_sender+=value*0.005;
        }
        return  power_sender;
    }

    //带外损失功率（dB）的定为为：
    //- 10 Log10[带内功率（归一化的）/1]  (dB)
    //发射带宽为30MHz,接收带宽为24MHz损失的功率
    public double band_loss(){
       double value;
        double power_receiver=0;
        for(double f=-br/2;f<=br/2;f+=0.005){
            value=(1/fc)* pow((Math.sin(Math.PI*f/fc))/(Math.PI*f/fc),2) ;
            power_receiver+=value*0.005;
        }

        //band_in是带内的损失；
        double band_in= power_receiver/power_sender();
        //带内损失转化为带外损失
        double band_out=-10*Math.log10(band_in/1);
        return band_out;
    }

    public void paint_time(int[] array){
        int k=array.length;
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(0,k+0.5,0.5);
        final NumberAxis yAxis = new NumberAxis(-1.5,1.5,0.5);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码片 chips");
        yAxis.setLabel("幅度");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BPSK-("+m+")码元时域图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("time graph with number of "+k+"code element \n"+ Arrays.toString(array));

        for(double t = 0;  t <= k; t = t + 0.001){
            series.getData().add(new XYChart.Data(t , (array[(int)Math.floor(t)])*
                    Math.sin(2*Math.PI*n*t)));
        }
        lineChart.getData().add(series);
        Picture_save picture_save=new Picture_save(lineChart,"BPSK_time_domain.png");
    }

    public void paint_self_correlation(){
        double a=0;
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-1,1,0.2);
        final NumberAxis yAxis = new NumberAxis(-0.2,1.2,0.4);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码片 chips");
        yAxis.setLabel("幅度");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BPSK-("+m+")自相关函数图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("self correlation function graph");

        //算出频率为0的值，用来归一化
        double value0=0;
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.05){
            value0 = value0 + fc * pow(Math.sin(Math.PI * f / fc), 2)
                    / (pow(Math.PI * f, 2)) * 0.05 ;
        }

        for(double t = -1;t <= 1; t = t + 0.001){
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.05){
                a = a + fc * pow(Math.sin(Math.PI * f / fc), 2)
                        / (pow(Math.PI * f, 2))  * Math.cos(2 * Math.PI * f * t ) * 0.05 ;
            }
            a /= value0;
            series.getData().add(new XYChart.Data(t, a));
            a = 0;
        }

        lineChart.getData().add(series);
        Picture_save picture_save=new Picture_save(lineChart,"BPSK_self_correlation.png");
    }

    //计算带限剩余功率
    public double getLimitedBandWidth() {
        double a = 0;
        //i为小矩形的宽
        double i = 0.05;
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.05){
            a = a +fc * pow(Math.sin(Math.PI * f / fc), 2) / (pow(Math.PI * f, 2))  * 0.05;
        }
        return a;
    }

    //计算均方根带宽
    public double getRMSBand() {
        double b=0;
        double i=0.05;
        //计算均方根带宽
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
            b = b + f * f * pow(getLimitedBandWidth(), -1) * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                    / (pow(Math.PI * f, 2))  * i  * pow(10,12);
        }
        b = pow(b , 0.5) / 1000000;
        return b;
    }


    //计算与自身的频谱隔离系数
    public double getFrequencyIsolationFactor() {
        double a = 0, b = 0, i = 0.05;
        for(double f = -15; f >= -15 && f <= 15; f = f + 0.05){
            a = a + fc * pow(Math.sin(Math.PI * f / fc), 2) / (pow(Math.PI * f, 2))  * 0.05 ;
        }
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.05){
            b = b + pow(fc * pow(Math.sin(Math.PI * f / fc), 2)
                    / (pow(Math.PI * f, 2)) ,2)* 0.05 * 1e-6 ;
        }
        b = b / a;
        b = 10 * Math.log10(b);
        return b;
    }


    //计算与BPSK1的频谱隔离系数
    public double getFrequencyIsolationFactorBPSK1() {
        double a = 0, b = 0, i = 0.05;
        for(double f = -15; f >= -15 && f <= 15; f = f + i){
            a = a + 1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2))  * i  ;
        }
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
            b = b + (fc * pow(Math.sin(Math.PI * f / fc), 2) / (pow(Math.PI * f, 2)) )*
                    (1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2)) )* i * 1e-6  ;
        }
        b = b / a;
        b = 10 * Math.log10(b);
        return b;
    }

    //计算与BOC105的频谱隔离系数,fs=10*1.023,fc=5*1.023
    public double getFrequencyIsolationFactorBOC105() {
        double value=0,i=0.05;
        double a=0;
        for (double f = -15; f >= -15 && f <= 15; f = f + i) {
            value = value + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                    * Math.sin(Math.PI * f / 5.115), 2)  / pow(Math.PI * f, 2) * i ;
        }
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.05){
            a = a + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                    * Math.sin(Math.PI * f / 5.115), 2)  / pow(Math.PI * f, 2) *
                    (fc * pow(Math.sin(Math.PI * f / fc), 2) / (pow(Math.PI * f, 2)) )*i*1e-6;
        }
        a=a/value;
        a=10*log10(a);
        return a;
    }


    //计算有效矩形带宽
    public double getRectBand(){
        double b=0;
        b = pow(fc , -1);
        b = this.getLimitedBandWidth() / b;
        return b;
    }

    //计算BOC四种重要性能参数
    public void four_parameters(){
        Chart_FourParas p = new Chart_FourParas(this.getLimitedBandWidth(),this.getRMSBand(),
                this.getFrequencyIsolationFactor(),this.getRectBand());
        TableView<Map_Parameters> table = p.getTable();
        Picture_save picture_save=new Picture_save(table,"BPSK_four_parameters.png");
    }

    //码跟踪误差的计算,分析不同条件下BPSk调制信号码跟踪精度的影响
    public double[]  trackerError(double t,double bl,double sCN0){
        double[] result = new double[2];
        double temp1, temp2, temp3, temp4;
        double result1, result2;
        double sum = 0;
        double delta = 0.1;
        for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
            sum += fc * Math.pow(Math.sin(Math.PI*f/fc),2)*fc
                    /(Math.PI * Math.PI * f * f)
                    * Math.pow(Math.sin(Math.PI * f * t * 1e-3), 2) * delta;
        }
        temp1 = sum;
        sum = 0;
        for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
            sum += fc * Math.pow(Math.sin(Math.PI*f/fc),2)*fc
                    /(Math.PI * Math.PI * f * f)
                    * Math.pow(Math.cos(Math.PI * f * t * 1e-3), 2) * delta;
        }
        temp2 = sum;
        sum = 0;
        for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
            sum += f * fc * Math.pow(Math.sin(Math.PI*f/fc),2)*fc
                    /(Math.PI * Math.PI * f * f)
                    * Math.sin(Math.PI * f * t * 1e-3) * delta * 1e6;
        }
        temp3 = 4 * Math.PI * Math.PI * sum * sum;
        sum = 0;
        for (double f = -this.br / 2; f <= this.br / 2; f += delta) {
            sum += fc * Math.pow(Math.sin(Math.PI*f/fc),2)*fc
                    /(Math.PI * Math.PI * f * f)
                    * Math.cos(Math.PI * f * t * 1e-3) * 0.05;
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

        lineChart.setTitle("BPSK码跟踪误差");
        //defining a series
        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();
        series1.setName("50bps");
        series2.setName("50bps");
        for (double t = 150;t<=300;t+=0.1){
            double[] result = this.trackerError(t,1,1000);
            series1.getData().add(new XYChart.Data(t,result[0]));
            series2.getData().add(new XYChart.Data(t,result[1]));
        }
        lineChart.getData().addAll(series1,series2);
        Picture_save picture_save=new Picture_save(lineChart,"errorbpsk");
    }
}
