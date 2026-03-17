package com.example.drawmap.data.common

/**
 * Sealed класс для типобезопасной обработки результатов операций
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()

    data class Error(
        val exception: Exception,
        val message: String = exception.message ?: "Unknown error"
    ) : Result<Nothing>()

    object Loading : Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    inline fun onError(action: (Exception) -> Unit): Result<T> {
        if (this is Error) {
            action(exception)
        }
        return this
    }

    companion object {
        inline fun <T> runCatching(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(e)
        }

        suspend inline fun <T> suspendRunCatching(crossinline block: suspend () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(e)
        }
    }
}

fun <T : Any> T?.toResult(errorMessage: String = "Value is null"): Result<T> {
    return if (this != null) {
        Result.Success(this)
    } else {
        Result.Error(NullPointerException(errorMessage))
    }
}

fun <T> Result<List<T>>.orEmptyList(): List<T> {
    return when (this) {
        is Result.Success -> data
        else -> emptyList()
    }
}
