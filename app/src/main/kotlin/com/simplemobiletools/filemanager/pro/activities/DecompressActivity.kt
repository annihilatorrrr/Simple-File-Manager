package com.simplemobiletools.filemanager.pro.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.adapters.DecompressItemsAdapter
import com.simplemobiletools.filemanager.pro.extensions.config
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.activity_decompress.*
import java.io.BufferedInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DecompressActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decompress)
        val uri = intent.data
        if (uri == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        val realPath = getRealPathFromURI(uri)
        title = realPath?.getFilenameFromPath() ?: uri.toString().getFilenameFromPath()

        try {
            val listItems = getListItems(uri)
            DecompressItemsAdapter(this, listItems, decompress_list) {

            }.apply {
                decompress_list.adapter = this
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_decompress, menu)
        updateMenuItemColors(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.decompress -> decompressFiles()
        }

        return true
    }

    private fun decompressFiles() {
        val defaultFolder = getRealPathFromURI(intent.data!!) ?: internalStoragePath
        FilePickerDialog(this, defaultFolder, false, config.showHidden, true, true) {

        }
    }

    @SuppressLint("NewApi")
    private fun getListItems(uri: Uri): ArrayList<ListItem> {
        val listItems = ArrayList<ListItem>()
        val inputStream = contentResolver.openInputStream(uri)
        val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
        var zipEntry: ZipEntry?
        while (true) {
            zipEntry = zipInputStream.nextEntry

            if (zipEntry == null) {
                break
            }

            val lastModified = if (isOreoPlus()) zipEntry.lastModifiedTime.toMillis() else 0
            val listItem = ListItem(zipEntry.name, zipEntry.name, zipEntry.isDirectory, 0, 0L, lastModified, false)
            listItems.add(listItem)
        }
        return listItems
    }
}
