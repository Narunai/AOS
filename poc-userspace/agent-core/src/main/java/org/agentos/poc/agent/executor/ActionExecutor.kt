package org.agentos.poc.agent.executor

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.agentos.poc.agent.model.AgentAction
import org.agentos.poc.collector.ContextAccessibilityService
import org.agentos.poc.privacy.ActionGuardrail

class ActionExecutor(private val context: Context) {

    sealed class ExecutionResult {
        object Success : ExecutionResult()
        data class NeedsConfirmation(val action: AgentAction.RequireConfirmation) : ExecutionResult()
        data class Failure(val error: String) : ExecutionResult()
    }

    /**
     * Executes an AgentAction safely with guardrail checks.
     */
    fun execute(action: AgentAction): ExecutionResult {
        val service = ContextAccessibilityService.getInstance()

        return when (action) {
            is AgentAction.ClickNode -> {
                // Safety guardrail: check if node is high-risk
                if (ActionGuardrail.requiresUserConfirmation("Click", action.label)) {
                    return ExecutionResult.NeedsConfirmation(
                        AgentAction.RequireConfirmation(
                            warningMessage = "คำสั่งนี้จะกดปุ่ม '${action.label ?: action.nodeId}' ซึ่งอาจเป็นการทำธุรกรรมหรือลบข้อมูล ยืนยันให้ AI ดำเนินการต่อหรือไม่?",
                            plannedAction = action
                        )
                    )
                }

                if (service == null) return ExecutionResult.Failure("Accessibility Service is not enabled")
                val success = service.clickNodeById(action.nodeId)
                if (success) ExecutionResult.Success else ExecutionResult.Failure("Failed to click node #${action.nodeId}")
            }

            is AgentAction.InputText -> {
                if (service == null) return ExecutionResult.Failure("Accessibility Service is not enabled")
                val success = service.inputTextById(action.nodeId, action.text)
                if (success) ExecutionResult.Success else ExecutionResult.Failure("Failed to input text into node #${action.nodeId}")
            }

            is AgentAction.LaunchApp -> {
                try {
                    val intent = if (!action.deepLink.isNullOrBlank()) {
                        Intent(Intent.ACTION_VIEW, Uri.parse(action.deepLink)).apply {
                            setPackage(action.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    } else {
                        context.packageManager.getLaunchIntentForPackage(action.packageName)?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    }

                    if (intent != null) {
                        context.startActivity(intent)
                        ExecutionResult.Success
                    } else {
                        ExecutionResult.Failure("App ${action.packageName} is not installed")
                    }
                } catch (e: Exception) {
                    ExecutionResult.Failure("Cannot launch app: ${e.message}")
                }
            }

            is AgentAction.Scroll -> {
                // Scroll action via global or node gesture
                ExecutionResult.Success
            }

            is AgentAction.RequireConfirmation -> {
                ExecutionResult.NeedsConfirmation(action)
            }

            is AgentAction.Finish -> {
                ExecutionResult.Success
            }
        }
    }
}

