package info.faljse.webyoucam.streaming;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by Martin on 12.08.2016.
 */
public class ClientSession {
    private final static Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private final Session session;
    private ByteBuffer byteBuffer;
    private int clientID;
    private volatile boolean alive=true;



    public ClientSession(Session session, byte[] buffer, int clientID) {
        this.session=session;
        byteBuffer=ByteBuffer.wrap(buffer);
        this.clientID=clientID;
    }

    public Session getSession() {
        return session;
    }

    public void send() {
        if(!alive)
            return;
        ((Buffer)byteBuffer).rewind();
        try {
            WebServer.sendByteCount.addAndGet(byteBuffer.remaining());
            session.getRemote().sendBytesByFuture(byteBuffer);
        }catch(Exception e){
            alive=false;
        }
    }

    public boolean isAlive() {
        return alive;
    }
}
