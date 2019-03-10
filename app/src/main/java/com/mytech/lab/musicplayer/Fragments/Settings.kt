package com.mytech.lab.musicplayer.Fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.*
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.utils.AccelerometerManager


/**
 * Created by lnx on 15/3/18.
 */
class Settings : Fragment() {


    lateinit var cntx:Context;
    lateinit var force_right:TextView
    lateinit var force_left:TextView
    lateinit var force_head: LinearLayout
    lateinit var action_right:TextView
    lateinit var action_left:TextView
    lateinit var action_head:LinearLayout
    lateinit var shake_stats:Switch
    lateinit var shake_status_head:LinearLayout

    lateinit var noaction:RadioButton
    lateinit var dialog:AlertDialog.Builder
    lateinit var rescan_head:LinearLayout





    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.cntx = context!!

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu!!.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)

        force_right = v.findViewById(R.id.force_right_text)
        force_left = v.findViewById(R.id.force_left_text)

        force_head = v.findViewById(R.id.force_head)

        action_right = v.findViewById(R.id.action_right_text)
        action_left = v.findViewById(R.id.action_left_text)
        action_head = v.findViewById(R.id.action_head)

        shake_stats = v.findViewById(R.id.shake_status_switch)
        shake_status_head = v.findViewById(R.id.shake_status_head)
        rescan_head = v.findViewById(R.id.rescan_head)


        if(Home.shakePreferences.getBoolean("shake_stats",false))
        {
            shake_stats.setChecked(true)

        }
        else
        {
             changecolor("lightgrey")
             setbuttonclickable(false)

        }

        shake_stats.setOnCheckedChangeListener { compoundButton, b ->

            if(b)
            {


                changecolor("black")
                setbuttonclickable(true)

            }
            else
            {



                changecolor("lightgrey")
                setbuttonclickable(false)


            }


            Home.shakePreferences.edit().putBoolean("shake_stats",b).apply()

        }
        return v;
    }

    fun setbuttonclickable(res:Boolean)
    {
        force_head.isClickable = res
        action_head.isClickable = res
    }

    fun changecolor(res:String)
    {
        var num:Int = 0
        if(res.equals("lightgrey"))
        {
            num = R.color.LightGray
        }
        else
        {
            num = R.color.lightblack1
        }
        action_right.setTextColor(activity!!.resources.getColor(num))
        force_right.setTextColor(activity!!.resources.getColor(num))
        action_left.setTextColor(activity!!.resources.getColor(num))
        force_left.setTextColor(activity!!.resources.getColor(num))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var dp = layoutInflater.inflate(R.layout.dialog_shake_action, null)
        dialog = AlertDialog.Builder(cntx)
        dialog.setView(dp)
        val sub_dialog = dialog.create()
        val cancel:TextView = dp.findViewById(R.id.cancel)
        val radiogroup:RadioGroup = dp.findViewById(R.id.radioGroup)
        val okey:TextView = dp.findViewById(R.id.ok)
        var radiobutton:RadioButton



        if(Home.shakePreferences.getString("action_id",null)==null)
        {
            noaction = dp.findViewById(R.id.radioButton_no_action);
            Home.shakePreferences.edit().putString("action_text","No Action").apply()
            noaction.setChecked(true);
            action_right.setText("None")
        }
        else
        {

            action_right.setText(Home.shakePreferences.getString("action_text","Ad"))

        }

        if(Home.shakePreferences.getInt("force_count",-1)!=-1)
        {
            force_right.setText(Home.shakePreferences.getInt("force_count",-1).toString())
        }




        action_head.setOnClickListener {

            if(Home.shakePreferences.getString("action_id",null)!=null)
            {

                var id:String = Home.shakePreferences.getString("action_id",null)

                radiobutton = dp.findViewById(id.toInt())
                radiobutton.setChecked(true)
            }

            okey.setOnClickListener {

                val radioId:Int = radiogroup.checkedRadioButtonId
                radiobutton = dp.findViewById(radioId)
                Home.shakePreferences.edit().putString("action_id",radioId.toString()).apply()
                Home.shakePreferences.edit().putString("action_text",radiobutton.text.toString()).apply()
                action_right.setText(radiobutton.text.toString())
                sub_dialog.dismiss()

            }

            cancel.setOnClickListener {
                sub_dialog.dismiss()
            }

            sub_dialog.show()

        }

        force_head.setOnClickListener {
            var fh = layoutInflater.inflate(R.layout.dialog_seekbar_shake_force,null)
            dialog = AlertDialog.Builder(cntx)
            dialog.setView(fh)
            val sub_dialog = dialog.create()
            val seek:SeekBar = fh.findViewById(R.id.force_seekbar)
            val count:TextView = fh.findViewById(R.id.force_count)
            val seek_okey:TextView = fh.findViewById(R.id.dialog_seek_okey)
            sub_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
            sub_dialog.show()

            seek.max = 100

            if(Home.shakePreferences.getInt("force_count",-1)!=-1)
            {
                seek.progress = Home.shakePreferences.getInt("force_count",0)
                count.setText(seek.progress.toString())
            }
            else
            {
                seek.progress = 30
            }


            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

                    count.setText(i.toString())

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })

            seek_okey.setOnClickListener {
                Home.shakePreferences.edit().putInt("force_count",seek.progress).apply()


                if(seek.progress<15)
                {
                    var builder = AlertDialog.Builder(context!!)
                    var dialog = AlertDialog.Builder(context!!).create();
                    dialog.setMessage("Below 15 is too low it may hang your device Click Default to set default else ok")
                    dialog.setTitle("Confirm")
                    dialog.setCancelable(true)
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Default") { dialogInterface, i ->

                        force_right.setText("15")
                        AccelerometerManager.configure(15,1000)
                    }

                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"Ok") { dialogInterface, i ->

                        force_right.setText(seek.progress.toString())
                        AccelerometerManager.configure(seek.progress.toString().toInt(),1000)

                    }

                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
                    dialog.show()

                }
                else
                {
                    force_right.setText(seek.progress.toString())
                    AccelerometerManager.configure(seek.progress.toString().toInt(),1000)
                }

                sub_dialog.dismiss()
            }



        }

        rescan_head.setOnClickListener {

             var ob = Refresh()
             ob.execute(cntx)
            Controls.createToast(cntx,"Library Refreshing...",Toast.LENGTH_SHORT)
        }




}

    companion object {

        class Refresh : AsyncTask<Context, Void, Boolean>() {
            override fun doInBackground(vararg param: Context): Boolean? {
                try {
                    Home.all_songs.clear()
                    Home.fetchallsong(param[0])
                } catch (e: Exception) {

                }

                return true
            }

            override fun onPostExecute(aBoolean: Boolean?) {
                super.onPostExecute(aBoolean)
                Home.getalbums()
                Home.getartist()
                Home.mini_track()

            }
        }

    }


    override fun onResume() {
        super.onResume()
//        Home.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//        Home.toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//        Home.toggle.isDrawerIndicatorEnabled = false
        (activity as Home).setActionBarTitle("Settings")
    }




}