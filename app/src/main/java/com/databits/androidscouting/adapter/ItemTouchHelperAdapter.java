package com.databits.androidscouting.adapter;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
    void onItemSelected();
    void onItemClear();
}
