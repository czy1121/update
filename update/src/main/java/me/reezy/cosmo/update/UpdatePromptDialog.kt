package me.reezy.cosmo.update

import android.app.Dialog
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class UpdatePromptDialog(activity: FragmentActivity, agent: UpdateAgent): Dialog(activity) {

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.update_prompt_view)


        val info = agent.info
        findViewById<TextView>(android.R.id.message)?.apply {
            movementMethod = ScrollingMovementMethod()
            isVerticalScrollBarEnabled = true
            text = info.updateContent
        }

        val vText = findViewById<TextView>(android.R.id.text1)
        val vProgress = findViewById<ProgressBar>(android.R.id.progress)
        vProgress?.max = 100
        agent.addDownloadListener(ProgressDownloadListener(vProgress, vText))


        // Update Now
        findViewById<TextView>(android.R.id.button1)?.setOnClickListener {
            agent.update()
            it.isEnabled = false
            it.alpha = 0.5f
        }

        // Later
        if (!info.isForce) {
            findViewById<TextView>(android.R.id.button2)?.apply {
                visibility = View.VISIBLE
                setOnClickListener { dismiss() }
            }
        }

        // Ignore
        if (!info.isForce && info.isIgnorable) {
            findViewById<TextView>(android.R.id.button3)?.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    agent.ignore()
                    dismiss()
                }
            }
        }

        setOnDismissListener {
            agent.cancel()
        }
    }
}