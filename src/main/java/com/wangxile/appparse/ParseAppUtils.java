package com.wangxile.appparse;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.Icon;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author wangqi
 * @version 1.0
 * @date 2020/6/12 0012 14:21
 */
public class ParseAppUtils {

    public static void main(String[] args) throws Exception {
        String localDir = "E:\\";
        String filePath = "E:\\88.ipa";
        File file = new File(filePath);
        AppInfo appInfo = parsePackage(file, localDir, true);
        System.out.println(appInfo);
    }

    public static AppInfo parseURL(String url, String localDir, Boolean isGetIcon) {
        int index = url.lastIndexOf("/");
        String name = url.substring(index + 1);
        name = name.trim();
        downloadFileFromURL(url, localDir, name);

        File file = new File(localDir + name);
        String uuid = UUID.randomUUID().toString();
        String imgPath = localDir + uuid + ".png";
        try {
            AppInfo appInfo = parse(file, imgPath, isGetIcon);
            return appInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static AppInfo parsePackage(File file, String localPath, Boolean isGetIcon) {
        String uuid = UUID.randomUUID().toString();
        String imgPath = localPath + uuid + ".png";
        try {
            return parse(file, imgPath, isGetIcon);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析apk ipa 并在指定路径下生成图标
     *
     * @param targetFile 待解析文件
     * @param imgPath    图标路径
     * @return
     * @throws Exception
     */
    private static AppInfo parse(File targetFile, String imgPath, Boolean isGetIcon) throws Exception {
        AppInfo appInfo = new AppInfo();
        String fileName = targetFile.getName();
        if (fileName.endsWith(".apk")) {
            //解析apk
            try (ApkFile apkFile = new ApkFile(targetFile)) {
                //设置语言,防止解析名称错误
                apkFile.setPreferredLocale(Locale.CHINA);
                ApkMeta apkMeta = apkFile.getApkMeta();
                appInfo = AppInfoUtils.infoFromAPKMeta(apkMeta);
                appInfo.setPlatformType(0);

                //判断是否解析图标
                if (isGetIcon) {
                    createApkIcon(apkFile, imgPath);
                }
            }
        } else if (fileName.endsWith(".ipa")) {
            //解析ipa
            IPAReader reader = new IPAReader();
            IPAInfo info = reader.parse(targetFile);
            appInfo = AppInfoUtils.infoFromIPAMeta(info, imgPath, isGetIcon);
            appInfo.setPlatformType(1);

            //IOS 需要获取到期时间和打包类型
            SaasIosProvision saasIosProvision = IpaUtil.readIPA(targetFile);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date date = df.parse(saasIosProvision.getExpirationDate());
            appInfo.setExpirationDate(date);
            appInfo.setPackType(saasIosProvision.getType());
        } else {
            throw new RuntimeException("can't find apk or ipa suffix");
        }
        System.out.println("Make Picture success,Please find image in " + imgPath);
        appInfo.setIconPath(imgPath);
        appInfo.setFileSize(targetFile.length());
        return appInfo;
    }

    public static void createApkIcon(ApkFile apkFile, String imgPath) throws IOException {
        List<Icon> icons = apkFile.getIconFiles();
        int i = 0;
        for (Icon icon : icons) {
            String path = icon.getPath();
            System.out.println(path);
            if (path.contains(".png")) {
                String n = "";
                if (path.contains("mdpi")) {
                    n = "mdpi";
                } else if (path.contains("xxxhdpi")) {
                    n = "xxxhdpi";
                } else if (path.contains("xxhdpi")) {
                    n = "xxhdpi";
                } else if (path.contains("xhdpi")) {
                    n = "xhdpi";
                } else if (path.contains("hdpi")) {
                    n = "hdpi";
                }

                byte[] bs = icon.getData();
                FileOutputStream fos = new FileOutputStream(imgPath);
                fos.write(bs);
                fos.flush();
                fos.close();
                i++;
            }
        }
        apkFile.close();
    }

    /**
     * 根据URL下载文件
     *
     * @param url
     * @param dirLocation
     * @param fileName
     * @return
     */
    private static String downloadFileFromURL(String url, String dirLocation, String fileName) {
        try {
            URL httpUrl = new URL(url);
            File file = new File(dirLocation + fileName);
            file.createNewFile();
            FileUtils.copyURLToFile(httpUrl, file);
            System.out.println("下载文件 " + fileName + "成功!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("下载文件 " + fileName + "失败!");
        }
        return "success";
    }

    /**
     * 获取apk icon二进制
     */
    private static byte[] getAppIconData(ApkFile apkFile) {
        try {
            Icon icon = apkFile.getIconFile();
            if (icon == null) {
                return null;
            }
            if (!isValidIconPath(icon.getPath())) {
                icon = pickAvailableIcon(apkFile);
            }
            return icon.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isValidIconPath(String iconPath) {
        return !iconPath.endsWith(".xml");
    }

    private static Icon pickAvailableIcon(ApkFile apkFile) throws Exception {
        Icon maxDensityValidIcon = null;
        for (Icon icon : apkFile.getIconFiles()) {
            if (!isValidIconPath(icon.getPath())) {
                continue;
            }
            int density = icon.getDensity();
            if (maxDensityValidIcon == null || density > maxDensityValidIcon.getDensity()) {
                maxDensityValidIcon = icon;
            }
        }
        return maxDensityValidIcon;
    }
}
