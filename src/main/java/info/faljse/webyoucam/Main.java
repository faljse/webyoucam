package info.faljse.webyoucam;

import info.faljse.webyoucam.streaming.MyNanoHTTPD;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Martin on 22.07.2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        AnsiConsole.systemInstall();
        MyNanoHTTPD ws = new MyNanoHTTPD(9090, true);
        try {
            ws.start();
            System.out.println("Server started, hit Enter to stop.\n");
            System.in.read();
        } catch (IOException ignored) {
        }

        System.out.println("Server stopped.\n");

    }
}
