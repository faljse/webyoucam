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

import info.faljse.webyoucam.Settings;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.protocol.Handshake;
import io.undertow.websockets.core.protocol.version07.Hybi07Handshake;
import io.undertow.websockets.core.protocol.version08.Hybi08Handshake;
import io.undertow.websockets.core.protocol.version13.Hybi13Handshake;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static io.undertow.Handlers.resource;

public class MyHTTPD implements Runnable {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MyHTTPD.class);
    private static final Logger LOG = Logger.getLogger(MyHTTPD.class.getName());

    public static Map<String, SendThread> list = new HashMap<String, SendThread>();
    public static AtomicLong sendByteCount = new AtomicLong();
    public static AtomicLong recvByteCount = new AtomicLong();
    private final int port;
    private java.util.Timer t = new Timer();
    private long lastRecvBytes = 0;
    private long lastSendBytes = 0;
    private final boolean debug;
    Undertow server;

    public MyHTTPD(int port, boolean debug) {
        this.debug = debug;
        this.port = port;
    }

    public void start() throws ServletException {

        Undertow.Builder builder = Undertow.builder();
        Set<Handshake> handshakes = new HashSet();
        handshakes.add(new Hybi13Handshake(Set.of("null"),false));
        handshakes.add(new Hybi08Handshake(Set.of("null"),false));
        handshakes.add(new Hybi07Handshake(Set.of("null"),false));

        WebSocketProtocolHandshakeHandler handler = new WebSocketProtocolHandshakeHandler(handshakes, new WebSocketConnectionCallback() {
            @Override
            public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                String id=exchange.getRequestParameters().get("id").get(0);
                channel.getReceiveSetter().set(new MyWebSocket(id, channel));
            }
        });

        builder.addHttpListener(port, "0.0.0.0");
        builder.setHandler(Handlers.routing()
                .post("/stream/input/{id}", new InputStreamServlet())
                .get("/stream/output/{id}", handler)
                .get("/*", resource(new PathResourceManager(Paths.get("webroot"), 100))
                        .setDirectoryListingEnabled(true))
        );
        server=builder.build();
        server.start();

        for(int i=0;i<10;i++) {
            String cmd=Settings.ffmpegCmd[i];
            if(cmd==null)
                continue;
            FFMpegThread f = new FFMpegThread(cmd);
            new Thread(f).start();
            SendThread st = new SendThread();
            new Thread(st).start();
            MyHTTPD.list.put(String.valueOf(i), st);
        }

    }

    private static class MyWebSocket extends AbstractReceiveListener {
        public MyWebSocket(String id, WebSocketChannel channel) {
            System.out.println("open" + this.toString());
            SendThread st = MyHTTPD.list.get(id);
            if (st == null)
                return;
            ClientSession s = st.ws.createAddSession(channel);
            logger.info(String.format("client connected: %s", channel.getPeerAddress().toString()));
            System.out.println("end" + this.toString());
        }

        @Override
        protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
            super.onClose(webSocketChannel, channel);
        }
    }

    @Override
    public void run() {
        long currentSendBytes = sendByteCount.get();
        long currentRecvBytes = recvByteCount.get();

        float recvRate = (currentRecvBytes - lastRecvBytes) / 1000000.0f * 8;
        float sendRate = (currentSendBytes - lastSendBytes) / 1000000.0f * 8;
        int clients = 0;
        for (SendThread st : list.values()) {
            clients += st.ws.getCount();
        }
        logger.info(String.format("%d clients; recv/send MBit %.2f/%.2f", clients, recvRate, sendRate));
        lastSendBytes = currentSendBytes;
        lastRecvBytes = currentRecvBytes;
    }
}