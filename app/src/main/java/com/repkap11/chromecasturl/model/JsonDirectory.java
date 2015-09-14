package com.repkap11.chromecasturl.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 9/10/15.
 */
public class JsonDirectory {
    public List<JsonFileDir> result = new ArrayList<>();

    public static class JsonFileDir implements Parcelable {
        public static final Parcelable.Creator<JsonFileDir> CREATOR = new Parcelable.Creator<JsonFileDir>() {
            public JsonFileDir createFromParcel(Parcel in) {
                return new JsonFileDir(in);
            }

            @Override
            public JsonFileDir[] newArray(int size) {
                return new JsonFileDir[size];
            }
        };
        public static final String TYPE_FILE = "file";
        public static final String TYPE_DIR = "dir";
        public String name;
        public String type;
        public String path64;
        public String path;
        public boolean isRoot = false;

        public JsonFileDir() {
        }

        public JsonFileDir(Parcel in) {
            if (in != null) {
                name = in.readString();
                type = in.readString();
                path64 = in.readString();
                path = in.readString();
                isRoot = in.readByte() != 0;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(type);
            dest.writeString(path64);
            dest.writeString(path);
            dest.writeByte((byte) (isRoot ? 1 : 0));
        }
    }
}
