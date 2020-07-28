package com.wangxile.appparse;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:打包校验描述文件和证书
 * @Author: xiaoran
 * @CreateDate: 2019/11/29
 */
public class IosPackUtil {

    /**
     * 解析描述文件
     */
    public static SaasIosProvision resolveProvision(InputStream inputStream) throws Exception {

        SaasIosProvision saasIosProvision = new SaasIosProvision();

        //解析描述文件mobileProvision->str
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String s = "";
            while ((s = bReader.readLine()) != null) {
                sb.append(s).append("\n");
            }
            bReader.close();
            //截取plist
            String str = sb.toString();
            String str1 = str.substring(str.indexOf("<plist"), str.lastIndexOf("</plist>")) + "</plist>";

            //去掉空格和换行
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher mm = p.matcher(str1);
            String result = mm.replaceAll("");

            //ExpirationDate
            String expirationDate = regexSingle("ExpirationDate", result, "date");
            saasIosProvision.setExpirationDate(expirationDate);

            //设备信息ProvisionedDevices(也可用来判断type类型)
            List<String> proDeviceList = regexMulti("ProvisionedDevices", result, "string");
            saasIosProvision.setPorvisionDevice(StringUtils.strip(proDeviceList.toString(), "[]"));

            //ProvisionsAllDevices(判断type)
            String allDevices = regexSingle("ProvisionsAllDevices", result, null);
            saasIosProvision.setProvisionAll("true".equals(allDevices) ? 1 : 0);

            //get-task-allow(判断type)
            String taskAllow = regexSingle("get-task-allow", result, null);
            saasIosProvision.setTaskAllow("true".equals(taskAllow) ? 1 : 0);

            //type
            if (saasIosProvision.getProvisionAll() == 1) {
                saasIosProvision.setType("enterprise");
            } else if (result.contains("ProvisionedDevices") && saasIosProvision.getTaskAllow() == 1) {
                saasIosProvision.setType("development");
            } else if (result.contains("ProvisionedDevices") && saasIosProvision.getTaskAllow() == 0) {
                saasIosProvision.setType("enterprise");
                //saasIosProvision.setType("ad-hoc");
            } else {
                saasIosProvision.setType("app-store");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("上传的不是有效的描述文件!");
        }
        return saasIosProvision;
    }

    //匹配单个key值/boolean值
    private static String regexSingle(String keyword, String str, String col) {
        String regex = "";
        String value = "";
        if (StringUtils.isNotBlank(col)) {
            //单个key值
            regex = "<key>" + keyword + "</key><" + col + ">" + "(.*?)" + "</" + col + ">";
        } else {
            //bool值
            regex = "<key>" + keyword + "</key><" + "(.*?)" + "/>";
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(str);
        List<String> fieldList = new ArrayList<>();
        while (m.find()) {
            if (StringUtils.isNotEmpty(m.group(1).trim())) {
                fieldList.add(m.group(1).trim());
            }
        }
        if (!fieldList.isEmpty()) {
            value = fieldList.get(0);
        }
        return value;
    }

    //匹配多个array
    private static List<String> regexMulti(String keyword, String str, String col) {
        //正则表达式
        String resultStr = regexSingle(keyword, str, "array");
        String regex = "<" + col + ">" + "(.*?)" + "</" + col + ">";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(resultStr);
        //匹配的有多个
        List<String> fieldList = new ArrayList<>();
        while (m.find()) {
            if (StringUtils.isNotEmpty(m.group(1).trim())) {
                fieldList.add(m.group(1).trim());
            }
        }
        return fieldList;
    }

}
