package inc.ahmedmourad.bakery.external.adapter;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class IngredientsRemoteViewsService extends RemoteViewsService {

	public static final String EXTRA_RECIPE_ID = "ri";

	@Override
	public RemoteViewsFactory onGetViewFactory(final Intent intent) {
		if (intent == null || intent.getExtras() == null || !intent.hasExtra(EXTRA_RECIPE_ID))
			return new IngredientsRemoteViewsFactory(this, -1);
		else
			return new IngredientsRemoteViewsFactory(this, intent.getExtras().getInt(EXTRA_RECIPE_ID));
	}
}