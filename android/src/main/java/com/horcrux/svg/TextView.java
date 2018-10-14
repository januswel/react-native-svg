/*
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.view.ViewParent;

import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nullable;

import static com.horcrux.svg.TextProperties.*;

/**
 * Shadow node for virtual Text view
 */

@SuppressLint("ViewConstructor")
class TextView extends GroupView {
    String mTextLength = null;
    private String mBaselineShift = null;
    TextLengthAdjust mLengthAdjust = TextLengthAdjust.spacing;
    private AlignmentBaseline mAlignmentBaseline;
    private @Nullable ReadableArray mPositionX;
    private @Nullable ReadableArray mPositionY;
    private @Nullable ReadableArray mRotate;
    private @Nullable ReadableArray mDeltaX;
    private @Nullable ReadableArray mDeltaY;

    public TextView(ReactContext reactContext) {
        super(reactContext);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        releaseCachedPath();
    }

    @ReactProp(name = "textLength")
    public void setTextLength(Dynamic length) {
        mTextLength = getStringFromDynamic(length);
        invalidate();
    }

    @ReactProp(name = "lengthAdjust")
    public void setLengthAdjust(@Nullable String adjustment) {
        mLengthAdjust = TextLengthAdjust.valueOf(adjustment);
        invalidate();
    }

    @ReactProp(name = "alignmentBaseline")
    public void setMethod(@Nullable String alignment) {
        mAlignmentBaseline = AlignmentBaseline.getEnum(alignment);
        invalidate();
    }

    @ReactProp(name = "baselineShift")
    public void setBaselineShift(Dynamic baselineShift) {
        mBaselineShift = getStringFromDynamic(baselineShift);
        invalidate();
    }

    @ReactProp(name = "verticalAlign")
    public void setVerticalAlign(@Nullable String verticalAlign) {
        if (verticalAlign != null) {
            verticalAlign = verticalAlign.trim();
            int i = verticalAlign.lastIndexOf(' ');
            try {
                mAlignmentBaseline = AlignmentBaseline.getEnum(verticalAlign.substring(i));
            } catch (IllegalArgumentException e) {
                mAlignmentBaseline = AlignmentBaseline.baseline;
            }
            try {
                mBaselineShift = verticalAlign.substring(0, i);
            } catch (IndexOutOfBoundsException e) {
                mBaselineShift = null;
            }
        } else {
            mAlignmentBaseline = AlignmentBaseline.baseline;
            mBaselineShift = null;
        }
        invalidate();
    }

    @ReactProp(name = "rotate")
    public void setRotate(Dynamic rotate) {
        mRotate = getStringArrayFromDynamic(rotate);
        invalidate();
    }

    @ReactProp(name = "dx")
    public void setDeltaX(Dynamic deltaX) {
        mDeltaX = getStringArrayFromDynamic(deltaX);
        invalidate();
    }

    @ReactProp(name = "dy")
    public void setDeltaY(Dynamic deltaY) {
        mDeltaY = getStringArrayFromDynamic(deltaY);
        invalidate();
    }

    @ReactProp(name = "x")
    public void setPositionX(Dynamic positionX) {
        mPositionX = getStringArrayFromDynamic(positionX);
        invalidate();
    }

    @ReactProp(name = "y")
    public void setPositionY(Dynamic positionY) {
        mPositionY = getStringArrayFromDynamic(positionY);
        invalidate();
    }

    @ReactProp(name = "font")
    public void setFont(@Nullable ReadableMap font) {
        mFont = font;
        invalidate();
    }

    @Override
    void draw(Canvas canvas, Paint paint, float opacity) {
        if (opacity > MIN_OPACITY_FOR_DRAW) {
            setupGlyphContext(canvas);
            clip(canvas, paint);
            getGroupPath(canvas, paint);
            drawGroup(canvas, paint, opacity);
        }
    }

    @Override
    Path getPath(Canvas canvas, Paint paint) {
        setupGlyphContext(canvas);
        return getGroupPath(canvas, paint);
    }

    @Override
    Path getPath(Canvas canvas, Paint paint, Region.Op op) {
        return getPath(canvas, paint);
    }

    AlignmentBaseline getAlignmentBaseline() {
        if (mAlignmentBaseline == null) {
            ViewParent parent = this.getParent();
            while (parent != null) {
                if (parent instanceof TextView) {
                    TextView node = (TextView)parent;
                    final AlignmentBaseline baseline = node.mAlignmentBaseline;
                    if (baseline != null) {
                        mAlignmentBaseline = baseline;
                        return baseline;
                    }
                }
                parent = parent.getParent();
            }
        }
        if (mAlignmentBaseline == null) {
            mAlignmentBaseline = AlignmentBaseline.baseline;
        }
        return mAlignmentBaseline;
    }

    String getBaselineShift() {
        if (mBaselineShift == null) {
            ViewParent parent = this.getParent();
            while (parent != null) {
                if (parent instanceof TextView) {
                    TextView node = (TextView)parent;
                    final String baselineShift = node.mBaselineShift;
                    if (baselineShift != null) {
                        mBaselineShift = baselineShift;
                        return baselineShift;
                    }
                }
                parent = parent.getParent();
            }
        }
        return mBaselineShift;
    }

    Path getGroupPath(Canvas canvas, Paint paint) {
        pushGlyphContext();
        Path groupPath = super.getPath(canvas, paint);
        popGlyphContext();

        return groupPath;
    }

    @Override
    void pushGlyphContext() {
        boolean isTextNode = !(this instanceof TextPathView) && !(this instanceof TSpanView);
        getTextRootGlyphContext().pushContext(isTextNode, this, mFont, mPositionX, mPositionY, mDeltaX, mDeltaY, mRotate);
    }
}