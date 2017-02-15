package info.faljse.webyoucam.streaming;

import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ClientEndpoint
@ServerEndpoint(value="/stream/output/{streamnumber}")
public class ServiceWS
{
    private final static Logger logger = LoggerFactory.getLogger(ServiceWS.class);
    private ServletHolder  holder;
    private InputStreamServlet iss;

    public ServiceWS(){
    }

    @OnOpen
    public void onWebSocketConnect(Session sess, @PathParam("streamnumber") final String streamNumber, EndpointConfig config)
    {
        System.out.println("Socket Connected: " + sess);
        holder = (ServletHolder) config.getUserProperties().get("holder");
        try {
            this.iss = (InputStreamServlet) holder.getServlet();
            iss.addSession(sess);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onWebSocketText(String message)
    {
        logger.info("Received TEXT message: " + message);
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        logger.warn("Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        logger.warn("Web socket error ",cause);
    }
}