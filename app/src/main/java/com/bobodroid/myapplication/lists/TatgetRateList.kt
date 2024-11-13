package com.bobodroid.myapplication.lists

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.CardButton
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.ui.theme.TitleCardColor

@Composable
fun TargetRateList(
    allViewModel: AllViewModel,
    selectedCurrency:(String)-> Unit,
    selectedHighAndLow:(String)-> Unit,
    selectedNumber:(String) -> Unit,
) {
    val targetRate = allViewModel.targetRateFlow.collectAsState()

    val dollarHighRate = targetRate.value.dollarHighRateList ?: emptyList()

    val dollarLowRate = targetRate.value.dollarLowRateList ?: emptyList()

    val yenHighRate = targetRate.value.yenHighRateList ?: emptyList()

    val yenLowRate = targetRate.value.yenLowRateList ?: emptyList()

    val highAndLowState = remember { mutableStateOf("고점") }

    var highDollarNumberState = remember { mutableStateOf("1") }

    var lowDollarNumberState = remember { mutableStateOf("1") }

    val filterHighDrData = dollarHighRate?.filter { it.number == highDollarNumberState.value }

    val filterLowDrData = dollarLowRate?.filter { it.number == lowDollarNumberState.value }


    var highYenNumberState = remember { mutableStateOf("1") }

    var lowYenNumberState = remember { mutableStateOf("1") }

    val filterHighYenData = yenHighRate?.filter { it.number == highYenNumberState.value }

    val filterLowYenData = yenLowRate?.filter { it.number == lowYenNumberState.value }

    val currencyState = remember { mutableStateOf("달러") }

    LaunchedEffect(key1 = targetRate, block = {

        highDollarNumberState.value = "1"

        lowDollarNumberState.value = "1"

        highYenNumberState.value = "1"

        lowYenNumberState.value = "1"
    })


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(10.dp)
            ),
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                CardButton(
                    label = "달러",
                    selectedLabel = currencyState.value,
                    fontSize = 15,
                    modifier = Modifier
                        .height(20.dp)
                        .width(40.dp),
                    fontColor = Color.Black,
                    buttonColor = TitleCardColor,
                    disableColor = Color.LightGray,
                    onClicked = {
                        currencyState.value = it
                        selectedCurrency.invoke(it)
                    }
                )

                CardButton(
                    label = "엔화",
                    selectedLabel = currencyState.value,
                    fontSize = 15,
                    modifier = Modifier
                        .height(20.dp)
                        .width(40.dp),
                    fontColor = Color.Black,
                    buttonColor = TitleCardColor,
                    disableColor = Color.LightGray,
                    onClicked = {
                        currencyState.value = it
                        selectedCurrency.invoke(it)
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                CardButton(
                    label = "고점",
                    selectedLabel = highAndLowState.value,
                    fontSize = 15,
                    modifier = Modifier
                        .height(20.dp)
                        .width(40.dp),
                    fontColor = Color.Black,
                    buttonColor = TitleCardColor,
                    disableColor = Color.LightGray,
                    onClicked = {
                        highAndLowState.value = it
                        selectedHighAndLow.invoke(it)

                    }
                )

                CardButton(
                    label = "저점",
                    selectedLabel = highAndLowState.value,
                    fontSize = 15,
                    modifier = Modifier
                        .height(20.dp)
                        .width(40.dp),
                    fontColor = Color.Black,
                    buttonColor = TitleCardColor,
                    disableColor = Color.LightGray,
                    onClicked = {
                        highAndLowState.value = it
                        selectedHighAndLow.invoke(it)
                    }
                )
            }

            when (currencyState.value) {
                "달러" -> {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        when(highAndLowState.value) {
                            "고점" -> {
                                if(dollarHighRate.isNullOrEmpty()) {
                                    Text(text = "목표환율이 설정되어 있지 않습니다.",
                                        modifier = Modifier.padding(bottom = 3.dp))
                                } else {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()) {
                                        dollarHighRate?.forEach { drRate ->
                                            CardButton(
                                                label = "${drRate.number}",
                                                selectedLabel = highDollarNumberState.value,
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                buttonColor = TitleCardColor,
                                                disableColor = Color.LightGray,
                                                onClicked = {
                                                    highDollarNumberState.value = it
                                                    selectedNumber.invoke(it)
                                                }
                                            )
                                        }
                                    }


                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, bottom = 10.dp, top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(text = "목표환율: ${filterHighDrData?.firstOrNull()?.rate ?: ""}")
                                    }
                                }
                            }
                            "저점" -> {
                                if(dollarLowRate.isNullOrEmpty()) {
                                    Text(text = "목표환율이 설정되어 있지 않습니다.",
                                        modifier = Modifier.padding(bottom = 3.dp))
                                } else {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()) {
                                        dollarLowRate?.forEach { drRate ->
                                            CardButton(
                                                label = "${drRate.number}",
                                                selectedLabel = lowDollarNumberState.value,
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                buttonColor = TitleCardColor,
                                                disableColor = Color.LightGray,
                                                onClicked = {
                                                    lowDollarNumberState.value = it
                                                    selectedNumber.invoke(it)
                                                }
                                            )
                                        }
                                    }


                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, bottom = 10.dp, top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(text = "목표환율: ${filterLowDrData?.firstOrNull()?.rate ?: ""}")
                                    }
                                }
                            }
                        }
                    }


                }

                "엔화" -> {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        when(highAndLowState.value) {
                            "고점" -> {
                                if(yenHighRate.isNullOrEmpty()) {
                                    Text(text = "목표환율이 설정되어 있지 않습니다.",
                                        modifier = Modifier.padding(bottom = 3.dp))
                                } else {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()) {
                                        yenHighRate?.forEach { yenRate ->
                                            CardButton(
                                                label = "${yenRate.number}",
                                                selectedLabel = highYenNumberState.value,
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                buttonColor = TitleCardColor,
                                                disableColor = Color.LightGray,
                                                onClicked = {
                                                    highYenNumberState.value = it
                                                    selectedNumber.invoke(it)
                                                }
                                            )
                                        }
                                    }


                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, bottom = 10.dp, top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(text = "목표환율: ${filterHighYenData?.firstOrNull()?.rate ?: ""}")
                                    }
                                }
                            }
                            "저점" -> {
                                if(yenLowRate.isNullOrEmpty()) {
                                    Text(text = "목표환율이 설정되어 있지 않습니다.",
                                        modifier = Modifier.padding(bottom = 3.dp))
                                } else {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()) {
                                        yenLowRate?.forEach { yenRate ->
                                            CardButton(
                                                label = "${yenRate.number}",
                                                selectedLabel = lowYenNumberState.value,
                                                fontSize = 12,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(25.dp)
                                                    .padding(horizontal = 5.dp),
                                                fontColor = Color.Black,
                                                buttonColor = TitleCardColor,
                                                disableColor = Color.LightGray,
                                                onClicked = {
                                                    lowYenNumberState.value = it
                                                    selectedNumber.invoke(it)
                                                }
                                            )
                                        }
                                    }


                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, bottom = 10.dp, top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(text = "목표환율: ${filterLowYenData?.firstOrNull()?.rate ?: ""}")
                                    }
                                }
                            }
                        }
                    }

                }


            }


        }
    }
}