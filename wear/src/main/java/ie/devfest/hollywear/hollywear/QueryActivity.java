package ie.devfest.hollywear.hollywear;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class QueryActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String HOLLY_WEAR_QUERY = "/hollywear/query";
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int CONFIRMATION_REQUEST_CODE = 1;

    private GoogleApiClient mGoogleApiClient;

    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Timber.d("Connected to Google Api Service");
        displaySpeechRecognizer();
//        processQuery("List movies starring Pierce Brosnan");
//        processQuery("Who played Flash Gordon");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    private ArrayList<String> getNodes() {
        ArrayList<String> results= new ArrayList<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            processQuery(spokenText);

        } else {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processQuery(String spokenText) {
        if(mGoogleApiClient.isConnected()) {
            new AsyncTask<String, Void, Void>() {

                @Override
                protected Void doInBackground(String... params) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, getNodes().get(0), HOLLY_WEAR_QUERY, params[0].getBytes()).await();

                    if (!result.getStatus().isSuccess()) {
                        Timber.e("ERROR: failed to send Message: %s", result.getStatus());
                    }


                    // Show Confirmation Activity
                    Intent confirmationIntent = new Intent(QueryActivity.this, ConfirmationActivity.class);
                    confirmationIntent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                    confirmationIntent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Query sent!");
                    startActivityForResult(confirmationIntent, CONFIRMATION_REQUEST_CODE);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });

                    return null;
                }
            }.execute(spokenText);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
