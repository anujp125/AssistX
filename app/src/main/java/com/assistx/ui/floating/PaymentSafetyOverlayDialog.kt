package com.assistx.ui.floating

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.assistx.ui.haptics.MetroHaptics

/**
 * Modern UI (Metro) Payment Safety Double-Approval Overlay Dialog.
 *
 * Characteristics:
 * - Direct WindowManager overlay modal ensuring instant, reliable presentation over any app or homescreen.
 * - Flat solid rectangular panel with 0dp sharp corners and 2dp StatusAmber (#F0A30A) outline.
 * - Exact typography and copy matching UI Style Guide (§ 4.12) & Decision Log (D-021/D-025):
 *     Title: "Pause AssistX for payments?"
 *     Message: "AssistX will detach its floating button and attempt to stop its Accessibility service. Screen-off and Home gestures will be unavailable while AssistX is paused."
 *     Callout Warning: "Note: To restore these gestures, you may need to re-enable AssistX in Android Accessibility settings."
 * - Two flat Metro buttons with 0dp corners:
 *     1. "Cancel" (Outlined button, dismisses immediately without pausing)
 *     2. "Pause AssistX" (Solid StatusAmber fill with black text, triggers payment safety pause)
 * - Outside scrim click dismisses immediately without pausing.
 */
class PaymentSafetyOverlayDialog(
    context: Context,
    private val onConfirmPause: () -> Unit,
    private val onCancel: () -> Unit
) : FrameLayout(context) {

    private val density = context.resources.displayMetrics.density
    private val haptics = MetroHaptics(context)

    init {
        // Darkened dismiss scrim
        setBackgroundColor(Color.argb(165, 0, 0, 0))

        setOnClickListener {
            haptics.tap()
            onCancel()
        }

        val amberColor = Color.parseColor("#F0A30A") // StatusAmber

        // Metro Flat Dialog Panel
        val dialogPanel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#121214"))
                setStroke((2 * density).toInt(), amberColor)
                cornerRadius = 0f
            }
            background = bgDrawable
            val p = (20 * density).toInt()
            setPadding(p, p, p, p)
            setOnClickListener { /* prevent scrim dismiss on inside click */ }
        }

        val panelParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            val margin = (24 * density).toInt()
            setMargins(margin, 0, margin, 0)
        }

        // 1. Title
        val titleView = TextView(context).apply {
            text = "Pause AssistX for payments?"
            setTextColor(amberColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(Typeface.DEFAULT_BOLD)
            val pb = (10 * density).toInt()
            setPadding(0, 0, 0, pb)
        }
        dialogPanel.addView(titleView)

        // 2. Message
        val messageView = TextView(context).apply {
            text = "AssistX will detach its floating button and attempt to stop its Accessibility service. Screen-off and Home gestures will be unavailable while AssistX is paused."
            setTextColor(Color.parseColor("#E0E0E0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setLineSpacing(3 * density, 1.15f)
            val pb = (14 * density).toInt()
            setPadding(0, 0, 0, pb)
        }
        dialogPanel.addView(messageView)

        // 3. Callout Box (Warning Note)
        val calloutBox = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val calloutBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#1A1A1E"))
                setStroke((1 * density).toInt(), Color.argb(150, 240, 163, 10))
                cornerRadius = 0f
            }
            background = calloutBg
            val cp = (10 * density).toInt()
            setPadding(cp, cp, cp, cp)
        }

        val calloutText = TextView(context).apply {
            text = "Note: To restore these gestures, you may need to re-enable AssistX in Android Accessibility settings."
            setTextColor(Color.parseColor("#F0A30A"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setLineSpacing(2 * density, 1.15f)
        }
        calloutBox.addView(calloutText)

        val calloutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = (18 * density).toInt()
        }
        dialogPanel.addView(calloutBox, calloutParams)

        // 4. Action Buttons Row
        val buttonsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        // Cancel Button
        val cancelButton = createMetroButton(
            text = "Cancel",
            borderColor = Color.parseColor("#80FFFFFF"),
            fillColor = Color.TRANSPARENT,
            textColor = Color.WHITE,
            pressedFillColor = Color.parseColor("#33FFFFFF")
        ) {
            haptics.tap()
            onCancel()
        }
        val cancelParams = LinearLayout.LayoutParams(0, (44 * density).toInt(), 1f).apply {
            rightMargin = (6 * density).toInt()
        }
        buttonsRow.addView(cancelButton, cancelParams)

        // Pause AssistX Button (Warning/Danger Amber)
        val pauseButton = createMetroButton(
            text = "Pause AssistX",
            borderColor = amberColor,
            fillColor = amberColor,
            textColor = Color.BLACK,
            pressedFillColor = Color.WHITE,
            pressedTextColor = Color.BLACK
        ) {
            haptics.tap()
            onConfirmPause()
        }
        val pauseParams = LinearLayout.LayoutParams(0, (44 * density).toInt(), 1f).apply {
            leftMargin = (6 * density).toInt()
        }
        buttonsRow.addView(pauseButton, pauseParams)

        dialogPanel.addView(buttonsRow)
        addView(dialogPanel, panelParams)
    }

    private fun createMetroButton(
        text: String,
        borderColor: Int,
        fillColor: Int,
        textColor: Int,
        pressedFillColor: Int,
        pressedTextColor: Int = textColor,
        onClick: () -> Unit
    ): TextView {
        val normalDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            setStroke((2 * density).toInt(), borderColor)
            cornerRadius = 0f
        }
        val pressedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(pressedFillColor)
            setStroke((2 * density).toInt(), borderColor)
            cornerRadius = 0f
        }

        val stateList = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
            addState(intArrayOf(), normalDrawable)
        }

        return TextView(context).apply {
            this.text = text
            this.setTextColor(textColor)
            this.gravity = Gravity.CENTER
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            this.setTypeface(Typeface.DEFAULT_BOLD)
            this.background = stateList
            this.isClickable = true
            this.isFocusable = true
            setOnClickListener { onClick() }
        }
    }
}

