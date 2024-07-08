package core;

import constant.Constant;
import util.FileMD5Checksum;
import util.FileUtils;
import util.HttpUtils;
import util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.*;

public class DownLoader {

    private Constant constant;
    //任务线程池对象
    ThreadPoolExecutor poolExecutor;

    //计时器
    private CountDownLatch countDownLatch;

    //打印日志线程池(被观察者)
    public Listener downLoadInfoListner;

    //MD5编码
    private String typicalMD5String;

    public DownLoader(Constant constant, Listener downLoadInfoListner, String MD5Code) {
        this.constant = constant;
        poolExecutor = new ThreadPoolExecutor(this.constant.THREAD_NUM, constant.THREAD_NUM, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(constant.THREAD_NUM));
        countDownLatch = new CountDownLatch(constant.THREAD_NUM);
        this.downLoadInfoListner = downLoadInfoListner;
        this.typicalMD5String = MD5Code;
//        System.out.println("日志线程池为"+scheduledExecutorService);
    }

    public void downLoad(String url) throws InterruptedException {
        //获取文件名
        String fileName = HttpUtils.getHttpFileName(url);

        //文件下载路径
        String httpFileName = constant.PATH + fileName;

        //获取本地文件的大小
        long localFileLength = FileUtils.getFileContentLength(httpFileName);

        //获取连接对象
        HttpURLConnection httpURLConnection = null;

        try {
            httpURLConnection = HttpUtils.getHttpURLConnection(url);

            //判断是否支持断点续传
            String acceptRanges = httpURLConnection.getHeaderField("Accept-Ranges");
            if (acceptRanges != null && acceptRanges.equals("bytes")) {
                // 支持断点续传
                LogUtils.info("{}链接支持断点续传", httpFileName);
            } else {
                // 不支持断点续传
                LogUtils.info("{}链接不支持断点续传", httpFileName);
            }

            //获取下载文件的总大小
            int contentLength = httpURLConnection.getContentLength();

            System.out.println("aaaaa"+contentLength);

            //判断文件是否已经下载过
            if (localFileLength >= contentLength) {
                LogUtils.info("{}已下载完毕，无需重新下载", httpFileName);
                return;
            }

//            //创建获取下载信息的任务对象
//            downLoadInfoThread = new DownLoadInfoThread(contentLength,constant,fileName);
//            //将任务交给线程执行，每隔一秒执行一次
//            scheduledExecutorService.scheduleAtFixedRate(downLoadInfoThread,1,1, TimeUnit.SECONDS);

            //切分任务，并执行下载
            ArrayList<Future> list = new ArrayList<>();
            split(url, list, downLoadInfoListner);

            countDownLatch.await();

//            String downInfo = String.format("已下载 100.00%%,速度 %skb/s,剩余时间 0.0s,剩余大小0.00", downLoadInfoListner.speed);
//            System.out.println(fileName + " " + downInfo);

            //合并并清理临时文件
            if (merge(httpFileName)) {
                clearTemp(httpFileName);
            }
            System.out.println("downLoadInfoListner.downSize："+downLoadInfoListner.getDownSize());

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.print("\r");
            System.out.print("下载完成");
            System.out.println(" ");
            //关闭连接对象
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

//            //关闭日志线程池
//            scheduledExecutorService.shutdownNow();

            //关闭任务线程池
            if (poolExecutor != null) {
                poolExecutor.shutdown();
                if (!poolExecutor.awaitTermination(20, TimeUnit.SECONDS)) {
                    poolExecutor.shutdownNow();
                }
            }
        }
    }

    //文件切分
    public void split(String url, ArrayList<Future> futureList, Listener downLoadInfoListner) {
        try {
            //文件名
            String fileName = HttpUtils.getHttpFileName(url);
            //获取下载文件大小
            long httpFileContentLength = HttpUtils.getHttpFileContentLength(url);
            //计算切分后的文件大小
            long sigleSize = httpFileContentLength / constant.THREAD_NUM;
            //获取本次下载前文件已下载的大小
            long finishedSize = getFinishedSize(constant.PATH + fileName);

            //计算分块个数
            for (int i = 0; i < constant.THREAD_NUM; i++) {
                File file = new File(constant.PATH + fileName + ".temp" + i);

                //计算下载起始位置
                long startPos;
                if (finishedSize <= 0) {
                    startPos = i * sigleSize;
                } else {
                    startPos = i * sigleSize + file.length();
                }

                //计算下载结束位置
                long endPos;
                if (i == constant.THREAD_NUM - 1) {
                    //最后一块，下载剩下所有内容
                    endPos = httpFileContentLength;
                } else {
                    if (finishedSize <= 0) {
                        endPos = startPos + sigleSize;
                    } else {
                        endPos = startPos + sigleSize - file.length();
                    }
                }

                //如果不是第一块，起始位置要加一，满足类似0~2，3~4，5~6...
                if (i != 0) {
                    startPos++;
                }

                Instant now = Instant.now();
                long startSecond = now.getEpochSecond();
                downLoadInfoListner.setStartTime(startSecond);

                //创建任务对象
                DownLoaderTask downLoaderTask = new DownLoaderTask(url, startPos, endPos, i, file.length(), countDownLatch, constant, downLoadInfoListner);

                //提交任务到线程池
                Future<Boolean> future = poolExecutor.submit(downLoaderTask);
                futureList.add(future);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //合并临时文件
    public boolean merge(String fileName) {
        LogUtils.info("正在合并资源包{}", fileName);
        byte[] buffer = new byte[constant.BYTE_SIZE];
        int len = -1;
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            for (int i = 0; i < constant.THREAD_NUM; i++) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + ".temp" + i))) {
                    while ((len = bis.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                    }
                }
            }
            LogUtils.info("文件合并完毕{}", fileName);
            String md5String = FileMD5Checksum.calculateMD5(fileName).toUpperCase();
            boolean checkMD5Result = FileMD5Checksum.checkMD5(typicalMD5String, md5String);
            if (checkMD5Result) {
                LogUtils.info("文件校验完成{}", fileName);
            } else {
                LogUtils.error("文件校验失败{}", fileName);
            }

        } catch (IOException e) {
            LogUtils.error("文件合并失败{}", fileName);
            return false;
        } catch (Exception e) {
            LogUtils.error("下载失败或校验异常{}", fileName);
        }
        return true;
    }

    //获取已完成大小(断点续传)
    public long getFinishedSize(String fileName) {
        for (int i = 0; i < constant.THREAD_NUM; i++) {
            File file = new File(fileName + ".temp" + i);
            downLoadInfoListner.setFinishedSize(file.length());
        }
        return downLoadInfoListner.getFinishedSize();
    }


    //清理临时文件
    public boolean clearTemp(String fileName) {
        LogUtils.info("正在清理临时文件");
        for (int i = 0; i < constant.THREAD_NUM; i++) {
            File file = new File(fileName + ".temp" + i);
            file.delete();
        }
        return true;
    }
}
