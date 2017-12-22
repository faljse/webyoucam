package info.faljse.webyoucam;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import info.faljse.webyoucam.streaming.IndexServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * Created by Martin on 28.07.2016.
 */
public class Freemarker {
    private static Configuration  _configuration= new Configuration(Configuration.VERSION_2_3_27);
    private final static Logger logger = LoggerFactory.getLogger(Freemarker.class);


    public synchronized void init() {

        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        // _configuration.setClassForTemplateLoading(this.getClass(), "/webroot/templates");
        try {
            FileTemplateLoader ftl = new FileTemplateLoader(new File("webroot/templates"));
            ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/webroot/templates");
            MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[] { ftl, ctl });
            _configuration.setTemplateLoader(mtl);
        } catch (IOException e) {
            logger.warn("set template loader failed",e);
            e.printStackTrace();
        }

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        _configuration.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        //RETHROW_HANDLER
        _configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        _configuration.setLogTemplateExceptions(false);

    }

    public static ByteArrayOutputStream process(HashMap map, String templatename) throws IOException, TemplateException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter out=new OutputStreamWriter(bos);
        Template template = _configuration.getTemplate(templatename+".ftl");
        template.process(map, out);
        return bos;

    }
}
