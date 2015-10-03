package io.khe.kenthackenough.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import io.khe.kenthackenough.Config;
import io.khe.kenthackenough.KHEApp;
import io.khe.kenthackenough.R;
import io.khe.kenthackenough.backend.About.AboutManager;

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

        // only navigate if we have to
        if(!Config.INFO_URL.equals(mAboutWebView.getUrl())) {
            mAboutWebView.loadUrl(Config.INFO_URL);
        }


        AboutManager aboutManager = ((KHEApp) getActivity().getApplication()).aboutManager;
        aboutManager.addListener(new AboutManager.AboutUpdateListener() {
            @Override
            public void aboutUpdated(AboutManager.About about) {
                Log.i(Config.DEBUG_TAG, "got response" + about.formatted);
                mAboutWebView.reload();
            }
        });
        return view;
    }

}
