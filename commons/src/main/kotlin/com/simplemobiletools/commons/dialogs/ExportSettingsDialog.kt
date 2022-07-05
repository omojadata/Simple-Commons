package com.simplemobiletools.commons.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_export_settings.view.*

class ExportSettingsDialog(
    val activity: BaseSimpleActivity, val defaultFilename: String, val hidePath: Boolean,
    callback: (path: String, filename: String) -> Unit
) {
    init {
        val layoutId = if (activity.baseConfig.isUsingSystemTheme) {
            R.layout.dialog_export_settings_material
        } else {
            R.layout.dialog_export_settings
        }

        val lastUsedFolder = activity.baseConfig.lastExportedSettingsFolder
        var folder = if (lastUsedFolder.isNotEmpty() && activity.getDoesFilePathExist(lastUsedFolder)) {
            lastUsedFolder
        } else {
            activity.internalStoragePath
        }

        val view = activity.layoutInflater.inflate(layoutId, null).apply {
            findViewById<AppCompatEditText>(R.id.export_settings_filename).setText(defaultFilename)

            if (hidePath) {
                export_settings_path_label.beGone()
                export_settings_path.beGone()
            } else {
                export_settings_path.text = activity.humanizePath(folder)
                export_settings_path.setOnClickListener {
                    FilePickerDialog(activity, folder, false, showFAB = true) {
                        export_settings_path.text = activity.humanizePath(it)
                        folder = it
                    }
                }
            }
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.export_settings) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = view.findViewById<AppCompatEditText>(R.id.export_settings_filename).value
                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val newPath = "${folder.trimEnd('/')}/$filename"
                        if (!newPath.getFilenameFromPath().isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        activity.baseConfig.lastExportedSettingsFolder = folder
                        if (!hidePath && activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(activity.getString(R.string.file_already_exists_overwrite), newPath.getFilenameFromPath())
                            ConfirmationDialog(activity, title) {
                                callback(newPath, filename)
                                alertDialog.dismiss()
                            }
                        } else {
                            callback(newPath, filename)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
