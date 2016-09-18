package com.example.mobliescanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileSelector.FSDelegate} interface
 * to handle interaction events.
 * Use the {@link FileSelector#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileSelector extends Fragment {
    private static String LOG_TAG = "FileSelector";

    //region [-Interface-]
    public interface FSDelegate
    {
        // TODO: Update argument type and name
        void onClickOK(ArrayList<String> selectedFiles);
        void onClickCancel();
    }
    public FSDelegate m_delegate;
    //endregion

    //region [-View Holder-]
    public static class FSViewHolder
    {
        ListView m_FileLV;
        Button m_BtnOK;
        Button m_BtnCancel;
        View m_emptyView;
    }
    //endregion

    //region [-Params-]
    public final static String EXTRA_FILE_PATH = "file_path";
    public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";
    public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";
    protected FSViewHolder m_FileSelectorVH;
    protected ArrayList<File> m_Files;
    protected FileSelectorAdapter m_FSAdapter;
    protected File m_FSDirectory;
    protected String[] acceptedFileExtensions;
    protected boolean ShowHiddenFiles = false;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Context mContext;
    private final static String DEFAULT_INITIAL_DIRECTORY = "/";
    //endregion

    ///////////////////////////////////////////////////////////////////////////
    public FileSelector()
    {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static FileSelector newInstance(String param1, String param2)
    {
        FileSelector fragment = new FileSelector();
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
        if (context instanceof FSDelegate)
        {
            mContext = context;
            if(m_FileSelectorVH == null)
            {
                m_FileSelectorVH = new FSViewHolder();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_file_selector, null);

        m_FileSelectorVH = new FSViewHolder();
        m_FileSelectorVH.m_FileLV = (ListView)v.findViewById(R.id.listView);
        m_FileSelectorVH.m_BtnOK = (Button)v.findViewById(R.id.btnOK);
        m_FileSelectorVH.m_BtnCancel = (Button)v.findViewById(R.id.btnCancel);

        // ListView Init
        m_FileSelectorVH.m_emptyView = inflater.inflate(R.layout.empty_view, null);
        m_FileSelectorVH.m_FileLV.setEmptyView(m_FileSelectorVH.m_emptyView);
        if(m_Files == null)
            m_Files = new ArrayList<File>();
        m_FSAdapter = new FileSelectorAdapter(getActivity(), m_Files);
        m_FileSelectorVH.m_FileLV.setAdapter(m_FSAdapter);
                m_FileSelectorVH.m_FileLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//        m_FileSelectorVH.m_FileLV.setItemsCanFocus(false);
        // Set initial directory
        m_FSDirectory = new File(DEFAULT_INITIAL_DIRECTORY);
        // Initialize the extensions array to allow any file extensions
        acceptedFileExtensions = new String[] {};
        // Get intent extras
        if(getActivity().getIntent().hasExtra(EXTRA_FILE_PATH))
            m_FSDirectory = new File(getActivity().getIntent().getStringExtra(EXTRA_FILE_PATH));
        if(getActivity().getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES))
            ShowHiddenFiles = getActivity().getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);
        if(getActivity().getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS))
        {
            ArrayList<String> collection =
                    getActivity().getIntent().getStringArrayListExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);

            acceptedFileExtensions = (String[])
                    collection.toArray(new String[collection.size()]);
        }
        m_FileSelectorVH.m_FileLV.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> partent, View v, int position, long id)
            {
                File newFile = (File)m_FileSelectorVH.m_FileLV.getItemAtPosition(position);
                SparseBooleanArray checked = m_FileSelectorVH.m_FileLV.getCheckedItemPositions();
                if(newFile.isFile()) {
                    Intent extra = new Intent();
                    extra.putExtra(EXTRA_FILE_PATH, newFile.getAbsolutePath());
//                    setResult(RESULT_OK, extra);
//                    finish();
                }
                else {
                    m_FSDirectory = newFile;
                    refreshFilesList();
//                    m_FileSelectorVH.m_FileLV.setItemChecked(-1, true);
                    m_FileSelectorVH.m_FileLV.clearChoices();
                    m_FileSelectorVH.m_FileLV.requestLayout();
                }
            }
        });

        // Ok button
        m_FileSelectorVH.m_BtnOK.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SparseBooleanArray checked = m_FileSelectorVH.m_FileLV.getCheckedItemPositions();
                if (checked == null)
                    return;
                ArrayList<String> selectedItems = new ArrayList<String>();
                for (int i = 0; i < checked.size(); i++)
                {
                    int position = checked.keyAt(i);
                    if (checked.valueAt(i))
                    {
                        String item = m_FileSelectorVH.m_FileLV.getAdapter().
                                getItem(position).toString();
                        selectedItems.add(item);
                        Log.i(LOG_TAG,item + " was selected");
                    }
                }
                if (m_delegate != null)
                {
                    m_delegate.onClickOK(selectedItems);
                }
//                getActivity().onBackPressed();
            }
        });

        // Cancel button
        m_FileSelectorVH.m_BtnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (m_delegate != null)
                {
                    m_delegate.onClickCancel();
                }
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
        refreshFilesList();
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


    //endregion

    //-------------------------------------------------------------------------
    //region [-Public-]
    //-------------------------------------------------------------------------
    public void popBackStack()
    {
        if(m_FSDirectory.getParentFile() != null)
        {
            m_FSDirectory = m_FSDirectory.getParentFile();
            refreshFilesList();
            return;
        }
    }
    //endregion
    public void onBackPressed()
    {
        if(m_FSDirectory.getParentFile() != null) {

            m_FSDirectory = m_FSDirectory.getParentFile();
            refreshFilesList();
        }
    }
    //-------------------------------------------------------------------------
    //region [-Private-]
    //-------------------------------------------------------------------------
    private class FileSelectorAdapter extends ArrayAdapter<File>
    {
        private List<File> mObjects;

        public FileSelectorAdapter(Context context, List<File> objects)
        {
//            super(context, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, objects);
            super(context, R.layout.list_item, objects);
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View row = null;

            if(convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.list_item, parent, false);
            }
            else
                row = convertView;

            File object = mObjects.get(position);

            ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
            TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
            CheckBox checkBox = (CheckBox)row.findViewById(R.id.myCheckBox);
            textView.setSingleLine(true);
            textView.setText(object.getName());

            if(object.isFile())
                imageView.setImageResource(R.drawable.file);

            else
                imageView.setImageResource(R.drawable.folder);

            if(checkBox.isChecked())
                checkBox.setChecked(false);
//            checkBox.setVisibility(View.GONE);
            return row;
        }
    }
    private class ExtensionFilenameFilter implements FilenameFilter
    {
        private String[] Extensions;

        public ExtensionFilenameFilter(String[] extensions)
        {
            super();
            Extensions = extensions;
        }

        public boolean accept(File dir, String filename)
        {
            if(new File(dir, filename).isDirectory())
            {
                // Accept all directory names
                return true;
            }

            if(Extensions != null && Extensions.length > 0)
            {
                for(int i = 0; i < Extensions.length; i++)
                {
                    if(filename.endsWith(Extensions[i]))
                    {
                        // The filename ends with the extension
                        return true;
                    }
                }
                // The filename did not match any of the extensions
                return false;
            }
            // No extensions has been set. Accept all file extensions.
            return true;
        }
    }
    private class FileComparator implements Comparator<File>
    {
        public int compare(File f1, File f2)
        {
            if(f1 == f2)
                return 0;

            if(f1.isDirectory() && f2.isFile())
                // Show directories above files
                return -1;

            if(f1.isFile() && f2.isDirectory())
                // Show files below directories
                return 1;

            // Sort the directories alphabetically
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }
    private void refreshFilesList()
    {
        m_Files.clear();
        ExtensionFilenameFilter filter = new ExtensionFilenameFilter(acceptedFileExtensions);

        File[] files = m_FSDirectory.listFiles(filter);

        if(files != null && files.length > 0)
        {
            for(File f : files)
            {
                if(f.isHidden() && !ShowHiddenFiles)
                {
                    continue;
                }
                m_Files.add(f);
            }

            Collections.sort(m_Files, new FileComparator());
        }

        m_FSAdapter.notifyDataSetChanged();
    }
    //endregion

}
