package me.antonio.noack.productivity.tasks

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.antonio.noack.productivity.ColorBarView
import me.antonio.noack.productivity.R

object EntryDisplayer {

    fun resetColorCounts(){
        colorCounts.clear()
        colorCountSum = 0
    }

    val colorCounts = HashMap<Int, Int>(8)
    var colorCountSum = 0
    fun incColorCount(color: Int){
        colorCounts[color] = colorCounts[color]?.plus(1) ?: 1
        colorCountSum ++
    }

    fun displayColorCounts(colorBarView: ColorBarView){
        colorBarView.invalidate()
    }

    fun display(inflater: LayoutInflater, list: ViewGroup, res: Resources, entry: Entry){

        // get those views
        val view = inflater.inflate(R.layout.result, list, false)
        val bg = view.findViewById<View>(R.id.background)
        val title = view.findViewById<TextView>(R.id.title)
        val flagView = view.findViewById<TextView>(R.id.flags)
        val detailView = view.findViewById<TextView>(R.id.details)
        val classView = view.findViewById<TextView>(R.id.clazz)

        val flags = entry.taskFlags

        // find the color
        val color = when {
            // flags.contains("deleted") -> res.getColor(R.color.deleted)
            flags.contains("fixing") || flags.contains("fix") -> res.getColor(R.color.fixing)
            flags.contains("todo") -> res.getColor(R.color.todo)
            flags.contains("done") -> res.getColor(R.color.done)
            flags.contains("doing") -> res.getColor(R.color.doing)
            else -> res.getColor(R.color.unknown)
        }

        incColorCount(color)

        bg.setBackgroundColor(color or 0xff000000.toInt())

        // done onclick open window/box with more details, e.g. the page name
        // - or do that on swipe? :) or just on click? would be nice, too :)

        list.addView(view)

        val text = entry.taskText

        val splitIndex = text.indexOf('\n')
        val headline = if(splitIndex > 0) text.substring(0, splitIndex) else text
        val info = if(splitIndex > 0) text.substring(splitIndex+1).trim() else ""

        title.text = headline
        detailView.text = info
        classView.text = entry.path
            .replace("java/me/antonio/noack/", "")
            .replace("me/antonio/noack/", "")
            .replace("/${FileHistory.projectName}/", "/")
            .replace("/${FileHistory.projectName.toLowerCase()}/", "/")
            .replace('/', '.')
        flagView.text = flags.joinToString(", "){ "#$it" }

        var hasDetails = false

        fun openDetails(){
            hasDetails = true
            if(info.isNotEmpty()) detailView.visibility = View.VISIBLE
            classView.visibility = View.VISIBLE
        }

        fun closeDetails(){
            hasDetails = false
            classView.visibility = View.GONE
            detailView.visibility = View.GONE
        }

        fun toggle(){
            if(hasDetails) closeDetails()
            else openDetails()
        }

        bg.setOnClickListener { toggle() }
        title.setOnClickListener { toggle() }
        flagView.setOnClickListener { toggle() }

        closeDetails()

    }

}