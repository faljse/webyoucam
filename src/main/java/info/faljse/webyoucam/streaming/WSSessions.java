package info.faljse.webyoucam.streaming;

import io.undertow.server.session.Session;
import io.undertow.websockets.core.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Created by Martin on 10.08.2016.
 */
public class WSSessions {
    private ArrayList<ClientSession> sessions=new ArrayList<>();
    private final static Logger logger = LoggerFactory.getLogger(WSSessions.class);
    private int sessionsSEQ=1;
    private byte[] buffer;

    public WSSessions(byte[] buffer) {
        this.buffer=buffer;
    }

    public ClientSession createAddSession(WebSocketChannel session){
        synchronized (sessions) {
            ClientSession s = new ClientSession(buffer, sessionsSEQ++, session);
            sessions.add(s);

            logger.info("Client added: {}", session.getPeerAddress());
            return s;
        }
    }

    public void send(){
        sessions.removeIf(cs -> !cs.isAlive());
        synchronized (sessions) {
            ArrayList<ClientSession> scopy = new ArrayList<>(sessions);
            for (ClientSession s : scopy) {
                s.send(buffer);
            }
        }
    }

    public int getCount() {
        synchronized (sessions) {
            return sessions.size();
        }
    }

}
