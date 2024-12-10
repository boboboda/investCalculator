package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.models.viewmodels.TotalProfitRangeDate
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@Composable
fun GetMoneyView(
    getMoney: String,
    totalProfitRangeDate: TotalProfitRangeDate
) {

    val mathContext = MathContext(0, RoundingMode.HALF_UP)

    val stringGetMoney = if (getMoney == "") "" else {
        getMoney
    }


    val date = if (totalProfitRangeDate.startDate == "" && totalProfitRangeDate.endDate == "")
        "조회기간: 조회된 값이 없습니다." else "조회기간: ${totalProfitRangeDate.startDate}~${totalProfitRangeDate.endDate}"

    val profitColor = if (getMoney == "") {
        Color.Black
    } else {
        if (BigDecimal(getMoney.replace(",", ""), mathContext).signum() == -1) {
            Color.Blue
        } else {
            Color.Red
        }
    }

    Column(
        modifier = Modifier
            .wrapContentWidth()
            .padding(start = 15.dp, end = 5.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Row {
            Text(text = "매도 총 수익: ", fontSize = 15.sp)

            Text(text = "${stringGetMoney}", fontSize = 15.sp, color = profitColor)
        }


        Text(text = date, fontSize = 15.sp)
    }


}