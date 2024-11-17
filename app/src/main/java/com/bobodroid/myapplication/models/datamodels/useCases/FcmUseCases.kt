package com.bobodroid.myapplication.models.datamodels.useCases

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import com.bobodroid.myapplication.models.datamodels.roomDb.RateDirection
import com.bobodroid.myapplication.models.datamodels.roomDb.RateType
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRates
import com.bobodroid.myapplication.models.datamodels.service.UserApi.Rate
import com.bobodroid.myapplication.util.result.Result
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserApi
import com.bobodroid.myapplication.models.datamodels.service.UserApi.UserRatesUpdateRequest
import kotlinx.coroutines.launch
import javax.inject.Inject

// 목표 환율 추가


class TargetRateUseCases(
   val targetRateAddUseCase: TargetRateAddUseCase
)




class TargetRateAddUseCase @Inject constructor() {
    suspend operator fun invoke(
        deviceId: String,
        targetRates: TargetRates,
        type: RateType,
        newRate: Rate
    ): Result<TargetRates> {
        return try {
            // 현재 목록 가져오기
            val currentList = when (type) {
                is RateType.USD_HIGH -> targetRates.dollarHighRates
                is RateType.USD_LOW -> targetRates.dollarLowRates
                is RateType.JPY_HIGH -> targetRates.yenHighRates
                is RateType.JPY_LOW -> targetRates.yenLowRates
            }?.toMutableList() ?: mutableListOf()

            // 새 rate 추가
            currentList.add(newRate)

            // 정렬 수행
            val sortedList = sortList(currentList, type.direction)

            // 결과 업데이트
            val updatedRates = targetRates.copy().apply {
                when (type) {
                    is RateType.USD_HIGH -> dollarHighRates = sortedList
                    is RateType.USD_LOW -> dollarLowRates = sortedList
                    is RateType.JPY_HIGH -> yenHighRates = sortedList
                    is RateType.JPY_LOW -> yenLowRates = sortedList
                }
            }

            val updateRequest = when(type) {
                is RateType.USD_HIGH -> UserRatesUpdateRequest(usdHighRates = sortedList)
                is RateType.USD_LOW -> UserRatesUpdateRequest(usdLowRates = sortedList)
                is RateType.JPY_HIGH -> UserRatesUpdateRequest(jpyHighRates = sortedList)
                is RateType.JPY_LOW -> UserRatesUpdateRequest(jpyLowRates = sortedList)
            }


           val fetchTargetRates = UserApi.userService.updateUserRates(deviceId = deviceId, updateRequest)

            Log.d(TAG("TargetRateUseCases","TargetRateAddUseCase"), fetchTargetRates.toString())

            Result.Success(
                data = updatedRates,
                message = "목표환율이 추가되었습니다"
            )
        } catch (e: Exception) {
            Result.Error(
                message = "목표환율 추가 중 오류가 발생했습니다",
                exception = e
            )
        }


    }

    private fun sortList(list: List<Rate>, direction: RateDirection): List<Rate> {
        return list
            .sortedBy { it.rate  }
            .let { sorted ->
                when (direction) {
                    RateDirection.HIGH -> sorted
                    RateDirection.LOW -> sorted.reversed()
                }
            }
            .mapIndexed { index, rate ->
                rate.copy(number = (index + 1))
            }
    }
}


//// 목표 환율 삭제
//fun targetRateRemove(
//    drHighRate: TargetRate? = null,
//    drLowRate: TargetRate? = null,
//    yenHighRate: TargetRate? = null,
//    yenLowRate: TargetRate? = null
//) {
//    if(drHighRate != null) {
//
//        val updateDollarRateList = targetRateFlow.value.dollarHighRateList?.toMutableList()?.apply {
//            remove(drHighRate)
//        }?.toList()
//
//        val removeRateData = targetRateFlow.value.copy(
//            customId = _localUser.value.customId,
//            fcmToken = _localUser.value.fcmToken,
//            dollarHighRateList = updateDollarRateList?.let { sortTargetRateList(type="달러고점", it) } as List<TargetRate>
//        )
//
//        //목표환율 api 추가 로직 !!!!!!!
//
//        viewModelScope.launch {
//            _targetRate.emit(removeRateData)
//        }
//    }
//
//    if(drLowRate != null) {
//        val updateDollarRateList = targetRateFlow.value.dollarLowRateList?.toMutableList()?.apply {
//            remove(drLowRate)
//        }?.toList()
//
//        val removeRateData = targetRateFlow.value.copy(
//            customId = _localUser.value.customId,
//            fcmToken = _localUser.value.fcmToken,
//            dollarLowRateList = updateDollarRateList?.let { sortTargetRateList(type="달러저점", it) } as List<TargetRate>
//        )
//
//        //목표환율 api 추가 로직 !!!!!!!
//
//        viewModelScope.launch {
//            _targetRate.emit(removeRateData)
//        }
//    }
//
//    if(yenHighRate != null) {
//        val updateYenRateList = targetRateFlow.value.yenHighRateList?.toMutableList()?.apply {
//            remove(yenHighRate)
//        }?.toList()
//
//        val removeRateData = targetRateFlow.value.copy(
//            customId = _localUser.value.customId,
//            fcmToken = _localUser.value.fcmToken,
//            yenHighRateList = updateYenRateList?.let { sortTargetRateList(type="엔화고점", it) } as List<TargetRate>
//        )
//
//        //목표환율 api 추가 로직 !!!!!!!
//
//        viewModelScope.launch {
//            _targetRate.emit(removeRateData)
//        }
//    }
//
//    if(yenLowRate != null) {
//        val updateYenRateList = targetRateFlow.value.yenLowRateList?.toMutableList()?.apply {
//            remove(yenLowRate)
//        }?.toList()
//
//        val removeRateData = targetRateFlow.value.copy(
//            customId = _localUser.value.customId,
//            fcmToken = _localUser.value.fcmToken,
//            yenLowRateList = updateYenRateList?.let { sortTargetRateList(type="엔화저점", it) } as List<TargetRate>
//        )
//
//        //목표환율 api 추가 로직 !!!!!!!
//
//        viewModelScope.launch {
//            _targetRate.emit(removeRateData)
//        }
//    }
//
//}
//
//
//// 목표 환율 정렬
//fun sortTargetRateList(type: String, rateList: List<Any>): List<Any>  {
//
//    when(type) {
//        "달러고점" -> {
//            rateList as List<TargetRate>
//
//            var sortList = rateList.sortedByDescending { it.rate }
//
//            sortList = sortList.mapIndexed { index, element ->
//                element.copy(number = "${index + 1}") }.toMutableList()
//
//            return sortList
//        }
//        "달러저점" -> {
//            rateList as List<TargetRate>
//
//            var sortList = rateList.sortedByDescending { it.rate }
//
//            sortList = sortList.mapIndexed { index, element ->
//                element.copy(number = "${index + 1}") }.toMutableList()
//
//            return sortList
//        }
//        "엔화고점" -> {
//            rateList as List<TargetRate>
//
//            var sortList = rateList.sortedByDescending { it.rate }
//
//            sortList = sortList.mapIndexed { index, element ->
//                element.copy(number = "${index + 1}") }.toMutableList()
//
//            return sortList
//        }
//        "엔화저점" -> {
//            rateList as List<TargetRate>
//
//            var sortList = rateList.sortedByDescending { it.rate }
//
//            sortList = sortList.mapIndexed { index, element ->
//                element.copy(number = "${index + 1}") }.toMutableList()
//
//            return sortList
//        }
//
//
//        else -> return emptyList()
//    }
//}


