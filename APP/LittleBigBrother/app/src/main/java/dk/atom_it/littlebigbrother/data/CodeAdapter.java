package dk.atom_it.littlebigbrother.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import dk.atom_it.littlebigbrother.R;

/**
 * Created by Steffan Sølvsten on 09-10-2016.
 */
public class CodeAdapter extends ArrayAdapter<CodeModel> {

    public CodeAdapter (Context context, ArrayList<CodeModel> code){
        super(context, 0, code);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        CodeModel codeModel = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wifibt_list_item, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.code_title);
        TextView code = (TextView) convertView.findViewById(R.id.code_code);
        title.setText(codeModel.getTitle());
        code.setText(codeModel.getCode());

        return convertView;
    }
}
