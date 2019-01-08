package function;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import signal.BOC;
import signal.BPSK;

import static java.lang.Math.abs;

/**
 * Created by kaixin on 2018/12/20.
 */
public class Chart_common_usage {
    private BPSK bpsk1=new BPSK(1);
    private BPSK bpsk10=new BPSK(10);
    private BOC boc52=new BOC(5,2);
    private BOC boc84=new BOC(8,4);
    private BOC boc105=new BOC(10,5);

    public Chart_common_usage() {
        this.paint_chart();
    }

    public TableView<Map_FiveValue_Parameters> getTable() {
        return table;
    }

    private TableView<Map_FiveValue_Parameters> table;

    public void paint_chart(){
        final Label label1 = new Label("频谱主瓣距频带中心的频偏(MHz)");
        label1.setFont(new Font("Arial", 5));
        final Label label2 = new Label("主瓣最大功率谱密度 (dBW/Hz)");
        label2.setFont(new Font("Arial", 5));
        final Label label3 = new Label("90%功率的带宽(MHz)");
        label3.setFont(new Font("Arial", 5));
        final Label label4 = new Label("带外的损失(dB)");
        label4.setFont(new Font("Arial", 5));
        final Label label5 = new Label("RMS带宽(MHz)");
        label1.setFont(new Font("Arial", 5));
        final Label label6 = new Label("等效矩形带宽(MHz)");
        label2.setFont(new Font("Arial", 5));
        final Label label7 = new Label("自身频谱隔离系数（dB/Hz）");
        label3.setFont(new Font("Arial", 5));
        final Label label8 = new Label("与1.023MHzBPSK的频谱隔离系数(dB/Hz)");
        label4.setFont(new Font("Arial", 5));
        final Label label9 = new Label("与BOC(10,5)的频谱隔离系数(dB/Hz)");
        label4.setFont(new Font("Arial", 5));
        final Label label10 = new Label("自相关函数主峰与第一副峰间的时延(ns)");
        label4.setFont(new Font("Arial", 5));
        final Label label11 = new Label("自相关函数第一副峰与主峰幅度平方之比");
        label4.setFont(new Font("Arial", 5));
        ObservableList<Map_FiveValue_Parameters> data =
                FXCollections.observableArrayList(
                        //频谱主瓣距频带中心的频偏
                        new Map_FiveValue_Parameters(label1.getText(), "0", "0",
                                "±"+String.format("%.1f",abs(boc52.get_Max_PowerSpectrum()[0])),
                                "±"+String.format("%.1f",abs(boc84.get_Max_PowerSpectrum()[0])),
                                "±"+String.format("%.1f",abs(boc105.get_Max_PowerSpectrum()[0]))),
                        //主瓣最大功谱密度
                        new Map_FiveValue_Parameters(label2.getText(),
                                String.format("%.1f",bpsk1.get_Max_PowerSpectrum()),
                                String.format("%.1f",bpsk10.get_Max_PowerSpectrum()),
                                String.format("%.1f",boc52.get_Max_PowerSpectrum()[1]),
                                String.format("%.1f",boc84.get_Max_PowerSpectrum()[1]),
                                String.format("%.1f",boc105.get_Max_PowerSpectrum()[1])),
                        //90%功率的带宽
                        new Map_FiveValue_Parameters(label3.getText(), String.format("%.1f",bpsk1.getNinetyPercentBand()), String.format("%.1f",bpsk10.getNinetyPercentBand()),
                                String.format("%.1f",boc52.getNinetyPercentBand()),String.format("%.1f",boc84.getNinetyPercentBand()),String.format("%.1f",boc105.getNinetyPercentBand())),
                        //带外损失
                        new Map_FiveValue_Parameters(label4.getText(), String.format("%.1f",bpsk1.band_loss()), String.format("%.1f",bpsk10.band_loss()),
                                String.format("%.1f",boc52.band_loss()),String.format("%.1f",boc84.band_loss()),String.format("%.1f",boc105.band_loss())),
                        //RMS带宽
                        new Map_FiveValue_Parameters(label5.getText(), String.format("%.1f",bpsk1.getRMSBand()), String.format("%.1f",bpsk10.getRMSBand()),
                                String.format("%.1f",boc52.getRMSBand()),String.format("%.1f",boc84.getRMSBand()),String.format("%.1f",boc105.getRMSBand())),
                        //等效矩形带宽
                        new Map_FiveValue_Parameters(label6.getText(), String.format("%.1f",bpsk1.getRectBand()), String.format("%.1f",bpsk10.getRectBand()),
                                String.format("%.1f",boc52.getRectBand()),String.format("%.1f",boc84.getRectBand()),String.format("%.1f",boc105.getRectBand())),
                        //与自身的频谱隔离系数
                        new Map_FiveValue_Parameters(label7.getText(), String.format("%.1f",bpsk1.getFrequencyIsolationFactor()), String.format("%.1f",bpsk10.getFrequencyIsolationFactor()),
                                String.format("%.1f",boc52.getFrequencyIsolationFactor()),String.format("%.1f",boc84.getFrequencyIsolationFactor()),String.format("%.1f",boc105.getFrequencyIsolationFactor())),
                        //与1．023MHzBPSK的频谱隔离系数
                        new Map_FiveValue_Parameters(label8.getText(), String.format("%.1f",bpsk1.getFrequencyIsolationFactorBPSK1()), String.format("%.1f",bpsk10.getFrequencyIsolationFactorBPSK1()),
                            String.format("%.1f",boc52.getFrequencyIsolationFactorBPSK1()),String.format("%.1f",boc84.getFrequencyIsolationFactorBPSK1()),String.format("%.1f",boc105.getFrequencyIsolationFactorBPSK1())),
                        //与1．023MHzBPSK的频谱隔离系数
                        new Map_FiveValue_Parameters(label9.getText(), String.format("%.1f",bpsk1.getFrequencyIsolationFactorBOC105()), String.format("%.1f",bpsk10.getFrequencyIsolationFactorBOC105()),
                                String.format("%.1f",boc52.getFrequencyIsolationFactorBOC105()),String.format("%.1f",boc84.getFrequencyIsolationFactorBOC105()),String.format("%.1f",boc105.getFrequencyIsolationFactorBOC105())),
                        //自相关函数主峰与第一副峰间的时延
                        new Map_FiveValue_Parameters(label10.getText(), "无", "无",
                                String.format("%.1f",abs(boc52.delay_SelfCorrelation()[0])),String.format("%.1f",abs(boc84.delay_SelfCorrelation()[0])),String.format("%.1f",abs(boc105.delay_SelfCorrelation()[0]))),
                        //自相关函数第一副峰与主峰幅度平方之比
                        new Map_FiveValue_Parameters(label11.getText(), "无", "无",
                                String.format("%.2f",boc52.delay_SelfCorrelation()[1]),String.format("%.2f",boc84.delay_SelfCorrelation()[1]),String.format("%.2f",boc105.delay_SelfCorrelation()[1]))


                        );

        table = new TableView<>();

        table.setEditable(true);

        TableColumn firstNameCol = new TableColumn("特性");
        firstNameCol.setMinWidth(300);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<>("parameters_name"));

        TableColumn BPSK1 = new TableColumn("BPSK(1)");
        BPSK1.setMinWidth(100);
        BPSK1.setCellValueFactory(
                new PropertyValueFactory<>("parameters_s1_value"));

        TableColumn BPSK10 = new TableColumn("BPSK(10)");
        BPSK10.setMinWidth(100);
        BPSK10.setCellValueFactory(
                new PropertyValueFactory<>("parameters_s2_value"));

        TableColumn BOC52 = new TableColumn("BOC(5,2)");
        BOC52.setMinWidth(100);
        BOC52.setCellValueFactory(
                new PropertyValueFactory<>("parameters_s3_value"));

        TableColumn BOC84 = new TableColumn("BOC(8,4)");
        BOC84.setMinWidth(100);
        BOC84.setCellValueFactory(
                new PropertyValueFactory<>("parameters_s4_value"));

        TableColumn BOC105 = new TableColumn("BOC(10,5)");
        BOC105.setMinWidth(100);
        BOC105.setCellValueFactory(
                new PropertyValueFactory<>("parameters_s5_value"));

        table.setItems(data);
        table.getColumns().addAll(firstNameCol, BPSK1, BPSK10,BOC52, BOC84, BOC105);
        // 将TableView高度调整为行数
        table.setFixedCellSize(50);
        table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(Bindings.size(table.getItems()).add(1)));
        table.minHeightProperty().bind(table.prefHeightProperty());
        table.maxHeightProperty().bind(table.prefHeightProperty());
    }

}
