package info.faljse.webyoucam.streaming;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Map;

public class InputStreamServlet implements HttpHandler {
    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(InputStreamServlet.class);


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        String id = params.get("id").getFirst();
        SendThread st = MyHTTPD.list.get(id);
        exchange.startBlocking();
        st.send(exchange.getInputStream());
        return;
    }
}
