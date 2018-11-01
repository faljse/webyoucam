package info.faljse.webyoucam.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by Martin on 12.08.2016.
 */
public class ClientSession {
    private final static Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private ByteBuffer byteBuffer;
    private int clientID;
    private volatile boolean alive=true;



    public ClientSession(byte[] buffer, int clientID) {
        byteBuffer=ByteBuffer.wrap(buffer);
        this.clientID=clientID;

    }

    public void send() {
        if(!alive)
            return;
        ((Buffer)byteBuffer).rewind();
        try {
            MyNanoHTTPD.sendByteCount.addAndGet(byteBuffer.remaining());
            //session.getRemote().sendBytesByFuture(byteBuffer);
        }catch(Exception e){
            alive=false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

}
