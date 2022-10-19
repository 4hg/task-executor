package taskExecutorImpl;

import java.lang.Thread.UncaughtExceptionHandler;

import taskExecutor.Task;
import taskExecutor.TaskExecutor;

public class TaskExecutorImpl implements TaskExecutor {

    Task[] fifoQueue = new Task[5];
    private int nextTask = 0;
    private int nextOpenTask = 0;
    private final Object takeMonitor = new Object();
    private final Object addMonitor = new Object();
    Thread[] threads;
    private int availableTasks = 0;
    private int waiting = 0;
    private boolean addIsNotified = false;
    private boolean takeIsNotified = false;

    
    public TaskExecutorImpl(int threadPoolSize) {
        Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                
            }
            
        });
        
//        System.out.println("Starting with " + threadPoolSize + " threads.");
        threads = new Thread[threadPoolSize];
        
//        Runnable r = new Runnable() {
//            public void run() {
//                System.out.println("MONITOR: "+Thread.currentThread().getName());
//                while (true) {
//                    StringBuffer sb = new StringBuffer();
//                    for (Thread t : threads) {
//                        if(t != null)
//                        if (t.isAlive()) {
//                            sb.append(t.getName()+t.getState() + ", ");
//                        } else {
//                            sb.append("DEAD, ");
//                        }
//                    }
//                    System.out.println(sb.toString());
//                    try {
//                        Thread.sleep(50);
//                        System.err.println(Thread.currentThread().getThreadGroup().activeCount());
//                        System.err.println(Thread.getAllStackTraces());
//                    } catch (Throwable e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        Thread monThread = new Thread(r);
//        monThread.setPriority(5);
//        monThread.start();
//        
        
        
        for (int i = 0; i < threadPoolSize; i++) {
            threads[i] = new Thread(new TaskHandler(i, this));
            threads[i].setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                    
                }
                
            });
            threads[i].start();
        }
        

       

    }

    // Used by test
    @Override
    public void addTask(Task task) {
        // Block when pointer points to an existing
        if (fifoQueue[nextOpenTask % fifoQueue.length] != null) {
            do {
                synchronized (addMonitor) {
                    try {
                        addMonitor.wait(20000);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                // Notify Task Handlers
                synchronized (takeMonitor) {
                    setTakeNotified(true);
                    takeMonitor.notify();
                }

            } while (!addIsNotified || fifoQueue[nextOpenTask % fifoQueue.length] != null);
            setAddNotified(false);
        }

        // Add new task to queue
        fifoQueue[nextOpenTask++ % fifoQueue.length] = task;
        availableTasks++;

        // Notify Task Handlers
        synchronized (takeMonitor) {
            setTakeNotified(true);
            takeMonitor.notify();
        }
//        
//        try {
//            System.err.println(Thread.currentThread().getName());
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
    }

    // Used by TaskHandler
    public Task removeTaskFromQueue() {
        Task removed = null;
        synchronized (takeMonitor) {
            // if queue is empty wait
            if (availableTasks == 0 && fifoQueue[nextTask % fifoQueue.length] == null) {
                waiting++;
                do {
                    try {
                        takeMonitor.wait(100);
                    } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } while (!takeIsNotified || fifoQueue[nextTask % fifoQueue.length] == null);
                // leave while loop if notification or removed is not null
                // reset notification
                setTakeNotified(false);
                waiting--;

            }

            removed = fifoQueue[nextTask % fifoQueue.length];
            
//            System.err.println(removed.getName() + " was removed by Thread-"+ Thread.currentThread().getName());

            fifoQueue[nextTask++ % fifoQueue.length] = null;
            availableTasks--;
//            System.err.println("available tasks:"+availableTasks);

            takeMonitor.notify();
        }

        // Notify Add Monitor observers
        synchronized (addMonitor) {
            setAddNotified(true);
            addMonitor.notify();
        }

        return removed;

    }

    public boolean nextTaskAvailable() {
        return fifoQueue[nextTask % fifoQueue.length] == null ? false : true;
    }

    public Object getAddMonitor() {
        return addMonitor;
    }

    public void setTakeNotified(boolean bool) {
        takeIsNotified = bool;
    }

    public void setAddNotified(boolean bool) {
        addIsNotified = bool;
    }

}
