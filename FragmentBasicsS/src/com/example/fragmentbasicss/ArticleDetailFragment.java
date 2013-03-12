package com.example.fragmentbasicss;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ArticleDetailFragment extends Fragment {
    int positon;
    TextView textView;
    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        positon=getArguments().getInt("position");
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return inflater.inflate(R.layout.fragment_article_detail, container, false);
    }
    
    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        textView=(TextView) getActivity().findViewById(R.id.article);
        textView.setText(Ipsum.Articles[positon]);
        super.onStart();
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
}
