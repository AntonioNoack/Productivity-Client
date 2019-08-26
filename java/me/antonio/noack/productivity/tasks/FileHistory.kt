package me.antonio.noack.productivity.tasks

import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.main.*
import me.antonio.noack.productivity.AllManager
import me.antonio.noack.productivity.R
import me.antonio.noack.productivity.tasks.WebAPI.getText
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.math.abs

object FileHistory {

    var projectName = ""
    val allEntries = ConcurrentSkipListSet<Entry>()
    val matchingEntries = ConcurrentSkipListSet<Entry>()
    var search = ""

    const val char0 = 0.toChar()
    const val str0 = char0.toString()

    var lastProjectName = ""
    var lastRequestTime = 0L
    var lastSearch = "???"

    val millisToNanos = 1000L * 1000L

    fun updateSearch(changedContent: Boolean, all: AllManager){
        val search = search
        if(changedContent || lastSearch != search){
            lastSearch = search

            if(search.isBlank()){
                matchingEntries.clear()
                matchingEntries.addAll(allEntries.filter { !it.taskFlags.contains("deleted") })
            } else {
                val parts = search.split(' ').map { it.trim() }.filter { it.isNotEmpty() }
                val flags = parts.filter { it.startsWith("#") && it.length > 1 }.map { it.substring(1) }
                val nonFlags = parts.filter { !it.startsWith("#") }
                val deleted = flags.contains("deleted")
                matchingEntries.clear()
                entries@ for(entry in allEntries){
                    if(deleted || !entry.taskFlags.contains("deleted")){
                        for(flag in flags){
                            if(!entry.taskFlags.contains(flag)){
                                continue@entries
                            }
                        }
                        for(nonFlag in nonFlags){
                            if(!entry.path.contains(nonFlag) && !entry.taskText.contentEquals(nonFlag)){
                                continue@entries
                            }
                        }
                        matchingEntries.add(entry)
                    }
                }
            }

            all.runOnUiThread {

                val inflater = all.layoutInflater

                val list = all.todoList
                list.removeAllViews()

                for(entry in matchingEntries){

                    val view = inflater.inflate(R.layout.result, list, false)
                    val bg = view.findViewById<View>(R.id.background)
                    val title = view.findViewById<TextView>(R.id.title)
                    val flagView = view.findViewById<TextView>(R.id.flags)
                    val detailView = view.findViewById<TextView>(R.id.details)
                    val classView = view.findViewById<TextView>(R.id.clazz)

                    val flags = entry.taskFlags

                    val res = all.resources

                    val color = when {
                        // flags.contains("deleted") -> res.getColor(R.color.deleted)
                        flags.contains("fixing") || flags.contains("fix") -> res.getColor(R.color.fixing)
                        flags.contains("todo") -> res.getColor(R.color.todo)
                        flags.contains("done") -> res.getColor(R.color.done)
                        flags.contains("doing") -> res.getColor(R.color.doing)
                        else -> res.getColor(R.color.unknown)
                    }

                    bg.setBackgroundColor(color or 0xff000000.toInt())

                    println("${entry.taskText}: ${flagView.text}")

                    // todo onclick open window/box with more details, e.g. the page name
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
                        .replace("/$projectName/", "/")
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

        }
    }

    fun updateList(all: AllManager){

        println("searching for $projectName")

        if(projectName.isEmpty()){
            allEntries.clear()
            return
        }

        val time = System.nanoTime()
        if(lastProjectName != projectName || abs(lastRequestTime - time) > 7000 * millisToNanos){
            lastProjectName = projectName
            lastRequestTime = time
        } else return updateSearch(false, all)

        // download all old stuff from the database
        val entries = ArrayList<Entry>(allEntries.size + 5)
        val text = getText("hist/listNow.php?project=$projectName")
        val lines = text.replace('\r', ' ').split("###").map { it.trim() }
        var first = true
        for(line in lines){
            if(first){
                first = false
                // the info about what is what
            } else {
                val parts = line.replace("\\;", str0).split(';').map { it.replace(char0, ';').trim() }
                if(parts.size > 6){
                    val taskUID = parts[0].toIntOrNull() ?: continue
                    val version = parts[1].toIntOrNull() ?: continue
                    val taskText = parts[2]
                        .replace("\\n", "\n")
                        .replace("\\\\", "\\")
                    val created = parts[3]
                    val fileName = parts[4]
                    val lastPartOfName = fileName.split('/').last()
                    val todoIndex = parts[5].toIntOrNull() ?: continue
                    val taskFlags = parts[6].split(',').map { it.trim() }.toHashSet()
                    val entry = Entry(taskUID, version, taskText, created, fileName, lastPartOfName, todoIndex, taskFlags)
                    // done do sth with entry
                    entries.add(entry)
                    println(entry)
                }
            }
        }

        var isDifferent = allEntries.size != entries.size
        if(!isDifferent){
            entries@ for(entry in allEntries){
                for(entry2 in entries){
                    if(entry.isTheSame(entry2)){
                        continue@entries
                    }
                }
                isDifferent = true
                break
            }
        }

        if(isDifferent){

            allEntries.clear()
            allEntries.addAll(entries)

        }

        updateSearch(isDifferent, all)

    }

}