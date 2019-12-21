package org.andan.av.sony.model;

public class SonyPlayingContentInfo {
    public String source;
    public String dispNumber;
    public String mediaType;
    public String title;
    public String uri;
    public String startDateTime;
    public int durationSec;
    public String programTitle;


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
}
