package inc.ahmedmourad.bakery.datasource;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import inc.ahmedmourad.bakery.R;

public class CacheDataSourceFactory implements DataSource.Factory {

	private static SimpleCache simpleCache;

	private final DefaultDataSourceFactory defaultDatasourceFactory;

	public CacheDataSourceFactory(final Context context) {
		super();

		final String userAgent = Util.getUserAgent(context, context.getString(R.string.en_app_name));

		final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

		defaultDatasourceFactory = new DefaultDataSourceFactory(context, bandwidthMeter, new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));

		final LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024);

		simpleCache = new SimpleCache(new File(context.getCacheDir(), context.getString(R.string.en_media)), evictor);
	}

	@Override
	public DataSource createDataSource() {
		return new CacheDataSource(
				simpleCache,
				defaultDatasourceFactory.createDataSource(),
				CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
				20 * 1024 * 1024
		);
	}
}
