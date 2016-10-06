package com.example.hhoo7.udacitycoursep8;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private EditText mQueryView;
    private ImageButton btn_search;
    private PaperListAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mEmptyView;
    private ListView mListView;

    private static final int INIT_VIEW = 0;
    private static final int NO_NETWORK = 1;
    private static final int NO_RESULTS = 2;
    private static final int SEARCHING = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOnline()){
                    Log.d(LOG_TAG, "onClick: " + mQueryView.getText().toString());
                    new SearchDateTask().execute(mQueryView.getText().toString());
                    upgradeEmptyView(SEARCHING);
                }else {
                    upgradeEmptyView(NO_NETWORK);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "长按打开web网站", Toast.LENGTH_SHORT).show();
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (isOnline()) {
                    Paper pager = mAdapter.getItem(i);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pager.getmWebUrl()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    return true;
                } else {
                    upgradeEmptyView(NO_NETWORK);
                    return true;
                }
            }
        });

    }

    private void initView() {
        mQueryView = (EditText) findViewById(R.id.query_edit_view);
        btn_search = (ImageButton) findViewById(R.id.btn_seatch_view);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.INVISIBLE);

        mListView = (ListView) findViewById(R.id.news_list_view);
        mAdapter = new PaperListAdapter(this, new ArrayList<Paper>());
        mListView.setAdapter(mAdapter);

        upgradeEmptyView(INIT_VIEW);
    }

    private class SearchDateTask extends AsyncTask<String, Void, List<Paper>> {
        @Override
        protected List<Paper> doInBackground(String... strings) {

            String apikey = "4fbc983a-0e39-42eb-b280-d98aefaec04b";
            String pager = "1";
            String mQueryWord = strings[0];

            final String BASE_URl = "http://content.guardianapis.com/search?";
            final String API_KEY_PARAM = "api-key";
            final String PAGER_PARAM = "pager";
            final String QUERY_PARAM = "q";

            Uri baseUri = Uri.parse(BASE_URl);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter(API_KEY_PARAM, apikey);
            uriBuilder.appendQueryParameter(PAGER_PARAM, pager);
            uriBuilder.appendQueryParameter(QUERY_PARAM, mQueryWord);

            URL url = null;
            try {
                url = new URL(uriBuilder.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "loadInBackground: " + uriBuilder.toString());

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                // 设定请求参数，
                // Http请求方法类型有 get阅读数据 和 post携带数据更新服务器两种，
                urlConnection.setRequestMethod("GET");
                // 设定连接主机的限定时间
                urlConnection.setReadTimeout(10000);
                // 设定从主机获取数据的限定时间
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();

                StringBuilder output = null;
                if (urlConnection.getResponseCode() == 200) {
                    // 读取服务器返回的流数据
                    inputStream = urlConnection.getInputStream();
                    output = new StringBuilder();
                    if (inputStream != null) {
                        // 使用 InpuStreamReader 类将流数据中0,1的计算机语言转化为人类可读的语言
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        // 由于InputStreamReader 类的读取效率比较慢，所以使用 BufferdReader 类封装一次提高读取效率
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line);
                        }

                    }
                }
                return fetchData(output.toString());

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "loadInBackground: " + e);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Paper> papers) {
            mAdapter.clear();
            if (papers != null) {
                upgradeEmptyView(INIT_VIEW);
                mAdapter.addAll(papers);
            } else {
                upgradeEmptyView(NO_RESULTS);
            }
        }

        private ArrayList<Paper> fetchData(String jsonStr) throws JSONException {
            ArrayList<Paper> results = new ArrayList<Paper>();

            JSONObject primaryJson = new JSONObject(jsonStr);
            JSONObject response = primaryJson.getJSONObject("response");

            if (response.getInt("total") > 0) {
                JSONArray primaryResults = response.getJSONArray("results");

                for (int i = 0; i < primaryResults.length(); i++) {
                    JSONObject info = primaryResults.getJSONObject(i);
                    String title = info.getString("webTitle");
                    String webPublicationDate = info.getString("webPublicationDate");
                    String webUrl = info.getString("webUrl");

                    results.add(new Paper(title, webPublicationDate, webUrl));
                }
            } else {
                return null;
            }

            if (results.size() > 0) {
                return results;
            } else {
                return null;
            }
        }

    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void upgradeEmptyView(int stateOfView) {
        switch (stateOfView) {
            case INIT_VIEW:
                mEmptyView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                break;
            case NO_NETWORK:
                mEmptyView.setText("当前网络状态不好，请检查网络后重新查询");
                mEmptyView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                break;
            case NO_RESULTS:
                mEmptyView.setText("查询数据无，请更换关键字");
                mEmptyView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                break;
            case SEARCHING:
                mEmptyView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                break;
        }

    }

}
