package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.UseCase
import com.antcashmanager.domain.validation.TransactionValidator
import com.antcashmanager.domain.validation.ValidationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Enhanced InsertTransactionUseCase with domain-level validation.
 *
 * **Key Differences from [InsertTransactionUseCase]**:
 * - ✅ Validates transaction before persistence
 * - ✅ Fails fast with [ValidationException] if invalid
 * - ✅ Collects all validation errors in single pass
 * - ✅ Prevents invalid data from reaching repository
 * - ✅ Better error messages for UI feedback
 *
 * **Validation Rules**:
 * - Title must not be empty
 * - Amount must be > 0
 * - Timestamp must not be in future
 * - Category must not be empty
 *
 * **Usage**:
 * ```kotlin
 * val result = validatedInsertTransactionUseCase(transaction)
 *
 * result
 *     .onSuccess { id ->
 *         showSuccessMessage("Transaction #$id saved")
 *     }
 *     .onFailure { error ->
 *         if (error is ValidationException) {
 *             showValidationErrors(error.errors)
 *         } else {
 *             showGenericError(error.message)
 *         }
 *     }
 * ```
 *
 * **Error Handling**:
 * - `ValidationException` → Validation failed (show errors to user)
 * - Other exceptions → Repository/storage errors (show generic error)
 *
 * @param transactionRepository Repository for persistence
 * @param validator Validator for domain rules
 * @param dispatcher Coroutine dispatcher (default: Default)
 */
public class ValidatedInsertTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val validator: TransactionValidator,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Transaction, Long>(dispatcher) {
    override suspend fun execute(params: Transaction): Long {
        // Validate before persistence
        val validationResult = validator.validate(params)
        if (validationResult.isInvalid()) {
            throw ValidationException(validationResult.errors)
        }

        // Validation passed, proceed with insertion
        return transactionRepository.insertTransaction(params)
    }
}

/**
 * Extension function for easier error handling of validated insertions.
 *
 * Distinguishes between validation errors and other exceptions.
 *
 * **Usage**:
 * ```kotlin
 * validatedInsertTransactionUseCase(transaction)
 *     .handleValidationError { errors ->
 *         showValidationUI(errors)
 *     }
 *     .handleOtherError { error ->
 *         showErrorToast(error.message)
 *     }
 * ```
 */
public fun <T> Result<T>.handleValidationError(
    action: (List<com.antcashmanager.domain.validation.ValidationError>) -> Unit,
): Result<T> {
    val exception = exceptionOrNull()
    if (exception is ValidationException) {
        action(exception.errors)
    }
    return this
}

/**
 * Extension function for handling non-validation errors.
 */
public fun <T> Result<T>.handleOtherError(action: (Throwable) -> Unit): Result<T> {
    val exception = exceptionOrNull()
    if (exception != null && exception !is ValidationException) {
        action(exception)
    }
    return this
}
