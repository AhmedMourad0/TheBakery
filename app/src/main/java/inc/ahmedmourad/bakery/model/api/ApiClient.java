package inc.ahmedmourad.bakery.model.api;

import android.support.annotation.NonNull;

import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

	private static final String BASE_URL = "http://go.udacity.com/";

	private static volatile Retrofit INSTANCE = null;

	@NonNull
	public static Retrofit getInstance() {

		if (INSTANCE != null) {

			return INSTANCE;

		} else {

			synchronized (ApiClient.class) {

				return INSTANCE != null ? INSTANCE : (INSTANCE = buildClient());
			}
		}
	}

	@NonNull
	private static Retrofit buildClient() {

		return new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.callbackExecutor(Executors.newSingleThreadExecutor())
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.build();
	}
}
