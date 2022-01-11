package com.crystal.airbnb

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener{

    private val mapView: MapView by lazy {
        findViewById(R.id.mapView)
    }
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private val viewpager: ViewPager2 by lazy {
        findViewById(R.id.houseViewpager)
    }
    private val viewPagerAdapter = HouseViewPagerAdapter(itemClicked =  {
        //외부로 공유하기
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "[지금 이 가격에 예약하세요!!] ${it.title}, ${it.price} 사진 보기 : ${it.imgUrl}")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))

    })
    private val houseRecyclerView: RecyclerView by lazy{
        findViewById(R.id.BottomSheetHouseRecyclerView)
    }
    private val houseRecyclerAdapter = HouseRecyclerViewAdapter()
    private val currentLocationButton: LocationButtonView by lazy {
        findViewById(R.id.currentLocationButton)
    }
    private val bottomSheetTitleTextView: TextView by lazy {
        findViewById(R.id.roomListTextView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        viewpager.adapter = viewPagerAdapter

        houseRecyclerView.adapter = houseRecyclerAdapter
        houseRecyclerView.layoutManager = LinearLayoutManager(this)

        viewpager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val selectedHouseModel = viewPagerAdapter.currentList[position]
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat, selectedHouseModel.lng))
                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map
        map.maxZoom = 18.0
        map.minZoom = 10.0

        //지도 위치 설청
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.4977876, 127.0271162))
        map.moveCamera(cameraUpdate)

        //버튼 클릭 엑션을 하기위해선 권한 필요
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        map.locationSource = locationSource

        //현위치 버튼 활성화
//        map.uiSettings.isLocationButtonEnabled = true

        currentLocationButton.map = naverMap //activity_main.xml에 있는 버튼으로 대체

        //맵이 모두 그려진 이후에 데이터를 가져오겠다...
        getHouseListFromAPI()
    }

    private fun getHouseListFromAPI() {
        val retrofit = Retrofit.Builder().baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HouseService::class.java).also {
            it.getHouseList()
                .enqueue(object : Callback<HouseDto> {
                    override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                        if (response.isSuccessful.not()) {
                            //실패 처리에 대한 구현
                            return
                        }

                        response.body()?.let { dto ->
                            markingHouseList(dto.items)
                            viewPagerAdapter.submitList(dto.items)
                            houseRecyclerAdapter.submitList(dto.items)
//                            houseRecyclerAdapter.notifyDataSetChanged()
                            bottomSheetTitleTextView.text = "${dto.items.size} 의 숙소"
                        }
                    }

                    override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                    }

                })
        }
    }

    private fun markingHouseList(houseList: List<HouseModel>) {
        houseList.forEach { house ->
            //marker 찍기
            val marker = Marker()
            marker.position = LatLng(
                house.lat,
                house.lng
            )
            marker.onClickListener = this
            marker.map = naverMap
            marker.tag = house.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onClick(overlay: Overlay): Boolean {

        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            it.id == overlay.tag
        }
        selectedModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewpager.currentItem = position
        }
        return true
    }

}