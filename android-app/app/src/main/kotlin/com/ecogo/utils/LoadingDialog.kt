package com.ecogo.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import com.ecogo.R

/**
 * Loading Dialog 工具类
 */
class LoadingDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private var messageTextView: TextView? = null

    fun show(message: String = "Loading...", cancelable: Boolean = false) {
        dismiss()

        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_loading)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(cancelable)

            messageTextView = findViewById(R.id.text_loading_message)
            messageTextView?.text = message

            show()
        }
    }

    fun updateMessage(message: String) {
        messageTextView?.text = message
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
        messageTextView = null
    }

    fun isShowing(): Boolean = dialog?.isShowing == true
}