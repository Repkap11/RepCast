package com.repkap11.repcast.model.filters;

import android.util.Log;
import android.widget.Filter;

import com.repkap11.repcast.model.parcelables.JsonTorrent;
import com.repkap11.repcast.model.adapters.TorrentListAdapter;


/**
 * Created by paul on 9/10/15.
 */
public class TorrentListFilter extends Filter {
    private static final String TAG = TorrentListFilter.class.getSimpleName();
    private final TorrentListAdapter mTorrentListAdapter;
    private final JsonTorrent mTorrents;

    public TorrentListFilter(JsonTorrent torrents, TorrentListAdapter fileListAdapter) {
        super();
        mTorrents = torrents;
        mTorrentListAdapter = fileListAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint == null || constraint.length() == 0) {
            results.values = mTorrents;
            results.count = mTorrents.torrents.size();
        } else {
            JsonTorrent resultDir = new JsonTorrent();

            for (JsonTorrent.JsonTorrentResult file : mTorrents.torrents) {
                if (file.name.toUpperCase().contains(constraint.toString().toUpperCase())) {
                    resultDir.torrents.add(file);
                }
            }
            results.values = resultDir;
            results.count = resultDir.torrents.size();
        }
        Log.e(TAG, "Searching Returned " + results.count);
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        mTorrentListAdapter.updateTorrentList((JsonTorrent) results.values, true);
    }

}
