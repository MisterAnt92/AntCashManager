package com.antcashmanager.domain.usecase

import com.antcashmanager.domain.repository.FeedbackRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase for sending feedback emails.
 * Encapsulates all Context and platform-specific details.
 *
 * @param feedbackRepository Repository for feedback operations
 * @param dispatcher CoroutineDispatcher for execution (default: Dispatchers.Default)
 */
public class SendFeedbackEmailUseCase(
    private val feedbackRepository: FeedbackRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<SendFeedbackEmailUseCase.Params, Boolean>(dispatcher) {
    public data class Params(
        val emailBody: String,
    )

    override suspend fun execute(params: Params): Boolean {
        return feedbackRepository.sendFeedbackEmail(params.emailBody)
    }
}
