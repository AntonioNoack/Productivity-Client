package me.antonio.noack.productivity.tasks

class Entry(var taskUID: Int,
            val version: Int,
            var taskText: String,
            val created: String,
            val path: String,
            val pathName: String,
            val todoIndex: Int,
            val taskFlags: HashSet<String>): Comparable<Entry> {

    var tlc = taskText.toLowerCase()

    override fun toString(): String {
        return "#$taskUID.$version: ${taskText.replace("\n", "\\n")}, ${taskFlags.joinToString(" "){"#$it"}}"
    }

    fun isTheSame(sec: Entry): Boolean {
        return sec.taskUID == taskUID && sec.version == version && sec.path == path && sec.todoIndex == todoIndex
    }

    val hashCode = taskUID * 1000 + version * 30 + todoIndex
    override fun hashCode(): Int {
        return hashCode
    }

    override fun compareTo(other: Entry): Int {
        val dif = hashCode.compareTo(other.hashCode)
        if(dif != 0) return dif
        return taskText.compareTo(other.taskText)
    }

}