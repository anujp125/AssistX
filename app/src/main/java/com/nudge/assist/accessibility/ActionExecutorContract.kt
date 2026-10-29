package com.nudge.assist.accessibility

import com.nudge.assist.service.AssistAccessibilityService

/**
 * Abstraction for triggering global platform accessibility actions.
 */
interface ActionExecutorContract {
    fun lockScreen(): Boolean
    fun performHome(): Boolean
    fun performBack(): Boolean
}

class AndroidActionExecutor : ActionExecutorContract {
    override fun lockScreen(): Boolean = AssistAccessibilityService.lockScreen()
    override fun performHome(): Boolean = AssistAccessibilityService.performHome()
    override fun performBack(): Boolean = AssistAccessibilityService.performBack()
}
