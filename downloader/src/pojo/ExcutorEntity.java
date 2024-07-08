package pojo;

import constant.Constant;

public class ExcutorEntity {

    private int downLoadTaskNum;

    private String[] urls;

    private Constant[] constants;

    private String[] MD5Codes;


    public ExcutorEntity(int downLoadTaskNum, String[] urls, Constant[] constants, String[] MD5Codes) {
        this.downLoadTaskNum = downLoadTaskNum;
        this.urls = urls;
        this.constants = constants;
        this.MD5Codes = MD5Codes;
    }

    public int getDownLoadTaskNum() {
        return downLoadTaskNum;
    }

    public void setDownLoadTaskNum(int downLoadTaskNum) {
        this.downLoadTaskNum = downLoadTaskNum;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public Constant[] getConstants() {
        return constants;
    }

    public void setConstants(Constant[] constants) {
        this.constants = constants;
    }

    public String[] getMD5Codes() {
        return MD5Codes;
    }

    public void setMD5Codes(String[] MD5Codes) {
        this.MD5Codes = MD5Codes;
    }
}
