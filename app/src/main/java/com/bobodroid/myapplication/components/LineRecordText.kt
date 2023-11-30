@file:OptIn(ExperimentalMaterial3Api::class)

package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.extensions.*
import com.bobodroid.myapplication.models.datamodels.*
import com.bobodroid.myapplication.models.viewmodels.DollarViewModel
import com.bobodroid.myapplication.models.viewmodels.WonViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.screens.TAG
import com.bobodroid.myapplication.ui.theme.Purple80
import com.bobodroid.myapplication.ui.theme.SelectedColor
import com.bobodroid.myapplication.ui.theme.SellPopColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineRecordText(
    data: DrBuyRecord,
    sellAction: Boolean = data.recordColor,
    sellActed: (DrBuyRecord) -> Unit,
    onClicked: ((DrBuyRecord)-> Unit)?,
    dollarViewModel: DollarViewModel) {

    val openDialog = remember { mutableStateOf(false) }


    Card(
        modifier = Modifier
            .height(60.dp)
            .padding(0.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = if(sellAction) SelectedColor else Color.White , contentColor = Color.Black),
        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
            onClicked?.invoke(data)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            RecordTextView(
                recordText = "${data.date}",
                TextHeight = 50.dp,
                13,
                2.5f,
                bottonPpaing = 0.dp,
                color = Color.Black)

            Spacer(modifier = Modifier.width(1.dp))

            RecordTextView(
                recordText = "${data.exchangeMoney.toDecUs()}\n (${data.money.toLong().toLongWon()})",
                TextHeight = 50.dp,
                13,
                2.5f,
                bottonPpaing = 0.dp,
                color = Color.Black)

            Spacer(modifier = Modifier.width(1.dp))

            RecordTextView(
                recordText = "${data.rate}",
                TextHeight = 50.dp,
                13,
                2.5f,
                bottonPpaing = 0.dp,
                color = Color.Black)

            Spacer(modifier = Modifier.width(1.dp))

            RecordTextView(
                recordText = "",
                TextHeight = 50.dp,
                13,
                2.5f,
                bottonPpaing = 0.dp,
                color = Color.Black)
        }

    }
    if (openDialog.value) {
       SellDialog(
           sellAction = { sellActed(data)},
           onDismissRequest = { openDialog.value = it},
           onClicked = {openDialog.value = it},
           dollarViewModel = dollarViewModel,)
    }
}


@Composable
fun SellLineRecordText(date: DrSellRecord,
                       onClicked: ((DrSellRecord)-> Unit)?,
                       dollarViewModel: DollarViewModel) {

    var openDialog = remember { mutableStateOf(false) }


    Card(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(60.dp),
        shape = RoundedCornerShape(0.dp),

        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),
        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
            onClicked?.invoke(date)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RecordTextView(recordText = "${date.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.money.toFloat().toDecUs()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.rate}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.exchangeMoney.toFloat().toWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Red)
        }

    }

}

@Composable
fun LineRecordText(
    date: YenBuyRecord,
    sellAction: Boolean = date.recordColor,
    sellActed: (YenBuyRecord) -> Unit,
    onClicked: ((YenBuyRecord)-> Unit)?,
    yenViewModel: YenViewModel) {

    val openDialog = remember { mutableStateOf(false) }



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(0.dp),

        colors = CardDefaults.cardColors(containerColor = if(sellAction) SelectedColor else Color.White , contentColor = Color.Black),

        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
            onClicked?.invoke(date)
        }
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RecordTextView(recordText = "${date.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.money.toFloat().toWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.rate}", TextHeight = 50.dp,13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.exchangeMoney.toYen()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
        }
    }


    if (openDialog.value) {
        YenSellDialog(
            sellAction = { sellActed(date)},
            onDismissRequest = { openDialog.value = it},
            onClicked = {openDialog.value = it},
            yenViewModel = yenViewModel)
    }
}


@Composable
fun SellLineRecordText(date: YenSellRecord,
                       onClicked: ((YenSellRecord)-> Unit)?,
                       yenViewModel: YenViewModel) {

    var openDialog = remember { mutableStateOf(false) }


    Card(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(60.dp),
        shape = RoundedCornerShape(0.dp),

        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),

        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
            onClicked?.invoke(date)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RecordTextView(recordText = "${date.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.money.toFloat().toYen()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.rate}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.exchangeMoney.toWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Red)
        }
    }

}




@Composable
fun WonLineRecordText(
    date: WonBuyRecord,
    sellAction: Boolean = date.recordColor,
    sellActed: (WonBuyRecord) -> Unit,
    onClicked: ((WonBuyRecord)-> Unit)?,
    wonViewModel: WonViewModel) {

    var openDialog = remember { mutableStateOf(false) }


    val moneyCg = when(date.moneyType) {
        1 -> {date.money.toInt().toUs() }
        2 -> {date.money.toFloat().toYen() }
        else -> null
    }


    Card(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(60.dp),
        shape = RoundedCornerShape(0.dp),

        colors = CardDefaults.cardColors(containerColor = if(sellAction) SelectedColor else Color.White, contentColor = Color.Black),
        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
            onClicked?.invoke(date)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RecordTextView(recordText = "${date.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${moneyCg}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.rate}", TextHeight = 50.dp,13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.exchangeMoney.toFloat().toWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
        }

    }
    if (openDialog.value) {
        WonSellDialog(
            onDismissRequest = { openDialog.value = it},
            onClicked = {openDialog.value = it},
            wonViewModel = wonViewModel,
            sellAction = { sellActed(date) })
    }
}





@Composable
fun WonSellLineRecordText(
    date: WonSellRecord,
    onClicked: ((WonSellRecord)-> Unit)?,
    wonViewModel: WonViewModel) {

    var openDialog = remember { mutableStateOf(false) }

    val moneyCg = when(date.moneyType) {
        1 -> {date.exchangeMoney.toDecUs() }
        2 -> {date.exchangeMoney.toFloat().toYen() }
        else -> null
    }


    Card(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .height(60.dp),
        shape = RoundedCornerShape(0.dp),

        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),


        onClick = {
            if(openDialog.value == false) openDialog.value = !openDialog.value else null
            onClicked?.invoke(date)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RecordTextView(recordText = "${date.date}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.money.toFloat().toWon()}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${date.rate}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Black)
            Spacer(modifier = Modifier.width(1.dp))
            RecordTextView(recordText = "${moneyCg}", TextHeight = 50.dp, 13, 2.5f, bottonPpaing = 0.dp, color = Color.Red)
        }
    }

}

