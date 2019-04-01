package com.example.retrofit

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.past3.ketro.model.Wrapper

class MainViewModel: ViewModel(){

    val list = mutableListOf<GitHubUser>()

    private val liveData = MutableLiveData<Wrapper<Response>>()

    fun searchUser(name:String): LiveData<Wrapper<Response>> {
        return GitHubRequest(name).doRequest()
    }

    fun searchUserB(name:String) {
        return GitHubRequest(name).executeRequest(liveData)
    }
}