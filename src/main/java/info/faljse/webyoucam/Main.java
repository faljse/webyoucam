package info.faljse.webyoucam;

import info.faljse.webyoucam.streaming.FFMpegThread;
import info.faljse.webyoucam.streaming.MyHTTPD;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by Martin on 22.07.2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        MyHTTPD server = new MyHTTPD(Settings.port, true);
        try {
            server.start();
            System.out.println("Server started, hit Enter to stop.\n");
            System.in.read();
        } catch (ServletException|IOException e) {
            logger.warn("asd",e);
        }

        System.out.println("Server stopped.\n");
    }
}
