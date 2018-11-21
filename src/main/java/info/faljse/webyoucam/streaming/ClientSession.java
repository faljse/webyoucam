package info.faljse.webyoucam.streaming;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by Martin on 12.08.2016.
 */
public class ClientSession {
    private final static Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private final IHTTPSession req;
    private final Response response;
    private volatile boolean alive=true;

    public ClientSession(byte[] buffer, int clientID, IHTTPSession handshakeRequest, Response response) {
        this.req=handshakeRequest;
        this.response=response;
    }

    public void send(byte[] buffer) {
        if(!alive)
            return;
        try {
            MyNanoHTTPD.sendByteCount.addAndGet(buffer.length);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
            baos.writeBytes(buffer);
            response.send((baos));
        }catch(Exception e){
            alive=false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

}
