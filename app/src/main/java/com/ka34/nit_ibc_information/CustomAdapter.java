package com.ka34.nit_ibc_information;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<CustomListView> {
    private LayoutInflater layoutInflater_;

    public CustomAdapter(Context context, int textViewResourceId, List<CustomListView> objects) {
        super(context, textViewResourceId, objects);
        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 特定の行(position)のデータを得る
        CustomListView item = getItem(position);

        // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
        if (null == convertView) {
            convertView = layoutInflater_.inflate(R.layout.list_layout, null);
        }

        // CustomDataのデータをViewの各Widgetにセットする
        TextView typetextView;
        switch ((item.getType())) {
            case "cancel":
                typetextView = (TextView)convertView.findViewById(R.id.type);
                typetextView.setText("休");
                typetextView.setTextColor(Color.RED);
                convertView.setBackgroundColor(Color.rgb(0xff,0xe4,0xdd));
                break;
            case "makeup":
                typetextView = (TextView)convertView.findViewById(R.id.type);
                typetextView.setText("補");
                typetextView.setTextColor(Color.BLUE);
                convertView.setBackgroundColor(Color.rgb(0xe9, 0xe7, 0xff));
                break;
            case "change":
                typetextView = (TextView)convertView.findViewById(R.id.type);
                typetextView.setText("変");
                typetextView.setTextColor(Color.rgb(0x02, 0xb6, 0x00));
                convertView.setBackgroundColor(Color.rgb(0xe6,0xff,0xe3));
                break;
            default:
                break;
        }

        TextView datetextView;
        datetextView = (TextView)convertView.findViewById(R.id.date);
        datetextView.setText(item.getDate());

        TextView conttextView;
        conttextView = (TextView)convertView.findViewById(R.id.cont);
        conttextView.setText(item.getCont());

        TextView termtextView;
        termtextView = (TextView)convertView.findViewById(R.id.term);
        termtextView.setText(item.getTerm());

        TextView clastextView;
        clastextView = (TextView)convertView.findViewById(R.id.clas);
        clastextView.setText(item.getClas());

        return convertView;
    }
}
