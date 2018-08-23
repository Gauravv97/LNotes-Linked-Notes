package data;

import java.io.Serializable;

/**
 * Created by Gaurav on 5/14/2017.
 */

public class entry implements Serializable{
    private int _id;
    private String Name;
    private String DateTime;
    private String []_im;
    private String []_sc;
    private boolean hasMarker[];
    private String []_mr;
    private int _total;
    private int _KPRows[];
    private int _KPCols[];
    private String _KeyPoints[];
    private int _serial[];

    public entry() {}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean[] HasMarker() {
        return hasMarker;
    }

    public void setHasMarker(boolean[] hasMarker) {
        this.hasMarker = hasMarker;
    }

    public String[] get_KeyPoints() {
        return _KeyPoints;
    }

    public void set_KeyPoints(String _KeyPoints[]) {
        this._KeyPoints = _KeyPoints;
    }



    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public entry(String _im[], String []_mr, int t) {
        _total=t;
        this._im=_im;
        this._mr=_mr;


    }

    public int[] get_KPRows() {
        return _KPRows;
    }

    public void set_KPRows(int []_KPRows) {
        this._KPRows = _KPRows;
    }

    public int[] get_KPCols() {
        return _KPCols;
    }

    public void set_KPCols(int []_KPCols) {
        this._KPCols = _KPCols;
    }

    public String[] get_im() {
        return _im;
    }

    public int[] get_serial() {
        return _serial;
    }

    public void set_serial(int[] _serial) {
        this._serial = _serial;
    }

    public void set_im(String []_im) {
        this._im = _im;
    }

    public int get_total(){
        return _total;
    }

    public void set_total(int _total) {
        this._total = _total;
    }

    public String []get_mr() {
        return _mr;
    }

    public void set_mr(String []_mr) {
        this._mr = _mr;
    }
    public String[] get_sc() {return  _sc;}
    public void set_sc(String []_sc){this._sc=_sc;}

}
