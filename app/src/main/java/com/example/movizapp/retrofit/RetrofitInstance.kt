package com.example.movizapp.retrofit

import android.content.Context
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

// Retrofit instance to make network requests
// object defines singleton
object RetrofitInstance {
    private const val Base_Url = "https://api.themoviedb.org/3/"

    private var retrofit: Retrofit? = null

    fun getApi(context: Context): ApiService {
        if (retrofit == null) {
            // 10 MB HTTP response cache
            val cacheDir = File(context.cacheDir, "http_cache")
            val cache = Cache(cacheDir, 10L * 1024 * 1024)

            // Interceptor that adds Cache-Control headers to responses
            // TMDB API doesn't always set cache headers, so we force caching
            val cacheInterceptor = Interceptor { chain ->
                val response = chain.proceed(chain.request())
                val cacheControl = CacheControl.Builder()
                    .maxAge(5, TimeUnit.MINUTES) // Cache API responses for 5 minutes
                    .build()
                response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheControl.toString())
                    .build()
            }

            // Offline interceptor — serve stale cache if no network
            val offlineCacheInterceptor = Interceptor { chain ->
                var request = chain.request()
                val cacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS) // Use cached data up to 7 days old when offline
                    .build()
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(offlineCacheInterceptor)
                .addNetworkInterceptor(cacheInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(Base_Url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }

    // Legacy accessor for backward compatibility (uses no cache)
    val api: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(Base_Url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
