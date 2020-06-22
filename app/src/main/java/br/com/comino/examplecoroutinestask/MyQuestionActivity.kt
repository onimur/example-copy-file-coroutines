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
import java.io.FileOutputStream

/**
 *
 * You can check my question at:
 * @see [KotlinDiscussion](https://discuss.kotlinlang.org/t/read-and-copy-file-with-coroutines/18145?u=murillocomino)
 * @see [StackOverFlowEn](https://stackoverflow.com/q/62486876/10526030)
 * @see [StackOverFlowPt](https://pt.stackoverflow.com/q/458209/128573)
 */
class MyQuestionActivity : BaseActivity() {
    private val job = Job()

    fun onClickStartTask(view: View) {
        var listNewPath = emptyList<String>()

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
                                    FileOutputStream(file).use { output ->
                                        val buffer = ByteArray(1024)
                                        var read: Int = input.read(buffer)
                                        while (read != -1) {
                                            if (isActive) {
                                                delay(20)
                                                output.write(buffer, 0, read)
                                                read = input.read(buffer)
                                            } else {
                                                Log.e(
                                                    TAG,
                                                    "task is canceled and ${file.name} deleted"
                                                )
                                                input.close()
                                                output.close()
                                                file.deleteRecursively()
                                                throw CancellationException()
                                            }
                                        }
                                    }
                                }
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
                    //
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