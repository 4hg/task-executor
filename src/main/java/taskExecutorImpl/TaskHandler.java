package taskExecutorImpl;

import taskExecutor.Task;

public class TaskHandler implements Runnable{
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
                current = taskExecutorImpl.removeTaskFromQueue();
                current.execute();
        }
    }

}
