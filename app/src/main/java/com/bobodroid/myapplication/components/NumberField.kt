package com.bobodroid.myapplication.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.bobodroid.myapplication.extensions.toUs
import com.bobodroid.myapplication.extensions.toYen
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import java.text.NumberFormat
import java.util.*
import androidx.compose.material.SnackbarHostState
import com.bobodroid.myapplication.extensions.toLongWon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberField(title: String, onClicked: ((String) -> Unit)?, snackBarHostState: SnackbarHostState) {
    var won = NumberFormat.getInstance(Locale.KOREA)
    var openDialog = remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf(0L) }

    var inputMoney = if(userInput == 0L) "$title" else "${userInput.toLongWon()}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(Color.White),

        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
        }
    ) {
        Text(
            text = inputMoney,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(3.dp, top = 10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
    if (openDialog.value) {
        Popup(
            alignment = Alignment.TopCenter,
            onDismissRequest = {openDialog.value = false},
            offset = IntOffset(0, 1100)
        ) {
            Box(){
                PopupNumberView(
                    onClicked = {
                    openDialog.value = false
                    userInput = it.toLong()
                    onClicked?.invoke(userInput.toString())
                },
                    snackBarHostState)
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateNumberField(title: String, onClicked: ((String) -> Unit)?) {

    var openDialog = remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf("0") }

    var inputMoney = if(userInput == "0") "$title" else "${userInput}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(Color.White),

        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
        }
    ) {
        Text(
            text = inputMoney,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(3.dp, top = 10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
    if (openDialog.value) {
        Popup(
            alignment = Alignment.TopCenter,
            onDismissRequest = {openDialog.value = false},
            offset = IntOffset(0, 1100)
        ) {
            Box(){
                FloatPopupNumberView(onClicked = {
                    openDialog.value = false
                    userInput = it
                    onClicked?.invoke(userInput.toString())},
                )
            }
        }
    } else {}
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WonNumberField(title: String, onClicked: ((String) -> Unit)?, wonViewModel: WonViewModel, snackbarHostState: SnackbarHostState) {
    var openDialog = remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf(0) }

    val moneyCgBtn = wonViewModel.moneyCgBtnSelected.collectAsState()


    var inputMoney = if(userInput == 0) "$title"
    else "${
        when(moneyCgBtn.value) {
            1 -> {userInput.toInt().toUs()}
            2 -> {userInput.toFloat().toYen()}
            else -> {"$title"} }
        }"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(Color.White),

        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
        }
    ) {
        Text(
            text = inputMoney,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(3.dp, top = 10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
    if (openDialog.value) {
        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(0, 1100)
        ) {
            Box(){
                PopupNumberView(
                    onClicked = {
                    openDialog.value = false
                    userInput = it.toInt()
                    onClicked?.invoke(userInput.toString())
                },
                    snackbarHostState )
            }
        }
    }
}