package com.example.fragmentbasicss;


import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TitleFragment extends ListFragment {
    
    private OnTitleItemSelectedListenter mListener;
    
    public interface OnTitleItemSelectedListenter{
        public void onItemSelected(int position);
    }
    
    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        try {
            mListener=(OnTitleItemSelectedListenter)activity;
        } catch (ClassCastException e) {
            // TODO: handle exception
            throw new ClassCastException(activity.toString()+" 必须实现（implement）OnTitleItemSelectedListenter");
        }
        super.onAttach(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
        setListAdapter(new ArrayAdapter<String>(getActivity(), layout, Ipsum.Headlines));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        mListener.onItemSelected(position);
    }
}
