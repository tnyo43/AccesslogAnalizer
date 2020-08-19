public class SimpleDisplay: Display {
    
    constructor(log: AccessLog): super(log) {
    }

    override public fun display() {
        println(String.format("総アクセス数: %4d", this.log.getSize()));
    }
}