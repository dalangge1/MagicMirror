package com.example.magicmirror

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
        val mquery = WeatherSearchQuery("北京", WeatherSearchQuery.WEATHER_TYPE_LIVE)
        val mweathersearch = WeatherSearch(activity)
        mweathersearch.setOnWeatherSearchListener(object : WeatherSearch.OnWeatherSearchListener{
            @SuppressLint("SetTextI18n")
            override fun onWeatherLiveSearched(weatherLiveResult: LocalWeatherLiveResult?, rCode: Int) {
                if (rCode == 1000) {
                    if (weatherLiveResult?.liveResult != null) {
                        val weatherlive = weatherLiveResult.liveResult
                        weather_report_time.text = weatherlive.reportTime + "发布"
                        weather_text.text = weatherlive.weather
                        weather_temperature.text = weatherlive.temperature + "°"
                        weather_wind.text = weatherlive.windDirection +"风     " + weatherlive.windPower + "级"
                        weather_humidity.text = "湿度         " + weatherlive.humidity + "%"
                    }else {
                        Toast.makeText(activity, "无天气查询结果", Toast.LENGTH_SHORT).show()
                    }
                }else {
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