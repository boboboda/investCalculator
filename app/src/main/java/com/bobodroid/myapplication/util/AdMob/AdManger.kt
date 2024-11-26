package com.bobodroid.myapplication.util.AdMob

import android.content.Context
import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.components.admobs.loadInterstitial
import com.bobodroid.myapplication.components.admobs.loadRewardedAdvertisement
import com.bobodroid.myapplication.components.admobs.loadTargetRewardedAdvertisement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {

    // 광고 준비 상태를 StateFlow로 관리
    private val _isRewardAdReady = MutableStateFlow(false)
    val isRewardAdReady = _isRewardAdReady.asStateFlow()

    private val _isRewardTargetAdReady = MutableStateFlow(false)
    val isRewardTargetAdReady = _isRewardTargetAdReady.asStateFlow()

    private val _isBannerAdReady = MutableStateFlow(false)
    val isBannerAdReady = _isBannerAdReady.asStateFlow()

    // 광고 로드 메서드
    fun loadRewardedAd(context: Context) {
        // 광고 로드 로직
        loadRewardedAdvertisement(context, onReadyAd = {
            _isRewardAdReady.value = it
        })

        loadTargetRewardedAdvertisement(context, onReadyAd = {
            _isRewardTargetAdReady.value = it
        })
        Log.d(TAG("AdManager",""), "리워드 광고 로딩 중...")
        // 광고 로딩이 완료되면 아래 상태 업데이트
    }

    fun loadBannerAd(context: Context) {

        // 배너 광고 로드 로직
        loadInterstitial(context, onReady = {
            _isBannerAdReady.value = it
        })
        Log.d(TAG("AdManager",""), "배너 광고 로딩 중...")
        // 광고 로딩이 완료되면 아래 상태 업데이트
        _isBannerAdReady.value = true
    }
}



