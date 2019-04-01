package com.example.retrofit

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("https://api.github.com/")
            .build()

        val gitHubService: GitHubService = retrofit.create(GitHubService::class.java)

        gitHubService.getGitHubService("ducnt")
            .subscribeOn(Schedulers.newThread()) //Schedulers.io()
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                Log.d("MainActivity", it.avatar_url)
            }

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://publicobject.com/helloworld.txt")
            .build()

        client.newCall(request).enqueue(object:  Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("hi", response.toString())
            }

        })


    }
}
