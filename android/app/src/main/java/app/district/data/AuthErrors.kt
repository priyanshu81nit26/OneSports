package app.district.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.functions.FirebaseFunctionsException

object AuthErrors {

    fun message(error: Throwable?): String {
        if (error == null) return "Something went wrong. Please try again."
        if (error is FirebaseNetworkException) {
            return "No connection right now. Check your network and try again."
        }
        if (error is FirebaseAuthWeakPasswordException) {
            return "Choose a stronger password — at least 6 characters."
        }
        if (error is FirebaseAuthInvalidCredentialsException) {
            return "Those details don't look right. Check your email and password."
        }
        if (error is FirebaseAuthInvalidUserException) {
            return "We couldn't find an account with that email. Try signing up instead."
        }
        if (error is FirebaseAuthUserCollisionException) {
            return "That email is already registered. Try signing in instead."
        }
        if (error is FirebaseAuthException) {
            return when (error.errorCode) {
                "ERROR_WRONG_PASSWORD" -> "Wrong password. Try again or reset it from settings."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "That email is already registered. Sign in instead."
                "ERROR_INVALID_EMAIL" -> "That email address doesn't look valid."
                "ERROR_USER_DISABLED" -> "This account has been disabled. Contact support."
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Wait a minute and try again."
                "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method isn't enabled yet."
                "ERROR_INVALID_VERIFICATION_CODE" -> "That verification code isn't correct."
                "ERROR_SESSION_EXPIRED" -> "This session expired. Request a new code."
                else -> "Sign-in didn't work. Check your details and try again."
            }
        }
        if (error is FirebaseFunctionsException) {
            return when (error.code) {
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> "Please sign in again to continue."
                FirebaseFunctionsException.Code.PERMISSION_DENIED -> "You don't have permission to do that."
                FirebaseFunctionsException.Code.NOT_FOUND ->
                    error.message?.takeIf { it.isNotBlank() }
                        ?: "That item wasn't found. If this keeps happening, try again in a moment."
                FirebaseFunctionsException.Code.ALREADY_EXISTS -> error.message ?: "That name is already taken."
                FirebaseFunctionsException.Code.UNAVAILABLE,
                FirebaseFunctionsException.Code.DEADLINE_EXCEEDED -> "Our servers are busy. Try again shortly."
                FirebaseFunctionsException.Code.INVALID_ARGUMENT -> error.message ?: "Some details look off. Check and retry."
                else -> "Something went wrong on our side. Try again in a moment."
            }
        }
        return error.message?.takeIf { it.isNotBlank() }
            ?: "Something went wrong. Please try again."
    }
}
