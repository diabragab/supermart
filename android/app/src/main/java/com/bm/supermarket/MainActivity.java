package com.bm.supermarket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.os.CancellationSignal;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import org.json.JSONObject;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setStatusBarColor(Color.parseColor("#089468"));
        w.setNavigationBarColor(Color.parseColor("#F4F6F5"));
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            w.getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0AA06E"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                String scheme = u.getScheme();
                if ("tel".equals(scheme) || "mailto".equals(scheme) || "http".equals(scheme) || "https".equals(scheme)) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                    return true;
                }
                return false;
            }
        });
        web.addJavascriptInterface(new NativeBridge(), "BMNative");
        setContentView(web);
        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        web.evaluateJavascript("window.appBack?window.appBack():false", value -> {
            if (!"true".equals(value)) MainActivity.super.onBackPressed();
        });
    }

    /** Called from the web app: window.BMNative.googleSignIn(webClientId) */
    private class NativeBridge {
        @JavascriptInterface
        public void googleSignIn(final String webClientId) {
            runOnUiThread(() -> startGoogleSignIn(webClientId));
        }
    }

    private void startGoogleSignIn(String webClientId) {
        GetSignInWithGoogleOption option = new GetSignInWithGoogleOption.Builder(webClientId).build();
        GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(option).build();
        CredentialManager.create(this).getCredentialAsync(this, request, new CancellationSignal(),
            ContextCompat.getMainExecutor(this),
            new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse response) {
                    Credential c = response.getCredential();
                    if (c instanceof CustomCredential
                            && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(c.getType())) {
                        try {
                            sendGoogle(GoogleIdTokenCredential.createFrom(c.getData()).getIdToken(), null);
                        } catch (Exception e) {
                            sendGoogle(null, "bad token");
                        }
                    } else {
                        sendGoogle(null, "unexpected credential");
                    }
                }

                @Override
                public void onError(GetCredentialException e) {
                    String code = e instanceof GetCredentialCancellationException ? "cancel"
                        : e instanceof NoCredentialException ? "noaccount"
                        : e.getClass().getSimpleName() + " " + e.getMessage();
                    sendGoogle(null, code);
                }
            });
    }

    private void sendGoogle(String token, String error) {
        String js = "window.bmGoogle&&window.bmGoogle(" + (token == null ? "null" : JSONObject.quote(token))
            + "," + (error == null ? "null" : JSONObject.quote(error)) + ")";
        web.post(() -> web.evaluateJavascript(js, null));
    }
}
