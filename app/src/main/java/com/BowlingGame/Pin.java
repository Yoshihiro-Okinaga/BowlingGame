package com.BowlingGame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;

public class Pin {
    private float x; // ピンのX座標 (中心)
    private float y; // ピンのY座標 (中心)
    private boolean isStanding; // ピンが立っているかどうかのフラグ

    // 定数
    public static final float PIN_WIDTH = 30; // ピンの幅 (描画用)
    public static final float PIN_HEIGHT = 100; // ピンの高さ (描画用)
    public static final float PIN_RADIUS = 15; // 衝突判定用の半径 (簡易的に円とみなす場合)

    public Pin(float x, float y) {
        this.x = x;
        this.y = y;
        this.isStanding = true; // 最初は立っている状態
    }

    /**
     * ピンを描画します。
     * 立っているピンと倒れているピンで描画方法を分けることができます。
     * @param canvas 描画対象のCanvas
     * @param paint 描画に使うPaintオブジェクト
     */
    public void draw(Canvas canvas, Paint paint) {
        if (isStanding) {
            // 立っているピンを白で描画
            paint.setColor(Color.WHITE);
            // 簡易的に縦長の長方形として描画
            canvas.drawRect(x - PIN_WIDTH / 2, y - PIN_HEIGHT / 2, x + PIN_WIDTH / 2, y + PIN_HEIGHT / 2, paint);
        } else {
            // 倒れているピンは描画しないか、異なる表現をする
            // この例では、倒れたピンは描画しない (見えなくなる)
            // あるいは、半透明にするなどの表現も可能
            // paint.setAlpha(100); // 半透明にする例
            // canvas.drawRect(x - PIN_WIDTH / 2, y - PIN_HEIGHT / 2, x + PIN_WIDTH / 2, y + PIN_HEIGHT / 2, paint);
            // paint.setAlpha(255); // 元に戻す
        }
    }

    /**
     * ピンを倒します。
     */
    public void fall() {
        this.isStanding = false;
        // TODO: ピンが倒れるアニメーションなどをここに追加することも可能
    }

    /**
     * ピンを立てた状態にリセットします。
     */
    public void reset() {
        this.isStanding = true;
    }

    /**
     * ピンが立っているかどうかを返します。
     * @return 立っていればtrue、そうでなければfalse
     */
    public boolean isStanding() {
        return isStanding;
    }

    // --- Getterメソッド ---
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
