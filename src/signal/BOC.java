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
 * BOC调制有两个参数，记为BOC(α,β);
 * 副载波频率fs=α*f0,扩频码速率fc=β*f0;f0为基频，默认为1.023MHz
 * n=2fs/fc=2α/β
 * Ct亚载波周期为2Ts,Ts=1/(2fs)
 */

public class BOC {
    private double x;
    private double y;
    private double fs;
    private double fc;
    private int n;

    private double br = 24;//br是BOC的限制带宽，如果用户没有输入限定带宽，默认是GPS信号接收的带宽


    public double getFc() {
        return fc;
    }

    /**
     * BOC(α,β)
     *
     * @param x  α
     * @param y  β
     * @param br 限制带宽
     */
    public BOC(double x, double y, double br) {
        this.x = x;
        this.y = y;
        this.fs = x * 1.023;
        this.fc = y * 1.023;
        this.n = (int) (2 * fs / fc);
        this.br = br;
    }

    /**
     * @param x α
     * @param y β
     */
    public BOC(double x, double y) {
        this.x = x;
        this.y = y;
        this.fs = x * 1.023;
        this.fc = y * 1.023;
        this.n = (int) (2 * fs / fc);
    }

    /**
     * 绘制BOC的归一化自相关函数图像
     * <ul>
     * <li>无需传入参数和返回值，用到的参数从本类中获取</li>
     * <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性</li>
     * <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）</li>
     * <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
     */
    public void self_correlation() {
        double p = x / y;
        int k;
        double a = 0;
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-1.2, 1.2, 0.2);
        final NumberAxis yAxis = new NumberAxis(-1, 1, 0.5);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码片 chips");
        yAxis.setLabel("幅度");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BOC(" + x + "," + y + ")自相关函数图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("self correlation function graph");
        if (br == -1) {
            for (double t = -1; t <= 1; t = t + 0.001) {
                k = (int) Math.ceil(2 * p * Math.abs(t));
                series.getData().add(new XYChart.Data(t, Math.pow(-1, k + 1) *
                        ((1 / p) * (-k * k + 2 * p * k + k - p) - (4 * p - 2 * k + 1) * Math.abs(t))));
            }
            for (double t = -1.2; t <= -1; t = t + 0.001) {
                series.getData().add(new XYChart.Data(t, 0));
            }
            for (double t = 1; t <= 1.2; t = t + 0.001) {
                series.getData().add(new XYChart.Data(t, 0));
            }
        } else if (n % 2 == 0) {
            double value0 = 0;
            for (double f = -(br / 2); f <= (br / 2); f = f + 0.1) {
                value0 = value0 + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * 0.1 * 1000000;
            }
            for (double t = -1; t <= 1; t = t + 0.001) {
                for (double f = -(br / 2); f <= (br / 2); f = f + 0.1) {
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t / fc) * 0.1 * 1000000;
                }
                a /= value0;
                series.getData().add(new XYChart.Data(t, a));
                a = 0;
            }
        } else {
            //求出┏=0时候的值，以便归一化
            double value0 = 0;
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + 0.1) {
                value0 = value0 + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * 0.1 * 1000000;
            }
            for (double t = -1; t <= 1; t = t + 0.001) {
                for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + 0.1) {
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t / fc) * 0.1 * 1000000;
                }
                a /= value0;
                series.getData().add(new XYChart.Data(t, a));
                a = 0;
            }
        }
        lineChart.getData().add(series);
        Picture_save picture_save = new Picture_save(lineChart, "BOC_self_correlation.png");
    }

    /**
     * 计算自相关函数主峰与第一副峰间的时延,与自相关函数第一副峰与主峰幅度平方之比
     *
     * @return 返回的是包含两个浮点型数的Double数组，
     * 第一个浮点数表示自相关函数主峰与第一副峰间的时延
     * 第二个浮点数表示自相关函数第一副峰与主峰幅度平方之比
     */
    public Double[] delay_SelfCorrelation() {
        double a = 0, i = 0.05;
        double x_min = 0, y_min = 0;//分别代表副峰的横坐标和纵坐标
        double y_max = 0;//y_max是主峰的幅度
        if ((2 * fs / fc) % 2 == 0) {

            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                y_max = y_max + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }

            for (double t = -500; t < 0; t = t + 0.2) {

                for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t * 0.001) * i * 1000000;
                }
                if (a < y_min) {
                    y_min = a;
                    x_min = t;
                }

                a = 0;
            }
        } else {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                y_max = y_max + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) * i * 1000000;
            }

            for (double t = -500; t < 0; t = t + 0.2) {

                for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                    a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2) *
                            Math.cos(2 * Math.PI * f * t * 0.001) * i * 1000000;
                }
                if (a < y_min) {
                    y_min = a;
                    x_min = t;
                }

                a = 0;
            }
        }
        return new Double[]{x_min, pow(y_min / y_max, 2)};
    }

    /**
     * 计算带限剩余功率
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     *
     * @return 带限剩余功率
     */
    public double getLimitedBandWidth() {
        double a = 0;
        //i为小矩形的宽
        double i = 0.05;
        if (n % 2 == 0) {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i;
            }
        } else {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i;
            }
        }
        return a;
    }

    /**
     * 计算均方根带宽
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     *
     * @return 均方根带宽
     */
    public double getRMSBand() {
        double a = 0;
        double i = 0.05;
        if (n % 2 == 0) {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + f * f * fc * pow(getLimitedBandWidth(), -1) * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i * pow(10, 12);
            }
            a = pow(a, 0.5) / 1000000;
        } else {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + f * f * fc * pow(getLimitedBandWidth(), -1) * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i * pow(10, 12);
            }
            a = pow(a, 0.5) / 1000000;
        }
        return a;
    }

    /**
     * 计算与自身的频谱隔离系数
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     *
     * @return 与自身的频谱隔离系数
     */
    public double getFrequencyIsolationFactor() {
        double a = 0, b = 0, i = 0.05;
        if (n % 2 == 0) {
            for (double f = -15; f >= -15 && f <= 15; f = f + i) {
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i;
            }
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                b = b + pow(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2), 2) * i * 1e-6;
            }
            b = b / a;
            b = 10 * log10(b);
        } else {
            for (double f = -15; f >= -15 && f <= 15; f = f + i) {
                a = a + fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i;
            }
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                b = b + pow(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2), 2) * i * 1e-6;
            }
            b = b / a;
            b = 10 * log10(b);
        }
        return b;
    }

    /**
     * 计算与BPSK-1的频谱隔离系数
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     *
     * @return 与BPSK1的频谱隔离系数
     */
    public double getFrequencyIsolationFactorBPSK1() {
        double a = 0, b = 0, i = 0.05, power_bpsk = 0;
        for (double f = -15; f >= -15 && f <= 15; f = f + i) {
            power_bpsk = power_bpsk + 1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2)) * i;
        }
        if (n % 2 == 0) {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                b = b + fc * pow(Math.tan(Math.PI * f / (2 * fs)) * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                        (1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2))) * i * 1e-6;
            }
            b = b / power_bpsk;
            b = 10 * log10(b);
        } else {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                b = b + fc * pow(Math.tan(Math.PI * f / (2 * fs)) * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                        (1.023 * pow(Math.sin(Math.PI * f / 1.023), 2) / (pow(Math.PI * f, 2))) * i * 1e-6;
            }
            b = b / power_bpsk;
            b = 10 * log10(b);
        }
        return b;
    }

    /**
     * 计算与BOC(10，5)的频谱隔离系数,fs=10*1.023,fc=5*1.023
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     *
     * @return 与BOC(10, 5)的频谱隔离系数
     */
    public double getFrequencyIsolationFactorBOC105() {
        double value = 0, i = 0.05;
        double a = 0;
        for (double f = -15; f >= -15 && f <= 15; f = f + i) {
            value = value + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                    * Math.sin(Math.PI * f / 5.115), 2) / pow(Math.PI * f, 2) * i;
        }
        if (n % 2 == 0) {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                        * Math.sin(Math.PI * f / 5.115), 2) / pow(Math.PI * f, 2) *
                        fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i * 1e-6;
            }
        } else {
            for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                a = a + 5.115 * pow(Math.tan(Math.PI * f / (2 * 10.23))
                        * Math.sin(Math.PI * f / 5.115), 2) / pow(Math.PI * f, 2) *
                        fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) * i * 1e-6;
            }
        }
        a = a / value;
        a = 10 * log10(a);
        return a;
    }

    /**
     * 计算有效矩形带宽
     * <ul>
     * <li>无需传入参数</li>
     * </ul>
     *
     * @return 有效矩形带宽
     */
    public double getRectBand() {
        double b = 0;
        if (n % 2 == 0) {
            b = fc * pow(Math.tan(Math.PI * get_Max_PowerSpectrum()[0] / (2 * fs))
                    * Math.sin(Math.PI * get_Max_PowerSpectrum()[0] / fc), 2) * pow(10, -6) / pow(Math.PI * (get_Max_PowerSpectrum()[0]), 2);
            b = getLimitedBandWidth() / b;
            b = b / 1000000;
        } else {
            //计算有效矩形带宽
            b = fc * pow(Math.tan(Math.PI * get_Max_PowerSpectrum()[0] / (2 * fs))
                    * Math.cos(Math.PI * get_Max_PowerSpectrum()[0] / fc), 2) * pow(10, -6) / pow(Math.PI * get_Max_PowerSpectrum()[0], 2);
            b = getLimitedBandWidth() / b;
            b = b / 1000000;
        }
        return b;
    }

    /**
     * 计算BOC四种重要性能参数
     * <ul>
     * <li>无需传入参数和返回值，用到的参数从本类中获取</li>
     * <li>具体方法是，构造一个Chart_FourParas类对象，将4个参数传入</li>
     * <li>生成一个javafx.scene.control中TableView对象，调用getTable()得到表格</li>
     * <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
     */
    public void four_parameters() {
        Chart_FourParas p = new Chart_FourParas(this.getLimitedBandWidth(), this.getRMSBand(),
                this.getFrequencyIsolationFactor(), this.getRectBand());
        TableView<Map_Parameters> table = p.getTable();
        Picture_save picture_save = new Picture_save(table, "BOC_four_parameters.png");
    }

    /**
     * 绘制BOC的时域图像
     * <ul>
     * <li>无需返回值</li>
     * <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性</li>
     * <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）</li>
     * <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
     *
     * @param array 整型数组是由 1 和 -1 组成的双极性码，1表示高电平，-1表示低电平
     */
    public void paint_time(int[] array) {
        int k = array.length;
        double Ts = 1 / (2 * fs);
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(0, k + 0.5, 0.5);
        final NumberAxis yAxis = new NumberAxis(-1.5, 1.5, 0.5);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码片 chips");
        yAxis.setLabel("幅度");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BOC(" + x + "," + y + ")调制时域图像");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("time graph with number of " + k + "code element \n" + Arrays.toString(array));

        for (double t = 0; t <= k; t = t + 0.001) {
            series.getData().add(new XYChart.Data(t, (array[(int) Math.floor(t)]) *
                    ((int) Math.floor(t * n) % 2 == 0 ? 1 : -1)));
        }
        lineChart.getData().add(series);
        Picture_save picture_save = new Picture_save(lineChart, "BOC_time_domain.png");
    }

    /**
     * 绘制BOC的功率谱密度图像
     * <ul>
     * <li>无需传入参数和返回值，用到的参数从本类中获取</li>
     * <li>具体方法是，生成一个javafx.scene.chart库中lineChart对象，设置其常用属性</li>
     * <li>再生成一个javafx.scene.chart库中XYChart对象，向其中添加（x，y）</li>
     * <li>最后把添加了XYChart的linechart引用对象传入到图片保存的类中增加保存图片的功能。</li>
     * </ul>
     */
    public void paint_frequency() {
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(-17.5, 17.5, 2.5);
        final NumberAxis yAxis = new NumberAxis(-180, -60, 15);
        //设置横轴和纵轴的标签
        xAxis.setLabel("频率（MHz）");
        yAxis.setLabel("功率谱密度（dBW/Hz）");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BOC(" + x + "," + y + ")调制功率谱");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("frequency within 15MHz");

        if ((2 * fs / fc) % 2 == 0) {
            for (double f = -15; f >= -15 && f <= 15; f = f + 0.01) {
                series.getData().add(new XYChart.Data(f, 10 * log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2))));
            }

        } else {
            for (double f = -15; f >= -15 && f <= 15; f = f + 0.01) {
                series.getData().add(new XYChart.Data(f, 10 * log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2))));
            }
        }
        lineChart.getData().add(series);
        Picture_save picture_save = new Picture_save(lineChart, "BOC_frequency_domain.png");
    }

    /**
     * 计算BOC调制信号的最大功率谱密度，找到一个（x,y），使x对应的y值最大
     * <ul>
     * <li>无需传入参数</li>
     * <li> 第一个浮点数表示频谱主瓣距频带中心的频偏</li>
     * <li>第二个浮点数表示主瓣最大功谱密度</li>
     * </ul>
     *
     * @return 返回的是包含两个浮点型数的Double数组
     */
    public Double[] get_Max_PowerSpectrum() {
        double max_power = 0;
        Map<Double, Double> map = new HashMap<>();
        if ((2 * fs / fc) % 2 == 0) {
            for (double f = -br / 2; f < 0; f = f + 0.001) {
                map.put(f, 10 * log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2)));
            }

        } else {
            for (double f = -br / 2; f < 0; f = f + 0.001) {
                map.put(f, 10 * log10(fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) * pow(10, -6) / pow(Math.PI * f, 2)));
            }
        }
        //这里将map.entrySet()转换成list
        List<Map.Entry<Double, Double>> list = new ArrayList<Map.Entry<Double, Double>>(map.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list, new Comparator<Map.Entry<Double, Double>>() {
            //升序排序
            public int compare(Map.Entry<Double, Double> o1,
                               Map.Entry<Double, Double> o2) {
                if (o1.getValue() - o2.getValue() > 0)
                    return -1;
                else if (o1.getValue() - o2.getValue() == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        return new Double[]{list.get(0).getKey(), list.get(0).getValue()};
    }

    /**
     * 计算90%功率的带宽band_n，其中90％功率的带宽指对于卫星发射的带宽为30MHz的信号，通过其90％的功率所需要的带宽
     * 利用功率谱的对称性，要对band_n做积分=0.9,即从-15，到band_n/2做积分等于0.05
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，需要用到积分，这里我们用微元法，即用小矩形的面积来计算积分的值。</li>
     * </ul>
     *
     * @return band which can get 90% power
     */
    public double getNinetyPercentBand() {
        //从-fi到fi上对功率谱积分应该等于0.9，band_n=2*fi
        double fi = -15;
        double power = 0;
        double value;
        for (; fi <= 15; fi += 0.005) {
            if ((2 * fs / fc) % 2 == 0) {
                value = fc * pow(Math.tan(Math.PI * fi / (2 * fs))
                        * Math.sin(Math.PI * fi / fc), 2) / pow(Math.PI * fi, 2);
            } else {
                value = fc * pow(Math.tan(Math.PI * fi / (2 * fs))
                        * Math.cos(Math.PI * fi / fc), 2) / pow(Math.PI * fi, 2);
            }
            power += value * 0.005;
            if (power > power_sender() * (1 - 0.9) / 2) {
                break;
            }
        }
        return 2 * Math.abs(fi);
    }

    /**
     * 求归一化（无限带宽上功率为1）后的30MHz频带内的发送功率
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     *
     * @return 返回对-15MHz到15MHz区域上的积分值，得到总功率以便归一化
     */
    private double power_sender() {
        double value;
        double power_sender = 0;
        if ((2 * fs / fc) % 2 == 0) {
            for (double f = -15; f <= 15; f += 0.005) {
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2);
                power_sender += value * 0.005;
            }
        } else {
            for (double f = -15; f <= 15; f += 0.005) {
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2);
                power_sender += value * 0.005;
            }

        }

        return power_sender;
    }

    /**
     * 带外损失功率（dB）的定为为：
     * - 10 Log10[带内功率（归一化的）/1]  (dB)
     * 发射带宽为30MHz,接收带宽为24MHz损失的功率
     * <ul>
     * <li>无需传入参数</li>
     * <li>具体计算中，设计到积分的计算用微元法</li>
     * </ul>
     *
     * @return 带外损失功率
     */
    public double band_loss() {
        double value;
        double power_receiver = 0;

        for (double f = -12; f <= 12; f += 0.005) {
            if ((2 * fs / fc) % 2 == 0) {
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2);
            } else {
                value = fc * pow(Math.tan(Math.PI * f / (2 * fs))
                        * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2);
            }
            power_receiver += value * 0.005;
        }

        //band_in是带内的损失；
        double band_in = power_receiver / power_sender();
        //带内损失转化为带外损失
        double band_out = -10 * log10(band_in / 1);
        return band_out;
    }

    /**
     * @param t    超前滞后本地码之间的间隔
     * @param bl   码跟踪环单边噪声等效矩形带宽 (Hz)
     * @param sCN0 信噪比
     * @return 返回两个浮点数，一个是传输速率为50bps的，一个是传输速率为200bps的
     * @author Wangyu
     * 码跟踪误差的计算,分析不同条件下BOC调制信号码跟踪精度的影响
     */
    public double[] trackerError(double t, double bl, double sCN0) {
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
        } else {
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

    /**
     * @author Wangyu
     * 绘制BOC的误码跟踪曲线，以超前滞后本地码之间的间隔为横坐标，跟踪误差为纵坐标
     */
    public void paint_errorInterval() {
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(20, 60, 5);
        final NumberAxis yAxis = new NumberAxis(0, 1, 0.1);
        //设置横轴和纵轴的标签
        xAxis.setLabel("码跟踪误差 △/ns");
        yAxis.setLabel("超前-滞后本地码间隔 /m");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("BOC(" + x + "," + y + ")码跟踪误差");
        //defining a series
        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();
        series1.setName("50bps");
        series2.setName("200bps");
        for (double t = 20; t <= 80; t += 0.1) {
            double[] result = this.trackerError(t, 1, 1000);
            series1.getData().add(new XYChart.Data(t, result[0]));
            series2.getData().add(new XYChart.Data(t, result[1]));
        }
        lineChart.getData().addAll(series1, series2);
        Picture_save picture_save = new Picture_save(lineChart, "errorboc.png");
    }

    /**
     * 绘制BOC的误码跟踪曲线，信噪比为横坐标，跟踪误差为纵坐标
     * BOC(5，2)为80ns，BOC(8，4)为50ns，BOC(10，5)为40ns
     */
    public void paint_errorSCN0() {
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis(20, 40, 5);
        final NumberAxis yAxis = new NumberAxis(0, 1, 0.1);
        //设置横轴和纵轴的标签
        xAxis.setLabel("C/N0 dB-Hz");
        yAxis.setLabel("Code Trace Error");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        lineChart.setTitle("信号改变信噪比时的码跟踪误差");
        //defining a series
        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();
        XYChart.Series series3 = new XYChart.Series();
        XYChart.Series series4 = new XYChart.Series();
        XYChart.Series series5 = new XYChart.Series();
        series1.setName("BOC(5，2)");
        series2.setName("BOC(8，4)");
        series3.setName("BOC(10，5)");
        series4.setName("BPSK-(1)");
        series5.setName("BPSK-(10)");
        double[] result;
        for (double cn = 20; cn <= 40; cn += 0.5) {
            result = new BOC(5, 2).trackerError(80, 0.1, pow(10, cn / 10));
            series1.getData().add(new XYChart.Data(cn, result[1]));
            result = new BOC(8, 4).trackerError(50, 0.1, pow(10, cn / 10));
            series2.getData().add(new XYChart.Data(cn, result[1]));
            result = new BOC(10, 5).trackerError(40, 0.1, pow(10, cn / 10));
            series3.getData().add(new XYChart.Data(cn, result[1]));
            result = new BPSK(1).trackerError(48.87, 0.1, pow(10, cn / 10));
            System.out.println((new BPSK(10).trackerError(48.87, 0.1, 100))[1]);
            series4.getData().add(new XYChart.Data(cn, result[1]));
            result = new BPSK(10).trackerError(48.87, 0.1, pow(10, cn / 10));
            //注：为了与论文图片保持一致，result[1]改成了result[0]*系数
            series5.getData().add(new XYChart.Data(cn, result[0] * 2.74));
        }
        lineChart.getData().addAll(series1, series2, series3, series4, series5);
        Picture_save picture_save = new Picture_save(lineChart, "errorCN0.png");
    }

    /**
     * 自定义的多径误差分析：根据输入的SMR和相关器间隔进行多径误差包络的绘制。
     */
    public void multipath_error(double smr, double Tc) {
        Stage stage = new Stage();
        //限定坐标轴的范围，tickUnit是坐标轴上一大格的刻度
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final double i = 0.05;
        //设置横轴和纵轴的标签
        xAxis.setLabel("多径延迟（m）");
        yAxis.setLabel("多径误差包络（m）");
        //creating the chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle("多径误差包络");
        //defining a series
        XYChart.Series<Number,Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number,Number> series2 = new XYChart.Series<>();
        double a = 0;
        double b = 0;
        double c = 0;
        if (Tc != 0) {
            if ((2 * fs / fc) % 2 == 0) {
                for (double t = 0; t <= 1400; t = t + 2) {
                    for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                        a = a + Math.pow(10, -smr / 20) * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                Math.sin(Math.PI * f * Tc * 0.001) * Math.sin(2 * Math.PI * f * t * 0.001) * i;
                        b = b + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                Math.sin(Math.PI * f * Tc * 0.001) * (1 + Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                        c = c + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                Math.sin(Math.PI * f * Tc * 0.001) * (1 - Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                    }
                    b = a / b;
                    series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
                    c = -a / c;
                    series2.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
                    a = 0;
                    b = 0;
                    c = 0;
                }
            } else {
                for (double t = 0; t <= 1400; t = t + 2) {
                    for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                        a = a + Math.pow(10, -smr / 20) * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                Math.sin(Math.PI * f * Tc * 0.001) * Math.sin(2 * Math.PI * f * t * 0.001) * i;
                        b = b + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                Math.sin(Math.PI * f * Tc * 0.001) * (1 + Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                        c = c + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                Math.sin(Math.PI * f * Tc * 0.001) * (1 - Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                    }
                    b = a / b;
                    series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
                    c = -a / c;
                    series2.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
                    a = 0;
                    b = 0;
                    c = 0;
                }
            }
        } else {
            if ((2 * fs / fc) % 2 == 0) {
                for (double t = 0; t <= 1400; t = t + 2) {
                    for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                        a = a + Math.pow(10, -smr / 20) * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                f * Math.sin(2 * Math.PI * f * t * 0.001) * i;
                        b = b + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                f * (1 + Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                        c = c + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.sin(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                f * (1 - Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                    }
                    b = a / b;
                    series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
                    c = -a / c;
                    series2.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));
                    a = 0;
                    b = 0;
                    c = 0;
                }
            } else {
                for (double t = 0; t <= 1400; t = t + 2) {
                    for (double f = -(br / 2); f >= -(br / 2) && f <= (br / 2); f = f + i) {
                        a = a + Math.pow(10, -smr / 20) * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                f * Math.sin(2 * Math.PI * f * t * 0.001) * i;
                        b = b + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                f * (1 + Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                        c = c + Math.PI * 2 * f * 1000000 * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                                * Math.cos(Math.PI * f / fc), 2) / pow(Math.PI * f, 2) *
                                f * (1 - Math.pow(10, -smr / 20)
                                * Math.cos(2 * Math.PI * f * t * 0.001)) * i;
                    }
                    b = a / b;
                    series1.getData().add(new XYChart.Data(t * 0.3, b * 3 * Math.pow(10, 8)));
                    c = -a / c;
                    series2.getData().add(new XYChart.Data(t * 0.3, c * 3 * Math.pow(10, 8)));

                    a = 0;
                    b = 0;
                    c = 0;
                }
            }
        }
        lineChart.getData().addAll(series1, series2);
        series1.nodeProperty().get().setStyle("-fx-stroke:IndianRed;");
        series2.nodeProperty().get().setStyle("-fx-stroke:IndianRed;");
        Picture_save picture_save = new Picture_save(lineChart, "multipath_boc.png");
    }

    public void wave_boc(double smr, double Tc){
        Stage stage = new Stage();
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
        if((2 * fs / fc) % 2 == 0){
            for(double t = 0; t <= 1467; t = t + 2){

                for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                    a = a + Math.pow(10, -smr/20) *  fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) *
                            f * f * Math.cos(2 * Math.PI * f * t * 0.001)* i ;

                    b = b +  f * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.sin(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) *
                            f * i ;
                }
                b = a/b;
                series1.getData().add(new XYChart.Data(t * 1 * 1.023 * 0.001,1+ b));
                series2.getData().add(new XYChart.Data(t * 1 * 1.023 * 0.001,1- b ));
                a = 0;
                b = 0;
            }
        }
        else{
            for(double t = 0; t <= 1400; t = t + 2){
                for(double f = -(br/2); f >= -(br/2) && f <= (br/2); f = f + i){
                    a = a + Math.pow(10, -smr/20) *  fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) *
                            f * f * Math.cos(2 * Math.PI * f * t * 0.001)* i ;

                    b = b +  f * fc * pow(Math.tan(Math.PI * f / (2 * fs))
                            * Math.cos(Math.PI * f / fc), 2)  / pow(Math.PI * f, 2) *
                            f * i ;
                }
                b = a/b;
                series1.getData().add(new XYChart.Data(t * 1 * 1.023 * 0.001,1+ b));
                series2.getData().add(new XYChart.Data(t * 1 * 1.023 * 0.001,1- b ));
                a = 0;
                b = 0;
            }

        }
    }
}
