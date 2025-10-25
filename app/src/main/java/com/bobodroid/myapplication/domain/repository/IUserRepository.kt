package com.bobodroid.myapplication.domain.repository

import com.bobodroid.myapplication.models.datamodels.roomDb.LocalUserData
import com.bobodroid.myapplication.models.datamodels.useCases.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * User Repository 인터페이스
 *
 * Domain Layer의 추상화된 사용자 데이터 접근 인터페이스
 * - Platform 독립적인 비즈니스 로직 정의
 * - 구현체는 Data Layer에 위치
 */
interface IUserRepository {

    /**
     * 사용자 데이터 StateFlow
     * StateFlow를 사용하여 현재 값에 즉시 접근 가능
     */
    val userData: StateFlow<UserData?>

    /**
     * 로컬 사용자 업데이트
     *
     * @param localUserData 업데이트할 사용자 데이터
     */
    suspend fun localUserUpdate(localUserData: LocalUserData): Int

    /**
     * 로컬 사용자 추가
     *
     * @param localUserData 추가할 사용자 데이터
     */
    suspend fun localUserAdd(localUserData: LocalUserData): Any

    /**
     * 로컬 사용자 삭제
     * 모든 사용자 데이터를 삭제
     */
    suspend fun localUserDataDelete(): Any

    /**
     * 로컬 사용자 데이터 조회
     *
     * @return LocalUserData Flow
     */
    fun localUserDataGet(): Flow<LocalUserData?>

    /**
     * UserData 업데이트
     *
     * @param data 업데이트할 UserData
     */
    fun updateUserData(data: UserData)

    /**
     * UserData 대기
     * userData가 null이 아닐 때까지 대기
     *
     * @return 첫 번째 non-null UserData
     */
    suspend fun waitForUserData(): UserData
}