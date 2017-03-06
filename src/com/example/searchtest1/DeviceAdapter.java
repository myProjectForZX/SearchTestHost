package com.example.searchtest1;


import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceAdapter extends BaseAdapter{
	private Context mContext;
	ArrayList<DeviceBean> mList;
	public DeviceAdapter(Context context,ArrayList<DeviceBean> list) {
		mContext = context;
		mList=list;
	}
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {	
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.device_item, null);
			viewHolder.ip = (TextView) convertView.findViewById(R.id.tv_ip);
			viewHolder.port = (TextView) convertView.findViewById(R.id.tv_port);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView .getTag();
		}
		viewHolder.ip.setText(mList.get(position).getIp());
		viewHolder.port.setText(String.valueOf(mList.get(position).getPort()));
		return convertView;
	}
	static class ViewHolder {
		TextView ip;
		TextView port;
	}
}
