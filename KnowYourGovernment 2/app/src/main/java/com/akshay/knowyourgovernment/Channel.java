package com.akshay.knowyourgovernment;

import java.io.Serializable;

public class Channel implements Serializable {
    private String type,id;

    public Channel(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
