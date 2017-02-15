package info.faljse.webyoucam.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.nio.ByteBuffer;

/**
 * Created by Martin on 12.08.2016.
 */
public class ClientSession implements SendHandler {
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
        byteBuffer.rewind();
        try {
            session.getAsyncRemote().sendBinary(byteBuffer, this);
        }catch(Exception e){
            alive=false;
        }
    }

    @Override
    public void onResult(SendResult result) {
        if(!result.isOK()){
            alive=false;
            logger.debug("Client {} send failed", clientID, result.getException());
        }

    }

    public boolean isAlive() {
        return alive;
    }
}
