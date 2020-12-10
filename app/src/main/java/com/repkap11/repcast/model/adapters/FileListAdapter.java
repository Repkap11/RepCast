package com.repkap11.repcast.model.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.repkap11.repcast.R;
import com.repkap11.repcast.VideoProvider;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.application.CastApplication;
import com.repkap11.repcast.fragments.RepcastFragment;
import com.repkap11.repcast.model.filters.FileListFilter;
import com.repkap11.repcast.model.parcelables.JsonDirectory;
import com.repkap11.repcast.model.rest.JsonDirectoryDownloader;
import com.repkap11.repcast.queue.QueueDataProvider;
import com.repkap11.repcast.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;


/**
 * Created by paul on 9/10/15.
 */
public class FileListAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = FileListAdapter.class.getSimpleName();
    private static final long ICON_DIR = -2;
    private static final long ICON_UNKNOWN = -1;
    private static IconMimeMap[] MIME_ARRAY;

    static {
        MIME_ARRAY = new IconMimeMap[]{
                new IconMimeMap("video/mp4", R.drawable.mp4, true),
                new IconMimeMap("audio/mpeg", R.drawable.mp3, true),
                new IconMimeMap("application/epub+zip", R.drawable.epub, false),
                new IconMimeMap("image/jpeg", R.drawable.jpg, false),
                new IconMimeMap("image/png", R.drawable.png, false),
                new IconMimeMap("application/pdf", R.drawable.pdf, false),
                new IconMimeMap("text/plain", R.drawable.txt, false),
                new IconMimeMap("video/x-matroska", R.drawable.mkv, true),
        };
    }

    private final JsonDirectory.JsonFileDir mDir;
    private final QueueDataProvider mQueueDataProvider;
    private final VideoCastConsumerImpl mCastListener;
    private FileListFilter mFilter;
    private RepcastFragment mFragment;
    private JsonDirectory mFileList;
    private boolean mAllowClicks = false;
    public FileListAdapter(RepcastFragment fragment, JsonDirectory.JsonFileDir dir) {
        mFileList = new JsonDirectory();
        mFilter = new FileListFilter(mFileList, this);
        mDir = dir;
        mQueueDataProvider = QueueDataProvider.getInstance();
        refreshContent(fragment);
        mCastListener = new VideoCastConsumerImpl(){
            @Override
            public void onConnected() {
                Log.e(TAG, "Paul onConnected: ");
                notifyDataSetChanged();
                super.onConnected();
            }

            @Override
            public void onDisconnected() {
                Log.e(TAG, "Paul onDisconnected: ");
                notifyDataSetChanged();
                super.onDisconnected();
            }
        };
        VideoCastManager.getInstance().addVideoCastConsumer(mCastListener);
    }


    public void refreshContent(RepcastFragment fragment) {
        mAllowClicks = false;
        JsonDirectoryDownloader downloader = new JsonDirectoryDownloader(this);
        downloader.execute(Utils.getDirGetURL(fragment.getContext(), false) + mDir.key);
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

    public boolean getItemShowsQuickQueue(int position) {
        JsonDirectory.JsonFileDir dir = mFileList.info.get(position);
        if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_DIR)) {
            return false;
        } else {
            for (int i = 0; i < MIME_ARRAY.length; i++) {
                if (MIME_ARRAY[i].mMime.equals(dir.mimetype)) {
                    return MIME_ARRAY[i].mHasQuickAddQueue;
                }
            }
        }
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        int iconResource = (int) getItemId(position);
        boolean hasQuickAddToQueue = getItemShowsQuickQueue(position);
        JsonDirectory.JsonFileDir result = mFileList.info.get(position);
        if (convertView == null) {
            int layout = R.layout.fragment_selectfile_list_element;
            convertView = LayoutInflater.from(mFragment.getActivity()).inflate(layout, parent, false);
            holder = new Holder();

            holder.mName = (TextView) convertView.findViewById(R.id.fragment_selectfile_list_element_name);
            holder.mDate = (TextView) convertView.findViewById(R.id.fragment_selectfile_list_element_date);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.fragment_selectfile_list_element_icon);
            holder.mQuickAddToQueue = (ImageButton) convertView.findViewById(R.id.fragment_selectfile_list_element_quick_add_to_queue);
            convertView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            holder.mQuickAddToQueue.setOnClickListener(this);
            convertView.setTag(holder);
            holder.mQuickAddToQueue.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mName.setText(result.name);
        holder.mDate.setText(result.date);
        holder.mIndex = position;
        if (iconResource == ICON_DIR) {
            holder.mIcon.setImageResource(R.drawable.folder);
        } else if (iconResource == ICON_UNKNOWN) {
            holder.mIcon.setImageResource(R.drawable.question_mark);
        } else {
            holder.mIcon.setImageResource(iconResource);
        }
        if (hasQuickAddToQueue && VideoCastManager.getInstance().isConnected()) {
            holder.mQuickAddToQueue.setVisibility(View.VISIBLE);
        } else {
            holder.mQuickAddToQueue.setVisibility(View.INVISIBLE);
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
        if (v.getId() == R.id.fragment_selectfile_list_element_quick_add_to_queue) {
//            try {
                String castPath = dir.path;
                String mimeType = dir.type;
                String title = dir.name;
                int trackType;
                int mediaType;
                if (mimeType.startsWith("video")) {
                    trackType = MediaTrack.TYPE_VIDEO;
                    mediaType = MediaMetadata.MEDIA_TYPE_MOVIE;
                } else if (mimeType.startsWith("audio")) {
                    trackType = MediaTrack.TYPE_AUDIO;
                    mediaType = MediaMetadata.MEDIA_TYPE_MUSIC_TRACK;
                } else {
                    trackType = MediaTrack.TYPE_UNKNOWN;
                    mediaType = MediaMetadata.MEDIA_TYPE_GENERIC;
                }

                MediaInfo.Builder builder = new MediaInfo.Builder(castPath);
                builder.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED);
                builder.setContentType(mimeType);
                MediaMetadata metadata = new MediaMetadata(mediaType);
                metadata.putString(MediaMetadata.KEY_TITLE, title);
                //metadata.putString(MediaMetadata.KEY_SUBTITLE, "Sub title Text");
                builder.setMetadata(metadata);
                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject();
                    jsonObj.put(VideoProvider.KEY_DESCRIPTION, "");
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to add description to the json object", e);
                }
                MediaTrack.Builder trackBuilder = new MediaTrack.Builder(1, MediaTrack.TYPE_VIDEO);

                //trackBuilder.setContentId(castPath);
                Log.e(TAG, "Setting path String: " + castPath);
                trackBuilder.setName(title);
                builder.setMediaTracks(Collections.singletonList(trackBuilder.build()));
                builder.setCustomData(jsonObj);
                MediaInfo mediaInfo = builder.build();


                Utils.doProcessButtons(v.getContext(), R.id.action_add_to_queue, mediaInfo);


//                MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(true).setPreloadTime(CastApplication.PRELOAD_TIME_S).build();
//                MediaQueueItem[] newItemArray = new MediaQueueItem[100];
//                newItemArray[0] = queueItem;
//
//                final QueueDataProvider provider = QueueDataProvider.getInstance();
//                Log.i(TAG, "onClick: "+ provider.getCount());
//                if (provider.getCount() == 0) {
//                    // temporary castManager.queueLoad(newItemArray, 0,
//                    // temporary        MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
//                    ((CastApplication) v.getContext().getApplicationContext()).loadQueue(newItemArray, 0);
//                } else {
//                    VideoCastManager.getInstance().queueAppendItem(queueItem, null);
//                }
//            } catch (TransientNetworkDisconnectionException e) {
//                e.printStackTrace();
//            } catch (NoConnectionException e) {
//                e.printStackTrace();
//            }
        } else {
            if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_DIR)) {
                ((RepcastActivity) mFragment.getActivity()).showContent(dir);
            } else if (dir.type.equals(JsonDirectory.JsonFileDir.TYPE_FILE)) {
                ((RepcastActivity) mFragment.getActivity()).showFile(dir, false);
            }
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
            Log.e(TAG, "Long press");
            ((RepcastActivity) mFragment.getActivity()).openDownloadDialog(dir);
            return true;
        }
        return false;
    }

    public Filter getFilter() {
        return mFilter;
    }

    public static class IconMimeMap {
        public int mIconResource;
        public String mMime;
        public boolean mHasQuickAddQueue;

        public IconMimeMap(String mime, int iconResource, boolean hasQuickAddQueue) {
            mMime = mime;
            mIconResource = iconResource;
            mHasQuickAddQueue = hasQuickAddQueue;
        }
    }

    public class Holder {
        public TextView mName;
        public ImageView mIcon;
        public TextView mDate;
        public int mIndex;
        public ImageButton mQuickAddToQueue;
    }
}
