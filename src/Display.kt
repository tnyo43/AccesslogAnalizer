public abstract class Display {
    internal val log: AccessLog;

    constructor(log: AccessLog) {
        this.log = log;
    }

    public abstract fun display();
}