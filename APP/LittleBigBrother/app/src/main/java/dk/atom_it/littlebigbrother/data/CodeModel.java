package dk.atom_it.littlebigbrother.data;

/**
 * Created by Steffan Sølvsten on 09-10-2016.
 */

public class CodeModel {
    private String title;
    private String code;

    public CodeModel(String title, String code){
        this.title = title;
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof CodeModel){
            CodeModel otha = (CodeModel) other;
            return otha.code.equals(this.code);
        } else {
            return false;
        }
    }
}
