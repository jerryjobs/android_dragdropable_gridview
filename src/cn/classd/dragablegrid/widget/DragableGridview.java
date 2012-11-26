/**
 * Copyright 2012 Easy Read Tech. All rights reserved.
 */
package cn.classd.dragablegrid.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * @author guojie
 * 
 * 
 * 已知问题列表：2：垂直滚动条 
 * 
 */
public class DragableGridview extends GridView implements OnGestureListener {

	private static final String			TAG			= "MyGridView";

	private int							lastX, lastY, newX, newY;

	private ImageView					mDragView;
	private WindowManager				mWindowManager;
	private WindowManager.LayoutParams	mWindowParams;

	private int							mDragPointX;				
																	
	private int							mDragPointY;				
																	
	private int							mXOffset;					
																	
	private int							mYOffset;					

	private Bitmap						mDragBitmap;
	
	private Drawable					mTrashcan;

	int									dragedItemIndex;
	int									dropedItemIndex;

	private OnSwappingListener			onSwappingListener;
	private OnItemClickListener			onItemClickListener;

	private GestureDetector				mGesture;
	// anim vars
	public static int					animT		= 150;

	private int							colCount	= 3;
	
	/** 通过控制是否启用来确定是否传递child的child的点击事件 */
	private boolean						enable		= true;

	public DragableGridview(Context context, AttributeSet attrs) {
		super(context, attrs);
		colCount = attrs.getAttributeIntValue("android", "numColumns", 3);
		mGesture = new GestureDetector(this);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return enable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return mGesture.onTouchEvent(ev);

		case MotionEvent.ACTION_MOVE:
			if (mIsDragging) {
				newX = (int) ev.getX();
				newY = (int) ev.getY();

				if (mDragView != null) {
					dragView(newX, newY);
					showSelectItem();
					dropedItemIndex = pointToPosition(newX, newY);
				}
				return true;
			}
			return mGesture.onTouchEvent(ev);

		case MotionEvent.ACTION_UP:
			if (mIsDragging) {
				stopDragging();
				mIsDragging = false;
				return true;
			} else {
				mGesture.onTouchEvent(ev);
			}
		}
		return mIsDragging;
	}

	private void startDragging() {
		dragedItemIndex = pointToPosition(lastX, lastY);

		if (dragedItemIndex != -1) {
			ViewGroup item = (ViewGroup) getChildAt(dragedItemIndex);
			mDragPointX = lastX - item.getLeft();
			mDragPointY = lastY - item.getTop();

			item.setDrawingCacheEnabled(true);
			Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
			
			mWindowParams = new WindowManager.LayoutParams();
			mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
			mWindowParams.x = lastX;// - mDragPointX; //+ mXOffset;
			mWindowParams.y = lastY;// - mDragPointY; // + mYOffset;

			mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
			mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

			mWindowParams.format = PixelFormat.TRANSLUCENT;
			mWindowParams.windowAnimations = 0;

			Context context = getContext();
			ImageView v = new ImageView(context);
			v.setPadding(0, 0, 0, 0);
			v.setImageBitmap(bitmap);
			mDragBitmap = bitmap;
			
//			TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE, mDragPointX, Animation.ABSOLUTE,
//					mWindowParams.x, Animation.ABSOLUTE, mDragPointY, Animation.ABSOLUTE, mWindowParams.y);
//			translate.setDuration(animT);
//			translate.setFillEnabled(true);
//			translate.setFillAfter(true);
//			v.clearAnimation();
//			v.startAnimation(translate);

			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.addView(v, mWindowParams);
			mDragView = v;
		}
	}

	private void dragView(int x, int y) {

		mWindowParams.x = x - mDragPointX;
		mWindowParams.y = y;// - mDragPointY;
		mWindowManager.updateViewLayout(mDragView, mWindowParams);
	}

	private void stopDragging() {

		if (mDragView != null) {
			mDragView.setVisibility(GONE);
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			wm.removeView(mDragView);
			mDragView.setImageDrawable(null);
			mDragView = null;
		}

		if (mDragBitmap != null) {
			mDragBitmap.recycle();
			mDragBitmap = null;
		}

		if (mTrashcan != null) {
			mTrashcan.setLevel(0);
		}

		if (dragedItemIndex != -1 && dropedItemIndex != -1 && dragedItemIndex != dropedItemIndex) {

			if (onSwappingListener != null)
				onSwappingListener.waspping(dragedItemIndex, dropedItemIndex);

			dragedItemIndex = -1;
			dropedItemIndex = -1;
		}
	}

	private void showSelectItem() {
	}

	public void setOnSwappingListener(OnSwappingListener l) {
		this.onSwappingListener = l;
	}
	
	public void setOnItemClick(OnItemClickListener l) {
		this.onItemClickListener = l;
	}

	/**
	 * 调换位置传
	 * 
	 * @author guojie
	 */
	public interface OnSwappingListener {
		public abstract void waspping(int oldIndex, int newIndex);
	}
	
	/**
	 * 传出itemOnClick事件
	 * 
	 * @author guojie
	 */
	public interface OnItemClickListener {
		public abstract void click(int index);
	}

	@Override
	public boolean onDown(MotionEvent ev) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	boolean	mIsDragging	= false;
	
	private ViewGroup dragedView;

	@Override
	public void onLongPress(MotionEvent ev) {
		mIsDragging = true;
		lastX = (int) ev.getX();
		lastY = (int) ev.getY();

		startDragging();
		
//		AlphaAnimation alphaAnimation = new AlphaAnimation(.6f, 1f);
//		alphaAnimation.setDuration(animT);
//		dragedView.startAnimation(alphaAnimation);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		int index = pointToPosition((int) e.getX(), (int) e.getY());
		if (index != -1) {
			dragedView = (ViewGroup) getChildAt(index);
			AlphaAnimation alphaAnimation = new AlphaAnimation(1f, .6f);
			alphaAnimation.setDuration(animT);
			dragedView.clearAnimation();
			dragedView.startAnimation(alphaAnimation);
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (!mIsDragging) {
			int index = pointToPosition((int) e.getX(), (int) e.getY());
			if (index != -1)
				onItemClickListener.click(index);
		}
		return false;
	}

	/**
	 * @return the enable
	 */
	public boolean isEnable() {
		return enable;
	}

	/**
	 * @param enable the enable to set
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
