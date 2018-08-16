package com.mytech.lab.musicplayer.Activity

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.*
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.R

class Wel : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        var txt:TextView = findViewById(R.id.welcome)
        Constants.current_directory_array.clear()
        if(Build.VERSION.SDK_INT<=23)
        {
            Constants.current_directory_array.add("/storage")
        }
        else
        {
            Constants.current_directory_array.add(Environment.getExternalStorageDirectory().getAbsolutePath())
        }

        colorshared = getSharedPreferences("colorshared", MODE_PRIVATE)
        txt.startAnimation(AnimationUtils.loadAnimation(this, R.anim.textview_anim_right_to_center));
        checkpermission()

    }


    inner class Fetch : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg voids: Void): Boolean? {
            try {
                Home.all_songs.clear()
                Home.fetchallsong(applicationContext)
            } catch (e: Exception) {

            }

            return true
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)
//            Home.setplayercard(applicationContext)
            Home.getalbums()
            Home.getartist()
            Home.mini_track()
            Handler().postDelayed({
                var i = Intent(this@Wel, Home::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                finish()
            }, 500)

        }
    }


    private fun checkpermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
            }
            else
            {
                val tsk = Fetch()
                tsk.execute()

            }
        }
        else {
            val tsk = Fetch()
            tsk.execute()

        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {


        when (requestCode) {
            123 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val tsk = Fetch()
                tsk.execute()

            }
            else
            {
                Toast.makeText(applicationContext, "Permission Denied, You should allow permission", Toast.LENGTH_SHORT).show()
                finish()
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    companion object {
        lateinit var colorshared:SharedPreferences;
    }

}












