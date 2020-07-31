package com.wangxile.appparse;

import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.io.IOUtils;
import org.python.core.PyFunction;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * @author wangqi
 * @version 1.0
 * @date 2020/6/12 0012 14:21
 */
public class AppInfoUtils {

    public static AppInfo infoFromAPKMeta(ApkMeta meta) {
        if (Objects.isNull(meta)) {
            return null;
        }
        AppInfo info = new AppInfo();
        info.setVersionCode(meta.getVersionCode());
        info.setVersionName(meta.getVersionName());
        info.setIcon(meta.getIcon());
        info.setPackageName(meta.getPackageName());
        info.setLabel(meta.getLabel());
        info.setFileSize(0);
        info.setMinSdkVersion(meta.getMinSdkVersion());
        info.setMinSdkString(minLevelString(0, info.getMinSdkVersion()));

        return info;
    }

    public static AppInfo infoFromIPAMeta(IPAInfo meta, File file) {

        if (Objects.isNull(meta)) {
            return null;
        }
        AppInfo info = new AppInfo();
        info.setVersionCode(Long.parseLong(meta.getBundleVersionString().replace(".", "")));
        info.setVersionName(meta.getBundleVersionString());
        info.setIcon(meta.getBundleIconFileName());
        info.setPackageName(meta.getBundleIdentifier());
        info.setLabel(meta.getBundleName());
        info.setFileSize(meta.getFileSize());
        info.setMinSdkVersion(meta.getMinimumOSVersion());
        info.setMinSdkString(minLevelString(1, info.getMinSdkVersion()));

        try {

            InputStream pyInput = AppInfoUtils.class.getClassLoader().getResourceAsStream("ipin.py");

            String pngPath = file.getParent() + "/icon_tmp.png";

            IOUtils.write(meta.getBundleIcon(), new FileOutputStream(pngPath));

            String[] args = {pngPath};
            PythonInterpreter.initialize(System.getProperties(), System.getProperties(), args);
            PythonInterpreter interpreter = new PythonInterpreter();
            interpreter.execfile(pyInput);

            info.setIconData(IOUtils.toByteArray(new FileInputStream(pngPath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static AppInfo infoFromIPAMeta(IPAInfo meta, String imgPath, Boolean isGetIcon) {
        if (Objects.isNull(meta)) {
            return null;
        }
        AppInfo info = new AppInfo();
        info.setVersionCode(Long.parseLong(meta.getBundleVersionString().replace(".", "")));
        info.setVersionName(meta.getBundleVersionString());
        info.setIcon(meta.getBundleIconFileName());
        info.setPackageName(meta.getBundleIdentifier());
        info.setLabel(meta.getBundleName());
        info.setFileSize(meta.getFileSize());
        info.setMinSdkVersion(meta.getMinimumOSVersion());
        info.setMinSdkString(minLevelString(1, info.getMinSdkVersion()));

        //判断是否获取图标
        if (isGetIcon) {
            FileInputStream fileInputStream = null;
            FileOutputStream fileOutputStream = null;
            PythonInterpreter interpreter = null;
            InputStream pyInput = null;
            try {
                pyInput = AppInfoUtils.class.getClassLoader().getResourceAsStream("ipin.py");
                fileOutputStream = new FileOutputStream(imgPath);

                IOUtils.write(meta.getBundleIcon(), fileOutputStream);

                //linux上需要设置jar包位置,不然会报错
                Properties props = new Properties();
                props.put("python.home", System.getProperty("user.dir") + File.separator + "jython-standalone");
                props.put("python.import.site", "false");

                //配置环境变量并初始化
                String[] args = {};
                PythonInterpreter.initialize(System.getProperties(), props, args);
                interpreter = new PythonInterpreter();
                interpreter.execfile(pyInput);

                //调用python脚本文件中的函数
                PyFunction pyFunction = interpreter.get("updatePNG", PyFunction.class);
                pyFunction.__call__(new PyString(imgPath));

                fileInputStream = new FileInputStream(imgPath);
                info.setIconData(IOUtils.toByteArray(fileInputStream));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (Objects.nonNull(interpreter)) {
                        interpreter.close();
                    }
                    if (Objects.nonNull(fileInputStream)) {
                        fileInputStream.close();
                    }
                    if (Objects.nonNull(fileOutputStream)) {
                        fileOutputStream.close();
                    }
                    if (Objects.nonNull(pyInput)) {
                        pyInput.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return info;
    }

    private static String minLevelString(int type, String minSdk) {

        if (minSdk == null) {
            return "";
        }
        if (type == 0) {
            String target = "";
            switch (Integer.parseInt(minSdk)) {
                case 1:
                    target = "1.0";
                    break;
                case 2:
                    target = "1.1";
                    break;
                case 3:
                    target = "1.5";
                    break;
                case 4:
                    target = "1.6";
                    break;
                case 5:
                    target = "2.0";
                    break;
                case 6:
                    target = "2.0.1";
                    break;
                case 7:
                    target = "2.1.x";
                    break;
                case 8:
                    target = "2.2.x";
                    break;
                case 9:
                    target = "2.3.0/1/2";
                    break;
                case 10:
                    target = "2.3.3/4";
                    break;
                case 11:
                    target = "3.0.x";
                    break;
                case 12:
                    target = "3.1.x";
                    break;
                case 13:
                    target = "3.2";
                    break;
                case 14:
                    target = "4.0.0/1/2";
                    break;
                case 15:
                    target = "4.0.3/4";
                    break;
                case 16:
                    target = "4.1";
                    break;
                case 17:
                    target = "4.2";
                    break;
                case 18:
                    target = "4.3";
                    break;
                case 19:
                    target = "4.4";
                    break;
                case 20:
                    target = "4.4W";
                    break;
                case 21:
                    target = "5.0";
                    break;
                case 22:
                    target = "5.1";
                    break;
                case 23:
                    target = "6.0";
                    break;
                case 24:
                    target = "7.0";
                    break;
                case 25:
                    target = "8.0";
                    break;
            }
            return "Android " + target;
        } else {
            return "iOS " + minSdk;
        }
    }

}
