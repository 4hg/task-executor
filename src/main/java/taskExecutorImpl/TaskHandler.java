package taskExecutorImpl;

import taskExecutor.Task;

public class TaskHandler implements Runnable {
    boolean isAvailable = true;
    Task task;
    TaskExecutorImpl taskExecutorImpl;
    int id;

    public TaskHandler(int id, TaskExecutorImpl taskExecutorImpl) {
        this.id = id;
        this.taskExecutorImpl = taskExecutorImpl;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean getAvailability() {
        return isAvailable;
    }

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        Task current;
        while (true) {
            System.out.println("Thread "+ Thread.currentThread().getId()+" beginning execution." );
            try {
                current = taskExecutorImpl.removeTaskFromQueue();
                current.execute();
            synchronized (taskExecutorImpl.getAddMonitor()) {
                System.out.println("removeTask: Thread " + Thread.currentThread().getId() + " has completed. "+ current.getName() +" Notifying main");
                taskExecutorImpl.getAddMonitor().notify();
            }
            }catch(Exception e) {
                System.err.println("ERROR" + Thread.currentThread().getId());
                e.printStackTrace();
            }
        }
    }
}
