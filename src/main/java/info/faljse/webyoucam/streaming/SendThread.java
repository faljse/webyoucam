package info.faljse.webyoucam.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendThread implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(FFMpegThread.class);

    private Thread sendThread;
    public WSSessions ws;
    private final byte[] readBuf=new byte[BUFFERSIZE];
    private final byte[] sendBuf=new byte[BUFFERSIZE];
    private Semaphore semSend;
    private Semaphore semRecv;
    private final Lock rcvLock=new ReentrantLock();
    private volatile boolean _running;
    private static final int BUFFERSIZE = 1024;

    public SendThread() {
        ws=new WSSessions();
        Thread.currentThread().setName("Stream input");
        sendThread = new Thread(this);
        sendThread.setName("Websocket send");
        _running = true;
        semSend=new Semaphore(0);
        semRecv=new Semaphore(1);
        sendThread.start();
    }

    public void send(InputStream is) {
        boolean locked=rcvLock.tryLock();
        if(locked){
            try {
                while (true){
                    readBuffer(is, readBuf);
                    semRecv.acquire();
                    System.arraycopy(readBuf, 0, sendBuf, 0, BUFFERSIZE);
                    semSend.release();
                    MyHTTPD.recvByteCount.addAndGet(readBuf.length);
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
    @Override
    public void run() {
        try {
            while (_running) {
                semSend.acquire();
                byte[] tmpBuf=new byte[sendBuf.length];
                System.arraycopy(sendBuf,0, tmpBuf,0, sendBuf.length);
                ws.send(tmpBuf);
                semRecv.release();
            }
        } catch (InterruptedException e) {
            logger.info("Send Thread interrupted", e);
        } catch (Exception e) {
            logger.error("Send Thread Error", e);
        }
        logger.info("Send Thread terminating.");
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
}
