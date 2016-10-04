package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 6/7/2016.
 */
public class SchemeFloat implements SchemeObject, HasValue<Double>, SchemeNumber {


    private Double value;

    public SchemeFloat(String i){
        value = Double.parseDouble(i);
    }

    public SchemeFloat(float i){
        value = (double) i;
    }

    public SchemeFloat(double i){
        value = i;
    }

    public Double getValue(){
        return value;
    }

    public String getStringValue(){
        return value.toString();
    }

    @Override
    public Double getNumericValue() {
        return value;
    }
}
