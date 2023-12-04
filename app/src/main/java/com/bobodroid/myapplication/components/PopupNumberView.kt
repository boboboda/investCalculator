@file:OptIn(ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import com.bobodroid.myapplication.models.datamodels.CalculateAction
import com.bobodroid.myapplication.ui.theme.ActionButtonBgColor
import com.bobodroid.myapplication.ui.theme.DollarColor
import java.text.NumberFormat
import androidx.compose.material.SnackbarHostState
import com.bobodroid.myapplication.screens.TAG
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun PopupNumberView(
    onClicked: ((String) -> Unit)?,
    snackbarHostState: SnackbarHostState

) {


    val buttons: List<String> = listOf(
        "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "0"
    )



    //매수금 입력
    var UserInput: String by remember { mutableStateOf("") }

    var inputMoney = if(UserInput == "") "" else "${UserInput.toLong().toLongWon()}"


    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    Card(modifier = Modifier
        .padding(15.dp),
        colors = CardDefaults.cardColors(containerColor = DollarColor)
    ) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()) {
            SnackbarHost(
                hostState = snackBarHostState, modifier = Modifier,
                snackbar = { snackbarData ->


                    androidx.compose.material.Card(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .padding(start = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {

                            Text(
                                text = snackbarData.message,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Card(
                                modifier = Modifier.wrapContentSize(),
                                onClick = {
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                }) {
                                androidx.compose.material.Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = "닫기"
                                )
                            }
                        }
                    }
                })
        }



        LazyVerticalGrid(
            modifier = Modifier.padding(15.dp),
            columns = GridCells.Fixed(5),
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
                        action = CalculateAction.AllClear,
                        onClicked = {
                            UserInput = "0"
                        })
                }

                item(){
                    ActionButton(
                        action = CalculateAction.Del,
                        onClicked = {
                            UserInput = if(UserInput.toString().length == 1) "0" else UserInput.dropLast(1)
                        })
                }

                item(span = {
                    GridItemSpan(2)
                }) {
                    ActionButton(action = CalculateAction.Enter,
                        onClicked = { if (UserInput.isNotEmpty())
                        { onClicked?.invoke(UserInput)
                        } else {return@ActionButton }
                        }

                    )
                }


                items(buttons) { aButtons ->
                    NumberButton(aButtons, onClicked = {


                        scope.launch {
                            if(UserInput.length >= 12) {

                                if(snackBarHostState.currentSnackbarData == null) {
                                    snackBarHostState.showSnackbar(
                                        "너무 큰 수를 입력하셨습니다.\n 열두자리 이하 숫자까지만 가능합니다.",
                                        actionLabel = "닫기", SnackbarDuration.Short
                                    )
                                } else {
                                    return@launch
                                }
                            } else {
                                if(UserInput == "0") UserInput = aButtons.toString() else UserInput += aButtons
                            }
                        }
                    }) //숫자 버튼
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    NumberButtonBottom(number = "99", onClicked = {

                        scope.launch {
                            if(UserInput.length >= 12) {

                                if(snackBarHostState.currentSnackbarData == null) {
                                    snackBarHostState.showSnackbar(
                                        "너무 큰 수를 입력하셨습니다.\n 열두자리 이하 숫자까지만 가능합니다.",
                                        actionLabel = "닫기", SnackbarDuration.Short
                                    )
                                } else {
                                    return@launch
                                }
                            } else {
                                UserInput += "99"
                            }
                        }



                    }, 20)
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    NumberButtonBottom(number = "00", onClicked = {

                        scope.launch {
                            if(UserInput.length >= 12) {

                                if(snackBarHostState.currentSnackbarData == null) {
                                    snackBarHostState.showSnackbar(
                                        "너무 큰 수를 입력하셨습니다.\n 열두자리 이하 숫자까지만 가능합니다.",
                                        actionLabel = "닫기", SnackbarDuration.Short
                                    )
                                } else {
                                    return@launch
                                }
                            } else {
                                UserInput += "00"
                            }
                        }
                    }, 20)
                }

                item(span = {
                    GridItemSpan(3)
                }) {
                    NumberButtonBottom(number = "000", onClicked = {

                        scope.launch {
                            if(UserInput.length >= 12) {

                                if(snackBarHostState.currentSnackbarData == null) {
                                    snackBarHostState.showSnackbar(
                                        "너무 큰 수를 입력하셨습니다.\n 열두자리 이하 숫자까지만 가능합니다.",
                                        actionLabel = "닫기", SnackbarDuration.Short
                                    )
                                } else {
                                    return@launch
                                }
                            } else {
                                UserInput += "000"
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


            } )}
}

@Composable
fun ActionButton(action: CalculateAction, onClicked: (() -> Unit)? = null)  {
    Card(
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
                minFontSize = 15.sp,
                color = Color.Black)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberButton(number: String, onClicked: () -> Unit) {
    Card(
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
            minFontSize = 15.sp,
            color = Color.Black)
        }
    }
}

@Composable
fun NumberButtonBottom(number: String, onClicked: () -> Unit, fontSize: Int) {
    Card(
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
fun FloatPopupNumberView(onClicked: ((String) -> Unit)?) {

    var won = NumberFormat.getInstance(Locale.KOREA)

    val actions: Array<CalculateAction> = CalculateAction.values()


    val buttons: List<String> = listOf(
        "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "0"
    )

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //매수금 입력
    var UserInput: String by remember { mutableStateOf("") }

    var inputMoney = if(UserInput == "") "" else "${UserInput.toLong().toLongWon()}"





    Card(modifier = Modifier
        .padding(15.dp),
        colors = CardDefaults.cardColors(containerColor = DollarColor)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()) {
            SnackbarHost(
                hostState = snackBarHostState, modifier = Modifier,
                snackbar = { snackbarData ->


                    androidx.compose.material.Card(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .padding(start = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {

                            Text(
                                text = snackbarData.message,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Card(
                                modifier = Modifier.wrapContentSize(),
                                onClick = {
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                }) {
                                androidx.compose.material.Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = "닫기"
                                )
                            }
                        }
                    }
                })
        }

        LazyVerticalGrid(modifier = Modifier.padding(15.dp),
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
                        action = CalculateAction.AllClear,
                        onClicked = {
                            UserInput = "0"
                        })
                }

                item(span = {
                    GridItemSpan(1)
                }){
                    FloatActionButton(
                        action = CalculateAction.Del,
                        onClicked = {
                            UserInput = if(UserInput.toString().length == 1) "0" else UserInput.dropLast(1)
                        })
                }


                item(span = {
                    GridItemSpan(2)
                }) {
                    FloatActionButton(action = CalculateAction.Enter,
                        onClicked = { if (UserInput.isNotEmpty())
                        { onClicked?.invoke(UserInput)
                        } else {return@FloatActionButton }
                        }

                    )
                }

                items(buttons) { aButtons ->
                    FloatNumberButton(aButtons, onClicked = {

                        scope.launch {
                            if(UserInput.length >= 10) {

                               if(snackBarHostState.currentSnackbarData == null) {
                                   snackBarHostState.showSnackbar(
                                       "너무 큰 수를 입력하셨습니다.\n 열자리 이하 숫자까지만 가능합니다.",
                                       actionLabel = "닫기", SnackbarDuration.Short
                                   )
                               } else {
                                   return@launch
                               }
                            } else {
                                if(UserInput == "0") UserInput = aButtons else UserInput += aButtons
                            }
                        }




                    }) //숫자 버튼
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    NumberButtonBottom(number = "00", onClicked = {

                        scope.launch {
                            if(UserInput.length >= 10) {
                                if(snackBarHostState.currentSnackbarData == null) {
                                    snackBarHostState.showSnackbar(
                                        "너무 큰 수를 입력하셨습니다.\n 열자리 이하 숫자까지만 가능합니다.",
                                        actionLabel = "닫기", SnackbarDuration.Short
                                    )
                                } else {
                                    return@launch
                                }
                            } else {
                                UserInput += "00"
                            }
                        }
                    }, 30)
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    FloatActionButton(action = CalculateAction.DOT,
                        onClicked = {

                            scope.launch {
                                if(UserInput.length >= 10) {
                                    if(snackBarHostState.currentSnackbarData == null) {
                                        snackBarHostState.showSnackbar(
                                            "너무 큰 수를 입력하셨습니다.\n 열자리 이하 숫자까지만 가능합니다.",
                                            actionLabel = "닫기", SnackbarDuration.Short
                                        )
                                    } else {
                                        return@launch
                                    }
                                } else {
                                    UserInput += "."
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

@Composable
fun FloatActionButton(action: CalculateAction, onClicked: (() -> Unit)? = null)  {
    Card(
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
fun FloatNumberButton(number: String, onClicked: () -> Unit) {
    Card(
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

