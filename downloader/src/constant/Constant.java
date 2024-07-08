package constant;

public class Constant {
    public String PATH ;

    public  double MB = 1024d * 1024d;

    public  int BYTE_SIZE = 1024 * 100;

    //线程数量
    public   int THREAD_NUM ;

    public String getPATH() {
        return PATH;
    }

    public void setPATH(String PATH) {
        this.PATH = PATH;
    }

    public int getTHREAD_NUM() {
        return THREAD_NUM;
    }

    public void setTHREAD_NUM(int THREAD_NUM) {
        this.THREAD_NUM = THREAD_NUM;
    }


    public Constant(String path,int threadNum){
        this.PATH = path;
        this.THREAD_NUM = threadNum;
    }

    @Override
    public String toString() {
        return "Constant{" +
                "PATH='" + PATH + '\'' +
                ", THREAD_NUM=" + THREAD_NUM +
                '}';
    }
}
