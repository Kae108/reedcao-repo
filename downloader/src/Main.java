import constant.Constant;
import core.DownLoadInfoListner;
import core.Listener;
import pojo.ExcutorEntity;
import util.HttpUtils;
import util.LogUtils;


import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        String[] urls = {"https://dldir1v6.qq.com/weixin/Windows/WeChatSetup.exe", "https://guanjia.lenovo.com.cn/download/lenovopcmanager_apps.exe", "https://dldir1.qq.com/qqfile/qq/QQNT/Windows/QQ_9.9.11_240617_x64_01.exe"};
        Constant[] constants = {new Constant("C:\\testdownloaddemo\\", 3), new Constant("C:\\testdownloaddemo\\", 4), new Constant("C:\\testdownloaddemo\\", 5)};
        String[] MD5Codes = {"7377170AC9BFD90D056012AF8613C310", "D9A0A3FE0045F3E1A3B06AD34B7B2CC9", "C3B55E0756DD4FECF06C64DE75297EE5"};
        ExcutorEntity entity = new ExcutorEntity(3, urls, constants, MD5Codes);

        Main.excute(entity);
    }

    public static void excute(ExcutorEntity entity) throws InterruptedException, IOException {

        //解析下载参数
        String[] urls = entity.getUrls();
        Constant[] constants = entity.getConstants();
        String[] MD5Codes = entity.getMD5Codes();
        int downLoadTaskNum = entity.getDownLoadTaskNum();

        //创建下载链接任务数组
        ArrayList<MainStartTask> taskList = new ArrayList<>();

        //创建下载返回值数组
        ArrayList<Future> futureArrayList = new ArrayList<>();

        if (urls.length != downLoadTaskNum || constants.length != downLoadTaskNum) {
            LogUtils.error("传参不匹配");
        } else {
            if (downLoadTaskNum > 2) {
                LogUtils.error("最多只能同时下载两个任务");
            }

            //遍历下载链接
            for (int i = 0; i < downLoadTaskNum; i++) {
                String url = urls[i];
                Constant constant = constants[i];
                Listener downLoadInfoListner = new Listener() {
                    private String url;
                    //下载开始时间
                    public long startTime;

                    //下载文件名
                    private String fileName;

                    private Constant constant;

                    //下载文件总大小
                    private long httpFileContentLength;

                    //本地已下载大小
                    public long  finishedSize = 0;

                    //累计下载了多少大小
                    public volatile long downSize;

                    //前一次写入下载的大小百分比
                    public String lastPercentStr;

//                    //上一次进度百分比
//                    volatile Double preDownPercent = 0.0;

                    //速度
                    public int speed;

                    //初始化方法
                    @Override
                    public void init(String url,Constant constant) throws IOException {
                        this.url = url;
                        this.constant = constant;
                        this.fileName = HttpUtils.getHttpFileName(url);
                        this.httpFileContentLength = HttpUtils.getHttpFileContentLength(url);
                    }

                    //获取上次下载已完成的大小(断点续传)
                    @Override
                    public long getFinishedSize(){
                        return this.finishedSize;
                    }

                    //设置上次已下载大小(断点续传)
                    @Override
                    public void setFinishedSize(long size){
                        this.finishedSize += size;
                    }

                    //链接任务开始时间
                    @Override
                    public void setStartTime(long startSecond){
                        this.startTime = startSecond;
                    }

                    @Override
                    public long getDownSize() {
                        return downSize;
                    }

                    //下载进度信息
                    @Override
                    public void downLoadInfo(int progress) {
                        //记录累计下载量
                        this.downSize+=progress;
                        System.currentTimeMillis();
                        Instant now = Instant.now();
                        long curTime = now.getEpochSecond();

                        //下载累计用时
                        long timeSecond = curTime - startTime;

                        //计算下载文件总大小，单位mb
                        String httpFileSize = String.format("%.2f", httpFileContentLength / constant.MB);

                        //计算每秒下载速度，单位kb
//                        System.out.println("downSize:"+downSize+"---"+"timeSecond:"+timeSecond);
                        if(timeSecond==0){
                            speed = 0;
                        }else{
                            speed = (int) (Double.valueOf(downSize) / (timeSecond*1024d));
                        }

                        //剩余文件的大小
                        double remainSize = httpFileContentLength - Double.valueOf(finishedSize) - Double.valueOf(downSize);

                        //剩余时间
                        String remainTime = String.format("%.1f", remainSize / 1024d / speed);

                        if (remainTime.equalsIgnoreCase("Infinity")) {
                            remainTime = "-";
                        }

                        //已经下载了多少大小
                        String currentFileSize = String.format("%.2f", (Double.valueOf(downSize) + Double.valueOf(finishedSize)) / constant.MB);
                        //向下取整
                        Double currentDownPercent = Math.floor((Double.parseDouble(currentFileSize) / Double.parseDouble(httpFileSize)) * 100);
                        //当前进度百分比
                        String curPercentStr = String.valueOf(currentDownPercent);
                        //每1%打印一次
                        if(!curPercentStr.equals(lastPercentStr)){
                            String downInfo = String.format("已下载 %s%%,速度 %skb/s,剩余时间 %ss,剩余大小" + String.format("%.2f",remainSize / constant.MB),curPercentStr, speed, remainTime);
                            System.out.println(fileName+" "+ downInfo);
                        }
                        lastPercentStr = curPercentStr;
                    }
                };

                downLoadInfoListner.init(url,constant);
                MainStartTask mainStartTask = new MainStartTask(i + 1, constants[i], url, downLoadInfoListner, MD5Codes[i]);
                taskList.add(mainStartTask);
            }

            //创建存储多条下载链接线程的线程池
            ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));
            for (int i = 0; i < taskList.size(); i++) {
                Future<String> future = poolExecutor.submit(taskList.get(i));
                futureArrayList.add(future);
            }

            //确保每个链接都下载完毕
            futureArrayList.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            //关闭线程池
            poolExecutor.shutdown();
            if (!poolExecutor.awaitTermination(20, TimeUnit.SECONDS)) {
                poolExecutor.shutdownNow();
            }
        }
    }
}


