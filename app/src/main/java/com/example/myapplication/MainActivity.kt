package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.FileProvider
import com.example.myapplication.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { _ ->
            shareImageResource(R.drawable.test_image, "test_image")
        }
    }
    fun shareImageResource(resourceId: Int, resourceName: String) {
        // 画像リソースを一時的なファイルにコピー
        val imageFile = File(externalCacheDir, "$resourceName.jpg")
        resources.openRawResource(resourceId).use { input ->
            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }
        }

        // FileProviderを通じて一時ファイルのUriを取得
        val imageUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            imageFile
        )

        // Uriを共有Intentに渡す
        shareTextAndImage("シェアしたいテキストです", imageUri)
    }

    fun shareTextAndImage(text: String, imageUri: Uri) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Get all the potential intents to handle the shared file
        val resInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)

        // Grant all of them permission in case the user selects an app that was not in the chooser dialog
        for (resolveInfo in resInfoList) {
            val packageName: String = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share images to.."))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}