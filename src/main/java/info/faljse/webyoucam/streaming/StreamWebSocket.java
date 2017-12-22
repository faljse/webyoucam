package info.faljse.webyoucam.streaming;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class StreamWebSocket implements WebSocketListener
{
	private final static Logger logger = LoggerFactory.getLogger(StreamWebSocket.class);
	private String id;
	
	public StreamWebSocket(String _id) {
		id=_id;
	}



	@Override
	public void onWebSocketClose(int code, String reason)
	{
		logger.info("close code: {}, reason: {}", code, reason);
	}

	@Override
	public void onWebSocketConnect(Session session) {
		logger.info(String.format("client connected: %s",session.getRemoteAddress().toString()));
		WSSessions ws = WebServer.list.get(id);
		ws.addSession(session);	
	}

	@Override
	public void onWebSocketError(Throwable arg0) {
	    logger.warn("ws error", arg0);
	}

	@Override
	public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
    	logger.info("onWebSocketBinary "+arg0);
	}

	@Override
	public void onWebSocketText(String arg0) {
    	logger.info("onWebSocketText"+arg0);
	}
    
    
}