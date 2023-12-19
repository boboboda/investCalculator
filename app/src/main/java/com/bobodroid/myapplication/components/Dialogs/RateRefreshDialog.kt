package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.ui.theme.SellButtonColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateRefreshDialog(
    allViewModel: AllViewModel,
    onDismissRequest: (Boolean) -> Unit,
    onClicked: () -> Unit,
) {

    val localUserData = allViewModel.localUserData.collectAsState()

    val freeChance = localUserData.value.rateResetCount

    val payChance = localUserData.value.rateAdCount

    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(top = 20.dp, bottom = 30.dp)
                .padding(horizontal = 20.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text("환율 새로고침 하시겠습니까? \n" +
                    "(무료기회가 우선적으로 차감됩니다.)")

            Text("무료기회: ${freeChance}회   광고기회: ${payChance}회")

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier =
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Buttons(label = "예",
                    onClicked = onClicked,

                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),
                    fontSize = 15)
                Spacer(modifier = Modifier.width(25.dp))

                Buttons(label = "아니요",
                    onClicked = {onDismissRequest(false)},
                    color = SellButtonColor,
                    fontColor = Color.White,
                    modifier = Modifier
                        .height(40.dp)
                        .wrapContentSize()
                    , fontSize = 15)
            }
        }
    }
}