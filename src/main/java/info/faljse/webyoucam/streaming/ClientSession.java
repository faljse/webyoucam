package info.faljse.webyoucam.streaming;

import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;

/**
 * Created by Martin on 12.08.2016.
 */
public class ClientSession implements WebSocketCallback {
    private final static Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private final WebSocketChannel session;

    private volatile boolean alive=true;

    public ClientSession(int clientID, WebSocketChannel session) {
        this.session=session;
    }

    public void send(byte[] buffer, int offset, int buffersize) {
        if(!alive)
            return;
        try {
            MyHTTPD.sendByteCount.addAndGet(buffer.length);
            WebSockets.sendBinary(ByteBuffer.wrap(buffer, offset, buffersize),session,this);
        }catch(Exception e){
            alive=false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void complete(WebSocketChannel webSocketChannel, Object o) {

    }

    @Override
    public void onError(WebSocketChannel webSocketChannel, Object o, Throwable throwable) {

    }
}
