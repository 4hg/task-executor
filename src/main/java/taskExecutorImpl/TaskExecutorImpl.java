package taskExecutorImpl;

import taskExecutor.Task;
import taskExecutor.TaskExecutor;
import taskExecutor.test.SimpleTestTask;

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
        System.out.println("Starting with "+threadPoolSize+" threads.");
        threads = new Thread[threadPoolSize];
        for (int i = 0; i < threadPoolSize; i++) {
            threads[i] = new Thread(new TaskHandler(i, this));
            threads[i].start();
            // System.out.println("Thread " + threads[i].getId() + " has started");
        }

    }

    // Used by test
    @Override
    public void addTask(Task task) {
        StringBuffer sb = new StringBuffer();
        for (Task t : fifoQueue) {
            if (t == null) {
                sb.append(", ");
            } else {
                sb.append(t.getName() + ", ");
            }
        }
        System.out.println(sb);

        fifoQueue[nextOpenTask++ % fifoQueue.length] = task;

        availableTasks++;
        System.out.println("MAIN: available tasks:"+availableTasks);
        synchronized (addMonitor) {
            try {
                if (availableTasks == fifoQueue.length) {
                    do {
                        System.out.println("MAIN: Waiting for handlers");
                        addMonitor.wait(100);
                        synchronized (takeMonitor) {
                            takeMonitor.notify();
                        }
                        
                    } while (!addIsNotified);
                    addIsNotified = false;
                    System.out.println("MAIN: Handler found");
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("Handler available - AddTask: Waiting count: " + waiting);

        synchronized (takeMonitor) {
            takeMonitor.notify();
            takeIsNotified = true;
        }

    }

    // Used by TaskHandler
    public Task removeTaskFromQueue() {
        Task removed = null;

        synchronized (takeMonitor) {

            while (fifoQueue[nextTask % fifoQueue.length] == null || removed == null) {
                try {

                    //System.out.println("Available tasks: " + availableTasks);
                    if (availableTasks == 0) {
                        do {
                            waiting++;
                            System.out.println("Waiting count: " + waiting);
                            takeMonitor.wait(100);
                            takeIsNotified = false;
                            waiting--;
                            System.out.println("Waiting count: " + waiting);
                            synchronized (addMonitor) {
                                System.out.println(Thread.currentThread().getId()+"-THREAD Notifying wait to main thread");
                                addMonitor.notify();
                                addIsNotified = true;
                            }
                        } while (!takeIsNotified);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                removed = fifoQueue[nextTask % fifoQueue.length];
            }

            fifoQueue[nextTask++ % fifoQueue.length] = null;
            availableTasks--;
            System.out.println(removed.getName() + " was removed from task pool " + Thread.currentThread().getId());

            synchronized (addMonitor) {
                System.out.println(Thread.currentThread().getId()+"-THREAD Notifying completion to main thread");
                addMonitor.notify();
                addIsNotified = true;
            }
//            
//            takeMonitor.notify();
//            takeIsNotified = true;

        }

        return removed;

    }

    public boolean nextTaskAvailable() {
        return fifoQueue[nextTask % fifoQueue.length] == null ? false : true;
    }

    public Object getAddMonitor() {
        return addMonitor;
    }

}
