package com.archive.inventory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.provider.OpenableColumns;
import android.database.Cursor;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int SAVE_FILE_REQUEST = 1002;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private byte[] pendingSaveBytes;
    private String pendingSaveMime = "text/csv";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/octet-stream"});
                startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                return true;
            }
        });
        if (savedInstanceState == null) webView.loadUrl("file:///android_asset/archive_inventory.html");
        else webView.restoreState(savedInstanceState);
    }

    private class AndroidBridge {
        @JavascriptInterface public void saveFile(String filename, String base64, String mime) {
            try {
                pendingSaveBytes = Base64.decode(base64, Base64.DEFAULT);
                pendingSaveMime = (mime == null || mime.isEmpty()) ? "text/csv" : mime;
                runOnUiThread(() -> {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(pendingSaveMime);
                    intent.putExtra(Intent.EXTRA_TITLE, filename == null ? "archive_export.csv" : filename);
                    startActivityForResult(intent, SAVE_FILE_REQUEST);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "تعذر تجهيز الملف", Toast.LENGTH_LONG).show());
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback != null) {
                Uri[] result = resultCode == RESULT_OK && data != null && data.getData() != null ? new Uri[]{data.getData()} : null;
                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            }
        } else if (requestCode == SAVE_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                if (out != null && pendingSaveBytes != null) out.write(pendingSaveBytes);
                Toast.makeText(this, "تم حفظ الملف", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "تعذر حفظ الملف", Toast.LENGTH_LONG).show();
            } finally { pendingSaveBytes = null; }
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) webView.saveState(outState);
    }
}
