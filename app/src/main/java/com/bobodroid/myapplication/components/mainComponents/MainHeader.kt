package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.RateView
import com.bobodroid.myapplication.components.admobs.BannerAd
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.models.viewmodels.MainViewUiState
import com.bobodroid.myapplication.ui.theme.WelcomeScreenBackgroundColor
import com.bobodroid.myapplication.ui.theme.primaryColor
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHeader(
    mainUiState: MainViewUiState,
    updateCurrentForeignCurrency: (CurrencyType) -> Unit,
    hideSellRecordState: Boolean,
    onHide:(Boolean) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val visibleIcon = if (hideSellRecordState)  R.drawable.ic_visible  else  R.drawable.ic_invisible

    Column {
        // 메인 최상단 뷰 -> 최신환율, 외화 선택 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(0.7f)
            ) {
                when(mainUiState.selectedCurrencyType) {
                    CurrencyType.USD -> {
                        RateView(
                            title = "USD",
                            recentRate = "${mainUiState.recentRate.usd}",
                            createAt = mainUiState.recentRate.createAt
                        )
                    }
                    CurrencyType.JPY -> {
                        RateView(
                            title = "JPY",
                            recentRate = "${
                                BigDecimal(mainUiState.recentRate.jpy).times(
                                    BigDecimal("100").setScale(-2),
                                )
                            }",
                            createAt = "${mainUiState.recentRate.createAt}"
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(0.3f)) {

                Box {
                    TextButton(
                        onClick = { dropdownExpanded = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text(
                            when(mainUiState.selectedCurrencyType) {
                                CurrencyType.USD -> "달러(USD)"
                                CurrencyType.JPY -> "엔화(JPY)"
                            }
                        )
                        Icon(Icons.Filled.ArrowDropDown, null)
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                updateCurrentForeignCurrency(CurrencyType.USD)
                                dropdownExpanded = false
                            },
                            text = { Text("달러(USD)") }
                        )
                        DropdownMenuItem(
                            onClick = {
                                updateCurrentForeignCurrency(CurrencyType.JPY)
                                dropdownExpanded = false
                            },
                            text = { Text("엔화(JPY)") }
                        )
                    }
                }

            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(bottom = 5.dp)
                .padding(end = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier
                .wrapContentWidth()
                .padding(end = 20.dp)) {
//                GetMoneyView(
//                    getMoney = "${totalDrSellProfit.value}",
//                    onClicked = {  },
//                    allViewModel
//                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Card(
                colors = CardDefaults.cardColors(WelcomeScreenBackgroundColor),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(1.dp),
                modifier = Modifier
                    .height(40.dp)
                    .wrapContentWidth(),
                onClick = {
                    if (!hideSellRecordState) onHide(true) else onHide(false)
                }) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "매도기록")

                    Image(
                        painter = painterResource(id = visibleIcon),
                        contentDescription = "매도기록 노출 여부"
                    )
                }
            }

        }

        if(!mainUiState.bannerAdState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                BannerAd()

            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
//            Text(
//                modifier = Modifier.padding(start = 10.dp),
//                text = "예상수익 새로고침 시간: ${reFreshDate.value}",
//                textAlign = TextAlign.Center
//            )
        }
    }
}