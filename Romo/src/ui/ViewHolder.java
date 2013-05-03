package ui;

import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
	
	private TextView titleView;
	private TextView subTitleView;
	private ImageView iconView;
	private ListItem listItem;
	
	public ViewHolder(TextView titleView, TextView subTitleView, ImageView iconView){
		
		this.titleView = titleView;
		this.subTitleView = subTitleView;
		this.iconView = iconView;
		listItem = null;
	}
	
	public ListItem getListItem() {
		return listItem;
	}
	
	public void setListItem(ListItem listItem) {
		
		this.listItem = listItem;
		
		if(titleView != null){
			titleView.setText(listItem.getTitle());
		}
		
		if(subTitleView != null){
			subTitleView.setText(listItem.getSubtitle());
		}
		
		if(iconView != null){
			iconView.setImageResource(listItem.getIconId());
		}
	}
}
