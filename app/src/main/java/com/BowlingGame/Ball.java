package com.BowlingGame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class Ball {
    private float x; // ボールの中心X座標
    private float y; // ボールの中心Y座標
    private float radius; // ボールの半径
    private float velocityX; // X方向の速度
    private float velocityY; // Y方向の速度
    private boolean isStopped; // ボールが停止しているかどうかのフラグ
    private boolean isThrowing; // ボールが投げられている最中かどうかのフラグ

    // 初期位置 (GameViewで画面サイズに合わせて設定されるべき)
    private float initialX;
    private float initialY;

    // 定数
    private static final float DEFAULT_RADIUS = 40; // ボールのデフォルト半径
    private static final float FRICTION_FACTOR = 0.98f; // 摩擦による速度減衰率
    private static final float STOP_THRESHOLD = 0.5f; // 停止とみなす速度の閾値
    private static final float MAX_VELOCITY_Y = 50.0f; // Y方向の最大速度 (投球速度の制限)

    public Ball() {
        this.radius = DEFAULT_RADIUS;
        reset(); // 初期状態にリセット
    }

    // GameViewから初期位置を設定するためのセッター
    public void setInitialPosition(float x, float y) {
        this.initialX = x;
        this.initialY = y;
    }

    /**
     * ボールの位置と状態を更新します。
     * 摩擦や壁との衝突などを考慮して速度を減衰させます。
     */
    public void update() {
        if (isStopped) {
            return;
        }

        // 速度に基づいて位置を更新
        x += velocityX;
        y += velocityY;

        // 摩擦による速度の減衰
        velocityX *= FRICTION_FACTOR;
        velocityY *= FRICTION_FACTOR;

        // ある程度速度が遅くなったら停止とみなす
        if (Math.abs(velocityX) < STOP_THRESHOLD && Math.abs(velocityY) < STOP_THRESHOLD) {
            isStopped = true;
            isThrowing = false;
            velocityX = 0;
            velocityY = 0;
            Log.d("Ball", "Ball stopped at (" + x + ", " + y + ")");
        }

        // TODO: レーンの左右の壁との衝突判定と反射処理を追加
        // レーンの幅はGameViewで定義されているので、それを参照するか、GameViewから情報を渡す
        // 例: if (x - radius < laneLeft || x + radius > laneRight) { velocityX *= -1; }
    }

    /**
     * ボールを描画します。
     * @param canvas 描画対象のCanvas
     * @param paint 描画に使うPaintオブジェクト
     */
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawCircle(x, y, radius, paint);
    }

    /**
     * ボールを投げる処理を開始します。
     * @param targetX ボールを投げる目標のX座標
     * @param initialSpeedY Y方向への初速
     */
    public void throwBall(float targetX, float initialSpeedY) {
        // ボールの初期位置から目標X座標への方向ベクトルを計算
        float dx = targetX - initialX;
        float dy = -initialSpeedY; // Y軸は上方向がマイナス

        // 速度ベクトルの正規化と初速の設定
        float angle = (float) Math.atan2(dy, dx); // 角度を計算
        // X方向の速度は目標Xまでの距離に比例させる（簡易的）
        // Y方向の速度は一定の初速で、かつ奥へ進むようにする
        velocityX = (dx / (initialY - (initialY / 2))) * MAX_VELOCITY_Y * 0.5f; // X方向の速度を調整
        velocityY = -initialSpeedY; // Y方向は奥へ進む (画面上方向がマイナス)

        // 速度の上限設定
        if (Math.abs(velocityY) > MAX_VELOCITY_Y) {
            velocityY = (velocityY > 0 ? 1 : -1) * MAX_VELOCITY_Y;
        }
        if (Math.abs(velocityX) > MAX_VELOCITY_Y * 0.5f) { // X方向はY方向より遅めに制限
            velocityX = (velocityX > 0 ? 1 : -1) * MAX_VELOCITY_Y * 0.5f;
        }

        isStopped = false;
        isThrowing = true;
        Log.d("Ball", "Ball thrown: vx=" + velocityX + ", vy=" + velocityY);
    }

    /**
     * 指定されたピンとボールが衝突しているかどうかを判定します。
     * @param pin 判定対象のPinオブジェクト
     * @return 衝突していればtrue、そうでなければfalse
     */
    public boolean collidesWith(Pin pin) {
        if (!pin.isStanding()) {
            return false; // 倒れているピンとは衝突しない
        }

        // ボール（円）とピン（円として扱う）の衝突判定
        // 二つの円の中心間の距離がそれぞれの半径の合計よりも小さければ衝突
        float distanceX = x - pin.getX();
        float distanceY = y - pin.getY();
        float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        return distance < (radius + Pin.PIN_RADIUS);
    }

    /**
     * ボールを初期位置にリセットし、停止状態にします。
     */
    public void reset() {
        this.x = initialX;
        this.y = initialY;
        this.velocityX = 0;
        this.velocityY = 0;
        this.isStopped = true;
        this.isThrowing = false;
        Log.d("Ball", "Ball reset to (" + initialX + ", " + initialY + ")");
    }

    /**
     * ボールを強制的に停止させます。
     */
    public void stop() {
        this.isStopped = true;
        this.isThrowing = false;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    // --- Getterメソッド ---
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public boolean isStopped() { return isStopped; }
    public boolean isThrowing() { return isThrowing; }
}
