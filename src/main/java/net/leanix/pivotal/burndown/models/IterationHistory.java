package net.leanix.pivotal.burndown.models;

import java.util.ArrayList;

/**
 *
 * @author berndschoenbach
 */
public class IterationHistory {

    private ArrayList<String> header;
    private ArrayList<ArrayList<String>> data;
    private String kind;

    public ArrayList<String> getHeader() {
        return header;
    }

    public void setHeader(ArrayList<String> header) {
        this.header = header;
    }

    public ArrayList<ArrayList<String>> getData() {
        return data;
    }

    public void setData(ArrayList<ArrayList<String>> data) {
        this.data = data;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
