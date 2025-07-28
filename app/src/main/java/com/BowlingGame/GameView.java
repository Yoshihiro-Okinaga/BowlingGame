package com.BowlingGame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private Ball ball;
    private Pin[] pins;
    private GameState gameState;

    // 描画用のPaintオブジェクト
    private Paint ballPaint;
    private Paint pinPaint;
    private Paint lanePaint;
    private Paint scoreTextPaint;

    // 定数
    private static final float LANE_WIDTH_RATIO = 0.8f; // レーンの幅の画面に対する比率
    private static final float LANE_HEIGHT_RATIO = 0.6f; // レーンの高さの画面に対する比率

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);

        // ゲームオブジェクトの初期化
        ball = new Ball();
        pins = new Pin[10]; // 10本のピンを管理
        gameState = new GameState();

        // Paintオブジェクトの初期化
        ballPaint = new Paint();
        ballPaint.setColor(Color.RED);
        ballPaint.setStyle(Paint.Style.FILL);
        ballPaint.setAntiAlias(true); // アンチエイリアス

        pinPaint = new Paint();
        pinPaint.setColor(Color.WHITE);
        pinPaint.setStyle(Paint.Style.FILL);
        pinPaint.setAntiAlias(true);
        pinPaint.setStrokeWidth(5); // ピンの縁の太さ

        lanePaint = new Paint();
        lanePaint.setColor(Color.parseColor("#8B4513")); // 木の色 (ブラウン)
        lanePaint.setStyle(Paint.Style.FILL);

        scoreTextPaint = new Paint();
        scoreTextPaint.setColor(Color.BLACK);
        scoreTextPaint.setTextSize(60); // テキストサイズ
        scoreTextPaint.setAntiAlias(true);
        scoreTextPaint.setTextAlign(Paint.Align.LEFT); // テキストの揃え方
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // ピンの初期配置は画面サイズが確定してから行う
        setupPins();
        
        gameThread = new GameThread(holder, this);
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 画面サイズ変更時の処理 (今回は特に何もしない)
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join(); // スレッドの終了を待つ
                retry = false;
            } catch (InterruptedException e) {
                // スレッド終了待ちに失敗した場合、リトライ
                Log.e("GameView", "surfaceDestroyed: " + e.getMessage());
            }
        }
    }

    // ピンを初期位置に配置するメソッド
    private void setupPins() {
        float laneCenterX = getWidth() / 2f;
        float laneTopY = getHeight() * (1 - LANE_HEIGHT_RATIO); // レーンの開始Y座標

        // ピンの基点Y座標 (レーン奥のピンを配置するY座標)
        float pinsBaseY = laneTopY + (getHeight() * LANE_HEIGHT_RATIO * 0.2f); // レーンの開始から20%くらいの位置
        float rowSpacing = Pin.PIN_HEIGHT * 0.8f; // 行ごとのY座標の間隔
        float pinSpacing = Pin.PIN_WIDTH * 1.2f; // ピンごとのX座標の間隔

        int pinIndex = 0;

        // ボーリングの標準的な10ピン配置
        // 1列目 (1本)
        pins[pinIndex++] = new Pin(laneCenterX, pinsBaseY);

        // 2列目 (2本)
        pins[pinIndex++] = new Pin(laneCenterX - pinSpacing / 2, pinsBaseY + rowSpacing);
        pins[pinIndex++] = new Pin(laneCenterX + pinSpacing / 2, pinsBaseY + rowSpacing);

        // 3列目 (3本)
        pins[pinIndex++] = new Pin(laneCenterX - pinSpacing, pinsBaseY + rowSpacing * 2);
        pins[pinIndex++] = new Pin(laneCenterX, pinsBaseY + rowSpacing * 2);
        pins[pinIndex++] = new Pin(laneCenterX + pinSpacing, pinsBaseY + rowSpacing * 2);

        // 4列目 (4本)
        pins[pinIndex++] = new Pin(laneCenterX - pinSpacing * 1.5f, pinsBaseY + rowSpacing * 3);
        pins[pinIndex++] = new Pin(laneCenterX - pinSpacing * 0.5f, pinsBaseY + rowSpacing * 3);
        pins[pinIndex++] = new Pin(laneCenterX + pinSpacing * 0.5f, pinsBaseY + rowSpacing * 3);
        pins[pinIndex++] = new Pin(laneCenterX + pinSpacing * 1.5f, pinsBaseY + rowSpacing * 3);

        // ボールの初期位置もレーンに合わせて調整
        ball.setInitialPosition(laneCenterX, getHeight() * 0.9f); // 画面下部、中央
        ball.reset();
    }


    /**
     * ゲームロジックの更新を行います。
     * ボールの移動、ピンとの衝突判定、スコア計算などを処理します。
     */
    public void update() {
        if (gameState.isGameRunning()) {
            if (!ball.isStopped()) {
                ball.update(); // ボールの位置更新

                // ピンとの衝突判定
                for (Pin pin : pins) {
                    if (pin.isStanding() && ball.collidesWith(pin)) {
                        pin.fall(); // ピンを倒す
                        // TODO: 衝突後のボールの挙動変更（弾む、方向転換など）をよりリアルにする
                    }
                }

                // ボールがピンのエリアを通過した、または停止したかを判定
                // 今回は簡易的に、ボールが特定のY座標より奥に進んだら停止とみなす
                if (ball.getY() < getHeight() * 0.2f || ball.isStopped()) {
                    ball.stop(); // ボールを完全に停止させる
                    processShotResult(); // ショット結果を処理
                }
            }
        }
    }

    // ショットの結果（倒れたピンの数）を処理し、スコアを更新し、次の状態へ移行
    private void processShotResult() {
        if (gameState.isShotProcessed()) { // 二重処理防止
            return;
        }

        int fallenPins = 0;
        for (Pin pin : pins) {
            if (!pin.isStanding()) {
                fallenPins++;
            }
        }
        gameState.scorePins(fallenPins);
        Log.d("GameView", "Fallen Pins: " + fallenPins + ", Current Score: " + gameState.getTotalScore());

        if (gameState.isFrameFinished()) {
            // フレーム終了、次のフレームへ
            resetAllPins();
            ball.reset();
            gameState.nextFrame();
        } else {
            // フレーム内の次の投球へ (2投目)
            if (gameState.getCurrentShot() == 1) { // 1投目が終わった場合
                // 倒れたピンはそのまま
                ball.reset(); // ボールのみリセット
                gameState.nextShot();
            } else { // 2投目が終わった場合
                resetAllPins();
                ball.reset();
                gameState.nextFrame();
            }
        }
        gameState.setShotProcessed(true); // ショット処理済みフラグを設定
    }

    // 全てのピンを立てた状態にリセット
    private void resetAllPins() {
        for (Pin pin : pins) {
            pin.reset();
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        // 背景色の描画
        canvas.drawColor(Color.parseColor("#ADD8E6")); // 空色 (LightBlue)

        // レーンの描画 (画面下部に配置)
        float laneTop = getHeight() * (1 - LANE_HEIGHT_RATIO);
        float laneBottom = getHeight();
        float laneLeft = getWidth() * ((1 - LANE_WIDTH_RATIO) / 2);
        float laneRight = getWidth() * ((1 + LANE_WIDTH_RATIO) / 2);
        canvas.drawRect(laneLeft, laneTop, laneRight, laneBottom, lanePaint);

        // ボールの描画
        ball.draw(canvas, ballPaint);

        // ピンの描画
        for (Pin pin : pins) {
            pin.draw(canvas, pinPaint);
        }

        // スコア表示
        canvas.drawText("Score: " + gameState.getTotalScore(), 50, 80, scoreTextPaint);
        canvas.drawText("Frame: " + gameState.getCurrentFrame() + " / 10", 50, 160, scoreTextPaint);
        canvas.drawText("Shot: " + gameState.getCurrentShot() + " / 2", 50, 240, scoreTextPaint);

        // ゲームオーバーメッセージ
        if (gameState.isGameOver()) {
            scoreTextPaint.setColor(Color.RED);
            scoreTextPaint.setTextSize(100);
            scoreTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Game Over!", getWidth() / 2f, getHeight() / 2f, scoreTextPaint);
            scoreTextPaint.setColor(Color.BLACK); // 元に戻す
            scoreTextPaint.setTextSize(60);
            scoreTextPaint.setTextAlign(Paint.Align.LEFT);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // タッチイベント処理
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameState.isWaitingForThrow() && !ball.isThrowing()) {
                // ボールが停止しており、かつ投球待ち状態の場合のみ処理
                float startX = ball.getX(); // ボールの初期X座標
                float startY = ball.getY(); // ボールの初期Y座標

                // タッチされた位置を目標点として、ボールを投げる
                // この例では、タッチしたY座標がボールのY座標より上（画面奥側）の場合に投げる
                if (event.getY() < ball.getY()) {
                    // タッチ座標とボールの現在位置から投げる方向と速度を決定
                    // ここでは、タッチしたX座標に投げるようにし、Y軸方向の速度は固定
                    float targetX = event.getX();
                    float throwSpeed = 30f; // 投球速度を調整
                    ball.throwBall(targetX, throwSpeed);
                    gameState.startThrow(); // 投球開始状態に移行
                    gameState.setShotProcessed(false); // 新しいショットが始まったのでフラグをリセット
                }
            } else if (gameState.isGameOver()) {
                // ゲームオーバー時に画面をタップしたらリスタート
                gameState.resetGame();
                resetAllPins();
                ball.reset();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
