package com.tagloy.taglock.models;

import java.io.Serializable;

public class Groups implements Serializable {
    public String group_id, group_name, group_desc;

    public Groups(){}
    public Groups(String group_id,String group_name, String group_desc){
        this.group_id = group_id;
        this.group_name = group_name;
        this.group_desc = group_desc;
    }

    public String getGroup_desc() {
        return group_desc;
    }

    public void setGroup_desc(String group_desc) {
        this.group_desc = group_desc;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }
}
