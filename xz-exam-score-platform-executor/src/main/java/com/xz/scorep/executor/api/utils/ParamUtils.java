package com.xz.scorep.executor.api.utils;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.StringUtil;

import com.xz.scorep.executor.api.AppException;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 参数处理工具类
 *
 * @author zhaorenwu
 */
public class ParamUtils {

    static final Logger LOG = LoggerFactory.getLogger(ParamUtils.class);

    /**
     * 解密参数信息
     *
     * @param p 待解密参数
     *
     * @return 结果
     */
    public static Param decipherParam(Function function, String p) {
        if (StringUtil.isNotBlank(p) && p.contains("%3D")) {
            try {
                p = URLDecoder.decode(p, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AppException("参数转码异常:" + p);
            }
        }

        //获取参数
        return parseParams(function, p);
    }

    private static Param parseParams(Function function, String value) {
        Param params = new Param();
        List<String> emptyParams = new ArrayList<>();
        Parameter[] funcParams = function == null ? new Parameter[]{} : function.parameters();

        // set parameters
        if (StringUtil.isNotBlank(value)) {
            String[] parameterArrays = value.split(";");
            for (String parameterElement : parameterArrays) {
                String paramName = StringUtils.substringBefore(parameterElement, "=");
                String paramValue = StringUtils.substringAfter(parameterElement, "=");

                params.setParameter(paramName, paramValue);
            }
        }

        for (Parameter paramDefinition : funcParams) {
            String paramName = paramDefinition.name();
            boolean required = paramDefinition.required();

            String paramValue = params.getString(paramName);
            if (StringUtil.isBlank(paramValue) && !required) {
                paramValue = paramDefinition.defaultValue();
                params.setParameter(paramName, paramValue);
            }

            if (StringUtil.isBlank(paramValue) && required) {
                emptyParams.add(paramName);
            }

            // 处理定义为数组的参数值
            processArrayParameter(params, paramName, paramValue, paramDefinition);
        }

        if (emptyParams.size() > 0) {
            throw new AppException("参数不能为空：" + emptyParams);
        }

        return params;
    }

    // 处理可能被定义为数组的参数值
    private static void processArrayParameter(Param param, String parameterName,
                                              String parameterValue, Parameter funcParam) {
        if (StringUtil.isBlank(parameterValue)) {
            return;
        }

        if (funcParam.type() == Type.StringArray) {
            param.setParameter(parameterName, parameterValue.split(","));

        } else if (funcParam.type() == Type.BooleanArray) {
            Boolean[] values = parseBooleanArray(parameterValue);
            param.setParameter(parameterName, values);

        } else if (funcParam.type() == Type.DateArray) {
            Date[] values = parseDateArray(parameterValue);
            param.setParameter(parameterName, values);

        } else if (funcParam.type() == Type.IntegerArray) {
            Integer[] values = parseIntegerArray(parameterValue);
            param.setParameter(parameterName, values);

        } else if (funcParam.type() == Type.DecimalArray) {
            Double[] values = parseDecimalArray(parameterValue);
            param.setParameter(parameterName, values);
        }
    }

    private static Double[] parseDecimalArray(String parameterValue) {
        String[] strValues = parameterValue.split(",");
        Double[] values = new Double[strValues.length];
        for (int i = 0; i < strValues.length; i++) {
            String strValue = strValues[i];
            if (strValue.trim().length() == 0) {
                values[i] = 0d;
            } else {
                values[i] = Double.parseDouble(strValue);
            }
        }
        return values;
    }

    private static Integer[] parseIntegerArray(String parameterValue) {
        String[] strValues = parameterValue.split(",");
        Integer[] values = new Integer[strValues.length];
        for (int i = 0; i < strValues.length; i++) {
            String strValue = strValues[i];
            if (strValue.trim().length() == 0) {
                values[i] = 0;
            } else {
                values[i] = Integer.parseInt(strValue);
            }
        }
        return values;
    }

    private static Date[] parseDateArray(String parameterValue) {
        String[] strValues = parameterValue.split(",");
        Date[] values = new Date[strValues.length];
        for (int i = 0; i < strValues.length; i++) {
            String strValue = strValues[i];
            if (strValue.trim().length() == 0) {
                values[i] = null;
            } else {
                values[i] = new Date(Integer.parseInt(strValue));
            }
        }
        return values;
    }

    private static Boolean[] parseBooleanArray(String parameterValue) {
        String[] strValues = parameterValue.split(",");
        Boolean[] values = new Boolean[strValues.length];
        for (int i = 0; i < strValues.length; i++) {
            String strValue = strValues[i];
            if (strValue.trim().length() == 0) {
                values[i] = false;
            } else {
                values[i] = Boolean.valueOf(strValue);
            }
        }
        return values;
    }
}
