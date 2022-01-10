package com.crystal.airbnb

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/24dd2337-4a17-423f-8e3c-92979266b9a2")//https://run.mocky.io/v3/24dd2337-4a17-423f-8e3c-92979266b9a2
    fun getHouseList(): Call<HouseDto>
}