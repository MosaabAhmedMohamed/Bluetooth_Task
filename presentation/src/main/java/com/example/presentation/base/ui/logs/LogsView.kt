package com.example.presentation.base.ui.logs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.presentation.R

@Composable
fun LogsView(
    modifier: Modifier = Modifier,
    logs: List<String>,
    onClearLogClicked: () -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    ) {
        ConstraintLayout {
            val (logsTv, clearLogsBtn, logsList) = createRefs()

            Text(
                text = stringResource(R.string.text_default_logs_value),
                style = MaterialTheme.typography.body2,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .constrainAs(logsTv) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .padding(start = 16.dp, top = 12.dp)
            )

            Button(onClick = {
                onClearLogClicked()
            }, elevation = ButtonDefaults.elevation(
                defaultElevation = 10.dp,
                pressedElevation = 15.dp,
                disabledElevation = 0.dp
            ), colors = ButtonDefaults.buttonColors(
                contentColor = Color.White
            ), modifier = Modifier
                .constrainAs(clearLogsBtn) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
                .padding(end = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.button_clear_log))
            }

            Logs(modifier = Modifier
                .constrainAs(logsList) {
                    top.linkTo(logsTv.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(16.dp),
                logs = logs)
        }
    }
}

