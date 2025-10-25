package com.bobodroid.myapplication.data.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.data.mapper.UserMapper.toDto
import com.bobodroid.myapplication.data.mapper.UserMapper.toEntity
import com.bobodroid.myapplication.domain.entity.UserEntity
import com.bobodroid.myapplication.domain.repository.IUserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserDatabaseDao
import com.bobodroid.myapplication.models.datamodels.useCases.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Repository 구현체 - Entity & DTO 버전
 *
 * [변경 사항]
 * - 외부(Domain): UserEntity 사용
 * - 내부(DAO): LocalUserDto 사용
 * - Mapper로 변환 처리
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localUserDatabaseDao: LocalUserDatabaseDao
) : IUserRepository {

    private val _userData = MutableStateFlow<UserData?>(null)
    override val userData: StateFlow<UserData?> = _userData.asStateFlow()

    /**
     * 로컬 사용자 업데이트
     * ⭐ UserEntity → Dto 변환
     */
    override suspend fun localUserUpdate(userEntity: UserEntity) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "🔥 localUserUpdate 호출됨!")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "요청 데이터:")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - id: ${userEntity.id}")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - isPremium: ${userEntity.isPremium}")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - socialId: ${userEntity.socialId}")

            // UserEntity → Dto 변환
            val dto = userEntity.toDto()

            // DAO 호출 (Dto 사용)
            val updatedDto = localUserDatabaseDao.updateAndGetUser(dto)

            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "updatedDto 결과: $updatedDto")

            if (updatedDto != null) {
                val currentUserData = _userData.value
                _userData.value = UserData(
                    localUserData = updatedDto.toEntity(),
                    exchangeRates = currentUserData?.exchangeRates
                )
                Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "✅ 유저 업데이트 성공!")
                Log.d(TAG("UserRepositoryImpl", "localUserUpdate"), "  - 최종 isPremium: ${updatedDto.isPremium}")
                1
            } else {
                Log.e(TAG("UserRepositoryImpl", "localUserUpdate"), "❌ updatedDto가 null입니다!")
                0
            }
        } catch (e: Exception) {
            Log.e(TAG("UserRepositoryImpl", "localUserUpdate"), "❌ 유저 업데이트 실패", e)
            0
        }
    }

    /**
     * 로컬 사용자 추가
     * ⭐ UserEntity → Dto 변환
     */
    override suspend fun localUserAdd(userEntity: UserEntity) = withContext(Dispatchers.IO) {
        try {
            // UserEntity → Dto 변환
            val dto = userEntity.toDto()

            localUserDatabaseDao.insert(dto)

            // LocalUserData 변환 (하위 호환성)
            val localUserData = userEntity

            _userData.value = UserData(
                localUserData = localUserData,
                exchangeRates = null
            )
        } catch (e: Exception) {
            Log.e(TAG("UserRepositoryImpl", "localUserAdd"), "유저 생성 실패", e)
        }
    }

    /**
     * 로컬 사용자 삭제
     */
    override suspend fun localUserDataDelete() = withContext(Dispatchers.IO) {
        try {
            localUserDatabaseDao.deleteAll()
            _userData.value = null
        } catch (e: Exception) {
            Log.e(TAG("UserRepositoryImpl", "localUserDataDelete"), "유저 삭제 실패", e)
        }
    }

    /**
     * 로컬 사용자 데이터 조회
     * ⭐ Dto → UserEntity 변환
     */
    override fun localUserDataGet(): Flow<UserEntity?> {
        return localUserDatabaseDao.getUserData()
            .map { dto ->
                // Dto → Entity 변환
                dto.toEntity()
            }
    }

    /**
     * UserData 업데이트
     */
    override fun updateUserData(data: UserData) {
        _userData.value = data
    }

    /**
     * UserData 대기
     */
    override suspend fun waitForUserData(): UserData {
        return userData.filterNotNull().first()
    }
}