package com.example.shiufai.sftoolsandroid;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements FragmentOne.FragmentOneListener, FragmentTwo.FragmentTwoListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnButton1 = (Button)findViewById(R.id.button);
        Button btnButton2 = (Button)findViewById(R.id.button2);

        if(btnButton1 != null)
        {
            btnButton1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Fragment fragment;
                    fragment = new FragmentOne();
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_place, fragment);
                    ft.commit();
                }
            });
        }

        if(btnButton2 != null)
        {
            btnButton2.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Fragment fragment;
                    fragment = new FragmentTwo();
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_place, fragment);
                    ft.commit();
                }
            });
        }

    }

    @Override
    public void onFragmentMessage(String TAG, Object data) {
        if (TAG.equals("Fragment1")){
            //Do something with 'data' that comes from fragment1
        }
        else if (TAG.equals("Fragment2")){
            //Do something with 'data' that comes from fragment2
        }
    }
}
