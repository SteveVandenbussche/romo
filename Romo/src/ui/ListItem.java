package ui;

public class ListItem {
	
	private String title;			
	private String subtitle;		
	private int iconId;	
	
	public ListItem(String title, String subtitle, int iconId){
		this.title = title;
		this.subtitle = subtitle;
		this.iconId = iconId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getSubtitle() {
		return subtitle;
	}
	
	public int getIconId() {
		return iconId;
	}
}
