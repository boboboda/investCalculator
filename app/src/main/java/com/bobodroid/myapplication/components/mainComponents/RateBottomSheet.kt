package com.bobodroid.myapplication.components.mainComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bobodroid.myapplication.components.Dialogs.FloatPopupNumberView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    rateInput: (String)-> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = sheetState
    ) {
        // Sheet content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            FloatPopupNumberView(onClicked = {
                rateInput(it)
            })
        }
    }
}