fun main() {
    val log = AccessLog();
    log.readLogFile("log.txt");

    var display: Display = SimpleDisplay(log);
    display.display();
    println();

    display = TimezoneDisplay(log);
    display.display();
    println();

    display = HostDisplay(log);
    display.display();
}
