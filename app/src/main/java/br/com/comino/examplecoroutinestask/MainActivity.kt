package br.com.comino.examplecoroutinestask

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private val listUri = mutableListOf<Uri>()
    private val mainScope = MainScope()
    private var job: Job? = null
    private val myAdapter = MyAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val packageName = "android.resource://${packageName}/raw/"
        for (i in 1..40) {
            listUri.add(Uri.parse("${packageName}file$i"))
        }

        //Init recyclerview
        recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
        }
    }

    @FlowPreview
    fun onClickStartTask(view: View) {
        val listNewPath = mutableListOf<String>()
        progressBar.visibility = VISIBLE
        job = mainScope.launch {
            try {
                listUri
                    .asFlow()
                    .flatMapMerge(4) {
                        flow { emit(processUri(it)) }
                            .flowOn(IO)
                    }.collect { handleResult(it, listNewPath) }

            } catch (e: Exception) {
                println(e.message)
            } finally {
                println("MainScope isActive: ${mainScope.isActive}")
                println(
                    "Job isActive: ${job?.isActive}" +
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

    private fun processUri(uri: Uri): String {
        println("Init async for file")
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
            println("${e.message} - ${file.name} deleted")
            file.deleteRecursively()
            throw e
        }
        //If completed then it returns the new path.
        println("The ${file.name} is Complete")
        return pathFileTemp
    }

    private fun handleResult(result: String, listNewPath: MutableList<String>) {
        listNewPath.add(result)
    }

    fun onClickCancelTask(view: View) {
        job?.cancel()
        println("Cancel the Job")
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }

    /*   //Old button action
        fun onClickStartTask(view: View) {
        var listNewPath = emptyList<String>()
        CoroutineScope(Main + job).launch {
            try {
                //
                //shows something in the UI
                progressBar.visibility = VISIBLE
                //
                withContext(IO) {
                    listNewPath = listUri.mapIndexed { index, uri ->
                        async {
                            println("Init async for file${index + 1}")
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
                                            output.write(buffer, 0, read)
                                            read = input.read(buffer)
                                        } else {
                                            println("task is canceled and ${file.name} deleted")
                                            input.close()
                                            output.close()
                                            file.deleteRecursively()
                                            throw CancellationException()
                                        }
                                    }
                                }
                            }
                            //If completed then it returns the new path.
                            println("The ${file.name} is Complete")
                            return@async pathFileTemp
                        }
                    }.awaitAll()
                }
            } finally {
                //
                //shows something in the UI
                progressBar.visibility = GONE
                //update adapter
                myAdapter.updateListChanged(listNewPath)
                //
            }
        }
    }*/
}