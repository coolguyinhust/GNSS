package function;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by kaixin on 2018/12/20.
 */
/*
 * Map_Parameters是为了绘制计算四个重要的参数的chart_cell
 * 作为chart的基本数据结构，相当于一个映射表，一个key(parameters_name),映射到一个value（parameters_value）
 */
public  class Map_Parameters {
        final SimpleStringProperty parameters_name;
        final SimpleStringProperty  parameters_value;

        public Map_Parameters(String parameters_name, String parameters_value) {
            this.parameters_name = new SimpleStringProperty(parameters_name);
            this.parameters_value = new SimpleStringProperty(parameters_value);
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

        public String getParameters_value() {
            return parameters_value.get();
        }

        public SimpleStringProperty parameters_valueProperty() {
            return parameters_value;
        }

        public void setParameters_value(String parameters_value) {
            this.parameters_value.set(parameters_value);
        }

}
