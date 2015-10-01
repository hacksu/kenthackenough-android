package io.khe.kenthackenough.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import io.khe.kenthackenough.Config;
import io.khe.kenthackenough.KHEApp;
import io.khe.kenthackenough.R;
import io.khe.kenthackenough.backend.AboutManager;

public class AboutFragment extends Fragment {
    private WebView mAboutWebView;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        mAboutWebView = (WebView) view.findViewById(R.id.about);
        WebSettings webSettings = mAboutWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mAboutWebView.loadUrl(Config.INFO_URL);


        AboutManager aboutManager = ((KHEApp) getActivity().getApplication()).aboutManager;
        aboutManager.addListener(new AboutManager.AboutUpdateListener() {
            @Override
            public void aboutUpdated(AboutManager.About about) {
                Log.i(Config.DEBUG_TAG, "got response" + about.formatted);
                mAboutWebView.loadUrl(Config.INFO_URL);
            }
        });
        return view;
    }

}
