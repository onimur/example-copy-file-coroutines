package br.com.comino.examplecoroutinestask

import android.net.Uri
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream

/**
 * You can see more details here:
 * @see [StackOverflowEn](https://stackoverflow.com/a/62510195/10526030)
 *
 */
class CircusmagnusReplyActivity : BaseActivity() {

    private val job = Job()

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun <T, R> Flow<T>.concurrentMap(concurrency: Int, transform: suspend (T) -> R): Flow<R> {
        require(concurrency > 1) { "No sense with concurrency < 2" }
        return channelFlow {
            val inputChannel = produceIn(this)
            repeat(concurrency) {
                launch {
                    for (input in inputChannel) send(transform(input))
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun onClickStartTask(view: View) {
        val listNewPath = mutableListOf<String>()
        progressBar.visibility = VISIBLE
        CoroutineScope(Main + job).launch {
            measureTimeSeconds {
                try {
                    listUri.asFlow()
                        .concurrentMap(concurrency = 4) {
                            processUri(it, this)
                        }.flowOn(IO)
                        .collect { handleResult(it, listNewPath) }
                } catch (e: Exception) {
                    Log.e(TAG, "onClickStartTask: ${e.message}")
                } finally {
                    Log.d(
                        TAG, "Scope isActive: ${this.isActive}" +
                                "\nJob isActive: ${job.isActive}" +
                                "\nisCompleted: ${job.isCompleted}" +
                                "\nisCancelled: ${job.isCancelled}"
                    )
                    if (job.isActive) {
                        //update UI
                        myAdapter.updateListChanged(listNewPath)
                        progressBar.visibility = GONE
                    } else {
                        //view is destroyed
                    }
                }
            }
        }
    }

    private fun processUri(uri: Uri, coroutineScope: CoroutineScope): String {
        Log.d(TAG, "processUri: Init async for file")
        //path to file temp
        val pathFileTemp =
            "${getExternalFilesDir("Temp").toString()}/${uri.lastPathSegment}"
        val file = File(pathFileTemp)
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(1024)
                    var read: Int = input.read(buffer)
                    while (read != -1) {
                        //check the job is active
                        coroutineScope.ensureActive()
                        runBlocking { delay(20) }
                        output.write(buffer, 0, read)
                        read = input.read(buffer)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "processUri: ${e.message} - ${file.name} deleted")
            file.deleteRecursively()
            throw e
        }
        //If completed then it returns the new path.
        Log.d(TAG, "processUri: The ${file.name} is Complete")
        return pathFileTemp
    }

    private fun handleResult(result: String, listNewPath: MutableList<String>) {
        listNewPath.add(result)
    }

    fun onClickCancelTask(view: View) {
        if (job.isActive) {
            job.cancelChildren()
            Log.d(TAG, "onClickCancelTask: Cancel children")
        }
    }
}