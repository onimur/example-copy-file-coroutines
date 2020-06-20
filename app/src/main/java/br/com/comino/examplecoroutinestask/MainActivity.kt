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
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private val listUri = mutableListOf<Uri>()
    private val job = Job()
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
        CoroutineScope(Main + job).launch {
            try {
                listUri.asFlow()
                    .flatMapMerge(4) {
                        flow { emit(processUri(it)) }
                    }.collect { handleResult(it, listNewPath) }

            } catch (e: CancellationException) {
                println(e.message)
            } finally {
                myAdapter.updateListChanged(listNewPath)
                progressBar.visibility = GONE
            }
        }
    }

    private suspend fun processUri(uri: Uri): String = withContext(IO) {
        println("Init async for file")
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
                    try {
                        //check the job is active
                        yield()
                        delay(20)
                        output.write(buffer, 0, read)
                        read = input.read(buffer)
                    } catch (e: CancellationException) {
                        println("task is canceled and ${file.name} deleted")
                        file.deleteRecursively()
                    }
                }
            }
        }
        //If completed then it returns the new path.
        println("The ${file.name} is Complete")
        return@withContext pathFileTemp
    }

    private fun handleResult(result: String, listNewPath: MutableList<String>) {
        listNewPath.add(result)
    }

    fun onClickCancelTask(view: View) {
        if (job.isActive) {
            job.cancelChildren()
            println("Cancel children")
        }
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