package com.crystal.airbnb

import retrofit2.Call
import retrofit2.http.GET
//https://run.mocky.io/v3/c893e39f-e73d-452b-8d2d-7028ddd291bf
interface HouseService {
    @GET("/v3/c893e39f-e73d-452b-8d2d-7028ddd291bf")
    fun getHouseList(): Call<HouseDto>
}