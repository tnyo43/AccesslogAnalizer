public class HostDisplay: Display {
    
    constructor(log: AccessLog): super(log) {
    }

    override public fun display() {
        println("ホストごとのアクセス数を表示")

        val result = log.groupByHostOrderbyCount();
        for (r in result) {
            println(String.format("%15s: %4d", r.first, r.second.getSize()));
        }
    }
}