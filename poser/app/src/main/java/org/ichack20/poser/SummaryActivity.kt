package org.ichack20.poser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.edvard.poseestimation.R
import java.util.*

class SummaryActivity : Activity(){

    val errors : MutableList<String> = LinkedList<String>()
    var num_errors = 0
    var reps = 0

    val errorViews : MutableList<TextView> = LinkedList()
    private var repsView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.thank_you_page)

        if (null == savedInstanceState){
            reps = intent.extras.getInt("reps")
            num_errors = intent.extras.getInt("num_errors")

            for (i in 0 until num_errors){
                errors.add(intent.extras.getString("error$i"))
            }
        }

        errorViews.add(findViewById(R.id.error1))
        errorViews.add(findViewById(R.id.error2))
        errorViews.add(findViewById(R.id.error3))

        repsView = findViewById(R.id.reps_text)

        populate()
    }

    private fun populate(){
        for(i in 0 until  num_errors){
            errorViews[i].text = "* ${errors[i]}"
        }

        repsView!!.text = "Total Reps: $reps.toString()"
    }

    fun Save() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
