package inc.ahmedmourad.bakery.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.pojos.WidgetEntry;

public class WidgetEntriesListAdapter extends BaseAdapter {

	private List<WidgetEntry> entriesList = new ArrayList<>();

	public WidgetEntriesListAdapter() {
		super();
	}

	public void updateEntries(final List<WidgetEntry> entriesList) {
		this.entriesList = entriesList;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return entriesList.size();
	}

	@Override
	public Object getItem(int position) {
		return entriesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Long.parseLong(entriesList.get(position).widgetId);
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {

		View view = convertView;

		ViewHolder holder;

		if (view == null) {

			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_widget_entry, parent, false);

			holder = new ViewHolder(view);

			view.setTag(holder);

		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.bind(position, entriesList.get(position));

		return view;
	}

	class ViewHolder {

		@BindView(R.id.entry_position)
		TextView positionTextView;

		@BindView(R.id.entry_name)
		TextView nameTextView;

		ViewHolder(final View itemView) {
			ButterKnife.bind(this, itemView);
		}

		private void bind(final int position, final WidgetEntry entry) {
			positionTextView.setText(String.valueOf(position + 1));
			nameTextView.setText(entry.recipeName);
		}
	}
}
