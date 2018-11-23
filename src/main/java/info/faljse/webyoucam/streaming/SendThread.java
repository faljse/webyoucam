package info.faljse.webyoucam.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(FFMpegThread.class);

    private Thread sendThread;
    public WSSessions ws;
    private final byte[] readBuf = new byte[BUFFERSIZE * BUFFERED_BLOCKS];
    private Semaphore semSend;
    private final Lock rcvLock = new ReentrantLock();
    private volatile boolean _running;
    private static final int BUFFERSIZE = 1024; //should be smaller than avg frame
    private static final int BUFFERED_BLOCKS = 100;


    public SendThread() {
        ws = new WSSessions();
        Thread.currentThread().setName("Stream input");
        sendThread = new Thread(this);
        sendThread.setName("Websocket send");
        _running = true;
        semSend = new Semaphore(0);
    }

    private volatile int writePos = 0;


    public void send(InputStream is) {
        boolean locked = rcvLock.tryLock();
        if (locked) {
            try {
                while (true) {
                    readBuffer(is, readBuf, (writePos) * BUFFERSIZE, BUFFERSIZE);
                    writePos = (writePos + 1) % (BUFFERED_BLOCKS);
                    semSend.release();
                    MyHTTPD.recvByteCount.addAndGet(readBuf.length);
                }
            } catch (IOException e) {
                logger.warn("Input Stream error", e);
            } finally {
                rcvLock.unlock();
                shutDown();
                semSend = null;
                sendThread = null;
            }
        }
    }

    @Override
    public void run() {
        try {
            int readPos=0;
            while (_running) {
                semSend.acquire();
                ws.send(readBuf, readPos * BUFFERSIZE, BUFFERSIZE);
                readPos = (readPos + 1) % (BUFFERED_BLOCKS);
            }
        } catch (InterruptedException e) {
            logger.info("Send Thread interrupted", e);
        } catch (Exception e) {
            logger.error("Send Thread Error", e);
        }
        logger.info("Send Thread terminating.");
    }

    private synchronized void shutDown() {
        _running = false;
        try {
            while (sendThread.isAlive()) {
                sendThread.interrupt();
                logger.warn("Waiting for sendThread to die");
                sendThread.join(1000);
            }
        } catch (Exception e) {
            logger.warn("Shutdown sendthread", e);
        }
    }

    private static void readBuffer(InputStream is, byte[] buffer, int offset, int length) throws IOException {
        int readPos = 0;
        while (true) {
            int count = is.read(buffer, readPos + offset, length - readPos);
            if (count < 0)
                throw new EOFException("EOF from: ");
            readPos += count;
            if (readPos == length)
                return;
        }
    }
}
