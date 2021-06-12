package dev.rifqimfahmi.betterimageupload

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import dev.rifqimfahmi.betterimageupload.util.DefaultImageUtil
import dev.rifqimfahmi.betterimageupload.util.ImageUtil
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var originalImg: ImageView? = null
    private var originalDimension: TextView? = null
    private var originalSize: TextView? = null
    private var originalLoadingIndicator: ProgressBar? = null
    private var optimizedImg: ImageView? = null
    private var optimizedDimension: TextView? = null
    private var optimizedSize: TextView? = null
    private var optimizedLoadingIndicator: ProgressBar? = null
    private var btnChooseImg: Button? = null

    // TODO: refactor use dependency injection
    private val imageUtil: ImageUtil = DefaultImageUtil()

    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = SupervisorJob() + Dispatchers.IO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindView()
        setupBtnChooseImg()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
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
        originalLoadingIndicator = findViewById(R.id.pg_original)
        optimizedImg = findViewById(R.id.optimized_image)
        optimizedDimension = findViewById(R.id.optimized_dimension)
        optimizedSize = findViewById(R.id.optimized_size)
        optimizedLoadingIndicator = findViewById(R.id.pg_optimized)
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

    private fun loadOriginalImage(imageUri: Uri) {
        coroutineScope.launch {
            startLoadingOriginalImage()
            val ctx: Context = this@MainActivity
            val fileSize = imageUtil.getImageFileSize(ctx, imageUri)
            val bitmap = imageUtil.decodeImageMetaData(ctx, imageUri)
            stopLoadingOriginalImage()
            updateOriginalInputData(fileSize, bitmap, imageUri)
        }
    }

    private fun optimizeImageBeforeUpload(imageUri: Uri) {
        coroutineScope.launch {
            startLoadingOptimizedImage()
            val ctx = this@MainActivity
            val scaledBitmap = ImageScaler.scaleBitmap(
                ctx, imageUri, MAX_PHOTO_SIZE, MAX_PHOTO_SIZE, true
            )
            val optimizedImagePath = ImageOptimizer.scaleAndSaveImage(
                scaledBitmap, Bitmap.CompressFormat.JPEG,
                MAX_PHOTO_SIZE, MAX_PHOTO_SIZE,
                80, 101, 101
            ) ?: return@launch
            val uri = Uri.fromFile(File(optimizedImagePath))
            val fileSize = imageUtil.getImageFileSize(ctx, uri)
            val bmOptions = imageUtil.decodeImageMetaData(ctx, uri)
            stopLoadingOptimizedImage()
            updateOptimizedOutputData(fileSize, bmOptions, uri)
        }
    }

    private suspend fun startLoadingOriginalImage() {
        withContext(Dispatchers.Main) {
            originalLoadingIndicator?.visibility = View.VISIBLE
            originalSize?.text = "..calculating..."
            originalDimension?.text = "..calculating..."
        }
    }

    private suspend fun stopLoadingOriginalImage() {
        withContext(Dispatchers.Main) {
            originalLoadingIndicator?.visibility = View.GONE
            originalSize?.text = "-"
            originalDimension?.text = "-"
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun updateOriginalInputData(
        fileSize: Long,
        bitmap: BitmapFactory.Options?,
        imageUri: Uri
    ) {
        bitmap ?: return
        withContext(Dispatchers.Main) {
            originalSize?.text = "${fileSize / 1024} KB"
            originalDimension?.text = "${bitmap.outWidth}x${bitmap.outHeight}"
            Glide.with(this@MainActivity).load(imageUri).into(originalImg!!)
        }
    }

    private suspend fun startLoadingOptimizedImage() {
        withContext(Dispatchers.Main) {
            optimizedImg?.setImageDrawable(null)
            optimizedLoadingIndicator?.visibility = View.VISIBLE
            optimizedSize?.text = "..calculating..."
            optimizedDimension?.text = "..calculating..."
        }
    }

    private suspend fun stopLoadingOptimizedImage() {
        withContext(Dispatchers.Main) {
            optimizedLoadingIndicator?.visibility = View.GONE
            optimizedSize?.text = "-"
            optimizedDimension?.text = "-"
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun updateOptimizedOutputData(
        fileSize: Long,
        bmOptions: BitmapFactory.Options?,
        uri: Uri?
    ) {
        bmOptions ?: return
        withContext(Dispatchers.Main) {
            optimizedSize?.text = "${fileSize / 1024} KB"
            optimizedDimension?.text = "${bmOptions.outWidth}x${bmOptions.outHeight}"
            Glide.with(this@MainActivity).load(uri).into(optimizedImg!!)
        }
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