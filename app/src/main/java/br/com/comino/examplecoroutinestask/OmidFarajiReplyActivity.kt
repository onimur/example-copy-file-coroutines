package br.com.comino.examplecoroutinestask

import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.File

/**
 * You can see more details here:
 * @see [StackOverflowEn](https://stackoverflow.com/a/62508951/10526030)
 *
 */
class OmidFarajiReplyActivity : BaseActivity() {
    private val job = Job()

    fun onClickStartTask(view: View) {
        var listNewPath = emptyList<String>()
        val copiedFiles = mutableListOf<File>()

        CoroutineScope(Main + job).launch {
            measureTimeSeconds {
                try {
                    //shows something in the UI
                    progressBar.visibility = VISIBLE
                    withContext(IO) {
                        listNewPath = listUri.mapIndexed { index, uri ->
                            async {
                                Log.d(TAG, "Init async for file${index + 1}")
                                //path to file temp
                                val pathFileTemp =
                                    "${getExternalFilesDir("Temp").toString()}/${uri.lastPathSegment}"
                                val file = File(pathFileTemp)
                                val inputStream = contentResolver.openInputStream(uri)
                                inputStream?.use { input ->
                                    file.outputStream().use { output ->
                                        var bytesCopied: Long = 0
                                        val buffer = ByteArray(1024)
                                        var bytes = input.read(buffer)
                                        while (bytes >= 0) {
                                            delay(200)
                                            output.write(buffer, 0, bytes)
                                            bytesCopied += bytes
                                            bytes = input.read(buffer)
                                        }
                                    }
                                }
                                copiedFiles.add(file)
                                //If completed then it returns the new path.
                                Log.d(TAG, "The ${file.name} is Complete")
                                return@async pathFileTemp
                            }
                        }.awaitAll()
                    }
                } finally {
                    //shows something in the UI
                    progressBar.visibility = GONE
                    //update adapter
                    myAdapter.updateListChanged(listNewPath)
                }
            }
        }.invokeOnCompletion {
            it?.takeIf { it is CancellationException }?.let {
                GlobalScope.launch(IO) {
                    copiedFiles.forEach { file ->
                        Log.e(TAG, "onClickStartTask: ${it.message} - ${file.name} deleted")
                        file.delete()
                    }
                }
            }
        }
    }

    fun onClickCancelTask(view: View) {
        if (job.isActive) {
            job.cancelChildren()
            Log.d(TAG, "Cancel children")
        }
    }
}