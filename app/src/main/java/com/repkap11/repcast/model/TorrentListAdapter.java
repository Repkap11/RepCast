package com.repkap11.repcast.model;

import android.util.Base64;
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
import com.repkap11.repcast.activities.fragments.SelectTorrentFragment;


/**
 * Created by paul on 9/10/15.
 */
public class TorrentListAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String TAG = TorrentListAdapter.class.getSimpleName();
    private final String mURL;
    private TorrentListFilter mFilter;
    private SelectTorrentFragment mFragment;
    private JsonTorrent mTorrentList;

    public TorrentListAdapter(String query) {
        String query64 = Base64.encodeToString(query.getBytes(), Base64.NO_WRAP);
        mURL = "https://repkam09.com/dl/torsearch/" + query64;
        mTorrentList = new JsonTorrent();
        mFilter = new TorrentListFilter(mTorrentList, this);
        JsonTorrentListDownloader downloader = new JsonTorrentListDownloader(this);
        downloader.execute(mURL);
    }

    public void updateContext(SelectTorrentFragment fragment) {
        Log.e(TAG,"Context Updateing:"+fragment);
        mFragment = fragment;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mTorrentList == null) {
            return 0;
        }
        return mTorrentList.torrents.size();
    }

    @Override
    public Object getItem(int position) {
        return mTorrentList.torrents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        long itemID = getItemId(position);
        JsonTorrent.JsonTorrentResult result = mTorrentList.torrents.get(position);
        if (convertView == null) {
            int layout = R.layout.fragment_selecttorrent_list_element;
            convertView = LayoutInflater.from(mFragment.getActivity()).inflate(layout, parent, false);
            holder = new Holder();
            holder.mName = (TextView) convertView.findViewById(R.id.fragment_selecttorrent_list_element_name);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.fragment_selecttorrent_list_element_icon);
            convertView.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mName.setText(result.name);
        holder.mIndex = position;
        int iconResource = R.drawable.torrent;
        holder.mIcon.setImageResource(iconResource);
        return convertView;
    }

    public void updateTorrentList(JsonTorrent fileList) {
        updateTorrentList(fileList, false);
    }

    public void updateTorrentList(JsonTorrent fileList, boolean isFiltered) {
        Log.e(TAG, "Torrent list changed");
        mTorrentList = fileList;
        if (!isFiltered) {
            mFilter = new TorrentListFilter(mTorrentList, this);
        }
        if (mTorrentList == null) {
            Toast.makeText(mFragment.getActivity().getApplicationContext(), "Unable to read torrent data from Repkam09.com", Toast.LENGTH_SHORT).show();
        }
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        Holder h = (Holder) v.getTag();
        JsonTorrent.JsonTorrentResult element = mTorrentList.torrents.get(h.mIndex);
        mFragment.uploadTorrent(element);
    }

    public Filter getFilter() {
        return mFilter;
    }

    public void torrentUploadComplete(Integer resultCode) {
        Log.e(TAG, "Torrent Complete:"+resultCode);
    }

    public class Holder {
        public TextView mName;
        public ImageView mIcon;
        public int mIndex;
    }
}
