package org.andan.av.sony.model;

public class SonyProgram {
    public String source;
    public String dispNumber;
    public int index;
    public String mediaType;
    public String title;
    public String uri;

    @Override
    public String toString() {
        return "SonyProgram{" +
                "source='" + source + '\'' +
                ", dispNumber='" + dispNumber + '\'' +
                ", index=" + index +
                ", mediaType='" + mediaType + '\'' +
                ", title='" + title + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    public SonyProgram(String source, String dispNumber, int index, String mediaType, String title, String uri) {
        this.source = source;
        this.dispNumber = dispNumber;
        this.index = index;
        this.mediaType = mediaType;
        this.title = title;
        this.uri = uri;
    }

    public String getShortSource() {
        int i2 = source.indexOf("#");
        if(i2 <0) i2 = source.length();
        int i1 = source.indexOf(":")+1;
        return source.substring(i1,i2);
    }

    public String getType() {
        int i2 = source.indexOf("#");
        if(i2 <0) return "";
        return "(" + source.substring(i2+1) + ")";
    }

    public String getSourceWithType() {
        return getShortSource() + " " + getType();
    }


    public String getAsItemString() {
        return title + " [No: " + dispNumber + "/" + getShortSource() + " " + getType() + "]";
    }

    public String getAsItemString(int id) {
        return title + " [id:" + id + "/" + getShortSource() + " " + getType() + "]";
    }
}
