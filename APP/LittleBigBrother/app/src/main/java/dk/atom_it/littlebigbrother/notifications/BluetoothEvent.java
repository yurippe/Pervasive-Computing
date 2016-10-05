package dk.atom_it.littlebigbrother.notifications;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by Kristian on 10/4/2016.
 */

public abstract class BluetoothEvent implements AbstractEvent{

    public static final int FILTER_MAC = 0;
    public static final int FILTER_NAME = 1;

    private int type;
    private String filter;
    private boolean entered = false;


    public BluetoothEvent(String filter, int filter_type){
        this.filter = filter;
        this.type = filter_type;
    }

    public void onUpdate(List<BluetoothDevice> results){
        for(BluetoothDevice device : results){
            if(type == FILTER_MAC){

                if(device.getAddress().equals(this.filter)){
                    if(!entered){
                        this.onEnter();
                        entered = true;
                    }
                    return;
                }

            } else if (type == FILTER_NAME){

                if(device.getName().equals(this.filter)){
                    if(!entered){
                        this.onEnter();
                        entered = true;
                    }
                    return;
                }

            }
        }

        //We didnt find any matches, so if we have entered, we have now exited
        if(entered){
            this.onExit();
            entered = false;
        }
    }

    public int getFilterType(){
        return this.type;
    }

    public String getFilterTypeAsString(){
        return (this.type == 0 ? "MAC" : "Name");
    }

    public String getFilter(){
        return this.filter;
    }

}