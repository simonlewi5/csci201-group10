package com.mygdx.game;

import com.google.gson.JsonObject;

public class Response {
    private String type;
    private JsonObject data;

    public String getType() {
        return type;
    }

    public JsonObject getData() {
        return data;
    }
}
