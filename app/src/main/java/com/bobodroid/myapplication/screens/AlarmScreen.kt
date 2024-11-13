package com.bobodroid.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.AutoSizeText
import com.bobodroid.myapplication.components.Dialogs.GuideDialog
import com.bobodroid.myapplication.components.Dialogs.TextFieldDialog
import com.bobodroid.myapplication.components.RateView
import com.bobodroid.myapplication.components.shadowCustom
import com.bobodroid.myapplication.models.datamodels.roomDb.TargetRate
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.ui.theme.HighRateColor
import com.bobodroid.myapplication.ui.theme.LowRateColor
import java.math.BigDecimal


@Composable
fun AlarmScreen(allViewModel: AllViewModel) {

    val recentRate = allViewModel.recentExchangeRateFlow.collectAsState()

    var trashExpanded by remember { mutableStateOf(false) }

    var trashHighRateExpanded by remember {
        mutableStateOf(false)
    }

    var trashLowRateExpanded by remember {
        mutableStateOf(false)
    }

    var addTargetRateExpanded by remember {
        mutableStateOf(false)
    }

    var targetRateMoneyType by remember {
        mutableStateOf(TargetRateMoneyType.Dollar)
    }

    var targetRateState by remember {
        mutableStateOf(TargetRateState.High)
    }

    var targetRateTitle = when(targetRateMoneyType) {
        TargetRateMoneyType.Dollar -> "달러"
        TargetRateMoneyType.Yen -> "엔화"
    }

    var number = listOf<String>("1", "2", "3", "4", "5")


    val targetRateData = allViewModel.targetRateFlow.collectAsState()

    val highTargetRate = when(targetRateMoneyType) {
        TargetRateMoneyType.Dollar -> targetRateData.value.dollarHighRateList
        TargetRateMoneyType.Yen -> targetRateData.value.yenHighRateList
    }

    val lowTargetRate = when(targetRateMoneyType) {
       TargetRateMoneyType.Dollar -> targetRateData.value.dollarLowRateList
        TargetRateMoneyType.Yen -> targetRateData.value.yenLowRateList
    }

    val addTargetTitle = when(targetRateState) {
        TargetRateState.High -> "고점 목표 환율"
        TargetRateState.Low -> "저점 목표 환율"
    }

    val addTargetMsg: TargetRate? = when(targetRateState) {
        TargetRateState.High -> highTargetRate?.lastOrNull()
        TargetRateState.Low -> lowTargetRate?.lastOrNull()
    }

    var addTargetDialog by remember {
        mutableStateOf(false)
    }

    var addTargetGuideDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(modifier = Modifier
            .weight(0.08f)
            .padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically){

            Image(painter = painterResource(id = R.drawable.alarm), contentDescription = "", modifier = Modifier
                .padding(end = 5.dp)
                .padding(start = 15.dp))

            Text(text = "$targetRateTitle 목표 환율설정", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f),
            horizontalArrangement = Arrangement.Center
        ) {
            RateView(
                title = "USD",
                recentRate = "${recentRate.value.usd}",
                subTitle = "JPY",
                subRecentRate = "${
                    BigDecimal(recentRate.value.jpy).times(
                        BigDecimal("100").setScale(-2)
                    )
                }",
                createAt = "${recentRate.value.createAt}"
            )
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(0.68f)
            .padding(bottom = 15.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {

            //고점 목표 환율
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .shadowCustom(
                            color = Color.LightGray,
                            offsetX = 10.dp,
                            offsetY = 10.dp,
                            blurRadius = 10.dp
                        )
                        .padding(horizontal = 10.dp)
                        .background(color = HighRateColor, shape = RoundedCornerShape(5.dp))
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.2f)
                            .padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoSizeText(value = "고점 목표 환율", minFontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f),
                            verticalArrangement = Arrangement.Center) {

                            AutoSizeText(
                                value = "순서",
                                minFontSize = 15.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 5.dp))
                        }


                        Column(
                            Modifier
                                .fillMaxHeight()
                                .weight(0.85f)) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(end = 10.dp),
                                columns = GridCells.Fixed(5),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.Center,
                                content = {

                                    items(number) { aNumber ->
                                        NumberGrid(aNumber)
                                    }

                                } )
                        }




                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f),
                            verticalArrangement = Arrangement.Center) {

                            AutoSizeText(
                                value = "환율",
                                minFontSize = 15.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 5.dp))
                        }


                        Column(
                            Modifier
                                .fillMaxHeight()
                                .weight(0.85f)) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(end = 10.dp),
                                columns = GridCells.Fixed(5),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.Center,
                                content = {

                                    highTargetRate?.let {

                                        items(it) { aRateValue ->

                                            aRateValue.rate?.let {aRate ->
                                                NumberGrid(aRate)
                                            }
                                        }

                                    }


                                } )
                        }




                    }





                }
            }

            //저점 목표 환율
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .shadowCustom(
                            color = Color.LightGray,
                            offsetX = 10.dp,
                            offsetY = 10.dp,
                            blurRadius = 10.dp
                        )
                        .padding(horizontal = 10.dp)
                        .background(color = LowRateColor, shape = RoundedCornerShape(5.dp))
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.2f)
                            .padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoSizeText(value = "저점 목표 환율", minFontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f),
                            verticalArrangement = Arrangement.Center) {

                            AutoSizeText(
                                value = "순서",
                                minFontSize = 15.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 5.dp))
                        }


                        Column(
                            Modifier
                                .fillMaxHeight()
                                .weight(0.85f)) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(end = 10.dp),
                                columns = GridCells.Fixed(5),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.Center,
                                content = {

                                    items(number) { aNumber ->
                                        NumberGrid(aNumber)
                                    }

                                } )
                        }




                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f),
                            verticalArrangement = Arrangement.Center) {

                            AutoSizeText(
                                value = "환율",
                                minFontSize = 15.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 5.dp))
                        }


                        Column(
                            Modifier
                                .fillMaxHeight()
                                .weight(0.85f)) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(end = 10.dp),
                                columns = GridCells.Fixed(5),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.Center,
                                content = {

                                    lowTargetRate?.let {

                                        items(it) { aRateValue ->

                                            aRateValue.rate?.let {aRate ->
                                                NumberGrid(aRate)
                                            }
                                        }

                                    }


                                } )
                        }




                    }





                }
            }
        }

        // 플로팅 버튼

        Row(modifier = Modifier
            .weight(0.14f)
            .padding(top = 5.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically)
        {

            Spacer(modifier = Modifier.weight(1f))


            FloatingActionButton(
                onClick = {
                    targetRateMoneyType = if(targetRateMoneyType == TargetRateMoneyType.Dollar) {TargetRateMoneyType.Yen} else {TargetRateMoneyType.Dollar}
                },
                containerColor = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 10.dp, end = 20.dp)
                    .height(60.dp)
                    .wrapContentWidth(),
            ) {
                Row(
                    Modifier
                        .wrapContentSize()
                        .padding(start = 17.dp, end = 17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(painter = painterResource(id = when(targetRateMoneyType) {
                        TargetRateMoneyType.Dollar -> R.drawable.yenicon
                        TargetRateMoneyType.Yen -> R.drawable.dricon}), contentDescription = "", tint = Color.White, modifier = Modifier.scale(2f))

                }





            }



            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                FloatingActionButton(
                    onClick = {
                        addTargetRateExpanded = true
                    },
                    containerColor = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 10.dp, end = 20.dp)
                        .height(60.dp)
                        .wrapContentWidth(),
                ) {
                    Row(
                        Modifier
                            .wrapContentSize()
                            .padding(start = 17.dp, end = 17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "삭제" ,
                            tint = Color.White
                        )
                    }

                }

                DropdownMenu(
                    scrollState = rememberScrollState(),
                    modifier = Modifier
                        .wrapContentSize(),
                    offset = DpOffset(x = 23.dp, y = 5.dp),
                    expanded = addTargetRateExpanded,
                    onDismissRequest = {
                        addTargetRateExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "고점 목표 환율",
                                        color = Color.Black,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "")
                                }

                            }
                        }, onClick = {

                            if(highTargetRate?.count() == 5) {
                                addTargetRateExpanded = false
                                addTargetGuideDialog = true
                            } else {
                                targetRateState = TargetRateState.High
                                addTargetDialog = true
                                addTargetRateExpanded = false
                            }

                        })




                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "저점 목표 환율",
                                        color = Color.Black,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "")
                                }
                            }
                        },
                        onClick = {
                            if(lowTargetRate?.count() == 5) {
                                addTargetRateExpanded = false
                                addTargetGuideDialog = true
                            } else {
                                targetRateState = TargetRateState.Low
                                addTargetDialog = true
                                addTargetRateExpanded = false
                            }
                        })



                }

            }

            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                FloatingActionButton(
                    onClick = {
                        trashExpanded = true
                    },
                    containerColor = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 10.dp, end = 20.dp)
                        .height(60.dp)
                        .wrapContentWidth(),
                ) {
                    Row(
                        Modifier
                            .wrapContentSize()
                            .padding(start = 17.dp, end = 17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Sharp.Delete,
                            contentDescription = "삭제" ,
                            tint = Color.White
                        )
                    }

                }

                DropdownMenu(
                    scrollState = rememberScrollState(),
                    modifier = Modifier
                        .wrapContentSize(),
                    offset = DpOffset(x = 23.dp, y = 5.dp),
                    expanded = trashExpanded,
                    onDismissRequest = {
                        trashExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "고점 목표 환율",
                                        color = Color.Black,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Icon(imageVector = Icons.Sharp.Delete, contentDescription = "")
                                }

                            }
                        }, onClick = {
                            trashHighRateExpanded = true
                        })

                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "저점 목표 환율",
                                        color = Color.Black,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Icon(imageVector = Icons.Sharp.Delete, contentDescription = "")
                                }
                            }
                        }, onClick = {

                            trashLowRateExpanded = true

                        })

                    DropdownMenu(
                        expanded = trashLowRateExpanded,
                        onDismissRequest = {
                            trashLowRateExpanded = false
                        }) {

                    }

                }

                DropdownMenu(
                    expanded = trashHighRateExpanded,
                    onDismissRequest = {
                        trashHighRateExpanded = false
                    },
                    offset = DpOffset(x = 160.dp, y = 60.dp)
                ) {

                    highTargetRate?.forEach {targetRate ->
                        DropdownMenuItem(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = targetRate.number ?: "없음",
                                            color = Color.Black,
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Icon(imageVector = Icons.Sharp.Delete, contentDescription = "")
                                    }
                                }
                            },
                            onClick = {
                                when(targetRateMoneyType) {
                                    TargetRateMoneyType.Dollar -> allViewModel.targetRateRemove(drHighRate = targetRate)
                                    TargetRateMoneyType.Yen -> allViewModel.targetRateRemove(yenHighRate = targetRate)
                                }
                            })
                    }
                }

                DropdownMenu(
                    expanded = trashLowRateExpanded,
                    onDismissRequest = {
                        trashLowRateExpanded = false
                    },
                    offset = DpOffset(x = 160.dp, y = 5.dp)
                ) {

                    lowTargetRate?.forEach {targetRate ->
                        DropdownMenuItem(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Row(
                                        modifier = Modifier.wrapContentSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = targetRate.number ?: "없음",
                                            color = Color.Black,
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Icon(imageVector = Icons.Sharp.Delete, contentDescription = "")
                                    }
                                }
                            },
                            onClick = {

                                when(targetRateMoneyType) {
                                    TargetRateMoneyType.Dollar -> allViewModel.targetRateRemove(drLowRate = targetRate)
                                    TargetRateMoneyType.Yen -> allViewModel.targetRateRemove(yenLowRate = targetRate)
                                }

                            })
                    }
                }


            }

//            Row(
//                modifier = Modifier
//                    .height(120.dp)
//                    .fillMaxWidth()
//                    .padding(start = 20.dp, bottom = 10.dp),
//                horizontalArrangement = Arrangement.End,
//                verticalAlignment = Alignment.CenterVertically
//
//            ) {
//
//
//
//
//            }
        }

        if(addTargetDialog) {
            TextFieldDialog(
                onDismissRequest = {
                                   addTargetDialog = it
                },
                keyboardType = KeyboardType.Number,
                onClickedLabel = "확인",
                placeholder = "목표환율을 입력해주세요",
                onClicked = { rate ->

                    val addNumber = addTargetMsg?.number?.toInt()?.plus(1) ?: 1

                    val newTargetRate = TargetRate(number = addNumber.toString(), rate = rate)

                            when(targetRateMoneyType) {
                                TargetRateMoneyType.Dollar -> when(targetRateState) {
                                    TargetRateState.High -> {
                                        allViewModel.targetRateAdd(drHighRate = newTargetRate)
                                        addTargetDialog = false
                                    }
                                        TargetRateState.Low -> {
                                            allViewModel.targetRateAdd(drLowRate = newTargetRate)
                                            addTargetDialog = false
                                        }
                                }
                                    TargetRateMoneyType.Yen -> when(targetRateState) {
                                        TargetRateState.High -> {
                                            allViewModel.targetRateAdd(yenHighRate = newTargetRate)
                                            addTargetDialog = false
                                        }
                                            TargetRateState.Low -> {
                                                allViewModel.targetRateAdd(yenLowRate = newTargetRate)
                                                addTargetDialog = false
                                            }
                                    }
                            }


                },
                closeButtonLabel = "닫기"
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Text(text = addTargetTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "등록순서: ${addTargetMsg?.number ?: "없음"}")
                    Text(text = "목표환율: ${addTargetMsg?.rate ?: "없음"}")
                }


            }
        }


        if(addTargetGuideDialog) {
            GuideDialog(onDismissRequest = {
                                           addTargetGuideDialog = it
            }, title = "안내", message = "목표 환율은 5개 까지만 가능합니다.", buttonLabel = "확인")
        }

    }
}

enum class TargetRateMoneyType {
    Dollar,
    Yen
}

enum class TargetRateState {
    High,
    Low
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberGrid(number: String) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                value = number,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .padding(horizontal = 1.dp),
                maxLines = 1,
                minFontSize = 8.sp,
                color = Color.Black
            )
        }

    }
}