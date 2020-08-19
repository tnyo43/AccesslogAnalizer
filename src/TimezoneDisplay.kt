public class TimezoneDisplay: Display {
    
    constructor(log: AccessLog): super(log) {
    }

    override public fun display() {
        println("時間ごとのアクセス数を表示")

        val result = log.groupByTimeZone();
        for (h in 0..23) {
            println(String.format("%2d:00 ~ %2d:59 : %4d", h, h, result.get(h).getSize()));
        }
    }
}