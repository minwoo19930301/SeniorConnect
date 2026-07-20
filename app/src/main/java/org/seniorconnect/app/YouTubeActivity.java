package org.seniorconnect.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * TV-style YouTube screen. Full screen from entry, auto-play, auto-advance,
 * no picture-in-picture, no state kept between sessions. There are no
 * on-screen controls at all: a touch shield swallows every touch, and the
 * only ways out are the system back gesture or locking the screen (which
 * ends the session via onStop). Ads are never blocked, skipped, or clicked —
 * the shield plus a passive notice is the only ad behavior (see AGENTS.md).
 */
public final class YouTubeActivity extends Activity {

    private WebView webView;
    private TextView adNotice;
    private TextView unavailableNotice;
    private String sourceJson;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        adNotice = findViewById(R.id.yt_ad_notice);
        unavailableNotice = findViewById(R.id.yt_unavailable);
        webView = findViewById(R.id.yt_webview);

        sourceJson = loadSourceForCountry();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setSupportMultipleWindows(true); // popups get no window
        webView.addJavascriptInterface(new Bridge(), "SeniorConnect");
        webView.setWebViewClient(new WebViewClient() {
            // The only page is our bundled player. Every navigation attempt —
            // links, redirects, intent:// and app deep links, the YouTube app,
            // external browsers — is ignored.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }
        });

        // The shield consumes all touches, ads and install prompts included.
        // Exiting is deliberate: system back gesture or locking the phone.
        findViewById(R.id.yt_touch_shield).setOnClickListener(v -> { });

        showAdNotice();
        // Any well-formed https origin works for the embed; claiming to be
        // youtube.com itself gets rejected by the player.
        webView.loadDataWithBaseURL(
                "https://seniorconnect.invalid",
                readAsset("player.html"),
                "text/html",
                "utf-8",
                null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Screen lock, home, or task switch all end the session; re-entering
        // starts fresh. No state survives.
        finish();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void showAdNotice() {
        runOnUiThread(() -> adNotice.setVisibility(View.VISIBLE));
    }

    /**
     * Builds the player source for this device's country (or the default):
     * {"playlistId": "..."} when a maintained public YouTube playlist is
     * configured — editable on YouTube without an app update — otherwise
     * {"videos": [...]} from the bundled list, shuffled per session.
     */
    private String loadSourceForCountry() {
        try {
            JSONObject root = new JSONObject(readAsset("playlists/playlists.json"));
            JSONObject countries = root.getJSONObject("countries");
            String country = Locale.getDefault().getCountry().toUpperCase(Locale.ROOT);
            if (!countries.has(country)) {
                country = root.getString("default_country");
            }
            JSONObject entry = countries.getJSONObject(country);
            JSONObject source = new JSONObject();
            String playlistId = entry.optString("playlist_id", "");
            if (!playlistId.isEmpty()) {
                source.put("playlistId", playlistId);
            } else {
                JSONArray videos = entry.getJSONArray("videos");
                List<String> ids = new ArrayList<>();
                for (int i = 0; i < videos.length(); i++) {
                    ids.add(videos.getJSONObject(i).getString("id"));
                }
                Collections.shuffle(ids);
                source.put("videos", new JSONArray(ids));
            }
            return source.toString();
        } catch (Exception e) {
            return "{\"videos\":[]}";
        }
    }

    private String readAsset(String name) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open(name), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            return "";
        }
        return sb.toString();
    }

    private final class Bridge {
        @JavascriptInterface
        public String getSourceJson() {
            return sourceJson;
        }

        /** Content clock advancing past ~1s means the actual video (not an ad)
         *  is playing, so the passive ad notice can come down. */
        @JavascriptInterface
        public void onContentProgress(double seconds) {
            if (seconds > 1.0) {
                runOnUiThread(() -> adNotice.setVisibility(View.GONE));
            }
        }

        @JavascriptInterface
        public void onVideoChanged() {
            showAdNotice();
        }

        @JavascriptInterface
        public void onAllVideosFailed() {
            runOnUiThread(() -> {
                adNotice.setVisibility(View.GONE);
                unavailableNotice.setVisibility(View.VISIBLE);
            });
        }
    }
}
