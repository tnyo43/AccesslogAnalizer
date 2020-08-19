fun getDisplayClass(option: String, log: AccessLog): Display =
    when (option) {
        "-h" -> HostDisplay(log)
        "-t" -> TimezoneDisplay(log)
        "-s" -> SimpleDisplay(log)
        else -> throw Exception("Unknown option Exception");
    }


fun main(args: Array<String>) {
    if (args.size == 0) {
        println("オプションを指定してください");
        println("Example 1: kotlin MainKt -s -> 総アクセス件数を表示");
        println("Example 2: kotlin MainKt -t -> 時間帯ごとのアクセス件数を表示");
        println("Example 3: kotlin MainKt -h -> ホストごとのアクセス件数を表示");
    }

    val log = AccessLog();
    log.readLogsDir("logs");

    val display: Display = getDisplayClass(args[0], log)
    display.display();
}
