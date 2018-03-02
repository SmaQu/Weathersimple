package com.smaqu.weather_simple;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText city;
    private Button searchBtn;
    private TextView weather;
    private ProgressBar progressBar;
    private static final String TAG = "MainActivity";

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                String message = "";
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray array = jsonObject.getJSONArray("list");
                    for (int i = 0; i < 1; i++) {

                        JSONObject jsonPart = array.getJSONObject(i);
                        String main = "";
                        String description = "";

                        JSONObject mainPart = jsonPart.getJSONObject("main");
                        JSONArray weatherPart = jsonPart.getJSONArray("weather");

                        for (int y = 0; y < weatherPart.length(); y++) {

                            JSONObject weatherType = weatherPart.getJSONObject(y);
                            description = weatherType.getString("main");
                        }

                        main = mainPart.getString("temp");

                        if (!Objects.equals(main, "") && !Objects.equals(description, "")) {
                            message += main + "C " + description + "\r\n";
                        }
                    }
                    if (!Objects.equals(message, "")) {
                        progressBar.setVisibility(View.GONE);
                        weather.setVisibility(View.VISIBLE);
                        weather.setText(message);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No city found", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        city = findViewById(R.id.et_city);
        searchBtn = findViewById(R.id.bt_search);
        weather = findViewById(R.id.tv_weather);
        progressBar = findViewById(R.id.progressBar);
        searchBtn.setOnClickListener(this);
        city.setOnEditorActionListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_search:
                findWeather();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            findWeather();
            return true;
        }
        return false;
    }

    private void findWeather() {

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(city.getWindowToken(), 0);
        weather.setVisibility(View.GONE);

        DownloadTask task = new DownloadTask();
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendQueryParameter("q", city.getText().toString())
                .appendQueryParameter("units", "metric")
                .appendQueryParameter("APPID", getResources().getString(R.string.appId));
        String url = builder.build().toString();
        Log.e(TAG, "URL: " + url);
        progressBar.setVisibility(View.VISIBLE);
        task.execute(url);
    }
}
