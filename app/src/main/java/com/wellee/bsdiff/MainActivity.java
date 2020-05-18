package com.wellee.bsdiff;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int INSTALL_PACKAGES_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 200);
            }
        }
        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(BuildConfig.VERSION_NAME);
    }

    @SuppressLint("StaticFieldLeak")
    public void patch(View view) {
        AsyncTask<Void, Void, File> task = new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                String oldApkPath = getApplicationInfo().sourceDir;
                File root = Environment.getExternalStorageDirectory() ;
                if (root != null && root.exists()) {
                    String parentPath = root.getAbsolutePath();
                    File patchFile = new File(parentPath, "patch.patch");
                    File newApk = createNewApk(parentPath);
                    BsPatchUtil.patch(oldApkPath, patchFile.getAbsolutePath(), newApk.getAbsolutePath());
                    return newApk;
                }
                return null;
            }

            @Override
            protected void onPostExecute(File file) {
                checkIsAndroidO(file);
            }
        };
        task.execute();
    }

    private File createNewApk(String parentPath) {
        File outFile = new File(parentPath, "new.apk");
        try {
            if (!outFile.exists()) {
                boolean newFile = outFile.createNewFile();
                Log.d("createNewApk", "success = " + newFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFile;
    }

    /**
     * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private void checkIsAndroidO(File apkFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                installApk(apkFile);
            } else {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUEST_CODE);
            }
        } else {
            installApk(apkFile);
        }
    }


    private void installApk(File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            return;
        }
        Intent intent = new Intent();
        //判断是否是android 7.0及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //7.0获取存储文件的uri
            Uri uri = FileProvider.getUriForFile(this, "com.wellee.bsdiff.fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //赋予临时权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //设置dataAndType
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

}
