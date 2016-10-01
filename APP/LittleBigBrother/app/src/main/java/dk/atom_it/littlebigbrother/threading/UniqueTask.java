package dk.atom_it.littlebigbrother.threading;

/**
 * Created by Kristian on 10/1/2016.
 */

public class UniqueTask extends Task {


    private int UUID;
    private Task task;

    public UniqueTask(Task task, int UUID){
        this.UUID = UUID;
        this.task = task;
    }

    public void update(Task newtask){
        this.task = newtask;
    }

    public Task getTask(){
        return this.task;
    }

    @Override
    public boolean equals(Object other){
        if (other instanceof UniqueTask){
            return this.UUID == ((UniqueTask) other).UUID;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return UUID;
    }


    @Override
    public void run() {
        this.task.run();
    }
}
