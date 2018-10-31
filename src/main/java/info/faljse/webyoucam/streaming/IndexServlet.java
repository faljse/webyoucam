package info.faljse.webyoucam.streaming;

import freemarker.template.TemplateException;
import info.faljse.webyoucam.Freemarker;
import info.faljse.webyoucam.Settings;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.router.RouterNanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class IndexServlet extends RouterNanoHTTPD
{
    private final static Logger logger = LoggerFactory.getLogger(IndexServlet.class);
    private static final int PORT = 9090;
    private String greeting="Hello World";


    public static class UserHandler extends DefaultHandler {

    @Override
    public String getText() {
        return "not implemented";
    }

    public String getText(Map<String, String> urlParams, IHTTPSession session) {
        String text = "<html><body>User handler. Method: " + session.getMethod().toString() + "<br>";
        text += "<h1>Uri parameters:</h1>";
        for (Map.Entry<String, String> entry : urlParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            text += "<div> Param: " + key + "&nbsp;Value: " + value + "</div>";
        }
        text += "<h1>Query parameters:</h1>";
        for (Map.Entry<String, String> entry : session.getParms().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            text += "<div> Query Param: " + key + "&nbsp;Value: " + value + "</div>";
        }
        text += "</body></html>";

        return text;
    }

    @Override
    public String getMimeType() {
        return "text/html";
    }

    @Override
    public IStatus getStatus() {
        return Status.OK;
    }

    public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        String text = getText(urlParams, session);
        ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
        int size = text.getBytes().length;
        return Response.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
    }

}

public static class StaticPageTestHandler extends RouterNanoHTTPD.StaticPageHandler {

    @Override
    protected BufferedInputStream fileToInputStream(File fileOrdirectory) throws IOException {
        if ("exception.html".equals(fileOrdirectory.getName())) {
            throw new IOException("trigger something wrong");
        }
        return super.fileToInputStream(fileOrdirectory);
    }
}

    /**
     * Create the server instance
     */
    public IndexServlet() throws IOException {
        super(PORT);
        addMappings();
        System.out.println("\nRunning! Point your browers to http://localhost:" + PORT + "/ \n");
    }

    /**
     * Add the routes Every route is an absolute path Parameters starts with ":"
     * Handler class should implement @UriResponder interface If the handler not
     * implement UriResponder interface - toString() is used
     */
    @Override
    public void addMappings() {
        super.addMappings();
        addRoute("/user", UserHandler.class);
        addRoute("/user", UserHandler.class); // add it twice to execute the
        // priority == priority case
        addRoute("/user/help", GeneralHandler.class);
        addRoute("/user/:id", UserHandler.class);
        addRoute("/general/:param1/:param2", GeneralHandler.class);
        addRoute("/photos/:customer_id/:photo_id", null);
        addRoute("/test", String.class);
        addRoute("/interface", UriResponder.class); // this will cause an error
        // when called
        addRoute("/toBeDeleted", String.class);
        removeRoute("/toBeDeleted");
        addRoute("/browse/(.)+", StaticPageTestHandler.class, new File("src/test/resources").getAbsoluteFile());
    }
}