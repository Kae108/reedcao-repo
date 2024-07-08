package core;

import constant.Constant;

import java.io.IOException;

public interface Listener {
    void init(String url,Constant constant) throws IOException;
    void downLoadInfo(int progress);
    long getFinishedSize();
    void setFinishedSize(long size);
    void setStartTime(long startSecond);
    long getDownSize();
//    void setDownSize(int len);
}
