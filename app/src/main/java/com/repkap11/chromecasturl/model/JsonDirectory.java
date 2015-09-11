package com.repkap11.chromecasturl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 9/10/15.
 */
public class JsonDirectory {
    public List<JsonFileDir> result = new ArrayList<>();

    public static class JsonFileDir {
        public static final String TYPE_FILE = "file";
        public static final String TYPE_DIR = "dir";
        public String name;
        public String type;
        public String path64;
        public String path;
    }
}
