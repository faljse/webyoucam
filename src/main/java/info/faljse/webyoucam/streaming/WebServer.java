package info.faljse.webyoucam.streaming;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class WebServer extends TimerTask {
    private final static Logger logger = LoggerFactory.getLogger(WebServer.class);

    public static Map<String, WSSessions> list = new HashMap<>();
	public static AtomicLong sendByteCount=new AtomicLong();
	public static AtomicLong recvByteCount=new AtomicLong();

    private java.util.Timer t=new Timer();


	public void start() {
        new MyNanoHTTPD(9090, true);


    }

	private long lastRecvBytes=0;
    private long lastSendBytes=0;

    @Override
    public void run() {
        long currentSendBytes=sendByteCount.get();
        long currentRecvBytes=recvByteCount.get();

        float recvRate=(currentRecvBytes-lastRecvBytes)/1000000.0f*8;
        float sendRate=(currentSendBytes-lastSendBytes)/1000000.0f*8;
        int clients=0;
        for(WSSessions ws:list.values()){
            clients+=ws.getCount();
        }
        logger.info(String.format("%d clients; recv/send MBit %.2f/%.2f", clients, recvRate, sendRate) );

        lastSendBytes=currentSendBytes;
        lastRecvBytes=currentRecvBytes;
    }
}
