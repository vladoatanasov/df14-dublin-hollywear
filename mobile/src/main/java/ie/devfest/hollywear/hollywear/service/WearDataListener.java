package ie.devfest.hollywear.hollywear.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ie.devfest.hollywear.hollywear.R;
import ie.devfest.hollywear.hollywear.api.AengusService;
import ie.devfest.hollywear.hollywear.api.model.AengusQuery;
import ie.devfest.hollywear.hollywear.api.model.AengusResponse;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by maui on 31.10.14.
 */
public class WearDataListener extends WearableListenerService {

    public static final String HOLLY_WEAR_QUERY = "/hollywear/query";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if(messageEvent.getPath().equals(HOLLY_WEAR_QUERY)) {
            String query = new String(messageEvent.getData());

            doAngusQuery(query);
        }

    }

    private void doAngusQuery(final String query) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(AengusService.SERVICE_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        AengusService aengusService = restAdapter.create(AengusService.class);

        aengusService.query(new AengusQuery(query), new Callback<AengusResponse>() {
            @Override
            public void success(AengusResponse result, Response response) {
                new AsyncTask<AengusResponse, Void, Void>() {

                    @Override
                    protected Void doInBackground(AengusResponse... aengusResponses) {
                        try {
                            handleAengusResult(query, aengusResponses[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(result);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void handleAengusResult(String query, AengusResponse result) throws IOException {
        if(result.getCategory().equals("ListActorMovies")) {
            showMoviesForActor(query, result);
        } else if(result.getCategory().equals("ActorsForMovie")) {
            showActorsForMovie(query, result);
        }
    }

    private void showMoviesForActor(String query, AengusResponse result) throws IOException {
        JsonArray movies = result.getResult().getAsJsonArray();

        if(movies.size() > 0) {

            int max = movies.size() > 5 ? 5 : movies.size();
            HashMap<Integer, Notification> notifications = new HashMap<Integer, Notification>();

            for(int i = 0; i < max; i++ ) {
                int notificationId = (int) (System.currentTimeMillis()+i);

                JsonObject movie = movies.get(i).getAsJsonObject();

                // Build an intent for an action to view element on connected device
                Intent showItemIntent = new Intent(Intent.ACTION_VIEW);
                Uri itemUri = Uri.parse("http://www.freebase.com"+ movie.get("mid").getAsString());
                showItemIntent.setData(itemUri);
                PendingIntent showPendingIntent =
                        PendingIntent.getActivity(this, 0, showItemIntent, 0);

                // Create a WearableExtender to add functionality for wearables
                NotificationCompat.WearableExtender wearableExtender =
                        new NotificationCompat.WearableExtender()
                                .setHintHideIcon(true)
                                .setBackground(Picasso.with(this).load("https://www.googleapis.com/freebase/v1/image" + movie.get("mid").getAsString() + "?maxwidth=400&maxheight=400&errorid=/freebase/no_image_png&key=AIzaSyCQVC9yA72POMg2VjiQhSJQQP1nf3ToZTs\n").get());

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this)
                                .setGroup(query)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(query)
                                .setWhen(System.currentTimeMillis())
                                .setVibrate(new long[]{0, 100, 1000})
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .extend(wearableExtender)
                                .addAction(R.drawable.show_action,
                                        getString(R.string.action_show_phone), showPendingIntent)
                                .setContentText(movie.get("name").getAsString());

                notifications.put(notificationId, notificationBuilder.build());
            }

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            for(Map.Entry<Integer, Notification> notificationEntry : notifications.entrySet()) {
                // Build the notification and issues it with notification manager.
                notificationManager.notify(notificationEntry.getKey(), notificationEntry.getValue());
            }
        }
    }

    private void showActorsForMovie(String query, AengusResponse result) throws IOException {
        JsonArray actors = result
                .getResult().getAsJsonArray().get(0).getAsJsonObject().get("starring").getAsJsonArray();

        if(actors.size() > 0) {

            int max = actors.size() > 5 ? 5 : actors.size();
            HashMap<Integer, Notification> notifications = new HashMap<Integer, Notification>();

            for(int i = 0; i < max; i++ ) {
                int notificationId = (int) (System.currentTimeMillis()+i);

                JsonObject actor = actors.get(i).getAsJsonObject().get("actor").getAsJsonObject();
                String character = actors.get(i).getAsJsonObject().get("character").getAsString();

                // Build an intent for an action to view element on connected device
                Intent showItemIntent = new Intent(Intent.ACTION_VIEW);
                Uri itemUri = Uri.parse("http://www.freebase.com" + actor.get("mid").getAsString());
                showItemIntent.setData(itemUri);
                PendingIntent showPendingIntent =
                        PendingIntent.getActivity(this, 0, showItemIntent, 0);

                // Create a WearableExtender to add functionality for wearables
                NotificationCompat.WearableExtender wearableExtender =
                        new NotificationCompat.WearableExtender()
                                .setHintHideIcon(true)
                                .setBackground(Picasso.with(this).load("https://www.googleapis.com/freebase/v1/image" + actor.get("mid").getAsString() + "?maxwidth=400&maxheight=400&errorid=/freebase/no_image_png&key=AIzaSyCQVC9yA72POMg2VjiQhSJQQP1nf3ToZTs\n").get());

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(query)
                                .setWhen(System.currentTimeMillis())
                                .setVibrate(new long[]{0, 100, 1000})
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .extend(wearableExtender)
                                .addAction(R.drawable.show_action,
                                        getString(R.string.action_show_phone), showPendingIntent)
                                .setContentText(actor.get("name").getAsString()+" plays "+ character);

                notifications.put(notificationId, notificationBuilder.build());
            }

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            for(Map.Entry<Integer, Notification> notificationEntry : notifications.entrySet()) {
                // Build the notification and issues it with notification manager.
                notificationManager.notify(notificationEntry.getKey(), notificationEntry.getValue());

            }
        }
    }
}
