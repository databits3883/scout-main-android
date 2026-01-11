package com.databits.androidscouting.factory;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.databits.androidscouting.adapter.SimpleItemTouchHelperCallback;
import com.databits.androidscouting.adapter.MultiviewTypeAdapter;

/**
 * Factory for creating and configuring RecyclerView instances.
 * Handles layout manager setup, drag-and-drop configuration, and item decorations.
 */
public class RecyclerViewFactory {
    private final Context context;
    private final RecyclerViewConfig config;

    public RecyclerViewFactory(Context context, RecyclerViewConfig config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Create and configure a RecyclerView from the given view hierarchy
     * @param view Parent view containing the RecyclerView
     * @param viewId Resource ID of the RecyclerView
     * @return Fully configured RecyclerView ready for adapter attachment
     */
    public RecyclerView create(View view, int viewId) {
        RecyclerView recyclerView = view.findViewById(viewId);

        // Set layout manager based on config
        if (config.useGridLayout) {
            recyclerView.setLayoutManager(createGridLayoutManager());
        } else {
            recyclerView.setLayoutManager(createLinearLayoutManager());
        }

        // Add item decoration
        recyclerView.addItemDecoration(createItemDecoration());

        // Configure drag-and-drop if enabled
        if (config.enableReordering) {
            attachItemTouchHelper(recyclerView);
        }

        // Optimize recycled view pool
        configureViewPool(recyclerView);

        return recyclerView;
    }

    /**
     * Create GridLayoutManager with vertical scrolling enabled
     */
    private GridLayoutManager createGridLayoutManager() {
        return new GridLayoutManager(context, config.gridColumnCount) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };
    }

    /**
     * Create LinearLayoutManager with vertical scrolling enabled
     */
    private LinearLayoutManager createLinearLayoutManager() {
        return new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };
    }

    /**
     * Create vertical divider item decoration
     */
    private RecyclerView.ItemDecoration createItemDecoration() {
        return new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
    }

    /**
     * Attach ItemTouchHelper for drag-and-drop functionality
     * Note: This requires the adapter to implement ItemTouchHelperAdapter
     */
    private void attachItemTouchHelper(RecyclerView recyclerView) {
        // Get adapter if already set, otherwise this will be attached later
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof MultiviewTypeAdapter) {
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback((MultiviewTypeAdapter) adapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    /**
     * Configure the recycled view pool for optimal performance
     */
    private void configureViewPool(RecyclerView recyclerView) {
        // Disable recycling for view type 1
        recyclerView.getRecycledViewPool().setMaxRecycledViews(1, 0);
    }
}
