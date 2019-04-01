package com.example.retrofit

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {

    @GET("users/{username}")
    fun getGitHubService(@Path("username") username: String): Observable<GitHubUser>

}