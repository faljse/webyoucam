package info.faljse.webyoucam;

import info.faljse.webyoucam.streaming.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin on 22.07.2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        WebServer ws = new WebServer();
        ws.start();

    }
}
