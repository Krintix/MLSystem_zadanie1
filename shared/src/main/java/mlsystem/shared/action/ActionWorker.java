package mlsystem.shared.action;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

public abstract class ActionWorker {
    protected class Worker extends Thread {
        @Override
        public void run() {
            while(!stop){
                Work();

                try {
                    Thread.sleep(interval.toMillis());
                } catch (InterruptedException e) {
                    logger.info(workerInterruptMessage);
                    e.printStackTrace();
                }
            }
        }
    }

    private Worker worker = new Worker();
    private String workerInterruptMessage;
    protected Logger logger;
    protected boolean stop;
    protected String lastMessage = "";
    protected Duration interval;
    protected List<IAction> listeners = new ArrayList<IAction>();

    public ActionWorker(String workerThreadName, String workerInterruptMessage, Duration interval, Logger logger) throws IOException {
        this.logger = logger;
        worker.setName(workerThreadName);
        this.workerInterruptMessage = workerInterruptMessage;
        SetInterval(interval);
    }

    public void Start() {
        stop = false;
        worker.start();
    }

    public void Stop() {
        stop = true;
    }

    public void AddListener(IAction listener) {
        listeners.add(listener);
    }

    public void RemoveListener(IAction listener) {
        listeners.remove(listener);
    }

    public void SetInterval(Duration intervalToSet) throws IOException
    {
        if(intervalToSet.isNegative() || intervalToSet.isZero())
            throw new IOException("Duration must be greater than 0");
        interval = intervalToSet;
    }

    protected void BroadCastToListeners(String message) {
        if(message.equals(lastMessage)) return;
        lastMessage = message;
        logger.info(message);
        for (IAction listener : listeners) {
            listener.Action(message);
        }
    }

    protected abstract void Work();
}
