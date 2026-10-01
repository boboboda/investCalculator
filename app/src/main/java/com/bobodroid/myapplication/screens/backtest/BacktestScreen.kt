package com.bobodroid.myapplication.screens.backtest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bobodroid.myapplication.models.viewmodels.BacktestViewModel
import com.bobodroid.myapplication.routes.BacktestRoute
import com.bobodroid.myapplication.routes.RouteAction

/**
 * 환테크 백테스트 시뮬레이터 — MyPageScreen과 동일하게 자체 내부 NavHost를 가진 최상위 화면
 * 하나의 BacktestViewModel을 Input/Result/History 3개 화면이 공유 (hiltViewModel()이 이 컴포저블
 * 스코프에서 한 번만 생성되고, 자식 NavHost의 각 composable은 파라미터로 전달받음)
 */
@Composable
fun BacktestScreen(
    onBackClick: () -> Unit,
    viewModel: BacktestViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val routeAction = remember {
        // ⚠️ 수정: defaultRoute를 넘기지 않습니다 (null).
        // RouteAction.navTo는 defaultRoute가 있으면 매번 popUpTo(defaultRoute){saveState=true}를 걸어버리는데,
        // 이건 "하단 탭 전환"(메인/알람/분석/마이페이지)처럼 서로 다른 탭을 오가며 상태를 보존하는
        // 용도로 만들어진 패턴입니다. Input → Result → History는 탭 전환이 아니라 순서대로 쌓이는
        // 흐름이라, defaultRoute를 "BacktestInput"으로 주면 화면을 옮길 때마다 백스택이
        // BacktestInput까지 접혔다가 다시 쌓여서 goBack()(=navigateUp())이 "바로 이전 화면"으로
        // 정상적으로 못 돌아가는 문제가 있었습니다. defaultRoute = null이면 popUpTo 블록 자체가
        // 실행되지 않아 평범하게 쌓이는 네비게이션이 되고, 뒤로가기도 한 단계씩 정상 동작합니다.
        RouteAction<BacktestRoute>(navController, defaultRoute = null)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = BacktestRoute.Input.routeName ?: ""
        ) {
            composable(BacktestRoute.Input.routeName!!) {
                BacktestInputScreen(
                    viewModel = viewModel,
                    onBackClick = onBackClick,
                    onSubmit = {
                        routeAction.navTo(BacktestRoute.Result)
                    },
                    onNavigateToHistory = {
                        routeAction.navTo(BacktestRoute.History)
                    }
                )
            }

            composable(BacktestRoute.Result.routeName!!) {
                BacktestResultScreen(
                    viewModel = viewModel,
                    onBackClick = { routeAction.goBack() }
                )
            }

            composable(BacktestRoute.History.routeName!!) {
                BacktestHistoryScreen(
                    viewModel = viewModel,
                    onBackClick = { routeAction.goBack() },
                    onSelectHistory = { id ->
                        viewModel.loadHistoryDetail(id)
                        routeAction.navTo(BacktestRoute.Result)
                    }
                )
            }
        }
    }
}