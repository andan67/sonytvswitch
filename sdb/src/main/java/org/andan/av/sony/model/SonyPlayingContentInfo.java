package org.andan.av.sony.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class SonyPlayingContentInfo {
    public String source;
    public String dispNumber;
    public String mediaType;
    public String title;
    public String uri;
    public String startDateTime;
    public int durationSec;
    public String programTitle;

    private static SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");
    private static SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static Calendar cal = Calendar.getInstance();

    @Override
    public String toString() {
        return "SonyPlayingContentInfo{" +
                "source='" + source + '\'' +
                ", dispNumber='" + dispNumber + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", title='" + title + '\'' +
                ", uri='" + uri + '\'' +
                ", startDateTime='" + startDateTime + '\'' +
                ", duractionSec=" + durationSec +
                ", programTitle='" + programTitle + '\'' +
                '}';
    }

    public SonyPlayingContentInfo(String source, String dispNumber, String mediaType, String title, String uri, String startDateTime, int durationSec, String programTitle) {
        this.source = source;
        this.dispNumber = dispNumber;
        this.mediaType = mediaType;
        this.title = title;
        this.uri = uri;
        this.startDateTime = startDateTime;
        this.durationSec = durationSec;
        this.programTitle = programTitle;
    }

    public String getSource() {
        return source;
    }

    public String getDispNumber() {
        return dispNumber;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public String getStartDateTimeFormatted() {
        try {
            Date date = sdfInput.parse(startDateTime);
            return sdfOutput.format(date);
        } catch (ParseException e) {
            return startDateTime;
        }
    }

    public String getEndDateTimeFormatted() {
        try {
            Date date = sdfInput.parse(startDateTime);
            cal.setTime(date);
            cal.add(Calendar.SECOND,durationSec);
            return sdfOutput.format(cal.getTime());

        } catch (ParseException e) {
            return "";
        }
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDispNumber(String dispNumber) {
        this.dispNumber = dispNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public void setProgramTitle(String programTitle) {
        this.programTitle = programTitle;
    }


    public static void main(String[] args) {
        System.out.print("Test");

        try {
            Date date = sdfInput.parse("2019-12-31T19:05:00+0100");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.SECOND, 70*60); //minus number would decrement the days
            cal.getTime();
            System.out.println(date);
            System.out.println(cal.getTime());
            System.out.println(sdfOutput.format(cal.getTime()));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
