package com.example.presentation.base.ui.logs

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.material.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.presentation.base.ui.theme.BluetoothTaskTheme
import java.text.SimpleDateFormat
import java.util.*
import com.example.presentation.R

@Composable
fun Logs(
    modifier: Modifier = Modifier,
    logs: List<String>
) {
    val listState = rememberLazyListState()
    Column(
        modifier = modifier
            .statusBarsPadding()
            .background(MaterialTheme.colors.background)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(4.dp)
        ) {
            items(
                items = logs,
                itemContent = { log ->
                    LogItem(
                        log = log
                    )
                }
            )
        }
    }
}

@Composable
private fun LogItem(
    modifier: Modifier = Modifier,
    log: String
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        color = MaterialTheme.colors.onBackground,
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier.padding(8.dp)
        ) {
            val (content) = createRefs()
            val strTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            Log.d("appendLog", log)
            Text(
                modifier = Modifier
                    .constrainAs(content) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                    }
                    .padding(start = 12.dp, top = 4.dp),
                text = stringResource(id = R.string.text_default_logs_value)+ "\n$strTime $log",
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@Composable
@Preview(name = "RadioPoster Light")
private fun RadioPosterPreviewLight() {
    BluetoothTaskTheme(darkTheme = false) {
        LogItem(
            log = "test : log"
        )
    }
}

@Composable
@Preview(name = "RadioPoster Dark")
private fun RadioPosterPreviewDark() {
    BluetoothTaskTheme(darkTheme = true) {
        LogItem(
            log = "test : log"
        )
    }
}
