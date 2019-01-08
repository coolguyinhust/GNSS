package function;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by kaixin on 2018/12/20.
 * 绘制GNSS常用特性时，一个key（特性）要对应五种常用信号的值
 * parameters_name表示特性；parameters_s1_value表示第一种信号对应的值
 */
public class Map_FiveValue_Parameters {
    private final SimpleStringProperty  parameters_name;
    private final SimpleStringProperty  parameters_s1_value;
    private final SimpleStringProperty  parameters_s2_value;
    private final SimpleStringProperty  parameters_s3_value;
    private final SimpleStringProperty  parameters_s4_value;
    private final SimpleStringProperty  parameters_s5_value;

    public Map_FiveValue_Parameters(String parameters_name, String parameters_s1_value,
                                    String parameters_s2_value,String parameters_s3_value,
                                    String parameters_s4_value,String parameters_s5_value) {
        this.parameters_name =  new SimpleStringProperty(parameters_name);
        this.parameters_s1_value = new SimpleStringProperty( parameters_s1_value);
        this.parameters_s2_value =  new SimpleStringProperty(parameters_s2_value);
        this.parameters_s3_value =  new SimpleStringProperty(parameters_s3_value);
        this.parameters_s4_value =  new SimpleStringProperty(parameters_s4_value);
        this.parameters_s5_value =  new SimpleStringProperty(parameters_s5_value);
    }

    public String getParameters_name() {
        return parameters_name.get();
    }

    public SimpleStringProperty parameters_nameProperty() {
        return parameters_name;
    }

    public void setParameters_name(String parameters_name) {
        this.parameters_name.set(parameters_name);
    }

    public String getParameters_s1_value() {
        return parameters_s1_value.get();
    }

    public SimpleStringProperty parameters_s1_valueProperty() {
        return parameters_s1_value;
    }

    public void setParameters_s1_value(String parameters_s1_value) {
        this.parameters_s1_value.set(parameters_s1_value);
    }

    public String getParameters_s2_value() {
        return parameters_s2_value.get();
    }

    public SimpleStringProperty parameters_s2_valueProperty() {
        return parameters_s2_value;
    }

    public void setParameters_s2_value(String parameters_s2_value) {
        this.parameters_s2_value.set(parameters_s2_value);
    }

    public String getParameters_s3_value() {
        return parameters_s3_value.get();
    }

    public SimpleStringProperty parameters_s3_valueProperty() {
        return parameters_s3_value;
    }

    public void setParameters_s3_value(String parameters_s3_value) {
        this.parameters_s3_value.set(parameters_s3_value);
    }

    public String getParameters_s4_value() {
        return parameters_s4_value.get();
    }

    public SimpleStringProperty parameters_s4_valueProperty() {
        return parameters_s4_value;
    }

    public void setParameters_s4_value(String parameters_s4_value) {
        this.parameters_s4_value.set(parameters_s4_value);
    }

    public String getParameters_s5_value() {
        return parameters_s5_value.get();
    }

    public SimpleStringProperty parameters_s5_valueProperty() {
        return parameters_s5_value;
    }

    public void setParameters_s5_value(String parameters_s5_value) {
        this.parameters_s5_value.set(parameters_s5_value);
    }
}
