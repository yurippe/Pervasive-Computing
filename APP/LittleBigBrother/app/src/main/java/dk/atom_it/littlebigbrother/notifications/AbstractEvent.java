package dk.atom_it.littlebigbrother.notifications;

/**
 * Created by Kristian on 10/4/2016.
 */

public interface AbstractEvent {

    public void onEnter();
    public void onExit();
    public String getJhemeCode();
}
