package cl.craftcloud.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.view.ViewGroup;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String LOCAL = "file:///android_asset/index.html";
    private static final int PICK_FILES = 440;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(11, 18, 32));
        getWindow().setNavigationBarColor(Color.rgb(11, 18, 32));
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(11, 18, 32));
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(new CatalogBridge(), "CraftCloudNative");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri uri = req.getUrl();
                String url = uri.toString();
                if (url.startsWith("file:///android_asset/")) return false;
                if ("https".equalsIgnoreCase(uri.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                    catch (Exception ignored) {}
                }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                try {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, PICK_FILES);
                    return true;
                } catch (Exception e) {
                    fileCallback = null;
                    cb.onReceiveValue(null);
                    return false;
                }
            }
        });
        setContentView(web, new ViewGroup.LayoutParams(-1, -1));
        web.loadUrl(LOCAL);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_FILES || fileCallback == null) return;
        Uri[] result = null;
        if (resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                result = new Uri[count];
                for (int i = 0; i < count; i++) result[i] = data.getClipData().getItemAt(i).getUri();
            } else if (data.getData() != null) {
                result = new Uri[]{data.getData()};
            }
        }
        fileCallback.onReceiveValue(result);
        fileCallback = null;
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    /** The bridge is available only to the bundled local page: outside HTTPS links open in the system browser. */
    public final class CatalogBridge {
        @JavascriptInterface public void searchMods(String query, String version, String loader, int requestId) {
            new Thread(() -> {
                JSONObject out = new JSONObject();
                try {
                    out.put("requestId", requestId);
                    String q = query == null ? "" : query.trim();
                    String v = version == null ? "" : version.trim();
                    String l = loader == null ? "" : loader.trim();
                    if (q.length() < 2 || q.length() > 70 || !v.matches("[0-9A-Za-z.\\-]{0,24}")
                        || !(l.isEmpty() || l.equals("fabric") || l.equals("forge") || l.equals("neoforge") || l.equals("quilt")))
                        throw new Exception("Verifica los filtros de búsqueda.");
                    JSONArray facets = new JSONArray().put(new JSONArray().put("project_type:mod"));
                    if (!v.isEmpty()) facets.put(new JSONArray().put("versions:" + v));
                    if (!l.isEmpty()) facets.put(new JSONArray().put("categories:" + l));
                    String url = "https://api.modrinth.com/v2/search?limit=12&query="
                        + URLEncoder.encode(q, "UTF-8") + "&facets="
                        + URLEncoder.encode(facets.toString(), "UTF-8");
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(12000);
                    conn.setRequestProperty("User-Agent", "CraftCloud-Android/0.5 (catalog-readonly)");
                    conn.setRequestProperty("Accept", "application/json");
                    try {
                        if (conn.getResponseCode() != 200) throw new Exception("El catálogo respondió HTTP " + conn.getResponseCode());
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        try (InputStream in = conn.getInputStream()) {
                            byte[] buf = new byte[8192];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                stream.write(buf, 0, len);
                                if (stream.size() > 350000) throw new Exception("Respuesta demasiado grande.");
                            }
                        }
                        JSONObject response = new JSONObject(stream.toString(StandardCharsets.UTF_8.name()));
                        JSONArray results = new JSONArray();
                        JSONArray hits = response.optJSONArray("hits");
                        if (hits != null) for (int i = 0; i < Math.min(12, hits.length()); i++) {
                            JSONObject hit = hits.getJSONObject(i);
                            JSONObject item = new JSONObject();
                            item.put("id", hit.optString("project_id"));
                            item.put("name", hit.optString("title"));
                            item.put("description", hit.optString("description"));
                            item.put("slug", hit.optString("slug"));
                            results.put(item);
                        }
                        out.put("items", results);
                    } finally { conn.disconnect(); }
                } catch (Exception e) {
                    try { out.put("error", e.getMessage() == null ? "Error del catálogo." : e.getMessage()); }
                    catch (Exception ignored) {}
                }
                String encoded = JSONObject.quote(out.toString());
                ui.post(() -> {
                    if (web != null && LOCAL.equals(web.getUrl()))
                        web.evaluateJavascript("window.onCatalogReply(" + encoded + ")", null);
                });
            }).start();
        }
    }
}
