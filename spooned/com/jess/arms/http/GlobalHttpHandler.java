package com.jess.arms.http;


import com.jess.arms.di.module.GlobalConfigModule;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public interface GlobalHttpHandler {
    Response onHttpResultResponse(String httpResult, Interceptor.Chain chain, Response response);

    Request onHttpRequestBefore(Interceptor.Chain chain, Request request);

    GlobalHttpHandler EMPTY = new GlobalHttpHandler() {
        @Override
        public Response onHttpResultResponse(String httpResult, Interceptor.Chain chain, Response response) {
            return response;
        }

        @Override
        public Request onHttpRequestBefore(Interceptor.Chain chain, Request request) {
            return request;
        }
    };
}

