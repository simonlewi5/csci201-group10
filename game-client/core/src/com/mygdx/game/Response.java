package com.mygdx.game;

import com.google.gson.JsonElement;

public class Response {
    private String type;
    private JsonElement data;

    public String getType() {
        return type;
    }

    public JsonElement getData() {
        return data;
    }
}
