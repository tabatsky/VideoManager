package jatx.video.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

@Composable
fun VideoContentsDialog() {
    val onDismiss = { Injector.viewModel.isVideoContentsDialogVisible = false }

    if (Injector.viewModel.isVideoContentsDialogVisible) {
        var videoName by remember { mutableStateOf(Injector.viewModel.currentVideo?.videoName ?: "") }
        var comment by remember { mutableStateOf(Injector.viewModel.currentVideo?.comment ?: "") }

        val dialogState = rememberDialogState(width = 400.dp, height = 550.dp)

        DialogWindow(
            onCloseRequest = onDismiss,
            state = dialogState
        ) {
            val showConfirmationState = remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Файл: ${Injector.viewModel.currentVideo?.file?.absolutePath}",
                    modifier = Modifier
                        .padding(12.dp)
                )
                TextField(
                    value = videoName,
                    onValueChange = { videoName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                TextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                val lastModified = Injector.viewModel.currentVideo?.lastModifiedFormatted ?: "неизвестно"
                Text(
                    text = "Изменено: $lastModified",
                    modifier = Modifier
                        .padding(12.dp)
                )
                val recordedFromEntry = Injector.viewModel.currentVideo?.recordedFormatted ?: "неизвестно"
                var recordedTextFieldValue by remember { mutableStateOf(recordedFromEntry) }
                var isParseError by remember { mutableStateOf(false) }
                val textFieldBgColor = if (isParseError) Color.Red else Color.White
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Записано: ",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                    TextField(
                        value = recordedTextFieldValue,
                        onValueChange = { recordedTextFieldValue = it },
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .background(textFieldBgColor)
                    )
                }
                val duration = Injector.viewModel.currentVideo
                    ?.duration?.formatDuration(true) ?: "неизвестно"
                Text(
                    text = "Длительность: $duration",
                    modifier = Modifier
                        .padding(12.dp)
                )
                Row {
                    Button(
                        onClick = {
                            try {
                                val newRecordedDate = recordedTextFieldValue.tryParseDate()
                                Injector.viewModel.updateCurrentVideoEntry(
                                    newRecordedDate = newRecordedDate,
                                    newVideoName = videoName,
                                    newComment = comment
                                )
                                onDismiss()
                            } catch (e: Exception) {
                                isParseError = true
                            }
                        }, modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                    ) {
                        Text("Сохранить",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }

                    val deleteOrRestoreLabel = if (Injector.viewModel.currentVideo?.deleted == true) {
                        "Восстановить"
                    } else {
                        "Удалить"
                    }

                    Button(onClick = {
                        showConfirmationState.value = true
                    }, modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)
                    ) {
                        Text(deleteOrRestoreLabel)
                    }
                }
            }

            DeleteVideoDialogWrapper(showConfirmationState)
        }
    }
}

@Composable
private fun DeleteVideoDialogWrapper(showConfirmationState: MutableState<Boolean>) {
    var showConfirmation by showConfirmationState

    if (showConfirmation) {
        val willBedeletedOrRestoredLabel = if (Injector.viewModel.currentVideo?.deleted == true) {
            "Видео будет восстановлено"
        } else {
            "Видео будет удалено в корзину"
        }

        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
            },
            title = {
                Text(text = "Вы уверены?")
            },
            text = {
                Text(text = willBedeletedOrRestoredLabel)
            },
            confirmButton = {
                Button(onClick = {
                    Injector.viewModel.toggleCurrentVideoDeleted()
                    showConfirmation = false
                }) {
                    Text(text = "Да")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showConfirmation = false
                }) {
                    Text(text = "Нет")
                }
            }
        )
    }
}