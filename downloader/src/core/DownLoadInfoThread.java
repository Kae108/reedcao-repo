package core;

import constant.Constant;
import util.LogUtils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class DownLoadInfoThread implements Runnable {

    //下载文件名
    private String fileName;

    private Constant constant;

    //下载文件总大小
    private long httpFileContentLength;

    //本地已下载大小
    public  AtomicLong finishedSize = new AtomicLong();

    //累计下载了多少大小
    public volatile AtomicLong downSize = new AtomicLong();

    //前一次下载的大小
    public double prevSize;

    public DownLoadInfoThread(long httpFileContentLength, Constant constant,String fileName) {
        this.httpFileContentLength = httpFileContentLength;
        this.constant = constant;
        this.fileName = fileName;
    }

    @Override
    public void run() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        //计算下载文件总大小，单位mb
        String httpFileSize = String.format("%.2f", httpFileContentLength / constant.MB);

        //计算每秒下载速度，单位kb
        int speed = (int) ((downSize.doubleValue() - prevSize) / 1024d);
        prevSize = downSize.doubleValue();

        //剩余文件的大小
        double remainSize = httpFileContentLength - finishedSize.doubleValue() - downSize.doubleValue();

        //剩余时间
        String remainTime = String.format("%.1f", remainSize / 1024d / speed);

        if (remainTime.equalsIgnoreCase("Infinity")) {
            remainTime = "-";
        }


        //已经下载了多少大小
        String currentFileSize = String.format("%.2f", (downSize.doubleValue() + finishedSize.doubleValue()) / constant.MB);
        Double currentDownPercent = (Double.parseDouble(currentFileSize) / Double.parseDouble(httpFileSize)) * 100;
        String percentStr = String.format("%.2f", currentDownPercent);
        String downInfo = String.format("已下载 %s%%,速度 %skb/s,剩余时间 %ss,剩余大小" + remainSize / constant.MB,
                percentStr, speed, remainTime);
        System.out.println(fileName+""+ downInfo);

    }
}
