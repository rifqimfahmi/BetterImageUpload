package dev.rifqimfahmi.betterimageupload

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var btnChooseImg: Button? = null
    private var originalImg: ImageView? = null
    private var originalDimension: TextView? = null
    private var originalSize: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindView()
        setupBtnChooseImg()
    }

    private fun bindView() {
        btnChooseImg = findViewById(R.id.btn_choose_image)
        originalImg = findViewById(R.id.original_image)
        originalDimension = findViewById(R.id.original_dimension)
        originalSize = findViewById(R.id.original_size)
    }

    private fun setupBtnChooseImg() {
        btnChooseImg?.setOnClickListener {
            chooseImage()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_PICTURE) {
            onSuccessReturnFromChooseImage(data)
        }
    }

    private fun onSuccessReturnFromChooseImage(data: Intent?) {
        val imageUri = data?.data ?: return
        loadOriginalImage(imageUri)
    }

    private fun loadOriginalImage(imageUri: Uri) {

    }

    companion object {
        const val REQUEST_SELECT_PICTURE = 1;
    }
}