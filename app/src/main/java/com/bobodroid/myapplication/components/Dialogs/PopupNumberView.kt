@file:OptIn(ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.util.CalculateAction
import com.bobodroid.myapplication.ui.theme.ActionButtonBgColor
import com.bobodroid.myapplication.ui.theme.DollarColor
import java.text.NumberFormat
import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.unit.Dp
import com.bobodroid.myapplication.components.AutoSizeText
import com.bobodroid.myapplication.screens.MainEvent
import com.bobodroid.myapplication.screens.PopupEvent
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun PopupNumberView(
    limitNumberLength: Int,
    event: (PopupEvent) -> Unit) {

    val buttons: List<String> = listOf(
        "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "0"
    )

    //매수금 입력
    var UserInput by remember { mutableStateOf("") }

    var inputMoney = if(UserInput == "") "" else "${UserInput.toLong().toLongWon()}"

    val scope = rememberCoroutineScope()


    Card(modifier = Modifier
        .heightIn(max = 450.dp)
        .padding(15.dp)
        .padding(bottom = 25.dp),
        colors = CardDefaults.cardColors(containerColor = DollarColor)
    ) {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            val availableHeight = maxHeight
            val availableWidth = maxWidth

            val horizontalSize = (availableWidth - (8.dp * 3)) / 4
            val verticalSize = (availableHeight - (8.dp * 4)) / 5


            val itemSize = minOf(horizontalSize, verticalSize)


            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 15.dp),
                columns = GridCells.Fixed(5),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = {

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                            Text(
                                text = "$inputMoney",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 45.sp,
                                maxLines = 1,
                                color = Color.Black
                            )
                        }
                    }

                    item(span = {
                        GridItemSpan(2)
                    }) {
                        ActionButton(
                            itemSize,
                            action = CalculateAction.AllClear,
                            onClicked = {
                                UserInput = ""})
                    }

                    item(){
                        ActionButton(
                            itemSize,
                            action = CalculateAction.Del,
                            onClicked = {
                                UserInput = if(UserInput.length == 1) "0" else UserInput.dropLast(1)
                            })
                    }

                    item(span = {
                        GridItemSpan(2)
                    }) {
                        ActionButton(
                            itemSize,
                            action = CalculateAction.Enter,
                            onClicked = { if (UserInput.isNotEmpty())
                            {
                                event(PopupEvent.OnClicked(UserInput))

                            } else {return@ActionButton }
                            }

                        )
                    }


                    items(buttons) { aButtons ->
                        NumberButton(
                            itemSize,
                            aButtons,
                            onClicked = {
                                scope.launch {
                                    if(UserInput.length >= limitNumberLength) {
                                        event(PopupEvent.SnackBarEvent("너무 큰 수를 입력하셨습니다.\n ${limitNumberLength}자리 이하 숫자까지만 가능합니다."))
                                        UserInput = ""
                                    } else {
                                        if(UserInput == "")
                                        {
                                            if(aButtons == "0") {
                                                event(PopupEvent.SnackBarEvent("0원은 입력할 수 없습니다."))
                                                UserInput = ""

                                            } else {
                                                UserInput += aButtons
                                            }

                                        } else UserInput += aButtons
                                    }
                                }
                            }) //숫자 버튼
                    }

                    item(span = {
                        GridItemSpan(1)
                    }) {
                        NumberButtonBottom(
                            itemSize,
                            number = "99", onClicked = {

                                scope.launch {
                                    if(UserInput.length >= limitNumberLength) {

                                        event(PopupEvent.SnackBarEvent("너무 큰 수를 입력하셨습니다.\n ${limitNumberLength}자리 이하 숫자까지만 가능합니다."))
                                        UserInput = ""
                                    } else {
                                        UserInput += "99"
                                    }
                                }



                            }, 20)
                    }

                    item(span = {
                        GridItemSpan(1)
                    }) {
                        NumberButtonBottom(
                            itemSize,
                            number = "00", onClicked = {

                                scope.launch {
                                    if(UserInput.length >= limitNumberLength) {
                                        event(PopupEvent.SnackBarEvent("너무 큰 수를 입력하셨습니다.\n ${limitNumberLength}자리 이하 숫자까지만 가능합니다."))
                                        UserInput = ""
                                    } else {

                                        if(UserInput == "") {
                                            event(PopupEvent.SnackBarEvent("0원은 입력할 수 없습니다."))
                                            UserInput = ""
                                        } else {
                                            UserInput += "00"
                                        }
                                    }
                                }
                            }, 20)
                    }

                    item(span = {
                        GridItemSpan(3)
                    }) {
                        NumberButtonBottom(
                            itemSize,
                            number = "000", onClicked = {

                                scope.launch {
                                    if(UserInput.length >= limitNumberLength) {
                                        event(PopupEvent.SnackBarEvent("너무 큰 수를 입력하셨습니다.\n ${limitNumberLength}자리 이하 숫자까지만 가능합니다."))
                                        UserInput = ""

                                    } else {

                                        if(UserInput == "") {
                                            event(PopupEvent.SnackBarEvent("0원은 입력할 수 없습니다."))
                                            UserInput = ""
                                        } else {
                                            UserInput += "000"
                                        }
                                    }
                                }


                            }, 20)
                    }


                    item(span = {
                        GridItemSpan(5)
                    }) {
                        Row(modifier = Modifier.height(5.dp)) {
                        }
                    }


                } )


        }
    }






}

@Composable
fun ActionButton(itemSize: Dp, action: CalculateAction, onClicked: (() -> Unit)? = null)  {
    Card(
        modifier = Modifier.size(itemSize),
        colors = CardDefaults.cardColors(
            containerColor = ActionButtonBgColor
        ),
        onClick = {
            onClicked?.invoke()
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            AutoSizeText(
                value = action.symbol,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp),
                maxLines = 1,
                minFontSize = 12.sp,
                color = Color.Black)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberButton(
    itemSize: Dp,
    number: String,
    onClicked: () -> Unit) {
    Card(
        modifier = Modifier.size(itemSize),
        onClick = onClicked
    ) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
        AutoSizeText(
            value = number,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp),
            maxLines = 1,
            minFontSize = 12.sp,
            color = Color.Black)
        }
    }
}

@Composable
fun NumberButtonBottom(
    itemSize: Dp,
    number: String, onClicked: () -> Unit, fontSize: Int) {
    Card(
        modifier = Modifier
            .size(itemSize),
        onClick = onClicked
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                value = number,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp),
                maxLines = 1,
                minFontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun FloatPopupNumberView(
    event: (PopupEvent) -> Unit) {
    //환율 입력하는 뷰

    val buttons: List<String> = listOf(
        "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "0"
    )

    val scope = rememberCoroutineScope()

    //매수금 입력
    var UserInput: String by remember { mutableStateOf("") }

    var inputMoney = if(UserInput == "") "" else "${UserInput}"


    Card(modifier = Modifier.heightIn(max = 450.dp)
        .padding(15.dp)
        .padding(bottom = 25.dp),
        colors = CardDefaults.cardColors(containerColor = DollarColor)
    ) {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            val availableHeight = maxHeight
            val availableWidth = maxWidth

            val horizontalSize = (availableWidth - (8.dp * 3)) / 4
            val verticalSize = (availableHeight - (8.dp * 4)) / 5

            // 더 작은 값을 선택하여 정사각형 유지
            val itemSize = minOf(horizontalSize, verticalSize)

            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 15.dp),
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = {

                    item(span = { GridItemSpan(maxLineSpan) }) {


                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {

                            Text(
                                text = "$inputMoney",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 45.sp,
                                maxLines = 1,
                                color = Color.Black
                            )
                        }


                    }

                    item(span = {
                        GridItemSpan(1)
                    }) {
                        FloatActionButton(
                            itemSize,
                            action = CalculateAction.AllClear,
                            onClicked = {
                                UserInput = ""
                            })
                    }

                    item(span = {
                        GridItemSpan(1)
                    }){
                        FloatActionButton(
                            itemSize,
                            action = CalculateAction.Del,
                            onClicked = {
                                UserInput = if(UserInput.toString().length == 1) "0" else UserInput.dropLast(1)
                            })
                    }


                    item(span = {
                        GridItemSpan(2)
                    }) {
                        FloatActionButton(
                            itemSize,
                            action = CalculateAction.Enter,
                            onClicked = { if (UserInput.isNotEmpty())
                            {
                                event(PopupEvent.OnClicked(UserInput))
                            } else {return@FloatActionButton }
                            }

                        )
                    }

                    items(buttons) { aButtons ->
                        FloatNumberButton(
                            itemSize,
                            aButtons, onClicked = {

                            scope.launch {
                                if(UserInput.length >= 10) {
                                    event(PopupEvent.SnackBarEvent("너무 큰 수를 입력하셨습니다.\n 열자리 이하 숫자까지만 가능합니다."))
                                    UserInput = ""
                                } else {
                                    if(UserInput == "")
                                    {
                                        if(aButtons == "0") {
                                            event(PopupEvent.SnackBarEvent("0원은 입력할 수 없습니다."))
                                            UserInput = ""
                                        } else {
                                            UserInput += aButtons
                                        }
                                    } else UserInput += aButtons
                                }
                            }




                        }) //숫자 버튼
                    }

                    item(span = {
                        GridItemSpan(1)
                    }) {
                        NumberButtonBottom(
                            itemSize,
                            number = "00", onClicked = {

                                scope.launch {
                                    if(UserInput.length >= 10) {
                                        event(PopupEvent.SnackBarEvent( "너무 큰 수를 입력하셨습니다.\n 열자리 이하 숫자까지만 가능합니다."))
                                        UserInput = ""
                                    } else {
                                        if(UserInput == "")
                                        {
                                            event(PopupEvent.SnackBarEvent("0원은 입력할 수 없습니다."))
                                            UserInput = ""

                                        } else UserInput += "00"
                                    }
                                }
                            }, 30)
                    }

                    item(span = {
                        GridItemSpan(1)
                    }) {
                        FloatActionButton(
                            itemSize,
                            action = CalculateAction.DOT,
                            onClicked = {

                                scope.launch {
                                    if(hasTwoOrMoreDots(UserInput)) {
                                        event(PopupEvent.SnackBarEvent("소수점은 2개 이상 찍을 수 없습니다."))
                                        UserInput = ""
                                    } else {
                                        if(UserInput.length >= 10) {
                                            event(PopupEvent.SnackBarEvent("너무 큰 수를 입력하셨습니다.\n 열자리 이하 숫자까지만 가능합니다."))
                                            UserInput = ""
                                        } else {
                                            if(UserInput == "")
                                            {
                                                event(PopupEvent.SnackBarEvent("소수점을 먼저 입력할 수 없습니다."))
                                                UserInput = ""
                                            } else UserInput += "."
                                        }
                                    }
                                }
                            })

                    }


                    item(span = {
                        GridItemSpan(5)
                    }) {
                        Row(modifier = Modifier.height(5.dp)) {
                        }
                    }


                } )


        }

    }






}

@Composable
fun FloatActionButton(
    itemSize: Dp,
    action: CalculateAction, onClicked: (() -> Unit)? = null)  {
    Card(
        modifier = Modifier.size(itemSize),
        colors = CardDefaults.cardColors(
            containerColor = ActionButtonBgColor
        ),
        onClick = {
            onClicked?.invoke()
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                value = action.symbol,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp),
                maxLines = 1,
                minFontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun FloatNumberButton(
    itemSize: Dp,
    number: String, onClicked: () -> Unit) {
    Card(
        modifier = Modifier.size(itemSize),
        onClick = onClicked
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                value = number,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp),
                maxLines = 1,
                minFontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}


fun hasTwoOrMoreDots(str: String): Boolean {
    return str.count { it == '.' } >= 1
}