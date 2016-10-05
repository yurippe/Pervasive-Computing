package dk.atom_it.littlebigbrother.data;

/**
 * Created by Kristian on 10/5/2016.
 */

public class Globals {

    public String token = null;

    //Singleton stuff:
    private static Globals instance;
    protected Globals(){}
    public static Globals getInstance(){
        if(instance == null){
            instance = new Globals();
        }
        return instance;
    }

}