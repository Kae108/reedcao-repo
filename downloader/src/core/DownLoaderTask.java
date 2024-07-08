package core;

import constant.Constant;
import util.HttpUtils;
import util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class DownLoaderTask implements Callable<Boolean> {

    private Constant constant;

    private String url;

    private long startPos;

    private long endPos;

    private int part;

    private long fileStarted;

    private CountDownLatch countDownLatch;

    private Listener downLoadInfoListner;


    public DownLoaderTask(String url, long startPos, long endPos, int part, long fileStarted, CountDownLatch countDownLatch, Constant constant, Listener downLoadInfoListner) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.fileStarted = fileStarted;
        this.countDownLatch = countDownLatch;
        this.constant = constant;
        this.downLoadInfoListner = downLoadInfoListner;
    }

    @Override
    public Boolean call() throws Exception {
        //文件名
        String fileName = HttpUtils.getHttpFileName(url);

        //分块文件名
        String httpFileName = fileName + ".temp" + part;

        //下载路径
        httpFileName = constant.PATH + httpFileName;

        //获取分块下载的连接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, startPos, endPos);


        try (
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                RandomAccessFile raf = new RandomAccessFile(httpFileName, "rw");
        ) {
            byte[] buffer = new byte[constant.BYTE_SIZE];
            int len = -1;

            //设置起始位置
            raf.seek(fileStarted);


            while ((len = bis.read(buffer)) != -1) {
                //已下载大小累加和
                synchronized (url){
//                    downLoadInfoListner.setDownSize(len);
                    downLoadInfoListner.downLoadInfo(len);
                }
//                a+=len

                raf.write(buffer, 0, len);
                Thread.sleep(10);

            }

        } catch (FileNotFoundException e) {
            LogUtils.error("下载文件不存在{}", url);
            return false;
        } catch (Exception e) {
            LogUtils.error("下载出现异常");
            return false;
        } finally {
            httpURLConnection.disconnect();
            countDownLatch.countDown();
        }


        return true;
    }
}
