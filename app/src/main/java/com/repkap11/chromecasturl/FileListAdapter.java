package com.repkap11.chromecasturl;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.repkap11.chromecasturl.activity.SelectFileActivity;
import com.repkap11.chromecasturl.model.JsonDirectory;
import com.repkap11.chromecasturl.model.JsonDirectoryDownloader;

/**
 * Created by paul on 9/10/15.
 */
public class FileListAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String TAG = FileListAdapter.class.getSimpleName();
    private final String mURL;
    private SelectFileActivity mActivity;
    private JsonDirectory mFileList;

    public FileListAdapter(String path64) {
        mURL = "https://repkam09.com/dl/dirget/" + path64;
        mFileList = new JsonDirectory();
        JsonDirectoryDownloader downloader = new JsonDirectoryDownloader(this);
        downloader.execute(mURL);
    }

    public void updateContext(SelectFileActivity activity) {
        mActivity = activity;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mFileList == null){
            return 0;
        }
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
        long itemID = getItemId(position);
        JsonDirectory.JsonFileDir result = mFileList.result.get(position);
        if (convertView == null) {
            int layout;
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
            holder.mIcon = (ImageView) convertView.findViewById(R.id.fragment_selectfile_list_element_icon);
            convertView.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mName.setText(result.name);
        holder.mIndex = position;
        int iconResource;
        if (itemID == TYPE_DIR) {
            iconResource = R.drawable.folder;
        } else if (itemID == TYPE_FILE) {
            iconResource = R.drawable.mp4;
        } else {
            throw new RuntimeException("Wrong ID");
        }
        holder.mIcon.setImageResource(iconResource);
        return convertView;
    }

    public void updataFileList(JsonDirectory fileList) {
        Log.e(TAG,"File list changed");
        mFileList = fileList;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        Holder h = (Holder) v.getTag();
        JsonDirectory.JsonFileDir dir = mFileList.result.get(h.mIndex);
        if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_DIR)) {
            mActivity.showListUsingDirectory(dir);
        } else if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_FILE)) {
            mActivity.showFile(dir);
        }
    }

    public class Holder {
        public TextView mName;
        public ImageView mIcon;
        public int mIndex;
    }
}
