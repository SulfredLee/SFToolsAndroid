package com.example.mobliescanner;

import android.app.Fragment;
//import android.support.v4.app.FragmentManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainPage.MFDelegate, FileSelector.FSDelegate{
    //region [-Params-]
    public MainPage m_MainPage;
    public FileSelector m_FileSelector;
    public MAViewHolder m_MainActivityVH;
    public FragmentManager m_FM;
    //endregion

    //region [-View Holder-]
    public static class MAViewHolder
    {

    }
    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        m_MainPage = new MainPage();
        m_MainPage.m_delegate = this;
        m_FileSelector = new FileSelector();
        m_FileSelector.m_delegate = this;

        m_FM = getFragmentManager();
        m_FM.beginTransaction().add(R.id.fragment, m_MainPage, "MainPage").commit();
        m_FM.beginTransaction().add(R.id.fragment, m_FileSelector, "FileSelector").commit();
        m_FM.executePendingTransactions();
        m_FM.beginTransaction().show(m_FM.findFragmentByTag("MainPage")).commit();
        m_FM.beginTransaction().hide(m_FM.findFragmentByTag("FileSelector")).commit();
    }

    //-------------------------------------------------------------------------
    //region [-Override-]
    //-------------------------------------------------------------------------
    @Override
    public void onAddPhoto()
    {
        m_FM.beginTransaction().show(m_FM.findFragmentByTag("FileSelector")).commit();
        m_FM.beginTransaction().hide(m_FM.findFragmentByTag("MainPage")).commit();
    }
    @Override
    public void onClickOK(ArrayList<String> selectedFiles)
    {
        m_FM.beginTransaction().show(m_FM.findFragmentByTag("MainPage")).commit();
        m_FM.beginTransaction().hide(m_FM.findFragmentByTag("FileSelector")).commit();
        m_MainPage.m_FilesInFolder.clear();
        for(String photo : selectedFiles)
        {
            m_MainPage.m_FilesInFolder.add(photo);
        }
        m_MainPage.m_FileAdapter.notifyDataSetChanged();
    }
    @Override
    public void onClickCancel()
    {
        m_FM.beginTransaction().show(m_FM.findFragmentByTag("MainPage")).commit();
        m_FM.beginTransaction().hide(m_FM.findFragmentByTag("FileSelector")).commit();
    }
    @Override
    public void onBackPressed() {

        if(m_FM.findFragmentByTag("FileSelector").isVisible())
        {
            m_FileSelector.onBackPressed();
            return;
        }


        super.onBackPressed();
    }
    //endregion
}
