package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 6/7/2016.
 */
public class SchemePair implements SchemeObject {

    private SchemeObject car;
    private SchemeObject cdr;



    public String getStringValue(boolean parenthesis){
        String r = parenthesis ? "(" : "";
            if(car == null && cdr == null){
                return (parenthesis ? "()" : "");
            }
            else if (cdr instanceof SchemePair){
                r += car.getStringValue();
                SchemePair ncdr = ((SchemePair) cdr);
                if(!(ncdr.isNull()))
                    r += " " + ncdr.getStringValue(false);
            } else {
                if(!parenthesis)
                    r += "(";
                r += car.getStringValue() + " . " + cdr.getStringValue();
                if(!parenthesis)
                    r += ")";
            }

        return r + (parenthesis ? ")" : "");
    }
    public String getStringValue() {
        return getStringValue(true);
    }

    public static SchemePair fromArray(SchemeObject[] arr){
        if (arr.length == 0){
            return new SchemePair(null, null);
        } else if (arr.length == 1){
            return new SchemePair(arr[0], new SchemePair(null, null));
        } else { /* Horribly inefficient */
            SchemeObject[] next = new SchemeObject[arr.length-1];
            for(int in=1; in<arr.length; in++){
                next[in-1] = arr[in];
            }
            return new SchemePair(arr[0], fromArray(next));
        }
    }

    public SchemePair(SchemeObject car, SchemeObject cdr){
        this.car = car;
        this.cdr = cdr;
    }


    public SchemeObject getCar(){
        return this.car;
    }

    public SchemeObject getCdr(){
        return this.cdr;
    }

    public boolean isNull(){
        return car == null && cdr == null;
    }

    public boolean isProperList(){
        if(isNull())
            return true;
        else
            return car instanceof SchemePair && ((SchemePair) car).isProperList();
    }

}
