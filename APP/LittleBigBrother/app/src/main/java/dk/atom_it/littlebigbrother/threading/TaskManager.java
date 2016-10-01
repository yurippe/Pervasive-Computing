package dk.atom_it.littlebigbrother.threading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Kristian on 10/1/2016.
 */

public class TaskManager implements Runnable{

    private static TaskManager taskManager = null;

    private Thread thread;
    private boolean running = true;
    private LinkedBlockingDeque<Task> tasks = new LinkedBlockingDeque<>();
    private HashMap<UniqueTask, UniqueTask> uniqueTasks = new HashMap<>();

    public static TaskManager getInstance(){
        if(taskManager == null){
            taskManager = new TaskManager();
        }
        return taskManager;
    }

    protected TaskManager(){
        thread = new Thread(this);
        thread.start();
    }

    public void run(){

        Task currentTask = null;
        while(running){
            //Here the magic happens
            currentTask = tasks.pollLast();
            if(currentTask == null){
                //Queue is empty, lets yield
                Thread.yield();
            }

            if(currentTask instanceof UniqueTask){
                uniqueTasks.remove((UniqueTask) currentTask);
            }
            currentTask.run();

        }
    }

    public Task addTask(Task task){
        if(task instanceof UniqueTask){
            UniqueTask uniqueTask = (UniqueTask) task;
            if(uniqueTasks.containsKey(uniqueTask)){
                UniqueTask retTask = uniqueTasks.get(uniqueTask);
                retTask.update(uniqueTask.getTask());
                return retTask;
            } else {
                uniqueTasks.put(uniqueTask, uniqueTask);
                tasks.addFirst(uniqueTask);
                return uniqueTask;
            }
        } else {
            tasks.addFirst(task);
            return task;
        }
    }

}
