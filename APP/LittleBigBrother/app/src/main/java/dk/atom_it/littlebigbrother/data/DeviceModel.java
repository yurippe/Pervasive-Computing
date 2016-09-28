package dk.atom_it.littlebigbrother.data;

/**
 * Created by Kristian on 9/28/2016.
 */

public class DeviceModel {

    public String name;
    public String mac;

    public DeviceModel(String mac, String name){

        this.mac = mac;
        this.name = name;

    }

    //MAC addresses uniquely defines this
    @Override
    public boolean equals(Object other){
        if(other instanceof DeviceModel){
            DeviceModel otha = (DeviceModel) other;
            return otha.mac.equals(this.mac);
        } else {
            return false;
        }
    }
}
