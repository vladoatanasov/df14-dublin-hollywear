package ie.devfest.hollywear.hollywear.api;

import com.google.gson.JsonObject;

import ie.devfest.hollywear.hollywear.api.model.AengusQuery;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by maui on 31.10.2014.
 */
public interface FreebaseService {

    public final String SERVICE_ENDPOINT = "https://www.googleapis.com/freebase/v1";
    public final String COMMON_TOPIC = "/common/topic";

    @GET("/topic/{topicPath}")
    void getTopic(@Path("topicPath") String topicPath, @Query("key") String apiKey, @Query("filter") String filter, Callback<JsonObject> callback);

    @GET("/topic/{topicPath}")
    JsonObject getTopic(@Path("topicPath") String topicPath, @Query("key") String apiKey, @Query("filter") String filter);
}
