package discovery;

import ui.ListItem;

public class BtListItem extends ListItem{
	
	
	public BtListItem(String title, String subtitle, int iconId){
		super(title, subtitle, iconId);
	}
	
	
	@Override
	public boolean equals(Object o) {
		BtListItem item = (BtListItem)o;
		return getSubtitle().equals(item.getSubtitle());
	}
	
	@Override
	public int hashCode() {
		return getSubtitle().hashCode();
	}
}
