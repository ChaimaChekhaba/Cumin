package com.jess.arms.http.log;


import android.text.TextUtils;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.utils.CharacterHandler;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.Request;
import timber.log.Timber;


public class DefaultFormatPrinter implements FormatPrinter {
    private static final String TAG = "ArmsHttpLog";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String DOUBLE_SEPARATOR = (DefaultFormatPrinter.LINE_SEPARATOR) + (DefaultFormatPrinter.LINE_SEPARATOR);

    private static final String[] OMITTED_RESPONSE = new String[]{ DefaultFormatPrinter.LINE_SEPARATOR, "Omitted response body" };

    private static final String[] OMITTED_REQUEST = new String[]{ DefaultFormatPrinter.LINE_SEPARATOR, "Omitted request body" };

    private static final String N = "\n";

    private static final String T = "\t";

    private static final String REQUEST_UP_LINE = "   ??????? Request ????????????????????????????????????????????????????????????????????????";

    private static final String END_LINE = "   ????????????????????????????????????????????????????????????????????????????????????????";

    private static final String RESPONSE_UP_LINE = "   ??????? Response ???????????????????????????????????????????????????????????????????????";

    private static final String BODY_TAG = "Body:";

    private static final String URL_TAG = "URL: ";

    private static final String METHOD_TAG = "Method: @";

    private static final String HEADERS_TAG = "Headers:";

    private static final String STATUS_CODE_TAG = "Status Code: ";

    private static final String RECEIVED_TAG = "Received in: ";

    private static final String CORNER_UP = "? ";

    private static final String CORNER_BOTTOM = "? ";

    private static final String CENTER_LINE = "? ";

    private static final String DEFAULT_LINE = "? ";

    private static boolean isEmpty(String line) {
        return (((TextUtils.isEmpty(line)) || (DefaultFormatPrinter.N.equals(line))) || (DefaultFormatPrinter.T.equals(line))) || (TextUtils.isEmpty(line.trim()));
    }

    @Override
    public void printJsonRequest(Request request, String bodyString) {
        final String requestBody = (((DefaultFormatPrinter.LINE_SEPARATOR) + (DefaultFormatPrinter.BODY_TAG)) + (DefaultFormatPrinter.LINE_SEPARATOR)) + bodyString;
        final String tag = DefaultFormatPrinter.getTag(true);
        Timber.tag(tag).i(DefaultFormatPrinter.REQUEST_UP_LINE);
        DefaultFormatPrinter.logLines(tag, new String[]{ (DefaultFormatPrinter.URL_TAG) + (request.url()) }, false);
        DefaultFormatPrinter.logLines(tag, DefaultFormatPrinter.getRequest(request), true);
        DefaultFormatPrinter.logLines(tag, requestBody.split(DefaultFormatPrinter.LINE_SEPARATOR), true);
        Timber.tag(tag).i(DefaultFormatPrinter.END_LINE);
    }

    @Override
    public void printFileRequest(Request request) {
        final String tag = DefaultFormatPrinter.getTag(true);
        Timber.tag(tag).i(DefaultFormatPrinter.REQUEST_UP_LINE);
        DefaultFormatPrinter.logLines(tag, new String[]{ (DefaultFormatPrinter.URL_TAG) + (request.url()) }, false);
        DefaultFormatPrinter.logLines(tag, DefaultFormatPrinter.getRequest(request), true);
        DefaultFormatPrinter.logLines(tag, DefaultFormatPrinter.OMITTED_REQUEST, true);
        Timber.tag(tag).i(DefaultFormatPrinter.END_LINE);
    }

    @Override
    public void printJsonResponse(long chainMs, boolean isSuccessful, int code, String headers, MediaType contentType, String bodyString, List<String> segments, String message, final String responseUrl) {
        bodyString = (RequestInterceptor.isJson(contentType)) ? CharacterHandler.jsonFormat(bodyString) : RequestInterceptor.isXml(contentType) ? CharacterHandler.xmlFormat(bodyString) : bodyString;
        final String responseBody = (((DefaultFormatPrinter.LINE_SEPARATOR) + (DefaultFormatPrinter.BODY_TAG)) + (DefaultFormatPrinter.LINE_SEPARATOR)) + bodyString;
        final String tag = DefaultFormatPrinter.getTag(false);
        final String[] urlLine = new String[]{ (DefaultFormatPrinter.URL_TAG) + responseUrl, DefaultFormatPrinter.N };
        Timber.tag(tag).i(DefaultFormatPrinter.RESPONSE_UP_LINE);
        DefaultFormatPrinter.logLines(tag, urlLine, true);
        DefaultFormatPrinter.logLines(tag, DefaultFormatPrinter.getResponse(headers, chainMs, code, isSuccessful, segments, message), true);
        DefaultFormatPrinter.logLines(tag, responseBody.split(DefaultFormatPrinter.LINE_SEPARATOR), true);
        Timber.tag(tag).i(DefaultFormatPrinter.END_LINE);
    }

    @Override
    public void printFileResponse(long chainMs, boolean isSuccessful, int code, String headers, List<String> segments, String message, final String responseUrl) {
        final String tag = DefaultFormatPrinter.getTag(false);
        final String[] urlLine = new String[]{ (DefaultFormatPrinter.URL_TAG) + responseUrl, DefaultFormatPrinter.N };
        Timber.tag(tag).i(DefaultFormatPrinter.RESPONSE_UP_LINE);
        DefaultFormatPrinter.logLines(tag, urlLine, true);
        DefaultFormatPrinter.logLines(tag, DefaultFormatPrinter.getResponse(headers, chainMs, code, isSuccessful, segments, message), true);
        DefaultFormatPrinter.logLines(tag, DefaultFormatPrinter.OMITTED_RESPONSE, true);
        Timber.tag(tag).i(DefaultFormatPrinter.END_LINE);
    }

    private static void logLines(String tag, String[] lines, boolean withLineSize) {
        for (String line : lines) {
            int lineLength = line.length();
            int MAX_LONG_SIZE = (withLineSize) ? 110 : lineLength;
            for (int i = 0; i <= (lineLength / MAX_LONG_SIZE); i++) {
                int start = i * MAX_LONG_SIZE;
                int end = (i + 1) * MAX_LONG_SIZE;
                end = (end > (line.length())) ? line.length() : end;
                Timber.tag(DefaultFormatPrinter.resolveTag(tag)).i(((DefaultFormatPrinter.DEFAULT_LINE) + (line.substring(start, end))));
            }
        }
    }

    private static ThreadLocal<Integer> last = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    private static final String[] ARMS = new String[]{ "-A-", "-R-", "-M-", "-S-" };

    private static String computeKey() {
        if ((DefaultFormatPrinter.last.get()) >= 4) {
            DefaultFormatPrinter.last.set(0);
        }
        String s = DefaultFormatPrinter.ARMS[DefaultFormatPrinter.last.get()];
        DefaultFormatPrinter.last.set(((DefaultFormatPrinter.last.get()) + 1));
        return s;
    }

    private static String resolveTag(String tag) {
        return (DefaultFormatPrinter.computeKey()) + tag;
    }

    private static String[] getRequest(Request request) {
        String log;
        String header = request.headers().toString();
        log = (((DefaultFormatPrinter.METHOD_TAG) + (request.method())) + (DefaultFormatPrinter.DOUBLE_SEPARATOR)) + (DefaultFormatPrinter.isEmpty(header) ? "" : ((DefaultFormatPrinter.HEADERS_TAG) + (DefaultFormatPrinter.LINE_SEPARATOR)) + (DefaultFormatPrinter.dotHeaders(header)));
        return log.split(DefaultFormatPrinter.LINE_SEPARATOR);
    }

    private static String[] getResponse(String header, long tookMs, int code, boolean isSuccessful, List<String> segments, String message) {
        String log;
        String segmentString = DefaultFormatPrinter.slashSegments(segments);
        log = (((((((((((((!(TextUtils.isEmpty(segmentString)) ? segmentString + " - " : "") + "is success : ") + isSuccessful) + " - ") + (DefaultFormatPrinter.RECEIVED_TAG)) + tookMs) + "ms") + (DefaultFormatPrinter.DOUBLE_SEPARATOR)) + (DefaultFormatPrinter.STATUS_CODE_TAG)) + code) + " / ") + message) + (DefaultFormatPrinter.DOUBLE_SEPARATOR)) + (DefaultFormatPrinter.isEmpty(header) ? "" : ((DefaultFormatPrinter.HEADERS_TAG) + (DefaultFormatPrinter.LINE_SEPARATOR)) + (DefaultFormatPrinter.dotHeaders(header)));
        return log.split(DefaultFormatPrinter.LINE_SEPARATOR);
    }

    private static String slashSegments(List<String> segments) {
        StringBuilder segmentString = new StringBuilder();
        for (String segment : segments) {
            segmentString.append("/").append(segment);
        }
        return segmentString.toString();
    }

    private static String dotHeaders(String header) {
        String[] headers = header.split(DefaultFormatPrinter.LINE_SEPARATOR);
        StringBuilder builder = new StringBuilder();
        String tag = "? ";
        if ((headers.length) > 1) {
            for (int i = 0; i < (headers.length); i++) {
                if (i == 0) {
                    tag = DefaultFormatPrinter.CORNER_UP;
                }else
                    if (i == ((headers.length) - 1)) {
                        tag = DefaultFormatPrinter.CORNER_BOTTOM;
                    }else {
                        tag = DefaultFormatPrinter.CENTER_LINE;
                    }

                builder.append(tag).append(headers[i]).append("\n");
            }
        }else {
            for (String item : headers) {
                builder.append(tag).append(item).append("\n");
            }
        }
        return builder.toString();
    }

    private static String getTag(boolean isRequest) {
        if (isRequest) {
            return (DefaultFormatPrinter.TAG) + "-Request";
        }else {
            return (DefaultFormatPrinter.TAG) + "-Response";
        }
    }
}

