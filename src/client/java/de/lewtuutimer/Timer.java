package de.lewtuutimer;

final class Timer {
    private enum State { STOPPED, RUNNING, PAUSED }
    private State state = State.STOPPED;
    private long elapsedMillis;
    private long startedAtMillis;

    void start() {
        elapsedMillis = 0;
        startedAtMillis = System.currentTimeMillis();
        state = State.RUNNING;
    }

    void stop() {
        state = State.STOPPED;
        elapsedMillis = 0;
    }

    void restorePaused(long savedMillis) {
        elapsedMillis = Math.max(0, savedMillis);
        state = State.PAUSED;
    }

    boolean pause() {
        if (state != State.RUNNING) return false;
        elapsedMillis = currentElapsedMillis();
        state = State.PAUSED;
        return true;
    }

    boolean resume() {
        if (state != State.PAUSED) return false;
        startedAtMillis = System.currentTimeMillis();
        state = State.RUNNING;
        return true;
    }

    boolean exists() {
        return state != State.STOPPED;
    }

    boolean isPaused() {
        return state == State.PAUSED;
    }

    long currentElapsedMillis() {
        if (state == State.RUNNING) {
            return elapsedMillis + System.currentTimeMillis() - startedAtMillis;
        }
        return elapsedMillis;
    }

    String formatted() {
        long seconds = currentElapsedMillis() / 1000L;
        long days = seconds / 86_400;
        long hours = (seconds % 86_400) / 3_600;
        long minutes = (seconds % 3_600) / 60;
        long secs = seconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append("d ");
        if (hours > 0) result.append(hours).append("h ");
        result.append(minutes).append("m ").append(secs).append("s");
        return result.toString();
    }
}
