import java.util.Collections;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.Calendar;
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
    private val host: String;
    private val identifier: String;
    private val user: String;
    private val date: Date;
    private val firstLine: String;
    private val state: Int;
    private val responseSize: Int;
    private val referer: String;
    private val userAgent: String;

    private constructor (
        host: String,
        identifier: String,
        user: String,
        date: Date,
        firstLine: String,
        state: Int,
        responseSize: Int,
        referer: String,
        userAgent: String
    ) {
        this.host = host;
        this.identifier = identifier;
        this.user = user;
        this.date = date;
        this.firstLine = firstLine;
        this.state = state;
        this.responseSize = responseSize;
        this.referer = referer;
        this.userAgent = userAgent;
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
                val identifier = matcher.group(2);
                val user = matcher.group(3);
                val date = String.format("%s/%s/%s %s:%s:%s", matcher.group(6), matcher.group(5).toMonth(), matcher.group(4), matcher.group(7), matcher.group(8), matcher.group(9)).toDate()
                val firstLine = matcher.group(10);
                val state = matcher.group(11).toInt();
                val responseSize = if (matcher.group(12) == "-") 0 else matcher.group(12).toInt();
                val referer = matcher.group(13);
                val userAgent = matcher.group(14);
                return Access(
                    host,
                    identifier,
                    user,
                    date,
                    firstLine,
                    state,
                    responseSize,
                    referer,
                    userAgent
                )
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

    constructor() {
        this.accesses = ArrayList<Access>();
    }

    private constructor(accesses: ArrayList<Access>) {
        this.accesses = accesses;
    }

    public fun readLogFile(filename: String) {
        val lines = File(filename).bufferedReader().readLines();
        for (line in lines) {
            this.accesses.add(Access.ofAccessLogLine(line));
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

        return groups.map { AccessLog(it) };
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
        groupsMap.forEach { _, v -> result.add(AccessLog(v)) }
        Collections.sort(result, AccessLogComparator());

        return result.map { Pair(it.accesses.get(0).getHost(), it) };
    }

    public fun getSize() = this.accesses.size;
}


public class AccessLogComparator: Comparator<AccessLog> {
	override public fun compare(log1: AccessLog, log2: AccessLog): Int = log2.getSize() - log1.getSize();
}
