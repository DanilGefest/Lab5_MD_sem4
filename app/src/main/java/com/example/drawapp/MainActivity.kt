package com.example.drawapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.Objects
import kotlin.random.Random


val REQUEST_IMAGE = 1

class MainActivity : AppCompatActivity() {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // результат интента для загрузки изображиний
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_IMAGE && data != null) {
            val uri: Uri? = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

            val dv = findViewById<DrawingView>(R.id.drawingView)
            dv.loadBackground(bitmap)
        }
    }
    fun saveImage(bitmap: Bitmap, fileName: String) {
        val fos: OutputStream
        try{
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
                val resolver = contentResolver
                val contentValue = ContentValues()
                contentValue.put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.jpg")
                contentValue.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                contentValue.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)!!
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                Objects.requireNonNull<OutputStream?>(fos)
                Toast.makeText(this, "Изображение сохранено", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dv = findViewById<DrawingView>(R.id.drawingView)
        val blackBnt = findViewById<Button>(R.id.blackBtn)
        val redBtn = findViewById<Button>(R.id.redBtn)
        val greenBtn = findViewById<Button>(R.id.greenBtn)
        val blueBtn = findViewById<Button>(R.id.blueBtn)
        val numberText = findViewById<EditText>(R.id.editTextNumber)
        val thicBtn = findViewById<Button>(R.id.thicBtn)
        val loadBtn = findViewById<Button>(R.id.LoadButton)
        val uploadBtn = findViewById<Button>(R.id.uploadButton)
        val buttonClear = findViewById<Button>(R.id.ButtonClear)

        blackBnt.setOnClickListener{
            dv.changeColor(Color.BLACK)
        }
        buttonClear.setOnClickListener{
            dv.changeColor(Color.WHITE)
        }

        redBtn.setOnClickListener{
            dv.changeColor(Color.RED)
        }

        greenBtn.setOnClickListener{
            dv.changeColor(Color.GREEN)
        }

        blueBtn.setOnClickListener{
            dv.changeColor(Color.BLUE)
        }

        thicBtn.setOnClickListener{
            val text = numberText.text.toString().toFloat()
            try {
                dv.changeThickness(text)
            }
            catch (e: Exception){
                Log.v("thic error", e.message!!)
            }
        }

        loadBtn.setOnClickListener {      // интент на загнрузку изображения
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }

        uploadBtn.setOnClickListener {
            val popupMenu = PopupMenu(this@MainActivity, uploadBtn)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->

                if(menuItem.title == "Save"){
                    val bitmap = dv.getBitMap()
                    saveImage(bitmap, Random.hashCode().toString())
                }
                if(menuItem.title == "Share"){
                    val contentUri: Uri = getImageUri(this, dv.getBitMap())

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "image/jpg"
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(intent, "Share"))
                }
                true
            }
            popupMenu.show()
        }
    }
}