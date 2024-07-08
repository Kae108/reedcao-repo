package core;

import constant.Constant;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class DownLoadInfoListner {

    //下载开始时间
    public long startTime;

    //下载文件名
    private String fileName;

    private Constant constant;

    //下载文件总大小
    private long httpFileContentLength;

    //本地已下载大小
    public long finishedSize;

    //累计下载了多少大小
    public volatile long downSize;

    //前一次下载的大小
    public String lastPercentStr;

    //上一次进度百分比
    volatile Double preDownPercent = 0.0;

    public int speed;

    public DownLoadInfoListner(long httpFileContentLength, Constant constant,String fileName){
        this.httpFileContentLength = httpFileContentLength;
        this.constant = constant;
        this.fileName = fileName;
    }

    public void downLoadInfo(){



        Instant now = Instant.now();
        long curTime = now.getEpochSecond();

        //下载累计用时
        long timeSecond = curTime - startTime;


        //计算下载文件总大小，单位mb
        String httpFileSize = String.format("%.2f", httpFileContentLength / constant.MB);

        //计算每秒下载速度，单位kb
        speed = (int) (Double.valueOf(downSize) / (timeSecond*1024d));
//        prevSize = Double.valueOf(downSize);

        //剩余文件的大小
        double remainSize = httpFileContentLength - Double.valueOf(finishedSize) - Double.valueOf(downSize);

        //剩余时间
        String remainTime = String.format("%.1f", remainSize / 1024d / speed);

        if (remainTime.equalsIgnoreCase("Infinity")) {
            remainTime = "-";
        }

        //已经下载了多少大小
        String currentFileSize = String.format("%.2f", (Double.valueOf(downSize) + Double.valueOf(finishedSize)) / constant.MB);
        Double currentDownPercent = Math.floor((Double.parseDouble(currentFileSize) / Double.parseDouble(httpFileSize)) * 100);
//        String percentStr = String.format("%.2f", currentDownPercent);
        String curPercentStr = String.valueOf(currentDownPercent);
//        double interval = currentDownPercent-preDownPercent;
//        System.out.println("currentDownPercent:"+currentDownPercent+"  preDownPercent:"+preDownPercent+"  interval:"+interval);

        if(!curPercentStr.equals(lastPercentStr)){
            String downInfo = String.format("已下载 %s%%,速度 %skb/s,剩余时间 %ss,剩余大小" + String.format("%.2f",remainSize / constant.MB),curPercentStr, speed, remainTime);
            System.out.println(fileName+" "+ downInfo);
        }

        lastPercentStr = curPercentStr;


    }

}
