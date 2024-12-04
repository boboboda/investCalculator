package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.ModalBottomSheetValue
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
import androidx.compose.material.TextField
import androidx.compose.material.rememberModalBottomSheetState
import com.bobodroid.myapplication.components.mainComponents.RateBottomSheet
import com.bobodroid.myapplication.extensions.toLongUs
import com.bobodroid.myapplication.extensions.toLongWon
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import com.bobodroid.myapplication.ui.theme.BottomSheetSelectedColor
import com.bobodroid.myapplication.ui.theme.BuyColor
import com.bobodroid.myapplication.ui.theme.SelectedColor
import androidx.compose.material3.rememberModalBottomSheetState
import com.bobodroid.myapplication.screens.PopupEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateNumberField(
    title: String,
    haveValue: String? = "0",
    modifier: Modifier,
    onClicked: () -> Unit
) {
    var userInput by remember { mutableStateOf(haveValue) }

    var inputMoney = if (userInput == "0") "$title" else "${userInput}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(Color.White),

        onClick = onClicked
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


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetNumberField(
    title: String,
    selectedState: Boolean,
    modifier: Modifier,
    onClicked: () -> Unit
) {

   val formatTitle = if(title == "") {
       "매수금(원)을 입력해주세요"
   } else {
       title.toLong().toLongWon()
    }

    val cardColor = if(selectedState) CardDefaults.cardColors(BottomSheetSelectedColor) else CardDefaults.cardColors(Color.White)

    Card(
        modifier = modifier
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
    placeholder: String,
    onClicked: () -> Unit
) {

    val formatTile = if (title == "") placeholder else title

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