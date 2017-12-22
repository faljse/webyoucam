package info.faljse.webyoucam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Martin on 25.08.2016.
 */
public class Settings {
    private final static Logger logger = LoggerFactory.getLogger(Settings.class);
    public static int port;
    public static String ffmpegCmd[]=new String[10];

    static {
        read();
    }

    private Settings(){

    }
    private static void read() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
            port=Integer.parseInt(prop.getProperty("port"));

            List<String> cmds=new ArrayList<String>();
            for(int i=1;i<10;i++){
                String val = prop.getProperty("ffmpegCmd" + i);
                if(val!=null)
                    cmds.add(val);
            }
            ffmpegCmd= cmds.toArray(new String[0]);
        } catch (IOException ex) {
            logger.error("Cant load config", ex);
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
