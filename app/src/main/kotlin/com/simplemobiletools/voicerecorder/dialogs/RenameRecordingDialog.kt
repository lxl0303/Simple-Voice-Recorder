package com.simplemobiletools.voicerecorder.dialogs

import android.content.ContentUris
import android.content.ContentValues
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isQPlus
import com.simplemobiletools.voicerecorder.R
import com.simplemobiletools.voicerecorder.models.Recording
import kotlinx.android.synthetic.main.dialog_rename_recording.view.*

class RenameRecordingDialog(val activity: BaseSimpleActivity, val recording: Recording, val callback: () -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_recording, null).apply {
            rename_recording_title.setText(recording.title.substringBeforeLast('.'))
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.rename) {
                    showKeyboard(view.rename_recording_title)
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val newTitle = view.rename_recording_title.value
                        if (newTitle.isEmpty()) {
                            activity.toast(R.string.empty_name)
                            return@setOnClickListener
                        }

                        if (!newTitle.isAValidFilename()) {
                            activity.toast(R.string.invalid_name)
                            return@setOnClickListener
                        }

                        ensureBackgroundThread {
                            if (isQPlus()) {
                                updateMediaStoreTitle(recording, newTitle)
                            }

                            activity.runOnUiThread {
                                callback()
                                dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun updateMediaStoreTitle(recording: Recording, newTitle: String) {
        val baseUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = ContentUris.withAppendedId(baseUri, recording.id.toLong())
        val oldExtension = recording.title.getFilenameExtension()
        val newDisplayName = "${newTitle.removeSuffix(".$oldExtension")}.$oldExtension"

        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.TITLE, newTitle.substringAfterLast('.'))
            put(MediaStore.Audio.Media.DISPLAY_NAME, newDisplayName)
        }

        activity.contentResolver.update(uri, values, null, null)
    }
}
