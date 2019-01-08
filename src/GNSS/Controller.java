package GNSS;

import function.Chart_common_usage;
import function.Map_FiveValue_Parameters;
import function.Picture_save;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import signal.BOC;
import signal.BPSK;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Controller implements Initializable{

    //primaryStage是从Main中动态加载的，为主舞台
    private Stage primaryStage=null;
    //如果没有新建窗口，using_Stage为null
    //如果新建了窗口，using_Stage当前的使用窗口
    private Stage using_Stage=null;

    public void setprimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    public void setUsing_Stage(Stage using_Stage) {
        this.using_Stage = using_Stage;
    }

    public Stage getUsing_Stage() {
        return using_Stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    private AnchorPane ap;
    @FXML
    private AnchorPane ap1;
    @FXML
    private AnchorPane ap2;

    @FXML
    private MenuBar menuBar;
    @FXML
    private SplitPane spane;
    @FXML
    private GridPane gp1;
    @FXML
    private GridPane gp2;

    @FXML
    private ImageView b1;
    @FXML
    private ImageView b2;
    @FXML
    private TextField text1;
    @FXML
    private TextField text2;
    @FXML
    private TextField text3;
    @FXML
    private TextField text3a;
    @FXML
    private TextField text4;
    @FXML
    private TextField text5;
    @FXML
    private TextField texta;
    @FXML
    private TextField texta1;
    @FXML
    private TextField textb;
    @FXML
    private TextField textb1;
    @FXML
    private TextField textc;
    @FXML
    private TextField textc1;
    /*
     * 新建一个窗口
     */
    @FXML
    public void new_window() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController(); //获取Controller的实例对象
        controller.setUsing_Stage(new Stage());
        int size=Main.controllerList.size();
        Main.controllerList.add(controller);
        controller.using_Stage.setTitle("GNSS信号分析仪"+"("+size+")");
        controller.using_Stage.setScene(new Scene(root, 650, 465));
        controller.using_Stage.show();
    }
    @FXML
    public void out_files(){
        final Desktop desktop = Desktop.getDesktop();
        final FileChooser fileChooser = new FileChooser();
        File file=null;
        if(using_Stage!=null){
             file= fileChooser.showOpenDialog(using_Stage);
        }
        if(primaryStage!=null){
             file = fileChooser.showOpenDialog(primaryStage);
        }
        final File file1=file;
        if (file != null) {
            EventQueue.invokeLater(() -> {
                try {
                    desktop.open(file1);
                } catch (IOException ex) {
                    Logger.getLogger(Controller.
                            class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            });
        }
    }
    @FXML
    public void close_current_window(){
        if(using_Stage!=null){
            this.using_Stage.close();
        }
        if(primaryStage!=null){
            this.primaryStage.close();
        }
    }

    @FXML
    public void close_all_window(){
        for(Controller c:Main.controllerList){
            if(c.getUsing_Stage()!=null){
                c.using_Stage.close();
            }
            if(c.getPrimaryStage()!=null){
                c.primaryStage.close();
            }
        }
    }

    @FXML
    public void full_screen(){
        if(this.getUsing_Stage()!=null){
            this.using_Stage.setMaximized(true);
        }
        if(this.getPrimaryStage()!=null){
            this.primaryStage.setMaximized(true);
        }
    }

    @FXML
    public void enlarge_screen(){
        if(this.getUsing_Stage()!=null){
            this.using_Stage.setHeight(this.using_Stage.getHeight()*1.25);
            this.using_Stage.setWidth(this.using_Stage.getWidth()*1.25);
        }
        if(this.getPrimaryStage()!=null){
            this.primaryStage.setHeight(this.primaryStage.getHeight()*1.25);
            this.primaryStage.setWidth(this.primaryStage.getWidth()*1.25);
        }
    }

    @FXML
    public void shrink_screen(){
        if(this.getUsing_Stage()!=null){
            this.using_Stage.setHeight(this.using_Stage.getHeight()*0.85);
            this.using_Stage.setWidth(this.using_Stage.getWidth()*0.85);
        }
        if(this.getPrimaryStage()!=null){
            this.primaryStage.setHeight(this.primaryStage.getHeight()*0.85);
            this.primaryStage.setWidth(this.primaryStage.getWidth()*0.855);
        }
    }

    @FXML
    public void linkweb(){
        Application application = new Application() {
            @Override
            public void start(Stage primaryStage) throws Exception {

            }
        };
        application.getHostServices().showDocument("https://coolguyinhust.github.io/");
    }

    //判断整数（int）
    private boolean isInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    //判断浮点数（double和float）
    private boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    private boolean judge_Text_valid(){
        if(!(isInteger(text1.getText())||isDouble(text1.getText()))){
            System.out.println("请在第一个文本输入框中请输入数字...");
            return false;
        }
        if(!(isInteger(text2.getText())||isDouble(text2.getText()))){
            System.out.println("请在第二个文本输入框中请输入数字...");
            return false;
        }
        return true;
    }

    /*
     * 绘制BOC自相关函数的曲线
     */
    @FXML
    public void boc_self_correlation(){
        if(!judge_Text_valid()){
            return;
        }
        double x = Double.parseDouble(text1.getText());
        double y = Double.parseDouble(text2.getText());
        BOC boc_signal=null;
        //如果限制带宽为空，则默认为无限带宽信号
        if(text5.getText().isEmpty()){
            boc_signal=new BOC(x,y);
        }
        else if(!(isInteger(text5.getText())||isDouble(text5.getText()))){
            System.out.println("请在第六个文本输入框中请输入数字...");
            return ;
        }
        else {
            double br = Double.parseDouble(text5.getText());
            boc_signal=new BOC(x,y,br);
        }
        boc_signal.self_correlation();
    }

    @FXML
    public void bpsk_self_correlation(){
        if(!(isInteger(texta.getText())||isDouble(texta.getText()))){
            System.out.println("请在BPSK第一个文本输入框中请输入数字...");
            return;
        }
        Integer m = Integer.parseInt(texta.getText());
        BPSK bpsk_signal=new BPSK(m);
        bpsk_signal.paint_self_correlation();
    }

    @FXML
    public void Chart_CommonUsage(){
        Chart_common_usage c = new Chart_common_usage();
        TableView<Map_FiveValue_Parameters> table = c.getTable();
        Picture_save picture_save=new Picture_save(table,"Chart_CommonUsage.png");
    }

    @FXML
    public void boc_four_parameters(){
        if(!judge_Text_valid()){
            return;
        }
        double x = Double.parseDouble(text1.getText());
        double y = Double.parseDouble(text2.getText());
        BOC boc_signal=null;
        if(!(isInteger(text5.getText())||isDouble(text5.getText()))){
            System.out.println("请在第六个文本输入框中请输入数字,单位是MHz");
            return ;
        }
        else {
            double br = Double.parseDouble(text5.getText());
            boc_signal=new BOC(x,y,br);
        }
        boc_signal.four_parameters();
    }
    @FXML
    public void bpsk_four_parameters(){
        if(!(isInteger(texta.getText())||isDouble(texta.getText()))){
            System.out.println("请在BPSK第一个文本输入框中请输入数字...");
            return;
        }
        Integer m = Integer.parseInt(texta.getText());
        if(!(isInteger(textc1.getText())||isDouble(textc1.getText()))){
            System.out.println("请在BPSK第六个文本输入框中请输入带限宽度");
            return;
        }
        double br = Double.parseDouble(textc1.getText());
        BPSK bpsk_signal=new BPSK(m);
        bpsk_signal.four_parameters();
    }
    @FXML
    public void boc_random_timegraph(){
        if(!judge_Text_valid()){
            return;
        }
        if(!(isInteger(text3.getText())||isDouble(text3.getText()))){
            System.out.println("请在第三个文本输入框中请输入数字...");
            return;
        }
        if(!(isInteger(text3a.getText())||isDouble(text3a.getText()))){
            System.out.println("请在第三个文本输入框中请输入数字...");
            return;
        }
        double p1=Double.parseDouble(text3a.getText());
        if(p1<0||p1>1){
            System.out.println("请输入[0，1]之间的数字");
            return;
        }
        double x = Double.parseDouble(text1.getText());
        double y = Double.parseDouble(text2.getText());
        int k = Integer.parseInt(text3.getText());
        int array[]=new int[k];
        //生成随机码元
        for(int i=0;i<k;i++){
            if(Math.random()>1-p1){
                array[i]=1;
            }
            else{
                array[i]=-1;
            }
        }
        BOC boc_signal=new BOC(x,y);
        boc_signal.paint_time(array);
    }

    public void bpsk_random_timegraph(){
        if(!(isInteger(texta.getText())||isDouble(texta.getText()))){
            System.out.println("请在BPSK第一个文本输入框中请输入数字...");
            return;
        }
        if(!(isInteger(texta1.getText())||isDouble(texta1.getText()))){
            System.out.println("请在BPSK第二个文本输入框中请输入数字...");
            return;
        }
        if(!(isInteger(textb.getText())||isDouble(textb.getText()))){
            System.out.println("请在BPSK第三个文本输入框中请输入数字...");
            return;
        }
        if(!(isInteger(textb1.getText())||isDouble(textb1.getText()))){
            System.out.println("请在BPSK第四个文本输入框中请输入数字...");
            return;
        }
        int m=Integer.parseInt(texta.getText());
        double n=Double.parseDouble(texta1.getText());
        int k=Integer.parseInt(textb.getText());
        double p1=Double.parseDouble(textb1.getText());
        if(p1<0||p1>1){
            System.out.println("请输入[0，1]之间的数字");
            return;
        }
        int array[]=new int[k];
        //生成随机码元
        for(int i=0;i<k;i++){
            if(Math.random()>1-p1){
                array[i]=1;
            }
            else{
                array[i]=-1;
            }
        }
        BPSK bpsk_signal=new BPSK(m,n);
        bpsk_signal.paint_time(array);
    }

    private int[] str_to_array(String str){
        int str_size=str.length();
        int array[]=new int[str_size];
        char carr[]=new char[str_size];
        carr=str.toCharArray();
        for(int i=0;i<str_size;i++){
            if (carr[i] == '1') {
                array[i]=1;
            }
            else if(carr[i]=='0'){
                array[i]=-1;
            }
            else {
                System.out.println("输入的不是01比特流，请重新输入");
                return null;
            }
        }
        return array;
    }


    /*
     * 绘制用户自定义的时域BOC方波调制波形
     */
    @FXML
    public void boc_user_define(){
        if(!judge_Text_valid()) {
            return;
        }
        if(!(isInteger(text4.getText())||isDouble(text4.getText()))){
            System.out.println("请在第四个文本输入框中请输入01比特串，形如0100100");
            return;
        }
        double x = Double.parseDouble(text1.getText());
        double y = Double.parseDouble(text2.getText());
        String str=text4.getText();
        int array[]=str_to_array(str);
        if(array==null){
            return;
        }
        BOC boc_signal=new BOC(x,y);
        boc_signal.paint_time(array);
    }
    /*
     * 绘制用户自定义的时域BPSK调制波形
     */
    public void bpsk_user_define(){
        int m=Integer.parseInt(texta.getText());
        double n=Double.parseDouble(texta1.getText());
        String str=textc.getText();
        int array[]=str_to_array(str);
        if(array==null){
            return;
        }
        BPSK bpsk_signal=new BPSK(m,n);
        bpsk_signal.paint_time(array);
    }

    /*
     * 计算BOC的功率谱密度；用户从主界面上输入值，在主交互界面外新开一个界面显示功率谱。
     */
    @FXML
    public void boc_power_spectrum(){
        if(!judge_Text_valid()) {
            return;
        }
        double x = Double.parseDouble(text1.getText());
        double y = Double.parseDouble(text2.getText());
        BOC boc_signal=new BOC(x,y);
        boc_signal.paint_frequency();
    }
    /*
     * 绘制用户自定义的BPSK调制波形
     */
    @FXML
    public void bpsk_power_spectrum(){
        if(!(isInteger(texta.getText())||isDouble(texta.getText()))){
            System.out.println("请在BPSK第一个文本输入框中请输入数字...");
            return;
        }
        int m = Integer.parseInt(texta.getText());
        BPSK bpsk_signal=new BPSK(m);
        bpsk_signal.paint_frequency();
    }

    @FXML
    public void boc_CodeTraceCN(){

    }

    @FXML
    public void boc_CodeTraceBand(){

    }

    @FXML
    public void boc_CodeTraceT(){

    }
    @FXML
    public void boc_CodeTraceInterval(){
        BOC boc_signal=new BOC(10,5);
        boc_signal.errorInterval();
    }

    @FXML
    public void bpsk_CodeTraceInterval(){
        BPSK bpsk = new BPSK(10);
        bpsk.errorInterval();
    }
    @FXML
    public void bpsk_CodeTraceCN(){

    }

    @FXML
    public void bpsk_CodeTraceBand(){

    }

    @FXML
    public void bpsk_CodeTraceT(){

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuBar.prefWidthProperty().bind(ap.widthProperty());//宽度绑定为Pane宽度
        menuBar.prefHeightProperty().bind(ap.heightProperty().multiply(0.1));
        spane.prefWidthProperty().bind(ap.widthProperty());
        spane.prefHeightProperty().bind(ap.heightProperty().multiply(0.9));
        b1.fitWidthProperty().bind(spane.widthProperty().multiply(0.25));
        b1.fitHeightProperty().bind(spane.heightProperty().multiply(0.75));
        b2.fitWidthProperty().bind(spane.widthProperty().multiply(0.25));
        b2.fitHeightProperty().bind(spane.heightProperty().multiply(0.75));
    }
}
