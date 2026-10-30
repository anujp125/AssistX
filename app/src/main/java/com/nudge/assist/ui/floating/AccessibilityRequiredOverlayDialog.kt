package com.nudge.assist.ui.floating

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
import com.nudge.assist.ui.haptics.MetroHaptics

/**
 * Modern UI (Metro) Accessibility Required Overlay Dialog.
 *
 * Characteristics:
 * - Direct WindowManager overlay modal ensuring instant, reliable presentation over any app.
 * - Flat solid rectangular panel with 0dp sharp corners and 2dp solid warning outline.
 * - Exact typography:
 *     Title: "Accessibility required"
 *     Message: "This action requires AssistX Accessibility Service. Enable it in Android Settings to continue."
 * - Two flat Metro buttons:
 *     1. "Go to settings" (AssistX purple accent fill, opens system accessibility settings)
 *     2. "Cancel" (Outlined button, dismisses immediately without replaying action)
 * - Outside scrim click dismisses immediately without executing action.
 */
class AccessibilityRequiredOverlayDialog(
    context: Context,
    private val accentHex: String = "#AA00FF",
    private val onGoToSettings: () -> Unit,
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

        val parsedAccent = try {
            Color.parseColor(accentHex)
        } catch (e: Exception) {
            Color.parseColor("#AA00FF")
        }

        // Metro Flat Dialog Panel
        val dialogPanel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#121214"))
                setStroke((2 * density).toInt(), Color.parseColor("#F0A30A")) // StatusAmber border
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
            text = "Accessibility required"
            setTextColor(Color.parseColor("#F0A30A"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(Typeface.DEFAULT_BOLD)
            val pb = (10 * density).toInt()
            setPadding(0, 0, 0, pb)
        }
        dialogPanel.addView(titleView)

        // 2. Message
        val messageView = TextView(context).apply {
            text = "This action requires AssistX Accessibility Service. Enable it in Android Settings to continue."
            setTextColor(Color.parseColor("#E0E0E0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setLineSpacing(3 * density, 1.15f)
            val pb = (20 * density).toInt()
            setPadding(0, 0, 0, pb)
        }
        dialogPanel.addView(messageView)

        // 3. Buttons Row
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

        // Go to settings Button
        val settingsButton = createMetroButton(
            text = "Go to settings",
            borderColor = parsedAccent,
            fillColor = parsedAccent,
            textColor = Color.WHITE,
            pressedFillColor = Color.WHITE,
            pressedTextColor = Color.BLACK
        ) {
            haptics.tap()
            onGoToSettings()
        }
        val settingsParams = LinearLayout.LayoutParams(0, (44 * density).toInt(), 1f).apply {
            leftMargin = (6 * density).toInt()
        }
        buttonsRow.addView(settingsButton, settingsParams)

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

