package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 6/7/2016.
 */
public class SchemeBoolean implements SchemeObject {

    private boolean value;

    public SchemeBoolean(boolean value){
        this.value = value;
    }

    public SchemeBoolean(String value){
        if(value.equals("#t")){
            this.value = true;
        } else if (value.equals("#f")){
            this.value = false;
        } else {
            throw new RuntimeException("Expected #t or #f");
        }
    }

    public boolean getValue(){
        return value;
    }

    @Override
    public String getStringValue() {
        return "#" + (value ? "t" : "f");
    }
}
