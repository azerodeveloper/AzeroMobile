package com.soundai.azero.azeromobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object BitmapUtils {
    fun createBlurredImageFromBitmap(
        bitmap: Bitmap,
        context: Context,
        inSampleSize: Int
    ): Drawable {

        val rs = RenderScript.create(context)
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageInByte = stream.toByteArray()
        val bis = ByteArrayInputStream(imageInByte)
        val blurTemplate = BitmapFactory.decodeStream(bis, null, options)

        val input = Allocation.createFromBitmap(rs, blurTemplate)
        val output = Allocation.createTyped(rs, input.getType())
        val script = ScriptIntrinsicBlur.create(
            rs,
            Element.U8_4(rs)

        )
        script.setRadius(18f)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(blurTemplate)

        return BitmapDrawable(context.resources, blurTemplate)
    }
}