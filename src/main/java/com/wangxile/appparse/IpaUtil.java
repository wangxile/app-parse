package com.wangxile.appparse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author wangqi
 * @version 1.0
 * @date 2020/6/23 0023 10:13
 * <p>
 * 解析ipa包，根据描述文件embedded.mobileprovision，获取过期时间和打包类型
 */
public class IpaUtil {
    public static SaasIosProvision readIPA(File file) {
        SaasIosProvision saasIosProvision = null;
        try {
            ZipFile zipFile = new ZipFile(file);
            InputStream is = new FileInputStream(file);
            ZipInputStream zipIns = new ZipInputStream(is);
            ZipEntry ze;
            while ((ze = zipIns.getNextEntry()) != null) {
                if (!ze.isDirectory()) {
                    String name = ze.getName();
                    if (null != name && name.toLowerCase().contains(".app/embedded.mobileprovision")) {
                        saasIosProvision = IosPackUtil.resolveProvision(zipFile.getInputStream(ze));
                        break;
                    }
                }
            }
            is.close();
            zipIns.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("解析IOS描述文件失败");
        }
        return saasIosProvision;
    }
}
