package info.faljse.webyoucam.streaming;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
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
    private int count=0;

    public WSSessions(byte[] buffer) {
        this.buffer=buffer;
    }


    public ClientSession createAddSession(IHTTPSession request, Response response){
        synchronized (sessions) {
            ClientSession s = new ClientSession(buffer, sessionsSEQ++, request, response);
            sessions.add(s);
            logger.info("Client added: {}", "s.getRemoteAddress().toString()");

            return s;
        }
    }

    public synchronized void send(){
        synchronized (sessions){
            count=sessions.size();
            Iterator<ClientSession> i = sessions.iterator();
            while (i.hasNext()) {
                ClientSession s = i.next(); // must be called before you can call i.remove()
                s.send(buffer);
                if(!s.isAlive()){
                    i.remove();
                    logger.info("Client removed: {}", s.toString());
                }
            }}
    }

    public int getCount() {
        return count;
    }

}
