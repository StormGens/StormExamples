package com.example.fragmentbasicss;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

public class FragmentBasicsS extends FragmentActivity implements TitleFragment.OnTitleItemSelectedListenter{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_basics_s);
        
        TitleFragment titlesF=new TitleFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.contener,titlesF).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_basics, menu);
        return true;
    }

    @Override
    public void onItemSelected(int position) {
        // TODO Auto-generated method stub
        ArticleDetailFragment newFragment=new ArticleDetailFragment();
        Bundle bundle=new Bundle();
        bundle.putInt("position",position);
        newFragment.setArguments(bundle);
       FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
       transaction.replace(R.id.contener, newFragment);
       transaction.addToBackStack(null);
       transaction.commit();
    }

}
