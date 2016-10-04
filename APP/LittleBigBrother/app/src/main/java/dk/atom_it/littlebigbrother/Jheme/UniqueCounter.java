package dk.atom_it.littlebigbrother.Jheme;

/**
 * Created by Kristian on 10/4/2016.
 */

public class UniqueCounter {

    private static UniqueCounter obj;

    private int current;
    protected UniqueCounter(){
        current = 0;
    }

    private int getNext(){
        current++;
        return current;
    }

    public static int getNextInt(){
        if(obj == null){
            obj = new UniqueCounter();
        }
        return obj.getNext();
    }
}
