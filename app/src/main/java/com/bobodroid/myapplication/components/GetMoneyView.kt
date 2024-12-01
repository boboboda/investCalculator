package com.bobodroid.myapplication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

//@Composable
//fun GetMoneyView(
//    getMoney: String,
//    allViewModel: AllViewModel
//) {
//
//    val mathContext = MathContext(0, RoundingMode.HALF_UP)
//
//    val startDate = allViewModel.startDateFlow.collectAsState()
//
//    val stringGetMoney = if (getMoney == "") "" else {
//        getMoney
//    }
//
//    var endDate = allViewModel.endDateFlow.collectAsState()
//
//    val date = if (startDate.value == "" && endDate.value == "")
//        "조회기간: 달력에서 조회 해주세요" else "조회기간: ${startDate.value}~${endDate.value}"
//
//    val profitColor = if (getMoney == "") {
//        Color.Black
//    } else {
//        if (BigDecimal(getMoney.replace(",", ""), mathContext).signum() == -1) {
//            Color.Blue
//        } else {
//            Color.Red
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .wrapContentWidth()
//            .padding(start = 15.dp, end = 5.dp),
//        horizontalAlignment = Alignment.Start,
//        verticalArrangement = Arrangement.spacedBy(5.dp)
//    ) {
//
//        Row {
//            Text(text = "매도 총 수익: ", fontSize = 15.sp)
//
//            Text(text = "${stringGetMoney}", fontSize = 15.sp, color = profitColor)
//        }
//
//
//        Text(text = date, fontSize = 15.sp)
//    }
//
//
//}