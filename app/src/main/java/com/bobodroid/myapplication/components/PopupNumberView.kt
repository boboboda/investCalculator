@file:OptIn(ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import java.util.*


@Composable
fun PopupNumberView(onClicked: ((String) -> Unit)?) {


    val buttons: List<String> = listOf(
        "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "0"
    )



    //매수금 입력
    var UserInput: String by remember { mutableStateOf("") }

    var inputMoney = if(UserInput == "") "" else "${UserInput.toLong().toLongWon()}"




    Card(modifier = Modifier
        .padding(15.dp),
        colors = CardDefaults.cardColors(containerColor = DollarColor)
    ) {
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
                        if(UserInput == "0") UserInput = aButtons.toString() else UserInput += aButtons
                    }) //숫자 버튼
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    NumberButtonBottom(number = "99", onClicked = {
                        UserInput += "99"
                    }, 20)
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    NumberButtonBottom(number = "00", onClicked = {
                        UserInput += "00"
                    }, 20)
                }

                item(span = {
                    GridItemSpan(3)
                }) {
                    NumberButtonBottom(number = "000", onClicked = {
                        UserInput += "000"
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
        elevation = CardDefaults.cardElevation(8.dp),
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

@Composable
fun NumberButton(number: String, onClicked: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp) ,
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
        elevation = CardDefaults.cardElevation(8.dp) ,
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




    //매수금 입력
    var UserInput: String by remember { mutableStateOf("") }

    var inputMoney = if(UserInput == "") "" else "${UserInput}"





    Card(modifier = Modifier
        .padding(15.dp),
        colors = CardDefaults.cardColors(containerColor = DollarColor)
    ) {
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
                        if(UserInput == "0") UserInput = aButtons else UserInput += aButtons
                    }) //숫자 버튼
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    NumberButtonBottom(number = "00", onClicked = {
                        UserInput += "00"
                    }, 30)
                }

                item(span = {
                    GridItemSpan(1)
                }) {
                    FloatActionButton(action = CalculateAction.DOT,
                        onClicked = {UserInput += "."})

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
fun FloatActionButton(action: CalculateAction, onClicked: (() -> Unit)? = null)  {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
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
        elevation = CardDefaults.cardElevation(8.dp) ,
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

