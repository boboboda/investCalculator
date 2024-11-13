package com.bobodroid.myapplication.models.datamodels.useCases

import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UserUseCases(
    val createUser: CreateUserUseCase,
    val logIn: LogInUseCase,
    val logout: LogoutUseCase,
    val localExistCheck: LocalExistCheckUseCase,
    val deleteUser: DeleteUserUseCase,
    val localUserUpdate: LocalUserUpdate
)


class LocalIdAddUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): LocalUserData {
        val createLocalUser = LocalUserData(
            userResetDate = "",
            rateAdCount = 0,
            rateResetCount = 3
        )
        return userRepository.localUserAdd(createLocalUser)
    }
}

class LocalUserUpdate @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(localUserData: LocalUserData) {

    }
}

class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(customId: String, pin: String, onResult: (String) -> Unit) {

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
        // 첫 번째 데이터 확인, null이 아니면 반환하고 끝냄
        val existingUser = userRepository.localUserDataGet()
            .distinctUntilChanged()
            .firstOrNull { it != null }

        // 이미 데이터가 존재하면 반환, 없으면 생성 후 반환
        return existingUser ?: localIdAddUseCase()
    }
}

