package util;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//http工具类
public class HttpUtils {

    //获取下载文件的大小
    public static long getHttpFileContentLength(String url) throws IOException {
        int contentLength;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = getHttpURLConnection(url);
            contentLength = httpURLConnection.getContentLength();
        } finally {
            if(httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return contentLength;
    }



    //获取分块下载连接
    public static HttpURLConnection getHttpURLConnection(String url,long startPos,long endPos) throws IOException {
        HttpURLConnection httpURLConnection = getHttpURLConnection(url);
        LogUtils.info("下载的区间是{}-{}",startPos,endPos);
        if(endPos!=0){
            httpURLConnection.setRequestProperty("RANGE","bytes="+startPos+"-"+endPos);
        }else{
            httpURLConnection.setRequestProperty("RANGE","bytes="+startPos+"-");
        }
        return httpURLConnection;
    }


    //获取链接
    public static HttpURLConnection getHttpURLConnection(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        //向文件所在服务器发送标识信息
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64;" +
                " x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36");
        return httpURLConnection;
    }

    //获取要下载的文件名
    public static String getHttpFileName(String url){
        int i = url.lastIndexOf("/");
        return url.substring(i+1);
    }
}
