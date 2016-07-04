package me.lab.photonics.photonicslab;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class RequestAdapter extends BaseAdapter {

    Context context;
    ArrayList<DayData> dayDatas;

    public RequestAdapter(Context context, ArrayList<DayData> dayDatas) {
        this.context = context;
        this.dayDatas = dayDatas;
    }

    @Override
    public int getCount() {
        return dayDatas.size();
    }

    @Override
    public DayData getItem(int position) {
        return dayDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context,
                    R.layout.row_daily_data, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        String timeStampString = dayDatas.get(position).getTimeStamp();
        if(timeStampString.length() < 6){
            timeStampString = "0" + timeStampString;
        }
        holder.timeTxt.setText(timeStampString.substring(0,2) + ":" + timeStampString.substring(2,4) + ":" + timeStampString.substring(4,6));
        holder.dataTxt.setText(dayDatas.get(position).getData());
        return convertView;
    }

    class ViewHolder {
        TextView timeTxt, dataTxt;

        public ViewHolder(View view) {
            timeTxt = (TextView) view.findViewById(R.id.timestamp);
            dataTxt = (TextView) view.findViewById(R.id.data);
            view.setTag(this);
        }
    }

    public boolean getSwipEnableByPosition(int position) {
        if(position % 2 == 0){
            return false;
        }
        return true;
    }
}
