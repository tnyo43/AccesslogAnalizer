import java.util.Collections;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.IllegalArgumentException;
import java.io.File;


@Throws(IllegalArgumentException::class, ParseException::class)
fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm:ss"): Date {
    val sdFormat = SimpleDateFormat(pattern)
    val date = sdFormat.let { it.parse(this) }
    return date
}

class Access {
    // 軽量にするために、保持するデータは解析で用いるhostとdateだけ
    private val host: String;
    private val date: Date;

    private constructor (host: String, date: Date) {
        this.host = host;
        this.date = date;
    }

    companion object {
        fun String.toMonth(): String =
            when (this) {
                "Jan" -> "01"
                "Feb" -> "02"
                "Mar" -> "03"
                "Apr" -> "04"
                "May" -> "05"
                "June" -> "06"
                "July" -> "07"
                "Aug" -> "08"
                "Sept" -> "09"
                "Oct" -> "10"
                "Nov" -> "11"
                "Dec" -> "12"
                else -> throw InvalidLogException();
            }

        fun ofAccessLogLine(str: String): Access {
            val regex = "(\\S+) (\\S+) (\\S+) \\[(\\d+)/(\\S+)/(\\d+):(\\d+):(\\d+):(\\d+) \\S+\\] \\\"([\\S+|\\s+]+)\\\" (\\d+) (\\d+|-) \\\"([\\S+|\\s+]+)\\\" \\\"([\\S+|\\s+]+)\\\"";
            val p = Pattern.compile(regex);
            val matcher = p.matcher(str);

            if (matcher.matches()) {
                val host = matcher.group(1);
                val date = String.format("%s/%s/%s %s:%s:%s", matcher.group(6), matcher.group(5).toMonth(), matcher.group(4), matcher.group(7), matcher.group(8), matcher.group(9)).toDate()
                return Access(host, date)
            } else {
                throw InvalidLogException();
            }
        }
    }

    public fun getHost() = this.host;
    public fun getDate() = this.date;
}


class AccessLog {
    private val accesses: ArrayList<Access>;
    private val start: Date?;
    private val end: Date?;


    constructor(start: String?, end: String?) {
        this.accesses = ArrayList<Access>();

        this.start = start?.toDate("yyyy/MM/dd");
        this.end = if (end == null) null else (end + " 23:59:59").toDate();
    }

    private constructor(accesses: ArrayList<Access>, start: Date?, end: Date?) {
        this.accesses = accesses;
        this.start = start;
        this.end = end;
    }

    private fun readLogFile(file: File) {
        val lines = file.bufferedReader().readLines();
        val newAccesses =
            lines
                .map { map_it ->
                    Access.ofAccessLogLine(map_it)
                }
                .filter { access ->
                    if (this.start == null) true else this.start <= access.getDate()
                }
                .filter { access ->
                    if (this.end == null) true else access.getDate() <= this.end
                };
        for (na in newAccesses) {
            this.accesses.add(na);
        }
    }

    public fun readLogsDir(dirname: String) {
        // 大きい方から順に見るほうが後半でも読める可能性が高い（貪欲な方法）
        val files = File(dirname)
                        .list()
                        .toList()
                        .map { filename -> File(String.format("%s/%s", dirname, filename))};
        Collections.sort(files, FileComparator());

        var openFileCount = 0;
        for (file in files) {
            val usingMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            val maxMemory = Runtime.getRuntime().maxMemory();
            val fileMemory = file.length();

            // "ファイルの展開" + "オブジェクトの生成" で "ファイルの容量" の2倍のメモリが確保できるか確認
            if (maxMemory - usingMemory > 2 * fileMemory) {
                readLogFile(file);
                openFileCount++;
            } else {
                val logger = Logger.getLogger("AccessLog");
                logger.setLevel(Level.WARNING); 
                logger.warning(String.format("ファイル \"%s\" が大きすぎて展開できません。ファイルを分割してください。", file.name));
            }
        }

        if (openFileCount == 0) {
            throw Exception("Not Enough Memory, No File opened");
        }
    }

    public fun groupByTimeZone(): List<AccessLog> {
        val groups = ArrayList<ArrayList<Access>>();
        for (i in 1..24) groups.add(ArrayList<Access>());

        for (access in this.accesses) {
            val c = Calendar.getInstance();
            c.setTime(access.getDate());
            val hour = c.get(Calendar.HOUR_OF_DAY);
            groups[hour].add(access);
        }

        return groups.map { AccessLog(it, this.start, this.end) };
    }

    public fun groupByHostOrderbyCount(): List<Pair<String, AccessLog>> {
        val groupsMap = mutableMapOf<String, ArrayList<Access>>();

        for (access in this.accesses) {
            val host = access.getHost();
            if (!groupsMap.containsKey(host)) {
                groupsMap.set(host, ArrayList<Access>());
            }
            groupsMap.get(host)?.add(access);
        }

        val result = ArrayList<AccessLog>();
        groupsMap.forEach { _, v -> result.add(AccessLog(v, this.start, this.end)) }
        Collections.sort(result, AccessLogComparator());

        return result.map { Pair(it.accesses.get(0).getHost(), it) };
    }

    public fun getSize() = this.accesses.size;
}


public class AccessLogComparator: Comparator<AccessLog> {
	override public fun compare(log1: AccessLog, log2: AccessLog): Int = log2.getSize() - log1.getSize();
}

public class FileComparator: Comparator<File> {
	override public fun compare(file1: File, file2: File): Int = (file2.length() - file1.length()).toInt();
}
