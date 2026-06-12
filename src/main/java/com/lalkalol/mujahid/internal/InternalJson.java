package com.lalkalol.mujahid.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class InternalJson {
    public static final Gson GSON = new GsonBuilder().create();

    private InternalJson() {
    }
}
