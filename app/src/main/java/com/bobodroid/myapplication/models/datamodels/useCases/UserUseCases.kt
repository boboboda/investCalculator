package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.billing.BillingClientLifecycle.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserRequest
import com.bobodroid.myapplication.util.InvestApplication
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UserUseCases(
    val logIn: LogInUseCase,
    val logout: LogoutUseCase,
    val localExistCheck: LocalExistCheckUseCase,
    val deleteUser: DeleteUserUseCase,
    val localUserUpdate: LocalUserUpdate,
    val customIdCreateUser: CustomIdCreateUser
)


class LocalIdAddUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): LocalUserData {

        val fcmToken = InvestApplication.prefs.getData("fcm_token", "")

        val createLocalUser = LocalUserData(
            userResetDate = "",
            rateAdCount = 0,
            rateResetCount = 3,
            fcmToken = fcmToken
        )

        val createUser = userRepository.localUserAdd(createLocalUser)

        return createUser
    }
}

class LocalUserUpdate @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData) {
    }
}


class CustomIdCreateUser @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData) {

    }
}

class LogInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String, pin: String, onSuccess: (String) -> Unit) {


    }
}

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(onLogout: (String) -> Unit) {
        val updatedUser = LocalUserData(customId = "")
        userRepository.localUserUpdate(updatedUser)
        onLogout("로그아웃되었습니다.")
    }
}


class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(onDeleteUser: (String) -> Unit) {
        userRepository.localUserDataDelete()
        onDeleteUser("삭제되었습니다.")
    }
}


class LocalExistCheckUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val localIdAddUseCase: LocalIdAddUseCase
) {
    suspend operator fun invoke(): LocalUserData {
        Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "실행")

        // 기존 사용자 확인 및 생성
        val existingUser = userRepository.localUserDataGet()
            .distinctUntilChanged()
            .firstOrNull()

        val createUser = existingUser ?: localIdAddUseCase()

        val fcmToken = InvestApplication.prefs.getData("fcm_token", "")


        val createServerUser = UserRequest(
            deviceId = createUser.id.toString(),
            customId = "",
            pin = "",
            fcmToken = createUser.fcmToken ?: fcmToken
        )

        try {
            if (existingUser?.id != null) {
                Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "로컬 유저 있음 $createUser")
                val serverUserCheckId = UserApi.userService.getUserRequest(existingUser.id.toString())

                if (serverUserCheckId.deviceId == null) {
                    Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "서버유저가 없음")
                    UserApi.userService.userAddRequest(createServerUser)
                } else {
                    Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "서버유저가 있음 $serverUserCheckId")
                }
            } else {
                Log.d(TAG("userUseCase", "LocalExistCheckUseCase"), "로컬 유저 없음 $createUser")
                UserApi.userService.userAddRequest(createServerUser)
            }
        } catch (e: Exception) {
            Log.e(TAG("userUseCase", "LocalExistCheckUseCase"), "에러 발생: ${e.message}")
        }

        return createUser
    }

}

