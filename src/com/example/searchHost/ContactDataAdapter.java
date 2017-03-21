package com.example.searchHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

public class ContactDataAdapter extends BaseAdapter{
	List<HashMap<String, String>>  listItems;
	private Context mContext;
	private LayoutInflater listContainer;
	private List<HashMap<String, String>> changeListId = null;
	
	public ContactDataAdapter(Context context, List<HashMap<String, String>> list) {
		mContext = context;
		listContainer = LayoutInflater.from(mContext);
		listItems = list;
		changeListId = new ArrayList<HashMap<String, String>>();
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
			final String contactName = (String)listItems.get(selectId).get("contactName");
			myView.ContactName.setText(contactName);
			myView.ContactName.setEnabled(false);
			myView.ContactNumber.setText(listItems.get(selectId).get("contactNumber"));
			final String contactId   = (String)listItems.get(selectId).get("contactId");
			myView.ContactNumber.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
						int arg3) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void afterTextChanged(Editable editable) {
					// TODO Auto-generated method stub
					boolean isExists = false;
					if(changeListId != null) {
						for(int i = 0; i < changeListId.size(); ++i) {
							if(contactId.equals(changeListId.get(i).get("contactId"))) {
								changeListId.get(i).put("contactNumber", editable.toString());
								isExists = true;
								break;
							}
						}
						if(!isExists) {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("contactId", String.valueOf(contactId));
							map.put("contactName", contactName);
							map.put("contactNumber", editable.toString());
							changeListId.add(map);
						}
					}
				}
			});
		}
		
		return convertView;
	}
	
	public List<HashMap<String, String>> getChangeNumber() {
		return changeListId;
	}

	private final class ContactView {
		public EditText ContactName;
		public EditText ContactNumber;
	}
}
