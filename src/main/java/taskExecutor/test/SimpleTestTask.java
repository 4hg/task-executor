package taskExecutor.test;

import taskExecutor.Task;

public class SimpleTestTask implements Task
{
    private static MutableInt mutableInt = new MutableInt();

    private String name;
    private boolean started;

    public SimpleTestTask(String name)
    {
        this.name = name;
        started = false;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void execute()
    {
        if (started) {
            throw new RuntimeException("Task started twice " + name);
        } else {
            started = true;
        }

        try {
            Thread.sleep(100);

            StringBuilder sb = new StringBuilder();
            sb.append("Hello From Thread: ");
            sb.append(Thread.currentThread().getName());
            sb.append(", Task: ");
            sb.append(name);
            sb.append(", numOfActivations: ");
            sb.append(mutableInt.increment());
            // Note that the printed message includes the Thread's unique name.
            System.out.println(sb);
        } catch (Throwable e) {
            e.printStackTrace();
        } 
    }
}

class MutableInt
{
    private int count = 0;

    synchronized int increment()
    {
        return ++count;
    }
}
