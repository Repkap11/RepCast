package com.repkap11.repcast.model.filters;

import android.util.Log;
import android.widget.Filter;

import com.repkap11.repcast.model.adapters.FileListAdapter;
import com.repkap11.repcast.model.parcelables.JsonDirectory;


/**
 * Created by paul on 9/10/15.
 */
public class FileListFilter extends Filter {
    private static final String TAG = FileListFilter.class.getSimpleName();
    private final JsonDirectory mFiles;
    private final FileListAdapter mFileListAdapter;

    public FileListFilter(JsonDirectory files, FileListAdapter fileListAdapter) {
        super();
        mFiles = files;
        mFileListAdapter = fileListAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint == null || constraint.length() == 0) {
            results.values = mFiles;
            results.count = mFiles.info.size();
        } else {
            JsonDirectory resultDir = new JsonDirectory();

            for (JsonDirectory.JsonFileDir file : mFiles.info) {
                if (file.name.toUpperCase().contains(constraint.toString().toUpperCase())) {
                    resultDir.info.add(file);
                }
            }
            results.values = resultDir;
            results.count = resultDir.info.size();
        }
        //Log.e(TAG,"Searching Returned "+results.count);
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        boolean filtered = results.count != mFiles.info.size();
        // Now we have to inform the adapter about the new list filtered
        //if (results.count == 0) {
        //    mFileListAdapter.notifyDataSetInvalidated();
        //} else {
            mFileListAdapter.updateFileList((JsonDirectory) results.values, filtered);
        //}
    }

}
