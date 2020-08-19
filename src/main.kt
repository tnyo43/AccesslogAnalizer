import kotlin.system.exitProcess;


fun getDisplayClass(option: String, log: AccessLog): Display =
    when (option) {
        "host" -> HostDisplay(log)
        "timezone" -> TimezoneDisplay(log)
        "simple" -> SimpleDisplay(log)
        else -> throw Exception("Unknown option Exception");
    }

fun getOptions(options: Array<String>): Pair<String?, String?> {
    var start: String? = null;
    var end: String? = null;

    for (i in 0..(options.size-1)) {
        if (options[i] == "--start") {
            start = options[i+1]
        } else if (options[i] == "--end") {
            end = options[i+1]
        }
    }

    return Pair(start, end);
}


fun main(args: Array<String>) {
    if (args.size == 0) {
        println("オプションを指定してください");
        println("Example 1: kotlin MainKt simple -> 総アクセス件数を表示");
        println("Example 2: kotlin MainKt timezone --start 2020/01/01-> 2020年1月1日以降の、時間帯ごとのアクセス件数を表示");
        println("Example 3: kotlin MainKt host --end 2020/07/31 -> 2020年7月31日までの、ホストごとのアクセス件数を表示");
        exitProcess(0);
    }

    val period = getOptions(args);
    val log = AccessLog(period.first, period.second);

    log.readLogsDir("logs");

    val display: Display = getDisplayClass(args[0], log)
    display.display();
}
