package info.faljse.webyoucam.streaming;

import io.undertow.websockets.core.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Martin on 10.08.2016.
 */
public class WSSessions {
    private ArrayList<ClientSession> sessions=new ArrayList<>();
    private final static Logger logger = LoggerFactory.getLogger(WSSessions.class);
    private int sessionsSEQ=1;

    public WSSessions() {

    }

    public ClientSession createAddSession(WebSocketChannel session){
        synchronized (sessions) {
            ClientSession s = new ClientSession(sessionsSEQ++, session);
            sessions.add(s);
            logger.info("Client added: {}", session.getPeerAddress());
            return s;
        }
    }

    public void send(byte[] buffer, int offset, int buffersize){
        synchronized (sessions) {
            sessions.removeIf(cs -> !cs.isAlive());
            ArrayList<ClientSession> scopy = new ArrayList<>(sessions);
            for (ClientSession s : scopy) {
                s.send(buffer, offset, buffersize);
            }
        }
    }

    public int getCount() {
        synchronized (sessions) {
            return sessions.size();
        }
    }

}
