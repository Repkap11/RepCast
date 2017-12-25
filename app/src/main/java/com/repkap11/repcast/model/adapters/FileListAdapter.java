package com.repkap11.repcast.model.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.fragments.RepcastFragment;
import com.repkap11.repcast.model.filters.FileListFilter;
import com.repkap11.repcast.model.parcelables.JsonDirectory;
import com.repkap11.repcast.model.rest.JsonDirectoryDownloader;
import com.repkap11.repcast.utils.Utils;


/**
 * Created by paul on 9/10/15.
 */
public class FileListAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = FileListAdapter.class.getSimpleName();
    private final JsonDirectory.JsonFileDir mDir;
    private FileListFilter mFilter;
    private RepcastFragment mFragment;
    private JsonDirectory mFileList;
    private boolean mAllowClicks = false;

    public FileListAdapter(RepcastFragment fragment, JsonDirectory.JsonFileDir dir) {
        mFileList = new JsonDirectory();
        mFilter = new FileListFilter(mFileList, this);
        mDir = dir;
        refreshContent(fragment);
    }

    public void refreshContent(RepcastFragment fragment) {
        mAllowClicks = false;
        JsonDirectoryDownloader downloader = new JsonDirectoryDownloader(this);
        downloader.execute(Utils.getDirGetURL(fragment.getContext()) + mDir.key);
    }

    public void updateContext(RepcastFragment fragment) {
        mFragment = fragment;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mFileList == null) {
            return 0;
        }
        return mFileList.info.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.info.get(position);
    }

    private static final long ICON_DIR = -2;
    private static final long ICON_UNKNOWN = -1;

    public static class IconMimeMap {
        public int mIconResource;
        public String mMime;

        public IconMimeMap(String mime, int iconResource) {
            mMime = mime;
            mIconResource = iconResource;
        }
    }

    private static IconMimeMap[] MIME_ARRAY;

    static {
        MIME_ARRAY = new IconMimeMap[]{
                new IconMimeMap("video/mp4", R.drawable.mp4),
                new IconMimeMap("audio/mpeg", R.drawable.mp3),
                new IconMimeMap("application/epub+zip", R.drawable.epub),
                new IconMimeMap("image/jpeg", R.drawable.jpg),
                new IconMimeMap("image/png", R.drawable.png),
                new IconMimeMap("application/pdf", R.drawable.pdf),
                new IconMimeMap("text/plain", R.drawable.txt),
                new IconMimeMap("video/x-matroska", R.drawable.mkv),
        };
    }

    @Override
    public long getItemId(int position) {
        JsonDirectory.JsonFileDir dir = mFileList.info.get(position);
        if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_DIR)) {
            return ICON_DIR;
        } else {
            for (int i = 0; i < MIME_ARRAY.length; i++) {
                if (MIME_ARRAY[i].mMime.equals(dir.mimetype)) {
                    return MIME_ARRAY[i].mIconResource;
                }
            }
        }
        return ICON_UNKNOWN;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        int iconResource = (int) getItemId(position);
        JsonDirectory.JsonFileDir result = mFileList.info.get(position);
        if (convertView == null) {
            int layout = R.layout.fragment_selectfile_list_element;
            convertView = LayoutInflater.from(mFragment.getActivity()).inflate(layout, parent, false);
            holder = new Holder();
            holder.mName = (TextView) convertView.findViewById(R.id.fragment_selectfile_list_element_name);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.fragment_selectfile_list_element_icon);
            convertView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mName.setText(result.name);
        holder.mIndex = position;
        if (iconResource == ICON_DIR) {
            holder.mIcon.setImageResource(R.drawable.folder);
        } else if (iconResource == ICON_UNKNOWN) {
            holder.mIcon.setImageResource(R.drawable.question_mark);
        } else {
            holder.mIcon.setImageResource(iconResource);
        }

        return convertView;
    }

    public void updateFileList(JsonDirectory fileList, boolean isFiltered) {
        //Log.e(TAG, "File list changed on"+this);
        mFileList = fileList;
        if (!isFiltered) {
            mFilter = new FileListFilter(mFileList, this);
            mFragment.setShouldProgressBeShown(false);
        }
        if (mFileList == null) {
            if (mFragment.getActivity() != null) {
                Toast.makeText(mFragment.getActivity().getApplicationContext(), "Unable to read file data from Repkam09.com", Toast.LENGTH_SHORT).show();
            }
        }
        mAllowClicks = true;
        mFragment.notifyNotRefreshing();//This is ued in the SelectFileFragment
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (!mAllowClicks) {
            return;
        }
        Holder h = (Holder) v.getTag();
        JsonDirectory.JsonFileDir dir = mFileList.info.get(h.mIndex);
        if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_DIR)) {
            ((RepcastActivity) mFragment.getActivity()).showContent(dir);
        } else if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_FILE)) {
            ((RepcastActivity) mFragment.getActivity()).showFile(dir);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!mAllowClicks) {
            return false;
        }
        Holder h = (Holder) v.getTag();
        JsonDirectory.JsonFileDir dir = mFileList.info.get(h.mIndex);
        if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_DIR)) {
            return false;
        } else if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_FILE)) {
            Log.e(TAG,"Long press");
            ((RepcastActivity) mFragment.getActivity()).openDownloadDialog(dir);
            return true;
        }
        return false;
    }

    public Filter getFilter() {
        return mFilter;
    }

    public class Holder {
        public TextView mName;
        public ImageView mIcon;
        public int mIndex;
    }
}
