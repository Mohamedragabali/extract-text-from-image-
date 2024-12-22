package com.example.extracttextfromimage

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException


class MainActivity : AppCompatActivity() {
    companion object{
        private val TAG= MainActivity::class.simpleName
    }
    private lateinit var imgView: ImageView
    private lateinit var tvText: TextView
    private lateinit var image: InputImage
    private lateinit var progressBar: ProgressBar
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    processImage(uri) // Your function to handle the image
                }
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        imgView=findViewById(R.id.imgView)
        tvText= findViewById(R.id.tvText)
        progressBar= findViewById(R.id.progressBar)
        imgView.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker(){
        ImagePicker.with(this)
            .crop()                // Crop image (optional)
            .compress(1024)        // Compress image to less than 1 MB (optional)
            .maxResultSize(1080, 1080) // Set maximum resolution (optional)
            .galleryOnly()         // Optional: Choose from gallery only
            .createIntent { pickerIntent ->
                imagePickerLauncher.launch(pickerIntent) // Pass the intent to the launcher
            }

    }
    private fun processImage(fileUri: Uri){
        tvText.text = ""
        progressBar.visibility = View.VISIBLE
        try {
            image = InputImage.fromFilePath(this, fileUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            //Image Processing
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully
                    //This VisionText holds the actual text information
                    Log.d(TAG, "processImage:success ")
                    val resultText = visionText.text
                    Log.d(TAG, "processImage: extractedText:"+resultText)

                    if(TextUtils.isEmpty(resultText)){
                        // show message
                        Toast.makeText(this,"No text found in the image!", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        progressBar.visibility = View.GONE
                        // set TextView
                        tvText.text=resultText
                    }


                }
                .addOnFailureListener {
                    // Task failed with an exception
                    Log.e(TAG, "processImage: failure:" )
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}