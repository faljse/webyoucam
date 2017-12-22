package info.faljse.webyoucam.streaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String id;
    private int count=0;

    public WSSessions(String id) {
        this.id=id;
    }

    public void setBuffer(byte[] buffer){
        this.buffer=buffer;
    }

    public void addSession(org.eclipse.jetty.websocket.api.Session session){
        synchronized (sessions) {
            ClientSession s = new ClientSession(session, buffer, sessionsSEQ++);
            sessions.add(s);
            logger.info("Client added: {}", s.getSession().getRemoteAddress().toString());
        }
    }

    public synchronized void send(){
        synchronized (sessions){
            count=sessions.size();
            Iterator<ClientSession> i = sessions.iterator();
            while (i.hasNext()) {
                ClientSession s = i.next(); // must be called before you can call i.remove()
                s.send();
                if(!s.isAlive()){
                    i.remove();
                    logger.info("Client removed: {}", s.getSession().getRemoteAddress().toString());
                }
            }}
    }

    public int getCount() {
        return count;
    }
}
