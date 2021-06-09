package dev.rifqimfahmi.betterimageupload

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var btnChooseImg: Button? = null

    private var originalImg: ImageView? = null
    private var originalDimension: TextView? = null
    private var originalSize: TextView? = null

    private var optimizedImg: ImageView? = null
    private var optimizedDimension: TextView? = null
    private var optimizedSize: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindView()
        setupBtnChooseImg()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_PICTURE) {
            onSuccessReturnFromChooseImage(data)
        }
    }

    private fun bindView() {
        btnChooseImg = findViewById(R.id.btn_choose_image)
        originalImg = findViewById(R.id.original_image)
        originalDimension = findViewById(R.id.original_dimension)
        originalSize = findViewById(R.id.original_size)
        optimizedImg = findViewById(R.id.optimized_image)
        optimizedDimension = findViewById(R.id.optimized_dimension)
        optimizedSize = findViewById(R.id.optimized_size)
    }

    private fun setupBtnChooseImg() {
        btnChooseImg?.setOnClickListener {
            if (!isPermissionGranted()) {
                requestRequiredPermission()
            } else {
                chooseImage()
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        val chooser = Intent.createChooser(intent, "Select Picture")
        startActivityForResult(chooser, REQUEST_SELECT_PICTURE)
    }

    private fun onSuccessReturnFromChooseImage(data: Intent?) {
        val imageUri = data?.data ?: return
        loadOriginalImage(imageUri)
        optimizeImageBeforeUpload(imageUri)
    }

    @SuppressLint("SetTextI18n")
    private fun loadOriginalImage(imageUri: Uri) {
        val fileSize = updateImageMetaDataSize(imageUri)
        val bitmap = updateImageMetaDataDimension(imageUri) ?: return
        originalSize?.text = "${fileSize / 1024} KB"
        originalDimension?.text = "${bitmap.outWidth}x${bitmap.outHeight}"
        Glide.with(this).load(imageUri).into(originalImg!!)
    }

    @SuppressLint("SetTextI18n")
    private fun optimizeImageBeforeUpload(imageUri: Uri) {
        val scaledBitmap = ImageLoader.loadBitmap(
            this, imageUri, MAX_PHOTO_SIZE, MAX_PHOTO_SIZE, true
        )
        val optimizedImagePath = ImageLoader.scaleAndSaveImage(
            scaledBitmap,
                MAX_PHOTO_SIZE,
                MAX_PHOTO_SIZE,
                80,
                101,
                101
            )
        val imageUri = Uri.fromFile(File(optimizedImagePath))
        val fileSize = updateImageMetaDataSize(imageUri)
        val bmOptions = updateImageMetaDataDimension(imageUri) ?: return
        optimizedSize?.text = "${fileSize / 1024} KB"
        optimizedDimension?.text = "${bmOptions.outWidth}x${bmOptions.outHeight}"
        Glide.with(this).load(imageUri).into(optimizedImg!!)
    }

    private fun updateImageMetaDataSize(imageUri: Uri): Long {
        var input: FileInputStream? = null
        try {
            input = contentResolver.openInputStream(imageUri) as FileInputStream
            return input.channel.size()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
        }
        return 0
    }

    private fun updateImageMetaDataDimension(imageUri: Uri): BitmapFactory.Options? {
        var input: InputStream? = null
        try {
            val bmOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            input = contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(input, null, bmOptions)
            return bmOptions
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
        }
        return null
    }

    private fun isPermissionGranted(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRequiredPermission() {
        ActivityCompat.requestPermissions(
            this,
            PERMISSIONS_STORAGE,
            REQUEST_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val REQUEST_SELECT_PICTURE = 1
        const val REQUEST_EXTERNAL_STORAGE = 2
        const val MAX_PHOTO_SIZE: Float = 1280f
    }
}