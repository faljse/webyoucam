package info.faljse.webyoucam.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Martin on 10.08.2016.
 */
public class WSSessions {
    private List<ClientSession> sessions=new ArrayList<ClientSession>();
    private final static Logger logger = LoggerFactory.getLogger(WSSessions.class);
    private int sessionsSEQ=1;
    private byte[] buffer;

    public WSSessions(){
    }

    public void setBuffer(byte[] buffer){
        this.buffer=buffer;
    }

    public void addSession(Session session){
       synchronized (sessions) {
           ClientSession s = new ClientSession(session, buffer, sessionsSEQ++);
           sessions.add(s);
           logger.info("Client added: {}; count: {}",s.getSession().getId(),sessions.size());
       }
    }

    public synchronized void send(){
        synchronized (sessions){
            Iterator<ClientSession> i = sessions.iterator();
            while (i.hasNext()) {
                ClientSession s = i.next(); // must be called before you can call i.remove()
                s.send();
                info.faljse.webyoucam.Stats.sentBytes.add(buffer.length);
                if(!s.isAlive()){
                    i.remove();
                    logger.info("Client removed: {}; count: {}", s.getSession().getId(), sessions.size());
                }
            }}
    }

}
