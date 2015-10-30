package com.freeme.themeclub.wallpaper.util;

import android.os.Handler;
import android.os.Message;

public abstract class MoveAnimation {

    public void onFinish(int totalMoveOffset) {
    }

    public abstract void onMove(int moveStep);

    public void start(int moveOffset) {
        start(moveOffset, 10, 5);
    }

    public void start(final int moveOffset, final int animMostCnt, final int animInterval) {
        (new Handler() {
            private int mAnimCnt = 0;
            
            @Override
            public void handleMessage(Message msg) {
                int moveStep;
                do {
                    ++mAnimCnt;
                    moveStep = (moveOffset * mAnimCnt) / animMostCnt
                    			- (moveOffset * (-1 + mAnimCnt)) / animMostCnt;
                } while (moveStep == 0 && mAnimCnt < animMostCnt);
                
                if (mAnimCnt > animMostCnt) {
                    onFinish(moveOffset);
                } else {
                    if (moveStep != 0) {
                        onMove(moveStep);
                    }
                    
                    sendEmptyMessageDelayed(0, animInterval);
                }
            }
        }).sendEmptyMessage(0);
    }
}
