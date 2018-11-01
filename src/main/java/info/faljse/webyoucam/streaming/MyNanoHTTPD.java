package info.faljse.webyoucam.streaming;

/*
 * #%L
 * NanoHttpd-Websocket
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import info.faljse.webyoucam.Main;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;
import org.nanohttpd.router.RouterNanoHTTPD;
import org.nanohttpd.util.IHandler;
import org.slf4j.LoggerFactory;

/**
 * @author Paul S. Hawke (paul.hawke@gmail.com) On: 4/23/14 at 10:31 PM
 */
public class MyNanoHTTPD extends RouterNanoHTTPD implements Runnable{
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MyNanoHTTPD.class);


    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(MyNanoHTTPD.class.getName());

    public static Map<String, WSSessions> list = new HashMap<>();
    public static AtomicLong sendByteCount=new AtomicLong();
    public static AtomicLong recvByteCount=new AtomicLong();
    private java.util.Timer t=new Timer();


    private long lastRecvBytes=0;
    private long lastSendBytes=0;


    private final boolean debug;

    public MyNanoHTTPD(int port, boolean debug) {
        super(port);
        this.debug = debug;
        this.addHTTPInterceptor(new MyNanoHTTPD.Interceptor());
        addMappings();
    }

    @Override
    public void addMappings() {
        super.addMappings();


        addRoute("/blocks", BlockHandler.class);
        addRoute("/user/help", BlockHandler.class);
        addRoute("/user/:id", BlockHandler.class);
        addRoute("/general/:param1/:param2", GeneralHandler.class);
        addRoute("/photos/:customer_id/:photo_id", null);
        addRoute("/test", String.class);
        addRoute("/interface", UriResponder.class); // this will cause an error
        // when called
        addRoute("/toBeDeleted", String.class);
        removeRoute("/toBeDeleted");

        addRoute("/static(.)+", IndexServlet.StaticPageTestHandler.class, new File("webroot/").getAbsoluteFile());
        addRoute("/", IndexServlet.StaticPageTestHandler.class, new File("webroot/index.html").getAbsoluteFile());
    }


    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new MyWebSocket(this, handshake);
    }




    private static class MyWebSocket extends WebSocket {

        private final MyNanoHTTPD server;

        public MyWebSocket(MyNanoHTTPD server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
            ClientSession s=new ClientSession(null,1);
            logger.info(String.format("client connected: %s",getHandshakeRequest().getRemoteIpAddress().toString()));
            WSSessions ws = MyNanoHTTPD.list.get(1);
            ws.addSession(s);
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (server.debug) {
                System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                        + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            }
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            try {
                message.setUnmasked();
                sendFrame(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                System.out.println("P " + pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            logger.warn("exception occured", exception);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("R " + frame);
            }
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("S " + frame);
            }
        }
    }

    @Override
    public void run() {
        long currentSendBytes=sendByteCount.get();
        long currentRecvBytes=recvByteCount.get();

        float recvRate=(currentRecvBytes-lastRecvBytes)/1000000.0f*8;
        float sendRate=(currentSendBytes-lastSendBytes)/1000000.0f*8;
        int clients=0;
        for(WSSessions ws:list.values()){
            clients+=ws.getCount();
        }
        logger.info(String.format("%d clients; recv/send MBit %.2f/%.2f", clients, recvRate, sendRate) );

        lastSendBytes=currentSendBytes;
        lastRecvBytes=currentRecvBytes;
    }

    public static String makeAcceptKey(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        String text = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        md.update(text.getBytes(), 0, text.length());
        byte[] sha1hash = md.digest();
        Base64.Encoder b64encoder = Base64.getEncoder();
        return b64encoder.encodeToString(sha1hash);
    }


    private boolean isWebSocketConnectionHeader(Map<String, String> headers) {
        String connection = (String)headers.get("connection");
        return connection != null && connection.toLowerCase().contains("Upgrade".toLowerCase());
    }

    protected boolean isWebsocketRequested(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        String upgrade = (String)headers.get("upgrade");
        boolean isCorrectConnection = this.isWebSocketConnectionHeader(headers);
        boolean isUpgrade = "websocket".equalsIgnoreCase(upgrade);
        return isUpgrade && isCorrectConnection;
    }

    public Response handleWebSocket(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        if (this.isWebsocketRequested(session)) {
            if (!"13".equalsIgnoreCase((String)headers.get("sec-websocket-version"))) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Invalid Websocket-Version " + (String)headers.get("sec-websocket-version"));
            } else if (!headers.containsKey("sec-websocket-key")) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Missing Websocket-Key");
            } else {
                WebSocket webSocket = this.openWebSocket(session);
                Response handshakeResponse = webSocket.getHandshakeResponse();

                try {
                    handshakeResponse.addHeader("sec-websocket-accept", makeAcceptKey((String)headers.get("sec-websocket-key")));
                } catch (NoSuchAlgorithmException var6) {
                    return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "The SHA-1 Algorithm required for websockets is not available on the server.");
                }

                if (headers.containsKey("sec-websocket-protocol")) {
                    handshakeResponse.addHeader("sec-websocket-protocol", ((String)headers.get("sec-websocket-protocol")).split(",")[0]);
                }

                return handshakeResponse;
            }
        } else {
            return null;
        }
    }

    protected final class Interceptor implements IHandler<IHTTPSession, Response> {
        public Interceptor() {
        }

        public Response handle(IHTTPSession input) {
            return MyNanoHTTPD.this.handleWebSocket(input);
        }
    }

    public static class BlockHandler extends DefaultHandler {

        @Override
        public String getMimeType() {
            return MIME_PLAINTEXT;
        }

        @Override
        public String getText() {
            return "not implemented";
        }

        @Override
        public IStatus getStatus() {
            return Status.OK;
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            JsonArray blocks = Json.array();

            return Response.newFixedLengthResponse(blocks.toString());
        }
    }
}