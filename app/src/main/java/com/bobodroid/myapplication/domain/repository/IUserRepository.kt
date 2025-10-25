package com.bobodroid.myapplication.domain.repository

import com.bobodroid.myapplication.domain.entity.UserEntity
import com.bobodroid.myapplication.models.datamodels.useCases.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * User Repository 인터페이스 - Entity 버전
 *
 * [변경 사항]
 * 기존: LocalUserData 사용
 * 신규: UserEntity 사용
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
     * ⭐ UserEntity 사용
     *
     * @param userEntity 업데이트할 사용자 Entity
     */
    suspend fun localUserUpdate(userEntity: UserEntity): Int

    /**
     * 로컬 사용자 추가
     * ⭐ UserEntity 사용
     *
     * @param userEntity 추가할 사용자 Entity
     */
    suspend fun localUserAdd(userEntity: UserEntity): Any

    /**
     * 로컬 사용자 삭제
     * 모든 사용자 데이터를 삭제
     */
    suspend fun localUserDataDelete(): Any

    /**
     * 로컬 사용자 데이터 조회
     * ⭐ UserEntity 반환
     *
     * @return UserEntity Flow
     */
    fun localUserDataGet(): Flow<UserEntity?>

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