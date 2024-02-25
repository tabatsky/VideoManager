package jatx.video.manager

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow

@Composable
fun AddFolderDialog(
    onChooseFolder: () -> Unit
) {
    val onDismiss = { Injector.viewModel.isAddFolderDialogVisible = false }

    if (Injector.viewModel.isAddFolderDialogVisible) {
        DialogWindow(onCloseRequest = { onDismiss() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = Injector.viewModel.playlistName,
                    onValueChange = { Injector.viewModel.playlistName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Row {
                    Text(
                        text = Injector.viewModel.folderPath,
                        modifier = Modifier
                            .weight(4f)
                    )
                    Button(
                        onClick = {
                            onChooseFolder()
                        }, modifier = Modifier
                            .weight(1f)
                    ) {
                        Text("...",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
                Row {
                    Text(
                        "Найдено видео в папке: ${Injector.viewModel.folderVideoCount}",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            Injector.viewModel.addCurrentFolderContents()
                            onDismiss()
                        }, modifier = Modifier
                            .width(180.dp)
                    ) {
                        Text("Добавить",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }
                    Button(
                        onClick = {
                            onDismiss()
                        }, modifier = Modifier
                            .width(180.dp)
                    ) {
                        Text("Отмена",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}
