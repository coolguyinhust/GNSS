package function;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;

/**
 * Created by kaixin on 2018/12/19.
 * 该类的功能是对以下四个重要参数生成相应的表格并显示出来：（是Chart_common_usage的子集）
 * BPSK、BOC调制信号的带限之后剩余功率、均方根（RMS）带宽rms、
 * 频谱隔离系数（Spectral Separation Coefficient）Kls和
 * 功率谱密度的有效矩形带宽rect等四种重要性能参数；
 */
public class Chart_FourParas {
    private double value1;
    private double value2;
    private double value3;
    private double value4;
    private TableView<Map_Parameters> table;
    public Chart_FourParas(double value1, double value2, double value3, double value4) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.value4 = value4;
        this.paint_FourPara_Tabel();
    }

    public TableView<Map_Parameters> getTable() {
        return table;
    }

    public void paint_FourPara_Tabel(){
        final Label label1 = new Label("带限剩余功率（W）");
        label1.setFont(new Font("Arial", 10));
        final Label label2 = new Label("均方根带宽（MHz）");
        label2.setFont(new Font("Arial", 10));
        final Label label3 = new Label("自身频谱隔离系数（dB/Hz）");
        label3.setFont(new Font("Arial", 10));
        final Label label4 = new Label("等效矩形带宽（MHz）");
        label4.setFont(new Font("Arial", 10));

        ObservableList<Map_Parameters> data =
                FXCollections.observableArrayList(
                        new Map_Parameters(label1.getText(),String.format("%.1f",value1)),
                        new Map_Parameters(label2.getText(),String.format("%.1f",value2)),
                        new Map_Parameters(label3.getText(),String.format("%.1f",value3)),
                        new Map_Parameters(label4.getText(),String.format("%.1f",value4))
                );

        table= new TableView<>();

        table.setEditable(true);

        TableColumn column1 = new TableColumn("特性");
        column1.setMinWidth(400);

        column1.setCellValueFactory(
                new PropertyValueFactory<>("parameters_name"));

        TableColumn column2 = new TableColumn("值");
        column2.setMinWidth(400);
        column2.setCellValueFactory(
                new PropertyValueFactory<>("parameters_value"));
        table.setItems(data);
        table.getColumns().addAll(column1,column2);

        // 将TableView高度调整为行数
        table.setFixedCellSize(120);
        table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(Bindings.size(table.getItems()).add(1)));
        table.minHeightProperty().bind(table.prefHeightProperty());
        table.maxHeightProperty().bind(table.prefHeightProperty());
    }

}
