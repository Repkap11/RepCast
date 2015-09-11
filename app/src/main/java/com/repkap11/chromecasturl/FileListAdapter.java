package com.repkap11.chromecasturl;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.repkap11.chromecasturl.model.JsonDirectory;
import com.repkap11.chromecasturl.model.JsonDirectoryDownloader;

/**
 * Created by paul on 9/10/15.
 */
public class FileListAdapter extends BaseAdapter {
    private final String mURL;
    private Context mActivity;
    private JsonDirectory mFileList;

    public FileListAdapter(String url) {
        mURL = url;
        mFileList = new JsonDirectory();
        JsonDirectoryDownloader downloader = new JsonDirectoryDownloader(this);
        downloader.execute(mURL);
    }

    public void updateContext(Activity activity) {
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return mFileList.result.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.result.get(position);
    }

    private static final long TYPE_DIR = 0;
    private static final long TYPE_FILE = 1;

    @Override
    public long getItemId(int position) {
        return mFileList.result.get(position).type.equals(JsonDirectory.JsonFileDir.TYPE_DIR) ? TYPE_DIR : TYPE_FILE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            int layout;
            long itemID = getItemId(position);
            if (itemID == TYPE_DIR) {
                layout = R.layout.fragment_selectfile_list_element;
            } else if (itemID == TYPE_FILE) {
                layout = R.layout.fragment_selectfile_list_element;
            } else {
                throw new RuntimeException("Wrong ID");
            }
            convertView = LayoutInflater.from(mActivity).inflate(layout, parent, false);
            holder = new Holder();
            holder.mName = (TextView) convertView.findViewById(R.id.fragment_selectfile_list_element_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mName.setText(mFileList.result.get(position).type);
        return convertView;
    }

    public void updataFileList(JsonDirectory fileList) {
        mFileList = fileList;
        notifyDataSetChanged();
    }

    public class Holder {
        public TextView mName;
    }
}
