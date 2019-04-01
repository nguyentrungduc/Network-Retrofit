package com.example.retrofit


import com.past3.ketro.api.ApiErrorHandler
import com.past3.ketro.api.GenericRequestHandler
import retrofit2.Call

class GitHubRequest(private val name: String) : GenericRequestHandler<Response>(){
    private val gitHubAPI: GitHubAPI by lazy {
        NetworkModule.createRetrofit().create(GitHubAPI::class.java)
    }

    override val errorHandler: ApiErrorHandler = GitHubErrorHandler()

    override fun makeRequest(): Call<Response> {
        return gitHubAPI.searchUser(name)
    }
}