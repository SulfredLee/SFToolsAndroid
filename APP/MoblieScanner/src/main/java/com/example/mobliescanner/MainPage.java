package com.example.mobliescanner;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainPage.MFDelegate} interface
 * to handle interaction events.
 * Use the {@link MainPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainPage extends Fragment {
    private static String LOG_TAG = "MainFragment";

    //region [-Interface-]
    public interface MFDelegate
    {
        void onAddPhoto();
    }
    public MFDelegate m_delegate;
    //endregion

    //region [-View Holder-]
    public static class MFViewHolder
    {
        Button btnAddPhoto;
        Button btnCamera;
        ListView lstPhotoList;
        Button btnStart;
        Button btnClear;
        Button btnRemove;
    }
    //endregion

    //region [-Params-]
    public ArrayList<String> m_FilesInFolder;
    public ArrayAdapter<String> m_FileAdapter;
    protected MFViewHolder m_MainFragmentVH;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String m_Param1;
    private String m_Param2;
    private Context m_Context;
    private Uri m_imageUri;
    private static final int TAKE_PICTURE = 5;
    //endregion

    ///////////////////////////////////////////////////////////////////////////
    public MainPage()
    {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static MainPage newInstance(String param1, String param2)
    {
        MainPage fragment = new MainPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    //region [-Fragment Life cycle-]
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof MFDelegate)
        {
            m_Context = context;
            if(m_MainFragmentVH == null)
            {
                m_MainFragmentVH = new MFViewHolder();
            }
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            m_Param1 = getArguments().getString(ARG_PARAM1);
            m_Param2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_main_page, null);

        m_MainFragmentVH = new MFViewHolder();
        m_MainFragmentVH.btnAddPhoto = (Button)v.findViewById(R.id.btnAddPhoto);
        m_MainFragmentVH.btnCamera = (Button)v.findViewById(R.id.btnCamera);
        m_MainFragmentVH.lstPhotoList = (ListView)v.findViewById(R.id.lstPhotoList);
        m_MainFragmentVH.btnStart = (Button)v.findViewById(R.id.btnStart);
        m_MainFragmentVH.btnClear = (Button)v.findViewById(R.id.btnClear);
        m_MainFragmentVH.btnRemove = (Button)v.findViewById(R.id.btnRemove);

        m_FilesInFolder = new ArrayList<String>();
        m_FileAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, m_FilesInFolder);
        m_MainFragmentVH.lstPhotoList.setAdapter(m_FileAdapter);
        m_MainFragmentVH.lstPhotoList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        m_MainFragmentVH.btnAddPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(m_delegate != null)
                {
                    m_delegate.onAddPhoto();
                }
            }
        });

        m_MainFragmentVH.btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photo));
                m_imageUri = Uri.fromFile(photo);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        });

        m_MainFragmentVH.btnClear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_FileAdapter.clear();
                m_FileAdapter.notifyDataSetChanged();
            }
        });

        m_MainFragmentVH.btnStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PhotoScanner phScan = new PhotoScanner();
                phScan.m_Photos = new ArrayList<String>();

                int count = m_MainFragmentVH.lstPhotoList.getCount();
                for(int i = 0; i < count; i++)
                {
                    String photoName = (String) m_MainFragmentVH.lstPhotoList.getItemAtPosition(i);
                    phScan.m_Photos.add(photoName);
                }
                phScan.m_shiftValue = 8;
                phScan.m_outputPath = "/storage/sdcard0/DCIM/Camera/";

                phScan.StartScanning();
            }
        });
        return v;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onStart()
    {
        super.onStart();
    }
    @Override
    public void onResume()
    {
        super.onResume();
    }
    @Override
    public void onPause()
    {
        super.onPause();
    }
    @Override
    public void onStop()
    {
        super.onStop();
    }
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    @Override
    public void onDetach()
    {
        super.onDetach();
        m_delegate = null;
    }
    //endregion

    //-------------------------------------------------------------------------
    //region [-Delegate-]
    //-------------------------------------------------------------------------

    //endregion

    //-------------------------------------------------------------------------
    //region [-Override-]
    //-------------------------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri selectedImage = m_imageUri;
                    getActivity().getContentResolver().notifyChange(selectedImage, null);
                    //ImageView imageView = (ImageView) findViewById(R.id.ImageView);
                    ContentResolver cr = getActivity().getContentResolver();
                    Bitmap bitmap;
                    FileOutputStream out = null;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        String filename = "/storage/sdcard0/DCIM/Camera/";
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        Date now = new Date();
                        filename += formatter.format(now) + ".jpg";
                        out = new FileOutputStream(filename);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        //imageView.setImageBitmap(bitmap);
                        Toast.makeText(getActivity(), filename, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Failed to load",
                                Toast.LENGTH_SHORT).show();
                        Log.e("Camera", e.toString());
                    }finally {
                        try{
                            if(out != null)
                            {
                                out.close();
                            }
                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
        }

    }
    //endregion

    //-------------------------------------------------------------------------
    //region [-Public-]
    //-------------------------------------------------------------------------

    //endregion

    //-------------------------------------------------------------------------
    //region [-Private-]
    //-------------------------------------------------------------------------

    //endregion
}
