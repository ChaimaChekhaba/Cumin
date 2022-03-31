package com.jess.arms.http.log;


import com.jess.arms.di.module.GlobalConfigModule;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.Request;


public interface FormatPrinter {
    void printJsonRequest(Request request, String bodyString);

    void printFileRequest(Request request);

    void printJsonResponse(long chainMs, boolean isSuccessful, int code, String headers, MediaType contentType, String bodyString, List<String> segments, String message, String responseUrl);

    void printFileResponse(long chainMs, boolean isSuccessful, int code, String headers, List<String> segments, String message, String responseUrl);
}

