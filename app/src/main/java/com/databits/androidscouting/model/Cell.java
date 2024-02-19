package com.databits.androidscouting.model;

public class Cell {
    private int mCellId;
    private String mTitle;
    private String mType;
    private CellParam mParam;

    public Cell(int CellId, String title, String type, CellParam param) {
        mCellId = CellId;
        mTitle = title;
        mType = type;
        mParam = param;
    }

    public CellParam getParam() {
        return mParam;
    }

    public void setParam(CellParam param) {
        mParam = param;
    }

    public int getCellId() {
        return mCellId;
    }

    public void setCellId(int CellId) {
        this.mCellId = CellId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

}