package discovery;


import ui.ListItem;
import ui.ViewHolder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.romo.R;

public class CustomArrayAdapter extends ArrayAdapter<ListItem> {

	// Global information about application environment
	private Context context;
	// The resource id for the layout used to represent a list item
	private int layoutResourceId;
		
	public CustomArrayAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}
	
	/**
	 * Adds the specified element to this set if it is not already present
	 */
	@Override
	public void add(ListItem object) {
		
		for(int i=0; i<getCount(); i++){
			
			if(getItem(i).equals(object)){
				return;
			}
		}
		
		super.add(object);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// A viewHolder keeps references to child views to avoid unnecessary calls
		// to findViewById() on each row
		ViewHolder holder;
		
        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
		if(convertView == null){
			
			// Get layout inflater from context
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			// Inflate layout from xml
			convertView = inflater.inflate(layoutResourceId, parent, false);
			
            // Creates a ViewHolder and store references to the child views we want to bind data to.
			holder = new ViewHolder(	(TextView)convertView.findViewById(R.id.txtViewTitle), 
									 	(TextView)convertView.findViewById(R.id.txtViewSubtitle), 
									 	(ImageView)convertView.findViewById(R.id.imgViewIcon));
			
			convertView.setTag(holder);
					
		}else{
			// Get the ViewHolder back to get fast access to the TextView and the ImageView.
			holder = (ViewHolder)convertView.getTag();
		}
		
		// Bind data to the holder
		holder.setListItem(getItem(position));
		
		// Return view
		return convertView;
	}
}
