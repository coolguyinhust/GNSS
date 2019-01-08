package GNSS;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {
    public static ArrayList<Controller> controllerList=new ArrayList<>();
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = fxmlLoader.load();
        //如果使用 Parent root = FXMLLoader.load(...) 静态读取方法，无法获取到Controller的实例对象
        Controller controller = fxmlLoader.getController(); //获取Controller的实例对象
        controllerList.add(controller);
        controller.setprimaryStage(primaryStage);

        primaryStage.setTitle("GNSS信号分析仪");
        primaryStage.setScene(new Scene(root, 750, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
      launch(args);
    }
}
