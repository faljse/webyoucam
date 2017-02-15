package info.faljse.webyoucam.streaming;

import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@Path("/")
public class Service {
    private final static Logger logger = LoggerFactory.getLogger(Service.class);
    @Context
    Configuration configuration;

    public Service() {
    }

    @GET
    public Response getIndex() throws IOException, TemplateException {
        ByteArrayOutputStream bos = info.faljse.webyoucam.Freemarker.process(new HashMap(), "index");
        return Response.status(200).entity(bos.toString()).build();
    }

    @GET
    @Path("/static/{filename:.*}")
    public Response getStaticFile(@PathParam("filename") String filename) {
        try {
            InputStream s = Service.class.getResourceAsStream("/static/" + filename);
            return Response.status(200).entity(s).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.toString()).build();
        }
    }
}