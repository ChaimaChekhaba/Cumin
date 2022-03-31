package com.jess.arms.http.log;


import android.support.annotation.Nullable;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.http.GlobalHttpHandler;
import com.jess.arms.utils.CharacterHandler;
import com.jess.arms.utils.ZipHelper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import timber.log.Timber;


@Singleton
public class RequestInterceptor implements Interceptor {
    @Inject
    @Nullable
    GlobalHttpHandler mHandler;

    @Inject
    FormatPrinter mPrinter;

    @Inject
    RequestInterceptor.Level printLevel;

    public enum Level {
        NONE, REQUEST, RESPONSE, ALL;}

    @Inject
    public RequestInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        boolean logRequest = ((printLevel) == (RequestInterceptor.Level.ALL)) || (((printLevel) != (RequestInterceptor.Level.NONE)) && ((printLevel) == (RequestInterceptor.Level.REQUEST)));
        if (logRequest) {
            if (((request.body()) != null) && (RequestInterceptor.isParseable(request.body().contentType()))) {
                mPrinter.printJsonRequest(request, RequestInterceptor.parseParams(request));
            }else {
                mPrinter.printFileRequest(request);
            }
        }
        boolean logResponse = ((printLevel) == (RequestInterceptor.Level.ALL)) || (((printLevel) != (RequestInterceptor.Level.NONE)) && ((printLevel) == (RequestInterceptor.Level.RESPONSE)));
        long t1 = (logResponse) ? System.nanoTime() : 0;
        Response originalResponse;
        try {
            originalResponse = chain.proceed(request);
        } catch (Exception e) {
            Timber.w(("Http Error: " + e));
            throw e;
        }
        long t2 = (logResponse) ? System.nanoTime() : 0;
        ResponseBody responseBody = originalResponse.body();
        String bodyString = null;
        if ((responseBody != null) && (RequestInterceptor.isParseable(responseBody.contentType()))) {
            bodyString = printResult(request, originalResponse, logResponse);
        }
        if (logResponse) {
            final List<String> segmentList = request.url().encodedPathSegments();
            final String header = originalResponse.headers().toString();
            final int code = originalResponse.code();
            final boolean isSuccessful = originalResponse.isSuccessful();
            final String message = originalResponse.message();
            final String url = originalResponse.request().url().toString();
            if ((responseBody != null) && (RequestInterceptor.isParseable(responseBody.contentType()))) {
                mPrinter.printJsonResponse(TimeUnit.NANOSECONDS.toMillis((t2 - t1)), isSuccessful, code, header, responseBody.contentType(), bodyString, segmentList, message, url);
            }else {
                mPrinter.printFileResponse(TimeUnit.NANOSECONDS.toMillis((t2 - t1)), isSuccessful, code, header, segmentList, message, url);
            }
        }
        if ((mHandler) != null)
            return mHandler.onHttpResultResponse(bodyString, chain, originalResponse);

        return originalResponse;
    }

    @Nullable
    private String printResult(Request request, Response response, boolean logResponse) throws IOException {
        try {
            ResponseBody responseBody = response.newBuilder().build().body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            String encoding = response.headers().get("Content-Encoding");
            Buffer clone = buffer.clone();
            return parseContent(responseBody, encoding, clone);
        } catch (IOException e) {
            e.printStackTrace();
            return ("{\"error\": \"" + (e.getMessage())) + "\"}";
        }
    }

    private String parseContent(ResponseBody responseBody, String encoding, Buffer clone) {
        Charset charset = Charset.forName("UTF-8");
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        if ((encoding != null) && (encoding.equalsIgnoreCase("gzip"))) {
            return ZipHelper.decompressForGzip(clone.readByteArray(), RequestInterceptor.convertCharset(charset));
        }else
            if ((encoding != null) && (encoding.equalsIgnoreCase("zlib"))) {
                return ZipHelper.decompressToStringForZlib(clone.readByteArray(), RequestInterceptor.convertCharset(charset));
            }else {
                return clone.readString(charset);
            }

    }

    public static String parseParams(Request request) throws UnsupportedEncodingException {
        try {
            RequestBody body = request.newBuilder().build().body();
            if (body == null)
                return "";

            Buffer requestbuffer = new Buffer();
            body.writeTo(requestbuffer);
            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = body.contentType();
            if (contentType != null) {
                charset = contentType.charset(charset);
            }
            return CharacterHandler.jsonFormat(URLDecoder.decode(requestbuffer.readString(charset), RequestInterceptor.convertCharset(charset)));
        } catch (IOException e) {
            e.printStackTrace();
            return ("{\"error\": \"" + (e.getMessage())) + "\"}";
        }
    }

    public static boolean isParseable(MediaType mediaType) {
        return (((((RequestInterceptor.isText(mediaType)) || (RequestInterceptor.isPlain(mediaType))) || (RequestInterceptor.isJson(mediaType))) || (RequestInterceptor.isForm(mediaType))) || (RequestInterceptor.isHtml(mediaType))) || (RequestInterceptor.isXml(mediaType));
    }

    public static boolean isText(MediaType mediaType) {
        if ((mediaType == null) || ((mediaType.type()) == null))
            return false;

        return mediaType.type().equals("text");
    }

    public static boolean isPlain(MediaType mediaType) {
        if ((mediaType == null) || ((mediaType.subtype()) == null))
            return false;

        return mediaType.subtype().toLowerCase().contains("plain");
    }

    public static boolean isJson(MediaType mediaType) {
        if ((mediaType == null) || ((mediaType.subtype()) == null))
            return false;

        return mediaType.subtype().toLowerCase().contains("json");
    }

    public static boolean isXml(MediaType mediaType) {
        if ((mediaType == null) || ((mediaType.subtype()) == null))
            return false;

        return mediaType.subtype().toLowerCase().contains("xml");
    }

    public static boolean isHtml(MediaType mediaType) {
        if ((mediaType == null) || ((mediaType.subtype()) == null))
            return false;

        return mediaType.subtype().toLowerCase().contains("html");
    }

    public static boolean isForm(MediaType mediaType) {
        if ((mediaType == null) || ((mediaType.subtype()) == null))
            return false;

        return mediaType.subtype().toLowerCase().contains("x-www-form-urlencoded");
    }

    public static String convertCharset(Charset charset) {
        String s = charset.toString();
        int i = s.indexOf("[");
        if (i == (-1))
            return s;

        return s.substring((i + 1), ((s.length()) - 1));
    }
}

