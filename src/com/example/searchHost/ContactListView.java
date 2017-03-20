package com.example.searchHost;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ContactListView extends ListView{
	
	public ContactListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ContactListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public ContactListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public void setListViewHeightBasedOnChildren() {
		setListViewHeightBasedOnChildren(8);
	}
	
	public void setListViewHeightBasedOnChildren(int NumberOfChildHeight) {
		ListAdapter listAdapter = getAdapter();
		if (listAdapter == null) {
			return;
		}

		int viewHeight = 0;
		View listItem = listAdapter.getView(0, null, this);
		listItem.measure(0, 0);
		viewHeight = listItem.getMeasuredHeight();
		
        ViewGroup.LayoutParams params = getLayoutParams();
        
		params.height  = (viewHeight + getDividerHeight()) * NumberOfChildHeight;				

		setLayoutParams(params);
	}
}
