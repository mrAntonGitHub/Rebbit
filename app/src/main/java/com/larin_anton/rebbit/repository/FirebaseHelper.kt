package com.larin_anton.rebbit.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseHelper @Inject constructor() {

    private var currentUser : FirebaseUser? = null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    init {
        currentUser = getCurrentFirebaseUser()
    }

    fun signOut() : Boolean{
        firebaseAuth.signOut()
        return firebaseAuth.currentUser == null
    }

    fun getDatabaseInstance() : FirebaseDatabase{
        return firebaseDatabase
    }

    suspend fun createNewFirebaseUser(email: String, password: String) : Pair<String, Exception?> {
        return CompletableDeferred<Pair<String, Exception?>>().apply {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("AppDebug", "FirebaseHelper:createNewFirebaseUser [1]")
                    this.complete(Pair("Пользователь создан", null))
                } else {
                    when (task.exception) {
                        is FirebaseAuthException -> {
                            when ((task.exception as FirebaseAuthException).errorCode) {
                                "ERROR_INVALID_EMAIL" -> {
                                    this.complete(Pair("Пользователя с такой почтой не существует", task.exception))
                                }
                                "ERROR_WRONG_PASSWORD" -> {
                                    this.complete(Pair("Неверный пароль", task.exception))
                                }
                                "ERROR_USER_NOT_FOUND" -> {
                                    this.complete(Pair("Пользователя с такой почтой не существует", task.exception))
                                }
                                "ERROR_USER_DISABLED" -> {
                                    this.complete(Pair("Пользователь заблокирован", task.exception))
                                }
                                "ERROR_TOO_MANY_REQUESTS" -> {
                                    this.complete(Pair("Вы делаете слишком много запросов. Попробуйте через некоторое время", task.exception))
                                }
                                "ERROR_OPERATION_NOT_ALLOWED" -> {
                                    this.complete(Pair("Вам не разрешено входить в это приложение", task.exception))
                                }
                                "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                    this.complete(Pair("Такая почта уже существует", task.exception))
                                }
                                else -> {
                                    this.complete(Pair("Произошла непредвиденная ошибка. Свяжитесь с нами и мы обязательно поможем!\nЧтобы связаться с нами перейдите в Настройки -> Связаться с нами", task.exception))
                                }
                            }
                        }
                        is FirebaseNetworkException -> {
                            this.complete(Pair("Интерент подключение отсутсвует", task.exception))
                        }
                        else -> {
                            this.complete(Pair("Произошла непредвиденная ошибка", task.exception))
                        }
                    }
                }
            }
        }.await()
    }

    fun removeCurrentUser(){
        firebaseAuth.currentUser?.delete()
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String) : Pair<String, Exception?>{
        return CompletableDeferred<Pair<String, Exception?>>().apply {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    this.complete(Pair("Пользователь вошел", null))
                }else{
                    when (task.exception) {
                        is FirebaseAuthException -> {
                            when ((task.exception as FirebaseAuthException).errorCode) {
                                "ERROR_INVALID_EMAIL" -> {
                                    this.complete(Pair("Пользователя с такой почтой не существует", task.exception))
                                }
                                "ERROR_WRONG_PASSWORD" -> {

                                    this.complete(Pair("Неверный пароль", task.exception))
                                }
                                "ERROR_USER_NOT_FOUND" -> {
                                    this.complete(Pair("Пользователя с такой почтой не существует", task.exception))
                                }
                                "ERROR_USER_DISABLED" -> {
                                    this.complete(Pair("Пользователь заблокирован", task.exception))
                                }
                                "ERROR_TOO_MANY_REQUESTS" -> {
                                    this.complete(Pair("Вы делаете слишком много запросов. Попробуйте через некоторое время", task.exception))
                                }
                                "ERROR_OPERATION_NOT_ALLOWED" -> {
                                    this.complete(Pair("Вам не разрешено входить в это приложение", task.exception))
                                }
                                "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                    this.complete(Pair("Этот аккаунт в данный момент используется, возможно, вы его открыли на другом устройстве. Пользоваться приложением можно только на одном устройстве", task.exception))
                                }
                                else -> {
                                    this.complete(Pair("Произошла непредвиденная ошибка. Свяжитесь с нами и мы обязательно поможем!\nЧтобы связаться с нами перейдите в Настройки -> Связаться с нами", task.exception))
                                }
                            }
                        }
                        is FirebaseNetworkException -> {
                            this.complete(Pair("Интерент подключение отсутсвует", task.exception))
                        }
                        is FirebaseTooManyRequestsException -> {
                            Log.e("AppDebug", "FirebaseHelper:signInWithEmailAndPassword [${task.exception} |||||| ${(task.exception as FirebaseTooManyRequestsException).stackTrace.map { it }}]")
                            this.complete(Pair("Количество попыток ввода пароля истекло. Попробуйте позже или восстановите пароль", task.exception))
                        }
                        else -> {
                            this.complete(Pair("Произошла непредвиденная ошибка", task.exception))
                        }
                    }
                }
            }
        }.await()
    }

    suspend fun verifyUserEmail() : Pair<String, Exception?>{
        return CompletableDeferred<Pair<String, Exception?>>().apply {
            firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    this.complete(Pair("Почта подтверждена", null))
                } else{
                    this.complete(Pair("Подтвердите Вашу почту", task.exception))
                }
            }
        }.await()
    }

    fun isUserEmailVerified() : Boolean{
        return firebaseAuth.currentUser?.isEmailVerified ?: false
    }

    fun getCurrentUserId() : String?{
        val currentFirebaseUser = currentUser
        return currentFirebaseUser?.uid ?: getCurrentFirebaseUser()?.uid
    }

    fun getCurrentFirebaseUser() : FirebaseUser?{
        return firebaseAuth.currentUser
    }

    fun isCurrentUserAnonymous() : Boolean?{
        return firebaseAuth.currentUser?.isAnonymous
    }

    fun signInAnAnonymously(){
        firebaseAuth.signInAnonymously()
    }
}