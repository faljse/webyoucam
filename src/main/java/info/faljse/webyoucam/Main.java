package info.faljse.webyoucam;

import info.faljse.SDNotify.SDNotify;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Extension;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Enumeration;
import java.util.Set;

/**
 * Created by Martin on 22.07.2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        try {
            new Freemarker().init();
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(info.faljse.webyoucam.Settings.port);
            server.addConnector(connector);

            // Setup the basic application "context" for this application at "/"
            // This is also known as the handler tree (in jetty speak)
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            ResourceConfig config = new ResourceConfig();
            config.register(info.faljse.webyoucam.streaming.Service.class);
            config.register(JacksonFeature.class);
            ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
            context.addServlet(jerseyServlet, "/*");
            ServletHolder sh=new ServletHolder();

            context.setAttribute("wsSessions", new info.faljse.webyoucam.streaming.WSSessions());
            server.setHandler(context);
            // Add WebSocket endpoint to javax.websocket layer

            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            for(int i = 0; i< info.faljse.webyoucam.Settings.ffmpegCmd.length; i++){
                String cmd= info.faljse.webyoucam.Settings.ffmpegCmd[i];
                if(cmd==null)
                    continue;
                cmd=cmd.trim();
                ServletHolder holder = context.addServlet(info.faljse.webyoucam.streaming.InputStreamServlet.class, "/stream/input/" + (i+1));
                holder.setInitParameter("cmd", cmd);
                holder.setInitOrder((i+1));
                ServerEndpointConfig sec = ServerEndpointConfig.Builder.create(info.faljse.webyoucam.streaming.ServiceWS.class, "/stream/output/"+(i+1)).build();
                sec.getUserProperties().put("holder", holder);
                wscontainer.addEndpoint(sec);
            }
            server.start();
            WebSocketUpgradeFilter filter = (WebSocketUpgradeFilter)context.getAttribute("org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter");
            filter.getFactory().getExtensionFactory().unregister("permessage-deflate");
            server.dump(System.err);
            SDNotify.sendNotify(); //notify: ready
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(0);
        }
    }
}
