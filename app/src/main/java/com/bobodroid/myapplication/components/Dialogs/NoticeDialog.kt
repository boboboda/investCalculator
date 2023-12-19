package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.CardIconButton
import com.bobodroid.myapplication.models.viewmodels.AllViewModel

@Composable
fun NoticeDialog(
    onDismissRequest: (Boolean) -> Unit,
    dateDelaySelected:() -> Unit,
    allViewModel: AllViewModel){

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties()
    ){
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(20.dp))
                .background(Color.White)
                .fillMaxHeight(0.8f)
                .fillMaxWidth(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {

                    CardIconButton(
                        imageVector = Icons.Filled.Close,
                        onClicked = { onDismissRequest.invoke(false)},
                        modifier = Modifier,
                        buttonColor = Color.White
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 30.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(Modifier
                            .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center) {
                            Text(fontSize = 25.sp,text = "개발 노트")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp),
                            horizontalAlignment = Alignment.Start) {
                            Text(fontSize = 20.sp, text = "업데이트 내용")

                            Text(fontSize = 15.sp,
                                lineHeight = 25.sp,
                                text = "1. 환율 변경에 따른 예상 수익 추가\n" +
                                        "2. 버튼 ui 변경\n" +
                                        "3. 평균 1시간 환율 업데이트\n" +
                                        "  - 새로고침 1~2분 전 환율 데이터 제공\n" +
                                        "  - 하루 3회 무료 새로고침 제공\n" +
                                        "  - 광고 시청 후 기회 누적 가능\n" +
                                        "  - 원화 오차 범위 수정\n" +
                                        "4. 달러, 엔화, 원화 이동은 버튼 클릭 시 변경")
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp),
                            horizontalAlignment = Alignment.Start) {
                            Text(fontSize = 20.sp, text = "업데이트 계획")

                            Text(fontSize = 15.sp,
                                lineHeight = 25.sp,
                                text = "1. 목표 환율 도달 알람 메시지 추가 예정\n" +
                                        "2. 스프레드 설정 추가 예정\n" +
                                        "3. 기록 수정 추가 예정\n")
                        }








                    }

                }

            }
            Row() {
                BasicCheckBox {
                    onDismissRequest(false)
                    dateDelaySelected.invoke()
                }
            }
        }
    }
}


@Composable
fun BasicCheckBox(clicked:()-> Unit){
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Spacer(modifier = Modifier.width(1.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_unchecked) ,
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clickable {
                    clicked.invoke()
                })

        Spacer(modifier = Modifier.width(5.dp))

        Text(text = "일주일 동안 보지 않기")
    }

}