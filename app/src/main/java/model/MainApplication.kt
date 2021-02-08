package model

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.magicmirror.R

class MainApplication : Application() {

    val connectionUrlMain = "http://sandyz.ink:8080/"

    companion object{
        lateinit var app: Application
        lateinit var context: Context
    }


    override fun onCreate() {
        super.onCreate()
        initTypeface()
        app = this
        context = applicationContext
    }

    private fun initTypeface(){
        val typeface = ResourcesCompat.getFont(this, R.font.alibaba_medium)
        val field = Typeface::class.java.getDeclaredField("MONOSPACE")
        field.isAccessible = true
        field.set(null,typeface)
    }

}