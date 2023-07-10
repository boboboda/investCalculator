package com.bobodroid.myapplication.models.datamodels


//계산기 액션 이넘 클래스
enum class CalculateAction(val info: String, val symbol: String) {
    AllClear("모두삭제", "AC"), // 모두삭제
    Del("지우기", "←"), // 지우기
    Enter("입력", "입력"),  // 입력
    DOT("점", ".") // 점
}
