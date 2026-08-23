package com.antcashmanager.domain.validation

import com.antcashmanager.domain.model.Transaction
import kotlinx.datetime.Clock

/**
 * Domain-level validation for Transaction entities.
 *
 * This layer ensures data integrity BEFORE persistence, catching issues at the
 * domain boundary rather than in ViewModels or repositories.
 *
 * **Benefits**:
 * - Single source of validation truth
 * - KMP-compatible (works on Android + iOS)
 * - Testable without mocking repositories
 * - Enables future constraints enforcement (domain events, etc.)
 *
 * **Validation Rules**:
 * 1. Title must not be empty
 * 2. Amount must be positive (> 0)
 * 3. Timestamp must not be in the future (must be <= now)
 * 4. Category must not be empty (format validation)
 *
 * **Usage**:
 * ```kotlin
 * val result = transactionValidator.validate(transaction)
 * result.onFailure { error ->
 *     showErrorMessage(error.message)
 * }.onSuccess {
 *     repository.insertTransaction(transaction)
 * }
 * ```
 */
public interface TransactionValidator {
    /**
     * Validates a transaction against domain rules.
     *
     * @param transaction The transaction to validate
     * @return [ValidationResult] containing all validation errors (if any)
     */
    public fun validate(transaction: Transaction): ValidationResult
}

/**
 * Validation result containing all errors found during validation.
 *
 * Use `isValid()` to check if validation passed, or `errors` to inspect failures.
 */
public data class ValidationResult(
    public val errors: List<ValidationError> = emptyList(),
) {
    /**
     * Returns true if validation passed (no errors found).
     */
    public fun isValid(): Boolean = errors.isEmpty()

    /**
     * Returns true if validation failed (at least one error found).
     */
    public fun isInvalid(): Boolean = errors.isNotEmpty()

    /**
     * Converts validation result to Kotlin Result<T>.
     *
     * Useful for chaining with other Result operations.
     */
    public fun toResult(value: Unit = Unit): Result<Unit> =
        if (isValid()) Result.success(value) else Result.failure(
            ValidationException(errors)
        )

    /**
     * Executes callback if validation passed.
     */
    public fun onSuccess(action: (Unit) -> Unit): ValidationResult {
        if (isValid()) action(Unit)
        return this
    }

    /**
     * Executes callback if validation failed.
     */
    public fun onFailure(action: (List<ValidationError>) -> Unit): ValidationResult {
        if (isInvalid()) action(errors)
        return this
    }
}

/**
 * Individual validation error with code and message.
 *
 * **Error Codes**:
 * - `TRANSACTION_TITLE_EMPTY`: Title is empty or blank
 * - `TRANSACTION_AMOUNT_NOT_POSITIVE`: Amount is <= 0
 * - `TRANSACTION_TIMESTAMP_FUTURE`: Timestamp is in the future
 * - `TRANSACTION_CATEGORY_EMPTY`: Category is empty or blank
 */
public data class ValidationError(
    public val code: String,
    public val message: String,
    public val field: String? = null,
)

/**
 * Exception thrown when transaction validation fails.
 *
 * Contains all validation errors for proper error handling and UI feedback.
 */
public class ValidationException(
    public val errors: List<ValidationError>,
) : Exception(
    "Transaction validation failed: ${errors.joinToString(", ") { it.code }}"
)

/**
 * Default implementation of [TransactionValidator].
 *
 * **Validations Performed**:
 * 1. Title: not empty, not blank
 * 2. Amount: > 0 (must be positive)
 * 3. Timestamp: <= Clock.System.now (not in future)
 * 4. Category: not empty
 *
 * @param clock Clock for getting current time (default: System)
 */
public class TransactionValidatorImpl(
    private val clock: Clock = Clock.System,
) : TransactionValidator {

    override fun validate(transaction: Transaction): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate title
        if (transaction.title.isBlank()) {
            errors.add(
                ValidationError(
                    code = "TRANSACTION_TITLE_EMPTY",
                    message = "Transaction title cannot be empty",
                    field = "title",
                )
            )
        }

        // Validate amount
        if (transaction.amount <= 0) {
            errors.add(
                ValidationError(
                    code = "TRANSACTION_AMOUNT_NOT_POSITIVE",
                    message = "Transaction amount must be positive (> 0)",
                    field = "amount",
                )
            )
        }

        // Validate timestamp (must not be in future)
        val currentTimeMs = clock.now().toEpochMilliseconds()
        if (transaction.timestamp > currentTimeMs) {
            errors.add(
                ValidationError(
                    code = "TRANSACTION_TIMESTAMP_FUTURE",
                    message = "Transaction timestamp cannot be in the future",
                    field = "timestamp",
                )
            )
        }

        // Validate category
        if (transaction.category.isBlank()) {
            errors.add(
                ValidationError(
                    code = "TRANSACTION_CATEGORY_EMPTY",
                    message = "Transaction category cannot be empty",
                    field = "category",
                )
            )
        }

        return ValidationResult(errors)
    }
}
