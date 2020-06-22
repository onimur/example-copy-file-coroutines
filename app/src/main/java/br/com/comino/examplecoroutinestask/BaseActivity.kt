package br.com.comino.examplecoroutinestask

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.measureTimeMillis

abstract class BaseActivity : AppCompatActivity(R.layout.activity_main) {
    val listUri = mutableListOf<Uri>()
    val myAdapter = MyAdapter(mutableListOf())

    companion object {
        const val TAG = "My Example"
    }

    suspend fun measureTimeSeconds(function: suspend () -> Any) {
        val time = measureTimeMillis {
            function.invoke()
        }
        Log.d(TAG, "measureTimeSeconds: ${time / 1000} seconds")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = "android.resource://${packageName}/raw/"
        for (i in 1..40) {
            listUri.add(Uri.parse("${packageName}file$i"))
        }

        //Init recyclerview
        recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            println(myAdapter)
            adapter = myAdapter
        }
    }
}