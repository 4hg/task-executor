package taskExecutor;

import org.junit.jupiter.api.Test;

import taskExecutor.test.SimpleTestTask;
import taskExecutorImpl.TaskExecutorImpl;

public class TaskExecutorTest
{
    
    public void runTest()
    {
        // Initialize the executor with 10 threads & a queue size of 100.
        final TaskExecutorImpl taskExecutor = new TaskExecutorImpl(10);

        // Inject 1000 tasks into the executor in a separate thread.
        Runnable inserter = new Runnable() {
            public void run()
            {
                for (int idx = 0; idx < 1000; idx++) {
                    // Note that Threads are assigned names.
                    String name = "SimpleTask" + idx;
                    Task myTask = new SimpleTestTask(name);
                    taskExecutor.addTask(myTask);
                    System.out.println("******  Adding Task: " + myTask.getName());
                }
            }
        };

        Thread insertThread = new Thread(inserter);
        insertThread.start();
        try {
            insertThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runTestSetup()
    {
        TaskExecutorTest app = new TaskExecutorTest();
        app.runTest();
    }
}
