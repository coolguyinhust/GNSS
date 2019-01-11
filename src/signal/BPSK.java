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
     *     <li>无需传入参数和返回值，用到的参数从本类中获取</li>
     *     <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性</li>
     *     <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）</li>
     *     <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
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
     *     <li>无需传入参数</li>
     *     <li>此处有一处需要注意，频率不能直接取0,因为出现在了分母中；</li>
     *     <li>我们只有采取求极限的方式，取一个趋近于0的数</li>
     * </ul>
     * @return  the maximum value of powerSpectrum
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
     *     <li>无需传入参数</li>
     *     <li>具体计算中，需要用到积分，这里我们用微元法，即用小矩形的面积来计算积分的值。</li>
     * </ul>
     * @return band which can get 90% power
     */
    public double getNinetyPercentBand(){
        //从-fi到fi上对功率谱积分应该等于0.9，band_n=2*fi
        double fi=-15;
        double power=0;
        double value;

        //  fi+=0.005中的0.005可以看成是小矩形的宽度
        for(;fi<=15;fi+=0.005){
            value=(1/fc)* pow((Math.sin(Math.PI*fi/fc))/(Math.PI*fi/fc),2);
            power+=value*0.005;
            if(power>power_sender()*0.1/2){
                break;
            }
        }
        return 2*Math.abs(fi);
    }

    /**
     * 求归一化（无限带宽上功率为1）后的30MHz频带内的发送功率
     * <ul>
     *     <li>无需传入参数</li>
     *     <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     * @return 返回对-15MHz到15MHz区域上的积分值，得到总功率以便归一化
     */
    private double power_sender(){
        double value;
        double power_sender=0;
        for(double f=-15;f<=15;f+=0.005){
            value= (1/fc)* pow((Math.sin(Math.PI*f/fc))/(Math.PI*f/fc),2) ;
            power_sender+=value*0.005;
        }
        return  power_sender;
    }

    /**
     * 带外损失功率（dB）的定为为：
     * - 10 Log10[带内功率（归一化的）/1]  (dB)
     * 发射带宽为30MHz,接收带宽为24MHz损失的功率
     * <ul>
     *     <li>无需传入参数</li>
     *     <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     * @return 带外损失功率
     */
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

    /**
     * 绘制BPSK的时域图像
     * <ul>
     *     <li>无需返回值</li>
     *     <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性</li>
     *     <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）</li>
     *     <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
     * @param array 整型数组是由 1 和 -1 组成的双极性码，1表示高电平，-1表示低电平
     */
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

    /**
     * 绘制BPSK的自相关函数图像
     * 利用自相关函数和功率谱互为傅里叶正反变换
     * <ul>
     *     <li>无需传入参数和返回值，用到的参数从本类中获取</li>
     *     <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性</li>
     *     <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）</li>
     *     <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
     */
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

    /**
     * 计算带限剩余功率
     * <ul>
     *     <li>无需传入参数</li>
     *     <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     * @return 带限剩余功率
     */
    public double getLimitedBandWidth() {
        double a = 0;
        //i为小矩形的宽
        double i = 0.05;
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + 0.05){
            a = a +fc * pow(Math.sin(Math.PI * f / fc), 2) / (pow(Math.PI * f, 2))  * 0.05;
        }
        return a;
    }

    /**
     * 计算均方根带宽
     * <ul>
     *     <li>无需传入参数</li>
     *     <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     * @return 均方根带宽
     */
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

    /**
     * 计算与自身的频谱隔离系数
     * <ul>
     *     <li>无需传入参数</li>
     *     <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     * @return 与自身的频谱隔离系数
     */
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

    /**
     * 计算与BPSK-1的频谱隔离系数
     * <ul>
     *     <li>无需传入参数</li>
     *     <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     * @return 与BPSK1的频谱隔离系数
     */
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

    /**
     * 计算与BOC(10，5)的频谱隔离系数,fs=10*1.023,fc=5*1.023
     * <ul>
     *     <li>无需传入参数</li>
     *     <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     * @return 与BOC(10,5)的频谱隔离系数
     */
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

    /**
     * 计算有效矩形带宽
     * <ul>
     *     <li>无需传入参数</li>
     * </ul>
     * @return 有效矩形带宽
     */
    public double getRectBand(){
        double b=0;
        b = pow(fc , -1);
        b = this.getLimitedBandWidth() / b;
        return b;
    }

    /**
     * 计算BPSK四种重要性能参数
     * <ul>
     *     <li>无需传入参数和返回值，用到的参数从本类中获取</li>
     *     <li>具体方法是，构造一个Chart_FourParas类对象，将4个参数传入</li>
     *     <li>生成一个javafx.scene.control中TableView对象，调用getTable()得到表格</li>
     *     <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
     */
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

    /**
     * 绘制BPSK的误码跟踪曲线，以超前滞后本地码之间的间隔为横坐标，跟踪误差为纵坐标
     */
    public void paint_errorInterval(){
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        //设置横轴和纵轴的标签
        yAxis.setLabel("超前-滞后本地码间隔 /m");
        xAxis.setLabel("码跟踪误差 △/ns");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BPSK改变本地码间隔时码跟踪误差");
        //defining a series
        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();
        series1.setName("m="+this.m+",T= "+"5ms");
        series2.setName("m="+this.m+",T= "+"20ms");
        double[] result;
        for (double t = 40;t<=220;t+=1){
            result = this.trackerError(t,1,1000);
            series1.getData().add(new XYChart.Data(t,result[0]));
            series2.getData().add(new XYChart.Data(t,result[1]));
        }
        lineChart.getData().addAll(series1,series2);
        Picture_save picture_save=new Picture_save(lineChart,"errorbpsk.png");
    }

    //把Tc!=0时计算多径误差包络的代码抽象出来
    private double[] TcNoneZero(double a,double b,double c,double i,double smr,double Tc,double t,double br){
        double result[]=new double[3];
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
            a = a + Math.pow(10, -smr/20) *  m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                    / (pow(Math.PI * f, 2))  *
                    Math.sin(Math.PI * f * Tc * 0.001) * Math.sin(2 * Math.PI * f * t * 0.001)* i ;

            b = b + Math.PI * 2 * f * 1000000 *m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                    / (pow(Math.PI * f, 2))  *
                    Math.sin(Math.PI * f * Tc * 0.001) * (1 + Math.pow(10, -smr/20)
                    * Math.cos(2 * Math.PI * f * t * 0.001))* i ;

            c = c + Math.PI * 2 * f * 1000000 * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                    / (pow(Math.PI * f, 2))  *
                    Math.sin(Math.PI * f * Tc * 0.001) * (1 - Math.pow(10, -smr/20)
                    * Math.cos(2 * Math.PI * f * t * 0.001))* i ;
        }
        result[0]=a;
        result[1]=b;
        result[2]=c;
        return result;
    }

    //把Tc=0时计算多径误差包络的代码抽象出来
    private double[] TcEqualZero(double m,double a,double b,double c,double i,double smr,double Tc,double t,double br){
        double result[]=new double[3];
        for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
            a = a + Math.pow(10, -smr/20) *  m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                    / (pow(Math.PI * f, 2))  *
                    f * Math.sin(2 * Math.PI * f * t * 0.001)* i ;

            b = b + Math.PI * 2 * f * 1000000 * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                    / (pow(Math.PI * f, 2))  *
                    f * (1 + Math.pow(10, -smr/20)
                    * Math.cos(2 * Math.PI * f * t * 0.001))* i ;

            c = c + Math.PI * 2 * f * 1000000 * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                    / (pow(Math.PI * f, 2))  *
                    f * (1 - Math.pow(10, -smr/20)
                    * Math.cos(2 * Math.PI * f * t * 0.001))* i ;
        }
        result[0]=a;
        result[1]=b;
        result[2]=c;
        return result;
    }

    /**
     * 自定义的多径误差分析：根据输入的SMR和相关器间隔进行多径误差包络的绘制。
     */
    public void multipath_error(double smr, double Tc){
        Stage stage = new Stage();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final double i = 0.05;
        xAxis.setLabel("多径延迟（m）");
        yAxis.setLabel("多径误差包络（m）");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setCreateSymbols(false);

        lineChart.setTitle("多径误差包络");
        //defining a series
        XYChart.Series<Number,Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series2 = new XYChart.Series<>();
        double a  = 0;
        double b  = 0;
        double c  = 0;
        if(Tc != 0){

            for(double t = 0; t <= 1400; t = t + 2){
                double Result[]=TcNoneZero( a, b, c, i, smr, Tc, t,br);
                b = Result[0]/Result[1];

                series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
                c = -Result[0]/Result[2];
                series2.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
                a = 0;
                b = 0;
                c = 0;
            }
        }
        else{

            for(double t = 0; t <= 1400; t = t + 2){
                double Result[]=TcEqualZero(m, a, b, c, i, smr, Tc, t,br);
                b = Result[0]/Result[1];

                series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
                c =-Result[0]/Result[2];
                series2.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
                a = 0;
                b = 0;
                c = 0;

            }
        }

        lineChart.getData().addAll(series1, series2);
        series1.nodeProperty().get().setStyle("-fx-stroke:IndianRed;");
        series2.nodeProperty().get().setStyle("-fx-stroke:IndianRed;");
        Picture_save picture_save = new Picture_save(lineChart, "multipath_boc.png");
    }

    /**
     * 在接受带宽不同时对PSK-1的误差包络分析比较
     */
    public  void bandchange_multi(){
        double Tc=0;
        double smr=6;
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final double i = 0.05;
        xAxis.setLabel("多径延迟（m）");
        yAxis.setLabel("多径误差包络（m）");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setCreateSymbols(false);

        lineChart.setTitle("不同接收带宽时多径误差包络");
        //defining a series
        XYChart.Series<Number,Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series11 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series2 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series22 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series3 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series33 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series4= new XYChart.Series<>();
        XYChart.Series<Number,Number> series44 = new XYChart.Series<>();
        double a  = 0;
        double b  = 0;
        double c  = 0;

        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcEqualZero(1, a, b, c, i, smr, Tc, t,4.0);
            b = Result[0]/Result[1];
            series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series11.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }

        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcEqualZero( 1,a, b, c, i, smr, Tc, t,8.0);
            b = Result[0]/Result[1];
            series2.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series22.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }
        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcEqualZero(1, a, b, c, i, smr, Tc, t,16.0);
            b = Result[0]/Result[1];
            series3.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series33.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }
        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcEqualZero(1, a, b, c, i, smr, Tc, t,30.0);
            b = Result[0]/Result[1];
            series4.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series44.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }

        lineChart.getData().addAll(series1, series11,series2,series22,series3,series33,series4,series44);
        series1.nodeProperty().get().setStyle("-fx-stroke:Violet;");
        series1.setName("30MHZ");
        series11.nodeProperty().get().setStyle("-fx-stroke:Violet;");
        series11.setName("  ");
        series2.setName("8MHZ");
        series2.nodeProperty().get().setStyle("-fx-stroke:SpringGreen;");
        series22.setName("  ");
        series22.nodeProperty().get().setStyle("-fx-stroke:SpringGreen;");
        series3.nodeProperty().get().setStyle("-fx-stroke:MediumBlue;");
        series3.setName("16MHZ");
        series33.nodeProperty().get().setStyle("-fx-stroke:MediumBlue;");
        series33.setName("4MHZ");
        series4.nodeProperty().get().setStyle("-fx-stroke:OrangeRed;");
        series44.nodeProperty().get().setStyle("-fx-stroke:OrangeRed;");
        Picture_save picture_save = new Picture_save(lineChart, "bandmultipath_bpsk.png");
    }

    public void changeTc_multi(){
        double smr=6;
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final double i = 0.05;
        xAxis.setLabel("多径延迟（m）");
        yAxis.setLabel("多径误差包络（m）");
        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setCreateSymbols(false);

        lineChart.setTitle("不同相关器间隔时多径误差包络");
        //defining a series
        XYChart.Series<Number,Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series11 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series2 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series22 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series3 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series33 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series4= new XYChart.Series<>();
        XYChart.Series<Number,Number> series44 = new XYChart.Series<>();
        double a  = 0;
        double b  = 0;
        double c  = 0;

        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcNoneZero( a, b, c, i, smr, 98, t,24);
            b = Result[0]/Result[1];
            series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series11.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }

        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcNoneZero( a, b, c, i, smr, 49, t,24);
            b = Result[0]/Result[1];
            series2.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series22.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }
        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcNoneZero( a, b, c, i, smr, 24, t,24);
            b = Result[0]/Result[1];
            series3.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series33.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }
        for(double t = 0; t <= 1400; t = t + 2){
            double Result[]=TcNoneZero( a, b, c, i, smr, 1, t,24);
            b = Result[0]/Result[1];
            series4.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
            c =-Result[0]/Result[2];
            series44.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
            a = 0;
            b = 0;
            c = 0;
        }
        lineChart.getData().addAll(series1, series11,series2,series22,series3,series33,series4,series44);
        series1.nodeProperty().get().setStyle("-fx-stroke:Violet;");
        series1.setName("δ->0");
        series11.nodeProperty().get().setStyle("-fx-stroke:Violet;");
        series11.setName("  ");
        series2.setName("δ=1/20");
        series2.nodeProperty().get().setStyle("-fx-stroke:SpringGreen;");
        series22.setName("  ");
        series22.nodeProperty().get().setStyle("-fx-stroke:SpringGreen;");
        series3.nodeProperty().get().setStyle("-fx-stroke:MediumBlue;");
        series3.setName("δ=1/40");
        series33.nodeProperty().get().setStyle("-fx-stroke:MediumBlue;");
        series33.setName("δ=1/10");
        series4.nodeProperty().get().setStyle("-fx-stroke:OrangeRed;");
        series44.nodeProperty().get().setStyle("-fx-stroke:OrangeRed;");
        Picture_save picture_save = new Picture_save(lineChart, "Tcmultipath_bpsk.png");
    }

    /**
     * BPSK的波动方程
     */
    public void wave_bpsk(double smr){
//限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final double i = 0.05;
        //设置横轴和纵轴的标签
        xAxis.setLabel("多径时延（码片）");
        yAxis.setLabel("波动函数数值");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle("波动函数");
        //defining a series
        XYChart.Series<Number,Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series2 = new XYChart.Series<>();
        double a = 0;
        double b = 0;
        for(double t = 0; t <= 1467; t = t + 2){
            for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                a = a + Math.pow(10, -smr/20) * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                        / (pow(Math.PI * f, 2)) *
                        f * f * Math.cos(2 * Math.PI * f * t * 0.001)* i ;

                b = b +  f * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                        / (pow(Math.PI * f, 2)) *
                        f * i ;
            }
            b = a/b;
            series1.getData().add(new XYChart.Data(t * m * 1.023 * 0.001,1+ b));
            series2.getData().add(new XYChart.Data(t * m * 1.023 * 0.001,1- b ));
            a = 0;
            b = 0;
        }
        lineChart.getData().addAll(series1, series2);
        series1.nodeProperty().get().setStyle("-fx-stroke:IndianRed;");
        series2.nodeProperty().get().setStyle("-fx-stroke:IndianRed;");
        Picture_save picture_save = new Picture_save(lineChart, "bpsk_wave.png");
    }

    /**
     * BPSK的滑动平均多径误差包络图绘制
     * @param smr
     * @param Tc
     */
    public void slip_bpsk(double smr, double Tc) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final double i = 0.05;
        //设置横轴和纵轴的标签
        xAxis.setLabel("多径延迟（m）");
        yAxis.setLabel("平均多径误差包络值（m）");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle("滑动平均多径误差包络");
        //defining a series
        XYChart.Series<Number, Number> series= new XYChart.Series<>();
        double a  = 0;
        double b  = 0;
        double c  = 0;
        double d  = 0;
        double e1  = 0;
        double e2  = 0;
        if(Tc != 0){
            for(double t = 0.01; t <= 1667; t = t + 0.5){
                for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                    a = a + Math.pow(10, -smr/20) *  m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                            / (pow(Math.PI * f, 2))  *
                            Math.sin(Math.PI * f * Tc * 0.001) * Math.sin(2 * Math.PI * f * t * 0.001)* i ;

                    b = b + Math.PI * 2 * f * 1000000 *m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                            / (pow(Math.PI * f, 2))  *
                            Math.sin(Math.PI * f * Tc * 0.001) * (1 + Math.pow(10, -smr/20)
                            * Math.cos(2 * Math.PI * f * t * 0.001))* i ;

                    c = c + Math.PI * 2 * f * 1000000 * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                            / (pow(Math.PI * f, 2))  *
                            Math.sin(Math.PI * f * Tc * 0.001) * (1 - Math.pow(10, -smr/20)
                            * Math.cos(2 * Math.PI * f * t * 0.001))* i ;
                }
                b = a/b;
                c = a/c;
                e1 = Math.abs((b + c) / 2);

                d = d + e1 * 0.5;
                e2 = d / t;
                series.getData().add(new XYChart.Data(t * 0.3, e2 * 3 * Math.pow(10, 8)));
                a = 0;
                b = 0;
                c = 0;
            }
        }
        else{
            for(double t = 0.01; t <= 1667; t = t + 0.5){

                for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                    a = a + Math.pow(10, -smr/20) *  m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                            / (pow(Math.PI * f, 2))  *
                            f * Math.sin(2 * Math.PI * f * t * 0.001)* i ;

                    b = b + Math.PI * 2 * f * 1000000 * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                            / (pow(Math.PI * f, 2))  *
                            f * (1 + Math.pow(10, -smr/20)
                            * Math.cos(2 * Math.PI * f * t * 0.001))* i ;

                    c = c + Math.PI * 2 * f * 1000000 * m * 1.023 * pow(Math.sin(Math.PI * f / (m * 1.023)), 2)
                            / (pow(Math.PI * f, 2))  *
                            f * (1 - Math.pow(10, -smr/20)
                            * Math.cos(2 * Math.PI * f * t * 0.001))* i ;
                }
                b = a/b;
                c = a/c;
                e1 = Math.abs((b + c) / 2);

                d = d + e1 * 0.5;
                e2 = d / t;
                series.getData().add(new XYChart.Data(t * 0.3, e2 * 3 * Math.pow(10, 8)));
                a = 0;
                b = 0;
                c = 0;
            }
        }
        lineChart.getData().add(series);
        Picture_save picture_save = new Picture_save(lineChart, "slip_bpsk.png");
    }
}
