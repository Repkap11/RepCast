package com.repkap11.repcast.model.adapters;

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
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.fragments.RepcastFragment;
import com.repkap11.repcast.model.parcelables.JsonTorrent;
import com.repkap11.repcast.model.filters.TorrentListFilter;
import com.repkap11.repcast.model.rest.JsonTorrentListDownloader;


/**
 * Created by paul on 9/10/15.
 */
public class TorrentListAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String TAG = TorrentListAdapter.class.getSimpleName();
    private final String mQuery64;
    private TorrentListFilter mFilter;
    private RepcastFragment mFragment;
    private JsonTorrent mTorrentList;

    public TorrentListAdapter(String query, RepcastFragment fragment) {
        mQuery64 = Base64.encodeToString(query.getBytes(), Base64.NO_WRAP);
        mTorrentList = new JsonTorrent();
        mFilter = new TorrentListFilter(mTorrentList, this);
        JsonTorrentListDownloader downloader = new JsonTorrentListDownloader(this);
        downloader.execute(fragment.getString(R.string.endpoint_torsearch) + mQuery64);
    }

    public void updateContext(RepcastFragment fragment) {
        Log.e(TAG, "Context Updateing:" + fragment);
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
            holder.mSize = (TextView) convertView.findViewById(R.id.fragment_selecttorrent_list_element_size);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.fragment_selecttorrent_list_element_icon);
            holder.mSeeders = (TextView) convertView.findViewById(R.id.fragment_selecttorrent_list_element_seeders);


            convertView.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mName.setText(result.name);
        holder.mSize.setText(holder.mSize.getResources().getText(R.string.torrent_size_prefix) + result.size);
        holder.mSeeders.setText(holder.mSize.getResources().getText(R.string.torrent_seeders_prefix)+Integer.toString(result.seeders));
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
            mFragment.setShouldProgressBeShown(false);
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
        ((RepcastActivity) mFragment.getActivity()).uploadTorrent(element);
    }

    public Filter getFilter() {
        return mFilter;
    }


    public class Holder {
        public TextView mName;
        public ImageView mIcon;
        public int mIndex;
        public TextView mSize;
        public TextView mSeeders;
    }
}
