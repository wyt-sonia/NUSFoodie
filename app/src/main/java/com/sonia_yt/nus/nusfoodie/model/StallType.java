package com.sonia_yt.nus.nusfoodie.model;

import java.util.ArrayList;
import java.util.HashMap;

public class StallType {
    private String id, name;
    private static HashMap<String, String> stallTypesList = new HashMap<>();

    public static HashMap<String, String> getStallTypesList() {
        return stallTypesList;
    }

    public static void setStallTypesList(HashMap<String, String> stallTypesList) {
        StallType.stallTypesList = stallTypesList;
    }

    public static void addItemToMap(StallType temp){
        StallType.stallTypesList.put(temp.id, temp.name);
    }

    public StallType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getName(String id) {
        return stallTypesList.get(id);
    }

    public void setName(String name) {
        this.name = name;
    }
}
