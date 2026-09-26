package com.arogyasevak.app;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Prints an HTML document using Android's system print dialog.
 * window.print() does nothing inside a WebView, so the app calls this
 * plugin instead when running natively.
 */
@CapacitorPlugin(name = "Print")
public class PrintPlugin extends Plugin {
    private WebView printWebView;
    private boolean printStarted;

    @PluginMethod
    public void print(PluginCall call) {
        String html = call.getString("html", "");
        String jobName = call.getString("jobName", "Arogya Sevak Report");
        if (html == null || html.isEmpty()) {
            call.reject("Empty HTML document");
            return;
        }
        getActivity().runOnUiThread(() -> {
            try {
                printStarted = false;
                // Strong reference so the WebView survives until the print job starts.
                printWebView = new WebView(getContext());
                printWebView.getSettings().setJavaScriptEnabled(false);
                printWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (printStarted) return;
                        printStarted = true;
                        try {
                            PrintManager printManager =
                                    (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);
                            PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(jobName);
                            PrintAttributes attrs = new PrintAttributes.Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                    .build();
                            printManager.print(jobName, adapter, attrs);
                            call.resolve();
                        } catch (Exception e) {
                            call.reject("Print failed: " + e.getMessage());
                        }
                    }
                });
                printWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            } catch (Exception e) {
                call.reject("Print failed: " + e.getMessage());
            }
        });
    }
}
