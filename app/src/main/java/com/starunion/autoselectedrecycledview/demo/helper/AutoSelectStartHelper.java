package com.starunion.autoselectedrecycledview.demo.helper;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;

/**
 * Discription:自动选择开始的helper
 * Created by sz on 17/10/16.
 */

public class AutoSelectStartHelper extends SnapHelper {
    private static final float INVALID_DISTANCE = 1f;

    // Orientation helpers are lazily created per LayoutManager.
    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {

        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(layoutManager,targetView,
                    getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(layoutManager, targetView,
                    getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        return out;

    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findStartView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findStartView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    /**
     * 找到第一个view 作为snapview
     *
     * @param layoutManager
     * @param horizontalHelper
     * @return
     */
    private View findStartView(RecyclerView.LayoutManager layoutManager, OrientationHelper horizontalHelper) {
        if (layoutManager instanceof LinearLayoutManager){//如果是线性的布局
            int firstPos = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
            if (firstPos == RecyclerView.NO_POSITION){//如果没有获取到第一个view
                return null;
            }

            //如果滑动到了最后就不再重新选中
            if (((LinearLayoutManager)layoutManager).findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1){
                return null;
            }

            //获取第一个view的位置
            View firstChild = layoutManager.findViewByPosition(firstPos);
            //判断位置获取要选中的view
            if (horizontalHelper.getDecoratedEnd(firstChild) >= horizontalHelper.getDecoratedMeasurement(firstChild)/2
                    && horizontalHelper.getDecoratedEnd(firstChild) > 0){
                return firstChild;
            }else {
                return layoutManager.findViewByPosition(firstPos+1);
            }
        }

        return null;
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return RecyclerView.NO_POSITION;
        }

        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        final View currentView = findSnapView(layoutManager);
        if (currentView == null) {
            return RecyclerView.NO_POSITION;
        }

        final int currentPosition = layoutManager.getPosition(currentView);
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        RecyclerView.SmoothScroller.ScrollVectorProvider vectorProvider =
                (RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager;
        // deltaJumps sign comes from the velocity which may not match the order of children in
        // the LayoutManager. To overcome this, we ask for a vector from the LayoutManager to
        // get the direction.
        PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
        if (vectorForEnd == null) {
            // cannot get a vector for the given position.
            return RecyclerView.NO_POSITION;
        }

        int vDeltaJump, hDeltaJump;
        if (layoutManager.canScrollHorizontally()) {
            hDeltaJump = estimateNextPositionDiffForFling(layoutManager,
                    getHorizontalHelper(layoutManager), velocityX, 0);
            if (vectorForEnd.x < 0) {
                hDeltaJump = -hDeltaJump;
            }
        } else {
            hDeltaJump = 0;
        }
        if (layoutManager.canScrollVertically()) {
            vDeltaJump = estimateNextPositionDiffForFling(layoutManager,
                    getVerticalHelper(layoutManager), 0, velocityY);
            if (vectorForEnd.y < 0) {
                vDeltaJump = -vDeltaJump;
            }
        } else {
            vDeltaJump = 0;
        }

        int deltaJump = layoutManager.canScrollVertically() ? vDeltaJump : hDeltaJump;
        if (deltaJump == 0) {
            return RecyclerView.NO_POSITION;
        }

        int targetPos = currentPosition + deltaJump;
        if (targetPos < 0) {
            targetPos = 0;
        }
        if (targetPos >= itemCount) {
            targetPos = itemCount - 1;
        }
        return targetPos;
    }

    /**
     * Return the child view that is currently closest to the center of this parent.
     *
     * @param layoutManager The {@link RecyclerView.LayoutManager} associated with the attached
     *                      {@link RecyclerView}.
     * @param helper The relevant {@link OrientationHelper} for the attached {@link RecyclerView}.
     *
     * @return the child view that is currently closest to the center of this parent.
     */
    private int estimateNextPositionDiffForFling(RecyclerView.LayoutManager layoutManager, OrientationHelper helper, int velocityX, int velocityY) {
        int[] distances = calculateScrollDistance(velocityX, velocityY);
        float distancePerChild = computeDistancePerChild(layoutManager, helper);
        if (distancePerChild <= 0) {
            return 0;
        }
        int distance =
                Math.abs(distances[0]) > Math.abs(distances[1]) ? distances[0] : distances[1];
        if (distance > 0) {
            return (int) Math.floor(distance / distancePerChild);
        } else {
            return (int) Math.ceil(distance / distancePerChild);
        }
    }

    /**
     * Computes an average pixel value to pass a single child.
     * <p>
     * Returns a negative value if it cannot be calculated.
     *
     * @param layoutManager The {@link RecyclerView.LayoutManager} associated with the attached
     *                      {@link RecyclerView}.
     * @param helper        The relevant {@link OrientationHelper} for the attached
     *                      {@link RecyclerView.LayoutManager}.
     *
     * @return A float value that is the average number of pixels needed to scroll by one view in
     * the relevant direction.
     */
    private float computeDistancePerChild(RecyclerView.LayoutManager layoutManager, OrientationHelper helper) {
        View minPosView = null;
        View maxPosView = null;
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return INVALID_DISTANCE;
        }

        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            final int pos = layoutManager.getPosition(child);
            if (pos == RecyclerView.NO_POSITION) {
                continue;
            }
            if (pos < minPos) {
                minPos = pos;
                minPosView = child;
            }
            if (pos > maxPos) {
                maxPos = pos;
                maxPosView = child;
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE;
        }
        int start = Math.min(helper.getDecoratedStart(minPosView),
                helper.getDecoratedStart(maxPosView));
        int end = Math.max(helper.getDecoratedEnd(minPosView),
                helper.getDecoratedEnd(maxPosView));
        int distance = end - start;
        if (distance == 0) {
            return INVALID_DISTANCE;
        }
        return 1f * distance / ((maxPos - minPos) + 1);
    }

    /**
     * 获取竖向helper
     * @param layoutManager
     * @return
     */
    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    /**
     * 获取横向helper
     * @param layoutManager
     * @return
     */
    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

    /**
     * 计算距离开始的距离
     *
     * @param targetView
     * @param helper
     * @return
     */
    private int distanceToStart(@NonNull RecyclerView.LayoutManager layoutManager,
                                View targetView, OrientationHelper helper) {
        int position = layoutManager.getPosition(targetView);
        final int childStart = helper.getDecoratedStart(targetView);
        final int containerStartGap;
        if (layoutManager.getClipToPadding() || position==0) {
            containerStartGap = helper.getStartAfterPadding();
        } else {
            containerStartGap = 0;
        }
        return childStart - containerStartGap;
    }


}
