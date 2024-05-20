package com.example.tasks_list.Utilities;

import static com.example.tasks_list.Utilities.Settings.DELETE_SWIPE_COLOR;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasks_list.Enums.SwipeType;
import com.example.tasks_list.Interfaces.DatabaseItemRemover;
import com.example.tasks_list.R;

import java.util.function.BiConsumer;

public class SwipeToDeleteManager {

    private static final int BACKGROUND_CORNER_OFFSET = 20;

    private final Context context;
    private final BiConsumer<RecyclerView.ViewHolder, DatabaseItemRemover> action;
    private final DatabaseItemRemover databaseItemRemover;
    private final ItemTouchHelper itemTouchHelper;
    private final SwipeType swipeType;

    public SwipeToDeleteManager(Context context, DatabaseItemRemover databaseItemRemover, SwipeType swipeType, BiConsumer<RecyclerView.ViewHolder, DatabaseItemRemover> action) {
        this.context = context;
        this.databaseItemRemover = databaseItemRemover;
        this.swipeType = swipeType;

        this.action = action;
        itemTouchHelper = new ItemTouchHelper(initItemTouchHelper());
    }

    private ItemTouchHelper.SimpleCallback initItemTouchHelper() {

        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            final Drawable background = new ColorDrawable(DELETE_SWIPE_COLOR);
            final Drawable deleteIcon = ContextCompat.getDrawable(context, getDeleteIcon());

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;

                if (dX < 0)
                    background.setBounds(itemView.getRight() + (int) dX - BACKGROUND_CORNER_OFFSET, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                else
                    background.setBounds(0, 0, 0, 0);

                background.draw(c);

                int deleteIconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconBottom = deleteIconTop + deleteIcon.getIntrinsicHeight();

                if (dX < 0) {
                    int deleteIconLeft = itemView.getRight() - deleteIconMargin - deleteIcon.getIntrinsicWidth();
                    int deleteIconRight = itemView.getRight() - deleteIconMargin;
                    deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                }

                deleteIcon.draw(c);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                action.accept(viewHolder, databaseItemRemover);
            }
        };

    }


    private int getDeleteIcon() {
        switch (swipeType) {
            case TASK:
                return R.drawable.ic_delete_task;

            case ATTACHMENT:
                return R.drawable.ic_delete_attachment;

            default:
                return R.drawable.ic_delete_task;
        }
    }


    public void attachToRecyclerView(RecyclerView recyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

}
