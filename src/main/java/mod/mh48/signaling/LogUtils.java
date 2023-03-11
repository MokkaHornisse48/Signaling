package mod.mh48.signaling;

public class LogUtils {
    public static void debug(String info){
        System.out.println("debug:"+info);
    }

    public static void info(String info){
        System.out.println("info:"+info);
    }

    public static void error(String info){
        System.out.println("error:"+info);
    }

    public static void fatal(String info){
        System.out.println("fatal:"+info);
    }
}
