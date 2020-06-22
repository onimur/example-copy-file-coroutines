package br.com.comino.examplecoroutinestask

import android.net.Uri
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream

/**
 * You can see more details here:
 * @see [KotlinDiscussion](https://discuss.kotlinlang.org/t/read-and-copy-file-with-coroutines/18145/5?u=murillocomino)
 *
 */
class NickallendevReplyActivity : BaseActivity() {
    private val mainScope = MainScope()
    private var job: Job? = null

    @FlowPreview
    fun onClickStartTask(view: View) {
        val listNewPath = mutableListOf<String>()
        progressBar.visibility = VISIBLE

        job = mainScope.launch {
            measureTimeSeconds {
                try {
                    listUri
                        .asFlow()
                        .flatMapMerge(4) {
                            flow { emit(processUri(it)) }
                                .flowOn(IO)
                        }.collect { handleResult(it, listNewPath) }

                } catch (e: Exception) {
                    Log.e(TAG, "onClickStartTask: ${e.message}")
                } finally {
                    Log.d(TAG, "MainScope isActive: ${mainScope.isActive}")
                    Log.d(
                        TAG, "Job isActive: ${job?.isActive}" +
                                "\nisCompleted: ${job?.isCompleted}" +
                                "\nisCancelled: ${job?.isCancelled}"
                    )
                    if (mainScope.isActive) {
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

    private fun processUri(uri: Uri): String {
        Log.d(TAG, "Init async for file")
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
                        job?.ensureActive()
                        runBlocking { delay(20) }
                        output.write(buffer, 0, read)
                        read = input.read(buffer)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "${e.message} - ${file.name} deleted")
            file.deleteRecursively()
            throw e
        }
        //If completed then it returns the new path.
        Log.d(TAG, "The ${file.name} is Complete")
        return pathFileTemp
    }

    private fun handleResult(result: String, listNewPath: MutableList<String>) {
        listNewPath.add(result)
    }

    fun onClickCancelTask(view: View) {
        job?.cancel()
        Log.d(TAG, "Cancel the Job")
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}