package com.wellee.bsdiff;

/**
 * @author : liwei
 * 创建日期 : 2019/10/28 14:58
 * 邮   箱 : liwei@worken.cn
 * 功能描述 :
 */
public class BsPatchUtil {

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * @param oldApkPath 旧apk文件路径
     * @param patchPath  生成的差分包的存储路径
     * @param newApkPath 新apk文件路径
     */
    public static native void patch(String oldApkPath, String patchPath, String newApkPath);

}
