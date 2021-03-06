package com.dyhdyh.instagram.login.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dyhdyh.instagram.login.InstagramAuthDialog;
import com.dyhdyh.instagram.login.InstagramRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView tv_log;
    ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_log = findViewById(R.id.tv_log);
        pb = findViewById(R.id.pb);
    }

    public void clickAccessToken(View v) {
        new InstagramAuthDialog(this)
                .setup(getString(R.string.client_id), getString(R.string.client_secret), getString(R.string.redirect_uri))
                .setupToolbar(new InstagramAuthDialog.AuthToolbarCallback() {
                    @Override
                    public View onSetupToolbar(ViewGroup parentView, View.OnClickListener backClickListener, View.OnClickListener clearAuthClickListener) {
                        View layout = LayoutInflater.from(parentView.getContext()).inflate(R.layout.layout_instagram_toolbar, parentView, false);
                        layout.findViewById(R.id.tv_back).setOnClickListener(backClickListener);
                        layout.findViewById(R.id.tv_change).setOnClickListener(clearAuthClickListener);
                        return layout;
                    }
                })
                .setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_horizontal))
                .setInstagramRequest(new InstagramRequest() {
                    @Override
                    public void requestAccessToken(String tokenUrl, Map<String, String> params) {
                        requestInstagramAccessToken(tokenUrl, params);
                    }
                })
                .show();
    }

    private void requestInstagramAccessToken(String tokenUrl, Map<String, String> params) {
        pb.setVisibility(View.VISIBLE);

        Set<Map.Entry<String, String>> entries = params.entrySet();
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : entries) {
            builder.add(entry.getKey(), entry.getValue());
        }
        Request post = new Request.Builder().url(tokenUrl).post(builder.build()).build();
        new OkHttpClient.Builder().build().newCall(post).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Instagram-Login", "登录失败", e);
                        pb.setVisibility(View.GONE);
                        tv_log.setText(e.getMessage());
                        Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                final String json = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Instagram-Login", "登录成功" + json);
                        pb.setVisibility(View.GONE);
                        tv_log.setText(formatJson(json));
                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }


    /**
     * 格式化json
     *
     * @param content
     * @return
     */
    public String formatJson(String content) {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        int count = 0;
        while (index < content.length()) {
            char ch = content.charAt(index);
            if (ch == '{' || ch == '[') {
                sb.append(ch);
                sb.append('\n');
                count++;
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
            } else if (ch == '}' || ch == ']') {
                sb.append('\n');
                count--;
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
                sb.append(ch);
            } else if (ch == ',') {
                sb.append(ch);
                sb.append('\n');
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
            } else {
                sb.append(ch);
            }
            index++;
        }
        return sb.toString();
    }
}
