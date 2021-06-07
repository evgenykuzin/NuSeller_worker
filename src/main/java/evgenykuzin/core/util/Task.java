package evgenykuzin.core.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@ToString
@EqualsAndHashCode
public class Task {
    private final TaskJob taskJob;
    private final int hours, startMinutes, endMinutes;
    private final Integer[] daysOfWeek;
    private boolean isComplete;

    public Task(TaskJob taskJob, int hours, int startMinutes, int endMinutes, Integer[] daysOfWeek) {
        this.taskJob = taskJob;
        this.hours = hours;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
        this.daysOfWeek = daysOfWeek;
        isComplete = false;
    }

    public Task(TaskJob taskJob, int hours, int startMinutes, int endMinutes) {
        this(taskJob, hours, startMinutes, endMinutes, null);
    }

    public boolean execute() {
        try {
            if (!checkDayOfWeek()) return false;

            var startTime = parseTimeDate(hours, startMinutes);
            var endTime = parseTimeDate(hours, endMinutes);
            var currentTime = getCurrentTime();

            boolean timeIsInRange = timeIsInRange(currentTime, startTime, endTime);
            boolean timeAfter = currentTime.after(endTime);
            if (timeIsInRange && !isComplete) {
                isComplete = taskJob.start();
                return isComplete;
            } else if (isComplete && timeAfter) {
                isComplete = false;
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Date parseTimeDate(int hour, int minute) throws ParseException {
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
        String hourStr = hour < 10 ? "0" + hour : "" + hour;
        String minuteStr = minute < 10 ? "0" + minute : "" + minute;
        String timeStr = String.format("%s:%s", hourStr, minuteStr);
        return ft.parse(timeStr);
    }

    private Date getCurrentTime() throws ParseException {
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
        var dateStr = ft.format(new Date());
        return ft.parse(dateStr);
    }

    private boolean timeIsInRange(Date currentTime, Date startTime, Date endTime) {
        return !currentTime.before(startTime) && currentTime.before(endTime);
    }

    private boolean checkDayOfWeek() {
        if (daysOfWeek == null) return true;
        int currentDay = new Date().getDay();
        currentDay = currentDay == 0 ? 7 : currentDay;
        for (int dayNumber : daysOfWeek) {
            if (dayNumber == currentDay) {
                return true;
            }
        }
        return false;
    }

    public interface TaskJob {
        boolean start();
    }

}