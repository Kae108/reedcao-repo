import constant.Constant;
import core.DownLoadInfoListner;
import core.DownLoader;
import core.Listener;

import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

//下载链接任务类
public class MainStartTask implements Callable<String> {
    private Integer taskId;
    private Constant constant;
    private String url;

    //下载信息监听器
    private Listener downLoadInfoListner;

    private String MD5Code;


    public Constant getConstant() {
        return constant;
    }

    public void setConstant(Constant constant) {
        this.constant = constant;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public MainStartTask(Integer taskId, Constant constant, String url, Listener downLoadInfoListner, String MD5Code) {
        this.taskId = taskId;
        this.constant = constant;
        this.url = url;
        this.downLoadInfoListner = downLoadInfoListner;
        this.MD5Code = MD5Code;
    }

    @Override
    public String call() throws Exception {

        DownLoader downLoader = new DownLoader(constant,this.downLoadInfoListner,MD5Code);
        downLoader.downLoad(url);
        return "任务"+taskId+"下载完成";
    }
}
