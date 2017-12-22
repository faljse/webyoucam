package info.faljse.webyoucam.streaming;

import info.faljse.webyoucam.Freemarker;
import info.faljse.webyoucam.Settings;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class WebServer extends TimerTask {
    private final static Logger logger = LoggerFactory.getLogger(WebServer.class);

    public static Map<String, WSSessions> list = new HashMap<>();
	public static AtomicLong sendByteCount=new AtomicLong();
	public static AtomicLong recvByteCount=new AtomicLong();

    private java.util.Timer t=new Timer();


	public void start() {
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(Settings.port);
		server.addConnector(connector);

		// Setup the basic application "context" for this application at "/"
		// This is also known as the handler tree (in jetty speak)
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
        for(int i = 0; i< Settings.ffmpegCmd.length; i++) {
            String cmd=Settings.ffmpegCmd[i];
            if(cmd==null)
                continue;
            cmd=cmd.trim();
            String id= String.valueOf(i+1);
            list.put(id, new WSSessions(id));
            ServletHolder holder = context.addServlet(InputStreamServlet.class, "/stream/input/" + (i+1));
            holder.setInitParameter("cmd", cmd);
            holder.setInitParameter("id", id);
            holder.setInitOrder((i+1));
            ServletHolder holderOutput = context.addServlet(StreamWebSocketServlet.class, "/stream/output/" + (i+1));
            holderOutput.setInitParameter("id", id);
            holderOutput.setInitOrder((i+1));
        }

		ServletHolder staticHolder = new ServletHolder(new DefaultServlet());
		staticHolder.setInitParameter("resourceBase", "./webroot");
		context.addServlet(staticHolder, "/static/*");
        context.addServlet(new ServletHolder(new IndexServlet()),"/");


        server.setHandler(context);
        t.scheduleAtFixedRate(this,0,1000L);

		try {
			server.start();
			server.dump(System.err);
			server.join();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	private long lastRecvBytes=0;
    private long lastSendBytes=0;

    @Override
    public void run() {
        long currentSendBytes=sendByteCount.get();
        long currentRecvBytes=recvByteCount.get();

        float recvRate=(currentRecvBytes-lastRecvBytes)/1000000.0f*8;
        float sendRate=(currentSendBytes-lastSendBytes)/1000000.0f*8;
        logger.info(String.format("recv/send MBit %.2f/%.2f",recvRate, sendRate) );

        lastSendBytes=currentSendBytes;
        lastRecvBytes=currentRecvBytes;
    }
}
