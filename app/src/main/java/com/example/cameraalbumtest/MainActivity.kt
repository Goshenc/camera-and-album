package com.example.cameraalbumtest

import android.app.Activity
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.FileProvider
import java.io.File
import android.provider.MediaStore
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import com.example.cameraalbumtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val takePhoto = 1
    val fromAlbum = 2
    lateinit var imageUri: Uri
    lateinit var outputImage: File
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.fromAlbumBtn.setOnClickListener(){
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            //这表示只显示图片文件。
            //如果想选择其他类型的文件，比如 PDF，可以将类型改为 "application/pdf"。
            intent.type="image/*"
            startActivityForResult(intent,fromAlbum)

        }
        binding.takePhotoBtn.setOnClickListener {
            // 创建File对象，用于存储拍照后的图片
            //使用 externalCacheDir 作为存储目录，创建一个名为 output_image.jpg 的文件。externalCacheDir 是一个临时的外部存储目录，用于存放应用的缓存数据。
            outputImage = File(externalCacheDir, "output_image.jpg")
            if (outputImage.exists()) {
                outputImage.delete()
            }
            outputImage.createNewFile() 
            //imageUri 是一个 Uri 对象，指定照片的存储路径
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "com.example.cameraalbumtest.fileprovider", outputImage);
            } else {
                Uri.fromFile(outputImage);
            }
            // 启动相机程序
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, takePhoto)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 将拍摄的照片显示出来
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                    binding.imageView.setImageBitmap(rotateIfRequired(bitmap))
                }
            }
            fromAlbum->{
                if(resultCode==Activity.RESULT_OK&&data!=null) {
                    //这里 data.data 代表返回的图片 URI
                data.data?.let {
                    urii->
                    val bitmap=getBitmapFromUri(urii)
                    binding.imageView.setImageBitmap(bitmap)
                }
                }
                }
            }

        }



    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix() // 创建 Matrix 变换矩阵，Matrix 是 Android 中 用于处理 2D 变换（如旋转、缩放、平移、倾斜等） 的类。
        matrix.postRotate(degree.toFloat())// 让矩阵旋转 degree 角度
        //Bitmap.createBitmap() 用于创建新的 Bitmap，并将旋转后的内容绘制到新 Bitmap 上。
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,  // 原始 Bitmap
            0, 0,  // 起始坐标 (x, y)
            bitmap.width, bitmap.height,  // 选取原始 Bitmap 的宽高
            matrix,  // 旋转变换矩阵
            true  // 是否开启抗锯齿，避免旋转后图像模糊
        )

        bitmap.recycle()
        return rotatedBitmap
    }
    //调用 contentResolver.openFileDescriptor 方法以读取模式 "r" 打开传入 Uri 指定的文件，返回一个 ParcelFileDescriptor 对象。
    private fun getBitmapFromUri(uri: Uri)=contentResolver.openFileDescriptor(uri,"r")?.use{
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    //在 use 块中，it 表示非空的 ParcelFileDescriptor。
    //调用 BitmapFactory.decodeFileDescriptor 方法，通过文件描述符解码生成 Bitmap 对象。
        }
}
