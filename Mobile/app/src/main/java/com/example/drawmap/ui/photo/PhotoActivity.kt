package com.example.drawmap.ui.photo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.drawmap.R

class PhotoActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_URI = "extra_uri"
        fun start(context: Context, uri: String) {
            val i = Intent(context, PhotoActivity::class.java)
            i.putExtra(EXTRA_URI, uri)
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // guard: if layout not found, use fallback
        setContentView(R.layout.activity_photo)

        val uri = intent.getStringExtra(EXTRA_URI)
        val iv = findViewById<ImageView?>(R.id.ivPhoto)
        if (iv == null) return

        if (!uri.isNullOrEmpty()) {
            val u: Uri = try { uri.toUri() } catch (_: Exception) { Uri.EMPTY }
            if (u != Uri.EMPTY) {
                iv.setImageURI(u)
            } else {
                iv.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            iv.setImageResource(R.drawable.ic_launcher_background)
        }
    }
}
