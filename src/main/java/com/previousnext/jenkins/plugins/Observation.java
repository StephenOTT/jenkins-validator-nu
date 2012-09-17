package com.previousnext.jenkins.plugins;

/**
 *
 * @author william.mcrae
 */
public class Observation {

    private String type;
    private Long lastLine;
    private Long lastColumn;
    private String message;
    private String extract;
    private Long hiliteStart;
    private Long hiliteLength;


    Observation() {}

    public Observation(String type, Long lastLine, Long lastColumn, String message, String extract, Long hiliteStart, Long hiliteLength) {
        this.type = type;
        this.lastLine = lastLine;
        this.lastColumn = lastColumn;
        this.message = message;
        this.extract = extract;
        this.hiliteStart = hiliteStart;
        this.hiliteLength = hiliteLength;
    }

    @Override
    public String toString() {
        String output = "";

        output += type == null ? "" : "Type: " + type + "\n";
        output += message == null ? "" : "Message: " + message + "\n";
        output += extract == null ? "" : "Extract: " + extract + "\n";
        output += lastLine == null ? "" : "Last Line: " + lastLine + "\n";
        output += lastColumn == null ? "" : "Last Column: " + lastColumn + "\n";
        output += hiliteStart == null ? "" : "hilite start: " + hiliteStart + "\n";
        output += hiliteLength == null ? "" : "hilite lenght: " + hiliteLength + "\n";

        return output;
    }

    public String getType() {
        return type;
    }

    public Long getLastLine() {
        return lastLine;
    }

    public Long getLastColumn() {
        return lastColumn;
    }

    public String getMessage() {
        return message;
    }

    public String getExtract() {
        return extract;
    }

    public Long getHiliteStart() {
        return hiliteStart;
    }

    public Long getHiliteLength() {
        return hiliteLength;
    }
}
