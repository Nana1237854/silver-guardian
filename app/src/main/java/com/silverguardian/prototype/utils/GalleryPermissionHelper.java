package com.silverguardian.prototype.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * 相册权限 + 打开图库的工具类。
 * 消除 AlbumFragment / ChildModeActivity 中的重复权限代码。
 */
public class GalleryPermissionHelper {
    public static final int REQUEST_READ_IMAGES = 1000;
    public static final int PICK_IMAGE = 1001;

    private final Fragment fragment;
    private Runnable onGranted;

    public GalleryPermissionHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    /** 检查权限，授权后执行回调；权限缺失则弹出系统对话框 */
    public void requestPermissionThen(Runnable onGranted) {
        this.onGranted = onGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_IMAGES);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_IMAGES);
                return;
            }
        }
        onGranted.run();
    }

    /** 在 Fragment.onRequestPermissionsResult 中调用 */
    public boolean onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_READ_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (onGranted != null) onGranted.run();
            }
            return true;
        }
        return false;
    }

    /** 打开系统图库（应确认权限后再调用） */
    public void openGallery() {
        fragment.startActivityForResult(
            new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE);
    }

    /** 在 Fragment.onActivityResult 中调用，返回选中的 URI 或 null */
    public static android.net.Uri handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            return data.getData();
        }
        return null;
    }
}
