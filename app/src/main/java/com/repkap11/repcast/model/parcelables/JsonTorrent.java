package com.repkap11.repcast.model.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 9/10/15.
 */
public class JsonTorrent {
    public String query;
    public int count;
    public List<JsonTorrentResult> torrents = new ArrayList<>();

    public static class JsonTorrentResult implements Parcelable {
        public static final Creator<JsonTorrentResult> CREATOR = new Creator<JsonTorrentResult>() {
            public JsonTorrentResult createFromParcel(Parcel in) {
                return new JsonTorrentResult(in);
            }

            @Override
            public JsonTorrentResult[] newArray(int size) {
                return new JsonTorrentResult[size];
            }
        };
        public String name;
        public String size;
        public String link;
        public JsonTorrentResultCategory category;
        public int seeders;
        public int leechers;
        public String uploadDate;
        public String magnetLink;
        public JsonTorrentResultCategorySubCategory subcategory;
        public String uploader;
        public String uploaderLink;

        public JsonTorrentResult() {
        }

        public JsonTorrentResult(Parcel in) {
            if (in != null) {
                name = in.readString();
                size = in.readString();
                link = in.readString();
                category = in.readParcelable(JsonTorrentResultCategory.class.getClassLoader());
                seeders = in.readInt();
                leechers = in.readInt();
                uploadDate = in.readString();
                magnetLink = in.readString();
                subcategory = in.readParcelable(JsonTorrentResultCategorySubCategory.class.getClassLoader());
                uploader = in.readString();
                uploaderLink = in.readString();
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(size);
            dest.writeString(link);
            dest.writeParcelable(category, flags);
            dest.writeInt(seeders);
            dest.writeInt(leechers);
            dest.writeString(uploadDate);
            dest.writeString(magnetLink);
            dest.writeParcelable(subcategory, flags);
            dest.writeString(uploader);
            dest.writeString(uploaderLink);
        }

        public static class JsonTorrentResultCategory implements Parcelable {
            public static final Creator<JsonTorrentResultCategory> CREATOR = new Creator<JsonTorrentResultCategory>() {
                public JsonTorrentResultCategory createFromParcel(Parcel in) {
                    return new JsonTorrentResultCategory(in);
                }

                @Override
                public JsonTorrentResultCategory[] newArray(int size) {
                    return new JsonTorrentResultCategory[size];
                }

            };

            public JsonTorrentResultCategory() {
            }

            public JsonTorrentResultCategory(Parcel in) {
                if (in != null) {
                    id = in.readString();
                    name = in.readString();
                }
            }

            public String id;
            public String name;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(id);
                dest.writeString(name);
            }
        }
    }
    public static class JsonTorrentResultCategorySubCategory implements Parcelable {
        public static final Creator<JsonTorrentResultCategorySubCategory> CREATOR = new Creator<JsonTorrentResultCategorySubCategory>() {
            public JsonTorrentResultCategorySubCategory createFromParcel(Parcel in) {
                return new JsonTorrentResultCategorySubCategory(in);
            }

            @Override
            public JsonTorrentResultCategorySubCategory[] newArray(int size) {
                return new JsonTorrentResultCategorySubCategory[size];
            }

        };

        public JsonTorrentResultCategorySubCategory() {
        }

        public JsonTorrentResultCategorySubCategory(Parcel in) {
            if (in != null) {
                id = in.readString();
                name = in.readString();
            }
        }

        public String id;
        public String name;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
        }
    }
}
