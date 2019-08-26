package me.antonio.noack.productivity.tasks

import kotlinx.android.synthetic.main.main.*
import me.antonio.noack.productivity.AllManager
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
                val nonFlags = parts.filter { !it.startsWith("#") }.map { it.toLowerCase() }
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
                            if(!entry.path.contains(nonFlag) && !entry.tlc.contains(nonFlag)){
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

                val res = all.resources

                EntryDisplayer.resetColorCounts()

                for(entry in matchingEntries.reversed()){

                    // display the items
                    // done put all this stuff into it's own class
                    EntryDisplayer.display(inflater, list, res, entry)

                }

                EntryDisplayer.displayColorCounts(all.colorBarView)

            }

        }
    }

    fun updateList(all: AllManager){

        // println("searching for $projectName")

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
                    // println(entry)
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