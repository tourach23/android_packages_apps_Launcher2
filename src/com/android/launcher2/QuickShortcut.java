/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher2;

import android.widget.ImageView;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.graphics.RectF;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.content.pm.PackageManager;

public class QuickShortcut extends ImageView implements View.OnClickListener, DropTarget, DragController.DragListener {
    private static final int ORIENTATION_HORIZONTAL = 1;
    private static final int TRANSITION_DURATION = 250;
    private static final int ANIMATION_DURATION = 200;

    private final int[] mLocation = new int[2];
    
    private Launcher mLauncher;
    private boolean mAssignMode;

    private AnimationSet mInAnimation;
    private AnimationSet mOutAnimation;
/*
    private Animation mHandleInAnimation;
    private Animation mHandleOutAnimation;
*/
    private int mOrientation;
    private DragController mDragController;

    private final RectF mRegion = new RectF();
    //private View mHandle;
    private final Paint mDropPaint = new Paint();
	private final int srcColor;
	
	private Intent intent;
	private String packageName;
	
	private PackageManager pm;
	
	private int appNumber;

    public QuickShortcut(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickShortcut(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        srcColor = context.getResources().getColor(R.color.shortcut_color_filter);
        mDropPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QuickShortcut, defStyle, 0);
        mOrientation = a.getInt(R.styleable.QuickShortcut_direction, ORIENTATION_HORIZONTAL);
        appNumber = a.getInt(R.styleable.QuickShortcut_appNumber, 0);
		a.recycle();

		pm = context.getPackageManager();
		this.setOnClickListener(this);
    }

	public void setApp(String appName, String appClass) {
		packageName = appName;
		intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName(appName, appClass);
		
		try {
			this.setImageDrawable(pm.getActivityIcon(intent));
		} catch(Exception e) {}
	}
	
	public void onClick(View v) {
		if (intent != null) {
			Log.d("Launcher2/QSApp", "Starting "+intent);
			mLauncher.startActivitySafely(intent);
		}
	}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
		final ItemInfo item = (ItemInfo) dragInfo;
		
		Log.d("Launcher2/QSApp", "Dropped onto QuickShortcut");
		Log.d("Launcher2/QSApp", item.getClass().toString());
		
		if (item instanceof ApplicationInfo) {
			Log.d("Launcher2/QSApp", "AcceptedDrop");
        	return true;
		} else {
			return false;
		}
    }
    
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;

		Log.d("Launcher2/QSApp", "Accepted dropped onto QuickShortcut");
		Log.d("Launcher2/QSApp", item.getClass().toString());
		
		setApp(((ApplicationInfo)item).intent.getComponent().getPackageName(), ((ApplicationInfo)item).intent.getComponent().getClassName());
		mLauncher.saveBottomApp(appNumber, packageName, ((ApplicationInfo)item).intent.getComponent().getClassName());
		
		Log.d("Launcher2/QSApp", "Dropped app "+packageName+" with class "+intent);
		
        if (item.container == -1) return;
		
        LauncherModel.deleteItemFromDatabase(mLauncher, item);
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
		final ItemInfo item = (ItemInfo) dragInfo;
		
		if (item != null && item instanceof ApplicationInfo) {
        	dragView.setPaint(mDropPaint);
		}
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
		dragView.setPaint(null);
    }

    public void onDragStart(DragSource source, Object info, int dragAction) {
        final ItemInfo item = (ItemInfo) info;
        if (item != null && item instanceof ApplicationInfo) {
            mAssignMode = true;
            createAnimations();
            startAnimation(mInAnimation);
            //mHandle.startAnimation(mHandleOutAnimation);
            //setVisibility(VISIBLE);
        }
    }

    public void onDragEnd() {
        if (mAssignMode) {
            mAssignMode = false;
            startAnimation(mOutAnimation);
            //mHandle.startAnimation(mHandleInAnimation);
            //setVisibility(GONE);
        }
    }

    private void createAnimations() {
        if (mInAnimation == null) {
            mInAnimation = new FastAnimationSet();
            final AnimationSet animationSet = mInAnimation;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                animationSet.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f));
            } else {
                animationSet.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                        1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(ANIMATION_DURATION);
        }
        /*if (mHandleInAnimation == null) {
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                mHandleInAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
            } else {
                mHandleInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                        1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f);
            }
            mHandleInAnimation.setDuration(ANIMATION_DURATION);
        }*/
        if (mOutAnimation == null) {
            mOutAnimation = new FastAnimationSet();
            final AnimationSet animationSet = mOutAnimation;
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                animationSet.addAnimation(new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f));
            } else {
                animationSet.addAnimation(new FastTranslateAnimation(Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f));
            }
            animationSet.setDuration(ANIMATION_DURATION);
        }
        /*if (mHandleOutAnimation == null) {
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                mHandleOutAnimation = new FastTranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f);
            } else {
                mHandleOutAnimation = new FastTranslateAnimation(Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f);
            }
            mHandleOutAnimation.setFillAfter(true);
            mHandleOutAnimation.setDuration(ANIMATION_DURATION);
        }*/
    }

    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    /*void setHandle(View view) {
        mHandle = view;
    }*/

    private static class FastTranslateAnimation extends TranslateAnimation {
        public FastTranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
                int fromYType, float fromYValue, int toYType, float toYValue) {
            super(fromXType, fromXValue, toXType, toXValue,
                    fromYType, fromYValue, toYType, toYValue);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }

    private static class FastAnimationSet extends AnimationSet {
        FastAnimationSet() {
            super(false);
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return true;
        }

        @Override
        public boolean willChangeBounds() {
            return false;
        }
    }
}
