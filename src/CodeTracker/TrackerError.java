package CodeTracker;

/**
 * Created by kaixin on 2018/12/22.
 */
/*
 * 接收机利用非相干超前一滞处理法时，
 */
public class TrackerError {
    //单边噪声等效矩形带宽为BL(Hz)
    private double BL;
    // C／N指信噪比？
    private double CN;
    //电文数据速率的倒数
    private double T;
    //超前滞后本地码之间的间隔
    private double interval;
}
