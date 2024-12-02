package com.bobodroid.myapplication.components.Dialogs

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
import com.bobodroid.myapplication.extensions.toYen
import java.text.NumberFormat
import java.util.*
import androidx.compose.material.SnackbarHostState
import com.bobodroid.myapplication.extensions.toLongUs
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.ui.theme.BottomSheetSelectedColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.SelectedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberField(
    title: String,
    haveValue: String? = "",
    onClicked: ((String) -> Unit)?
) {

    var won = NumberFormat.getInstance(Locale.KOREA)
    val openDialog = remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf(haveValue) }

    val inputMoney = if (userInput == "") title else "${userInput?.toLong()?.toLongWon()}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(Color.White),

        onClick = {
            if (!openDialog.value) openDialog.value = !openDialog.value else null
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
            alignment = Alignment.BottomCenter,
            onDismissRequest = { openDialog.value = false },
            offset = IntOffset(0, -180)
        ) {
            Box() {
                PopupNumberView(
                    onClicked = {
                        openDialog.value = false
                        userInput = it
                        onClicked?.invoke(it)
                    },
                    limitNumberLength = 10
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateNumberField(
    title: String,
    haveValue: String? = "0",
    modifier: Modifier,
    onClicked: ((String) -> Unit)?
) {

    var openDialog = remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf(haveValue) }

    var inputMoney = if (userInput == "0") "$title" else "${userInput}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(Color.White),

        onClick = {
            if (openDialog.value == false) openDialog.value = !openDialog.value else null
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
            alignment = Alignment.BottomCenter,
            onDismissRequest = { openDialog.value = false },
            offset = IntOffset(0, -180)
        ) {
            Box() {
                FloatPopupNumberView(
                    onClicked = {
                        openDialog.value = false
                        userInput = it
                        onClicked?.invoke(userInput.toString())
                    },
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetNumberField(
    title: String,
    selectedState: Boolean,
    onClicked: () -> Unit
) {

   val formatTitle = if(title == "") {
       "매수금(원)을 입력해주세요"
   } else {
       title.toLong().toLongWon()
    }

    val cardColor = if(selectedState) CardDefaults.cardColors(BottomSheetSelectedColor) else CardDefaults.cardColors(Color.White)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = cardColor,

        onClick = onClicked
    ) {
        Text(
            text = formatTitle,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(3.dp, top = 10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetRateNumberField(
    title: String,
    modifier: Modifier,
    selectedState: Boolean,
    onClicked: () -> Unit
) {

    val formatTile = if (title == "") "매수환율을 입력해주세요" else title

    val cardColor = if(selectedState) CardDefaults.cardColors(BottomSheetSelectedColor) else CardDefaults.cardColors(Color.White)


    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = cardColor,

        onClick = onClicked
    ) {
        Text(
            text = formatTile,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(3.dp, top = 10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}