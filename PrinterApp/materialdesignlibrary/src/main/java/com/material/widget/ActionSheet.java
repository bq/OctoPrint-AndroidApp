package com.material.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: keith.
 * Date: 14-10-9.
 * Time: 16:02.
 */
public class ActionSheet extends Dialog {

    public static final int LIST_STYLE = 1;
    public static final int GRID_STYLE = 2;

    private CharSequence mTitle;
    private OnItemClickListener mListener;
    private List<Item> itemList = new ArrayList<Item>();

    private ItemAdapter adapter;

    public ActionSheet(Context context) {
        super(context, android.R.style.Theme_NoTitleBar);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void setTitle(CharSequence title) {
        this.mTitle = title;
    }

    @Override
    public void setTitle(int titleId) {
        this.mTitle = getContext().getResources().getString(titleId);
    }

    public ActionSheet addItems(List<Item> items) {
        if (items == null || items.size() == 0) {
            return this;
        } else {
            itemList.addAll(items);
        }
        return this;
    }

    private void createView() {
        FrameLayout parent = new FrameLayout(getContext());

        GridView gridView = new GridView(getContext());
        adapter = new ItemAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }
        });
        parent.addView(gridView);
    }

    private class ItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Item getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            LinearLayout parent = new LinearLayout(getContext());

            TextView textView = new TextView(getContext());
            parent.addView(textView);

            ImageView imageView = new ImageView(getContext());
            parent.addView(imageView);

            return parent;
        }
    }

    public class Item {
        public int mIcon;
        public String mText;

        public Item(int icon, String text) {
            super();
            this.mIcon = icon;
            this.mText = text;
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(int position);
    }
}
