package info.faljse.webyoucam.streaming;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.router.RouterNanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InputStreamServlet extends RouterNanoHTTPD.DefaultStreamHandler {
    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(InputStreamServlet.class);

    @Override
    public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        SendThread st=uriResource.initParameter(SendThread.class);
        st.send(session.getInputStream());
        System.out.println("posted");
        return Response.newFixedLengthResponse("OK");
    }

    @Override
    public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        FFMpegThread f=uriResource.initParameter(FFMpegThread.class);

        return Response.newChunkedResponse(this.getStatus(), this.getMimeType(), this.getData());
    }






    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public IStatus getStatus() {
        return null;
    }

    @Override
    public InputStream getData() {
        return null;
    }

}
