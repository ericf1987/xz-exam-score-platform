package com.xz.scorep.executor.api.utils;

import com.mysql.fabric.xmlrpc.base.Params;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.http.HttpRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @author by fengye on 2017/6/4.
 */
public class HttpUtils {

    static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    public static void sendRequest(String url, Map<String, String> params) {
        HttpRequest httpRequest = new HttpRequest(url);
        for (String key : params.keySet()) {
            httpRequest.setParameter(key, params.get(key));
        }

        try {
            String result = httpRequest.request();
        } catch (IOException e) {
            LOG.info("请求异常：{}, 参数：{}", url, params);
            e.printStackTrace();
        }
    }

    private String regenerateUrl(String url, Map<String, List<String>> parameters) {
        if (parameters.isEmpty()) {
            return url;
        }

        String string = "";
        for (String key : parameters.keySet()) {
            List<String> values = parameters.get(key);
            for (String value : values) {
                if (value != null) {
                    string += key + "=" + encodeUrl(value) + "&";
                }
            }
        }
        string = StringUtils.removeEnd(string, "&");

        if (url.contains("?")) {
            if (url.endsWith("?")) {
                return url + string;
            } else {
                return url + "&" + string;
            }
        } else {
            return url + "?" + string;
        }
    }

    private static String encodeUrl(String str) {
        if (str == null) {
            return null;
        }

        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }
}
