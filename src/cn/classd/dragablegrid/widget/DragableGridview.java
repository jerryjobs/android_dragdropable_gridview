/**
 * Copyright 2012 Easy Read Tech. All rights reserved.
 */
package cn.classd.dragablegrid.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.GridView;
import android.widget.ImageView;
import cn.classd.dragablegrid.R;

/**
 * @author guojie
 */
public class DragableGridview extends GridView implements OnGestureListener {

	private static final String			TAG			= "MyGridView";

	private int							lastX, lastY, newX, newY;
	private ImageView					mDragView;
	private WindowManager				mWindowManager;
	private WindowManager.LayoutParams	mWindowParams;
	private int							mDragPointX;					// at what x offset inside the item did the user
	private int							mXOffset;						// the difference between screen coordinates and
	private int							mYOffset;						// the difference between screen coordinates and
	private Bitmap						mDragBitmap;
	private Drawable					mTrashcan;
	int									dragedItemIndex;
	int									dropedItemIndex;
	private OnSwappingListener			onSwappingListener;
	private OnItemClickListener			onItemClickListener;
	
	boolean								mIsDragging		= false;
	boolean								mIsScrolling	= false;
	int									scroll			= 0, maxScroll = 0;

	private GestureDetector				mGesture;
	// anim vars
	public static int					animT		= 150;

	private int							colCount	= 3;

	/** 通过控制是否启用来确定是否传递child的child的点击事件 */
	private boolean						enable		= true;

	private Handler						handler		= new Handler();

	private SmoothScrollRunnable		smoothScrollRunnable;

	public DragableGridview(Context context, AttributeSet attrs) {
		super(context, attrs);
		colCount = attrs.getAttributeIntValue("android", "numColumns", 3);
		mGesture = new GestureDetector(getContext(), this);
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

			/** reset scroll value */
			if (mIsScrolling) {

				if (scroll < 0) {
					if (null != smoothScrollRunnable) {
						smoothScrollRunnable.stop();
					}
					
					smoothScrollRunnable = new SmoothScrollRunnable(getHandler(), scroll, 0);
					handler.post(smoothScrollRunnable);
					scroll = 0;
				}

				if (scroll > maxScroll) {
					if (null != smoothScrollRunnable) {
						smoothScrollRunnable.stop();
					}
					smoothScrollRunnable = new SmoothScrollRunnable(getHandler(), scroll, maxScroll);
					handler.post(smoothScrollRunnable);
					scroll = maxScroll;
				}
				
				mIsScrolling = false;
			}
		}
		return mIsDragging;
	}

	private void startDragging() {
		dragedItemIndex = pointToPosition(lastX, lastY);

		if (dragedItemIndex != -1) {
			ViewGroup item = (ViewGroup) getChildAt(dragedItemIndex);
			mDragPointX = lastX - item.getLeft();
//			mDragPointY = lastY - item.getTop();

			item.setDrawingCacheEnabled(true);
			Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());

			mWindowParams = new WindowManager.LayoutParams();
			mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
			mWindowParams.x = mXOffset - lastX + item.getLeft();
			mWindowParams.y = mYOffset - lastY + item.getTop();

			mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
			mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

			mWindowParams.format = PixelFormat.TRANSLUCENT;
			mWindowParams.windowAnimations = 0;

			Context context = getContext();
			ImageView v = new ImageView(context);
			v.setPadding(0, 0, 0, 0);
			v.setImageBitmap(bitmap);
			mDragBitmap = bitmap;

			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.addView(v, mWindowParams);
			mDragView = v;
			mDragView.setVisibility(View.GONE);
		}
	}

	private void dragView(int x, int y) {

		if (mDragView.getVisibility() == View.GONE)
			mDragView.setVisibility(View.VISIBLE);
		mWindowParams.x = x - mDragPointX;
		mWindowParams.y = y;
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

	public void setOnSwappingListener(OnSwappingListener l) {
		this.onSwappingListener = l;
	}

	public void setOnItemClick(OnItemClickListener l) {
		this.onItemClickListener = l;
	}

	@Override
	public boolean onDown(MotionEvent ev) {
		maxScroll = getMaxScroll();

		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onLongPress(MotionEvent ev) {

		this.invalidate();

		mIsDragging = true;
		lastX = (int) ev.getX();
		lastY = (int) ev.getY();

		mXOffset = (int) ev.getRawX();
		mYOffset = (int) ev.getRawY();

		((ImageView) getChildAt(pointToPosition((int) ev.getX(), (int) ev.getY())).findViewById(R.id.imageView1))
				.setAlpha(255);

		startDragging();
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		mIsScrolling = true;

		if (scroll < 0 || scroll > maxScroll) {
			scrollBy(0, (int) distanceY / 2);
			scroll += distanceY / 2;
		} else {
			scroll += distanceY;
			scrollBy(0, (int) distanceY);
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onShowPress(MotionEvent e) {

		this.invalidate();
		((ImageView) getChildAt(pointToPosition((int) e.getX(), (int) e.getY())).findViewById(R.id.imageView1))
				.setAlpha(155);
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

	private int getMaxScroll() {
		int rowCount = (int) Math.ceil((double) getChildCount() / colCount);
		int maxScroll = 0;
		if (rowCount > 0) {
			maxScroll = getChildAt(0).getHeight() * rowCount;
		}

		if (maxScroll < getHeight()) {
			maxScroll = 0;
			return maxScroll;
		} else {
			maxScroll -= getHeight();
		}

		return maxScroll;
	}

	final class SmoothScrollRunnable implements Runnable {

		static final int ANIMATION_DURATION_MS = 200;
		static final int ANIMATION_FPS = 1000 / 60;

		private final Interpolator interpolator;
		private final int scrollToY;
		private final int scrollFromY;
		private final Handler handler;

		private boolean continueRunning = true;
		private long startTime = -1;
		private int currentY = -1;

		public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
			this.handler = handler;
			this.scrollFromY = fromY;
			this.scrollToY = toY;
			this.interpolator = new AccelerateDecelerateInterpolator();
		}

		@Override
		public void run() {

			/**
			 * Only set startTime if this is the first time we're starting, else
			 * actually calculate the Y delta
			 */
			if (startTime == -1) {
				startTime = System.currentTimeMillis();
			} else {

				/**
				 * We do do all calculations in long to reduce software float
				 * calculations. We use 1000 as it gives us good accuracy and
				 * small rounding errors
				 */
				long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / ANIMATION_DURATION_MS;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int deltaY = Math.round((scrollFromY - scrollToY)
						* interpolator.getInterpolation(normalizedTime / 1000f));
				this.currentY = scrollFromY - deltaY;
				// setHeaderScroll(currentY);
				scrollTo(0, currentY);
			}

			// If we're not at the target Y, keep going...
			if (continueRunning && scrollToY != currentY) {
				handler.postDelayed(this, ANIMATION_FPS);
			}
		}

		public void stop() {
			this.continueRunning = false;
			this.handler.removeCallbacks(this);
		}
	};
	
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
}
