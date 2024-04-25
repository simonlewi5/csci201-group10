package com.mygdx.game;

import com.google.gson.annotations.SerializedName;

public enum MatchState {
    @SerializedName("INIT")
    INIT,
    @SerializedName("WAITING")
    WAITING,
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,
    @SerializedName("COMPLETE")
    COMPLETE,
}
