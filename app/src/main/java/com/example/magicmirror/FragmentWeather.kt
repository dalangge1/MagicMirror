package com.example.magicmirror

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.navi.AMapNavi
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import kotlinx.android.synthetic.main.fragment_weather.*

class FragmentWeather : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //声明AMapLocationClient类对象
        //声明AMapLocationClient类对象
        val mLocationClient: AMapLocationClient?
        //声明定位回调监听器
        //声明定位回调监听器
        val mLocationListener = AMapLocationListener {
            playWeather(it.adCode)
            Log.e("sandyzhang", it.adCode)
        }
        //初始化定位
        //初始化定位
        mLocationClient = AMapLocationClient(activity!!)
        //设置定位回调监听
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener)
        //声明AMapLocationClientOption对象
        //声明AMapLocationClientOption对象
        val mLocationOption: AMapLocationClientOption?
        //初始化AMapLocationClientOption对象
        //初始化AMapLocationClientOption对象
        mLocationOption = AMapLocationClientOption()

        val option = AMapLocationClientOption()
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        option.locationPurpose = AMapLocationClientOption.AMapLocationPurpose.SignIn
        mLocationClient.setLocationOption(option)
        //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        mLocationClient.stopLocation()
        mLocationClient.startLocation()

        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true)

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true)

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption)
        //启动定位
        mLocationClient.startLocation()




    }
    fun playWeather(cityCode: String) {
        //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
        val mquery = WeatherSearchQuery(cityCode, WeatherSearchQuery.WEATHER_TYPE_LIVE)
        val mweathersearch = WeatherSearch(activity)
        mweathersearch.setOnWeatherSearchListener(object : WeatherSearch.OnWeatherSearchListener {
            @SuppressLint("SetTextI18n")
            override fun onWeatherLiveSearched(weatherLiveResult: LocalWeatherLiveResult?, rCode: Int) {
                if (rCode == 1000) {
                    if (weatherLiveResult?.liveResult != null) {
                        val weatherlive = weatherLiveResult.liveResult
                        weather_report_time.text = weatherlive.reportTime + "发布"
                        weather_text.text = weatherlive.city + "  " + weatherlive.weather
                        weather_temperature.text = weatherlive.temperature + "°"
                        weather_wind.text = weatherlive.windDirection + "风     " + weatherlive.windPower + "级"
                        weather_humidity.text = "湿度         " + weatherlive.humidity + "%"

                        val a = AMapNavi.getInstance(activity)
                        a.setUseInnerVoice(true, true)
                        val s = weatherlive.city + "的天气是: " + weatherlive.weather + "，温度是  " + weatherlive.temperature + "度" + "   ，目前" + weatherlive.windDirection + "风" + weatherlive.windPower + "级" + "，湿度         " + weatherlive.humidity + "%" + "   ," + weatherlive.province + weatherlive.reportTime + "发布"
                        Log.e("sandyzhang", a.playTTS(s, true).toString())

                    } else {
                        Toast.makeText(activity, "无天气查询结果", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "查询天气错误代码[$rCode]", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onWeatherForecastSearched(p0: LocalWeatherForecastResult?, p1: Int) {
                TODO("Not yet implemented")
            }
        })
        mweathersearch.setQuery(mquery)
        mweathersearch.searchWeatherAsyn() //异步搜索
    }

}