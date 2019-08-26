package me.antonio.noack.productivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import kotlinx.android.synthetic.main.all.*
import kotlinx.android.synthetic.main.main.*
import kotlinx.android.synthetic.main.welcome.*
import me.antonio.noack.productivity.tasks.FileHistory
import kotlin.concurrent.thread

class AllManager : AppCompatActivity() {

    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all)

        pref = getPreferences(Context.MODE_PRIVATE)

        if(Build.VERSION.SDK_INT >= 21){
            window.navigationBarColor = 0xff000000.toInt()
        }

        actionBar?.hide()

        val lastProject = pref.getString("lastProject", null)
        if(lastProject == null){
            // lastProjectButton.visibility = GONE
        } else {
            projectField.setText(lastProject.replace('\n', ' ').trim())
        }

        lastProjectButton.setOnClickListener {
            val projectName = projectField.text.toString()
            joinProject(projectName)
        }

        search.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                FileHistory.search = s.toString()
                thread {
                    FileHistory.updateList(this@AllManager)
                }
            }
        })

        back.setOnClickListener {
            flipper.displayedChild = 0
        }

        reload.setOnClickListener {
            FileHistory.lastRequestTime = 0L
            thread {
                FileHistory.updateList(this@AllManager)
            }
        }

    }

    fun joinProject(projectName: String){

        pref.edit().putString("lastProject", projectName).apply()

        flipper.displayedChild = 1

        FileHistory.projectName = projectName

        thread {
            FileHistory.updateList(this)
        }

    }

    override fun onResume() {
        super.onResume()

        FileHistory.lastRequestTime = 0L
        FileHistory.projectName = ""

        thread {
            FileHistory.updateList(this)
        }

    }
}
