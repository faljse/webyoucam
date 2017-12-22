package info.faljse.webyoucam.streaming;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

@WebSocket
public class StreamWebSocket implements WebSocketListener
{
	private final static Logger logger = LoggerFactory.getLogger(StreamWebSocket.class);
	private String id;
	
	public StreamWebSocket(String _id) {
		id=_id;
	}

    @OnWebSocketMessage
    public void onWebSocketText(Session session, byte buf[], int offset, int len)
    {
    	System.out.println(ByteBuffer.wrap(buf,offset,len));
    }

    
	@Override
	public void onWebSocketClose(int arg0, String arg1)
	{
		logger.info("close "+arg0 + arg1);
	}

	@Override
	public void onWebSocketConnect(Session session) {
		logger.info("client connected: "+session.getRemoteAddress().toString());
		logger.info("client count: "+WebServer.list.size());
		WSSessions ws = WebServer.list.get(id);
		ws.addSession(session);	
	}

	@Override
	public void onWebSocketError(Throwable arg0) {
		// TODO Auto-generated method stub
		
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