package com.androidxyq.event;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: gongdewei
 * Date: 12-3-31
 * Time: 上午12:18
 */
public class ActionDelegator {
    private Vector<Runnable> taskQueue = new Vector<Runnable>();

    public ActionDelegator() {
        new ActionWorker(taskQueue).start();
    }

    public void publish(Runnable task){
        synchronized (taskQueue){
            taskQueue.add(task);
            taskQueue.notifyAll();
        }
    }
    
    private static class ActionWorker extends Thread{
        private Vector<Runnable> taskQueue;

        private ActionWorker(Vector<Runnable> taskQueue) {
            super("ActionWorker");
            setDaemon(true);
            this.taskQueue = taskQueue;
        }
        public void run() {
            while(true){
                if(!taskQueue.isEmpty()){
                    Runnable task = taskQueue.remove(taskQueue.size() - 1);
                    task.run();
                }else {
                    synchronized (taskQueue){
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
