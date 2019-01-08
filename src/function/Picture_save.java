package function;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by kaixin on 2018/12/19.
 */
/*
 * 将图表lineChart用界面的方式呈现，并添加保存等功能
 */
public class Picture_save {
    private Stage stage= new Stage();
    private Scene scene;
    private LineChart<Number, Number> lineChart;
    private TableView<?> table ;
    private BorderPane root = new BorderPane();
    private MenuBar menuBar = new MenuBar();
    private Menu menu = new Menu("保存");
    private MenuItem menuItem1 = new MenuItem("默认保存");
    private MenuItem menuItem2 = new MenuItem("另存为");
    private String name;//默认保存时保存的图片的名字
    public Picture_save(LineChart<Number, Number> lineChart,String name) {
        this.lineChart = lineChart;
        this.name=name;
        menuBar.prefWidthProperty().bind(stage.widthProperty());
        menu.getItems().add(menuItem1);
        menu.getItems().add(menuItem2);
        menuItem1.setOnAction(e -> save());
        menuItem2.setOnAction(e -> save_as());
        menuBar.getMenus().add(menu);
        root.setTop(menuBar);
        scene = new Scene(root, 800, 600);
        root.setCenter(lineChart);
        stage.setScene(scene);
        stage.show();
    }

    public Picture_save(TableView<?> table, String name) {
        this.table=table;
        this.name=name;
        menuBar.prefWidthProperty().bind(stage.widthProperty());
        menu.getItems().add(menuItem1);
        menu.getItems().add(menuItem2);
        menuItem1.setOnAction(e -> save());
        menuItem2.setOnAction(e -> save_as());
        menuBar.getMenus().add(menu);
        root.setTop(menuBar);
        scene = new Scene(root, 800, 600);
        scene.getStylesheets().add("./stylesheet/tabelview.css");
        root.setCenter(table);
        stage.setScene(scene);
        stage.show();
    }



    public void save() {
        WritableImage snapShot = scene.snapshot(null);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null),
                    "png", new File("images\\"+name));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void save_as() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Picture_save Image");
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
}
