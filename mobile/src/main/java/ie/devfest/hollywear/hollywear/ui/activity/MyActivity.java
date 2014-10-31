package ie.devfest.hollywear.hollywear.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ie.devfest.hollywear.hollywear.R;
import ie.devfest.hollywear.hollywear.api.AengusService;
import ie.devfest.hollywear.hollywear.api.model.AengusQuery;
import ie.devfest.hollywear.hollywear.api.model.AengusResponse;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyActivity extends Activity {

    private AengusService aengusService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(AengusService.SERVICE_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        aengusService = restAdapter.create(AengusService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

        aengusService.query(new AengusQuery("List movies starring Pierce Brosnan"), new Callback<AengusResponse>() {
            @Override
            public void success(AengusResponse result, Response response) {
                handleAengusResult(result);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void handleAengusResult(AengusResponse result) {
        Toast.makeText(MyActivity.this, result.getCategory(), Toast.LENGTH_LONG).show();

        if(result.getCategory().equals("ListActorMovies")) {
            JsonArray movies = result.getResult().getAsJsonArray();

            if(movies.size() > 0) {
                Toast.makeText(MyActivity.this, movies.get(0).getAsJsonObject().get("name").getAsString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
