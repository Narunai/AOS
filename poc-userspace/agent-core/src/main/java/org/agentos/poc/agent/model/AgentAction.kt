package org.agentos.poc.agent.model

import kotlinx.serialization.Serializable

@Serializable
sealed class AgentAction {
    @Serializable
    data class ClickNode(
        val nodeId: Int,
        val label: String? = null
    ) : AgentAction()

    @Serializable
    data class InputText(
        val nodeId: Int,
        val text: String
    ) : AgentAction()

    @Serializable
    data class Scroll(
        val direction: String = "DOWN" // UP, DOWN, LEFT, RIGHT
    ) : AgentAction()

    @Serializable
    data class LaunchApp(
        val packageName: String,
        val deepLink: String? = null
    ) : AgentAction()

    @Serializable
    data class RequireConfirmation(
        val warningMessage: String,
        val plannedAction: ClickNode
    ) : AgentAction()

    @Serializable
    data class Finish(
        val message: String
    ) : AgentAction()
}

@Serializable
data class AgentGoal(
    val id: String,
    val userPrompt: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // PENDING, PLANNING, EXECUTING, AWAITING_CONFIRMATION, COMPLETED, FAILED
)

