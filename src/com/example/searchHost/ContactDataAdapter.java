package com.example.searchHost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

public class ContactDataAdapter extends BaseAdapter{
	List<HashMap<String, String>>  listItems;
	private Context mContext;
	private LayoutInflater listContainer;
	
	public ContactDataAdapter(Context context, List<HashMap<String, String>> list) {
		mContext = context;
		listContainer = LayoutInflater.from(mContext);
		listItems = list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItems != null ? listItems.size() : 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int postion, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ContactView myView = null;
		final int selectId = postion;
		
		if(convertView == null) {
			myView = new ContactView();
			
			convertView = listContainer.inflate(R.layout.contact_item, null);
			
			myView.ContactName = (EditText)convertView.findViewById(R.id.contact_name);
			myView.ContactNumber   = (EditText)convertView.findViewById(R.id.contact_number);
			
			convertView.setTag(myView);
		} else {
			myView = (ContactView)convertView.getTag();
		}
		
		if(listItems != null && selectId < listItems.size()) {
			myView.ContactName.setText(listItems.get(selectId).get("contactName"));
			myView.ContactNumber.setText(listItems.get(selectId).get("contactNumber"));
		}
		
		return convertView;
	}

	private final class ContactView {
		public EditText ContactName;
		public EditText ContactNumber;
	}
}
