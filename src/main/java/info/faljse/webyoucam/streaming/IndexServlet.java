package info.faljse.webyoucam.streaming;

import freemarker.template.TemplateException;
import info.faljse.webyoucam.Freemarker;
import info.faljse.webyoucam.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class IndexServlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger(IndexServlet.class);
    private final static Freemarker fm=new Freemarker();
    static {
        fm.init();
    }
    private String greeting="Hello World";
    public IndexServlet(){}
    public IndexServlet(String greeting)
    {
        this.greeting=greeting;
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        HashMap<String, Object> h=new HashMap<>();
            h.put("cmd", Settings.ffmpegCmd);

        try {
            ByteArrayOutputStream res = fm.process(h, "index");
            res.writeTo(response.getOutputStream());
        } catch (TemplateException e) {
            logger.warn("template failed", e);
        }
    }
}