package mod.mh48.signaling;

public class LogUtils {

    public static LogUtils logger = new LogUtils();

    public static void debug(String info){
        logger.pdebug(info);
    }

    public static void info(String info){
        logger.pinfo(info);
    }

    public static void error(String info){
        logger.perror(info);
    }

    public static void fatal(String info){
        logger.pfatal(info);
    }

    public void pdebug(String info){
        System.out.println("debug:"+info);
    }

    public void pinfo(String info){
        System.out.println("info:"+info);
    }

    public void perror(String info){
        System.out.println("error:"+info);
    }

    public void pfatal(String info){
        System.out.println("fatal:"+info);
    }
}
