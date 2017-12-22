package info.faljse.webyoucam.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InputStreamServlet extends HttpServlet implements Runnable {
    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(InputStreamServlet.class);
    private static final int BUFFERSIZE = 1024;
    private final byte[] readBuf=new byte[BUFFERSIZE];
    private final byte[] sendBuf=new byte[BUFFERSIZE];
    private Semaphore semSend=null;
    private Semaphore semRecv=null;

    private final Lock rcvLock=new ReentrantLock();
    private volatile boolean _running = false;
    private Thread sendThread;
    private WSSessions ws;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String cmd=config.getInitParameter("cmd");
        String id=config.getInitParameter("id");
        ws=WebServer.list.get(id);
        ws.setBuffer(sendBuf);
        if(cmd.length()>0)
            try{
                logger.info("Running: \"{}\"", cmd);
                Thread t=new Thread(new FFMpegThread(cmd));
                t.start();
            }catch (Exception e){
                logger.warn("Failed to start ffmpeg for {}",getServletContext().toString());
            }
        else
            logger.info("ffmpegCmd empty, not starting ffmpeg");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        boolean locked=rcvLock.tryLock();
        if(locked){
            try {
                Thread.currentThread().setName("Stream input");
                sendThread = new Thread(this);
                sendThread.setName("Websocket send");
                _running = true;
                semSend=new Semaphore(0);
                semRecv=new Semaphore(1);
                sendThread.start();
                InputStream is = req.getInputStream();
                while (true){
                    readBuffer(is, readBuf);
                    semRecv.acquire();
                    System.arraycopy(readBuf, 0, sendBuf, 0, BUFFERSIZE);
                    semSend.release();
                    WebServer.recvByteCount.addAndGet(readBuf.length);
                }
            } catch (IOException e) {
                logger.warn("Input Stream error", e);
            } catch (InterruptedException e) {
                logger.warn("Input Stream error", e);

            } finally {
                rcvLock.unlock();
                shutDown();
                semSend=null;
                sendThread=null;
            }
        }
    }

    private static void readBuffer(InputStream is, byte[] buffer) throws IOException {
        int readPos=0;
        while (true) {
            int count=is.read(buffer, readPos, buffer.length-readPos);
            if(count<0)
                throw new EOFException("EOF from: ");
            readPos+=count;
            if(readPos==buffer.length)
                return;
        }
    }

    private synchronized void shutDown()
    {
        _running = false;
        try {
            while(sendThread.isAlive()){
                sendThread.interrupt();
                logger.warn("Waiting for sendThread to die");
                sendThread.join(1000);
            }
        } catch (Exception e) {
            logger.warn("Shutdown sendthread", e);
        }
    }

    @Override
    public void run() {
        try {
            while (_running) {
                semSend.acquire();
                ws.send();
                semRecv.release();
            }
        } catch (InterruptedException e) {
            logger.info("Send Thread interrupted", e);
        } catch (Exception e) {
            logger.error("Send Thread Error", e);
        }
        logger.info("Send Thread terminating.");
    }
}
