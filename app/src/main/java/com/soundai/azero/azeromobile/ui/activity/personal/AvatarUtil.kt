package com.soundai.azero.azeromobile.ui.activity.personal

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

object AvatarUtil {

    fun createTakePhotoFile(context: Context) =
        File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")

    fun takePhoto(activity: Activity, cameraImageUri: Uri) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        activity.startActivityForResult(intent, PersonalInfoActivity.REQUEST_CODE_CAMERA)
    }

    fun pickPhoto(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        activity.startActivityForResult(intent, PersonalInfoActivity.REQUEST_CODE_ALBUM)
    }

    fun createCameraUri(context: Context, imageFile: File) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
        } else {
            Uri.fromFile(imageFile)
        }

    fun cropPhoto(activity: Activity, srcUri: Uri?, cropImageUri: Uri) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(srcUri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", 300)
        intent.putExtra("outputY", 300)
        intent.putExtra("scale", true)
        intent.putExtra("return-data", false)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        activity.startActivityForResult(intent, PersonalInfoActivity.REQUEST_CODE_CROP)
    }

    fun createCropFile(context: Context) =
        File(context.externalCacheDir, "${System.currentTimeMillis()}_crop.jpg")

    fun createCropUri(cropFile: File) = Uri.fromFile(cropFile)
}