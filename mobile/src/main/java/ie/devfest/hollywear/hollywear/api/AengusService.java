package ie.devfest.hollywear.hollywear.api;

import ie.devfest.hollywear.hollywear.api.model.AengusQuery;
import ie.devfest.hollywear.hollywear.api.model.AengusResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by maui on 31.10.2014.
 */
public interface AengusService {

    public final String SERVICE_ENDPOINT = "https://aengus.herokuapp.com/api/v1";

    @POST("/nlp/query")
    void query(@Body AengusQuery request, Callback<AengusResponse> callback);

    @POST("/nlp/query")
    AengusResponse query(@Body AengusQuery request);
}
