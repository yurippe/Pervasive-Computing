package dk.atom_it.littlebigbrother.notifications;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Kristian on 10/4/2016.
 */

public abstract class WifiEvent implements AbstractEvent {

    public static final int FILTER_MAC = 0;
    public static final int FILTER_NAME = 1;

    private int type;
    private String filter;
    private boolean entered = false;

    private int noteID;

    public WifiEvent(String filter, int filter_type){
        this.filter = filter;
        this.type = filter_type;
    }

    public void onUpdate(List<ScanResult> results){
        for(ScanResult result : results){
            if(type == FILTER_MAC){

                if(result.BSSID.equals(this.filter)){
                    if(!entered){
                        this.onEnter();
                        entered = true;
                    }
                    return;
                }

            } else if (type == FILTER_NAME){

                if(result.SSID.equals(this.filter)){
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

    @Override
    public int getNoteId() {
        return this.noteID;
    }

    @Override
    public void setNoteId(int id){
        this.noteID = id;
    }

}