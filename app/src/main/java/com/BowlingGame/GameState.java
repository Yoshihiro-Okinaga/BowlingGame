package com.BowlingGame;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private int currentFrame; // 現在のフレーム数 (1-10)
    private int currentShot; // 現在の投球回数 (1 or 2, 10フレーム目は最大3)
    private int totalScore; // 合計スコア

    // 各フレームのスコアを格納するリスト (例: 10フレーム + ボーナスフレーム用)
    private List<FrameScore> frameScores;

    private boolean isWaitingForThrow; // 投球待ち状態
    private boolean isGameRunning; // ゲームが進行中か
    private boolean isGameOver; // ゲームが終了したか
    private boolean shotProcessed; // 現在のショットの結果が処理済みか

    private static final int MAX_FRAMES = 10;

    public GameState() {
        resetGame();
    }

    /**
     * ゲーム全体を初期状態にリセットします。
     */
    public void resetGame() {
        currentFrame = 1;
        currentShot = 1;
        totalScore = 0;
        frameScores = new ArrayList<>();
        for (int i = 0; i < MAX_FRAMES; i++) {
            frameScores.add(new FrameScore());
        }
        isWaitingForThrow = true;
        isGameRunning = true;
        isGameOver = false;
        shotProcessed = false;
        Log.d("GameState", "Game reset.");
    }

    /**
     * 投球開始状態に設定します。
     */
    public void startThrow() {
        isWaitingForThrow = false;
        Log.d("GameState", "Throw started.");
    }

    /**
     * 倒れたピンの数に基づいてスコアを計算し、現在のフレームに記録します。
     * ストライク、スペアの処理もここで行います。
     * @param fallenPins 倒れたピンの数
     */
    public void scorePins(int fallenPins) {
        if (!isGameRunning || isGameOver) {
            return;
        }

        FrameScore currentFrameScore = frameScores.get(currentFrame - 1);
        currentFrameScore.addShot(fallenPins);
        Log.d("GameState", "Frame " + currentFrame + ", Shot " + currentShot + ": " + fallenPins + " pins fallen.");

        // スコア計算ロジック（簡易版）
        // ストライクやスペアのボーナス計算は複雑になるため、ここでは基本的な加算のみ
        // 実際のボーリングゲームでは、次の投球結果を参照してボーナスを加算する必要があります。

        // 未処理のストライクやスペアに対するボーナスを加算する
        // 過去のフレームを遡ってボーナスを計算
        for (int i = 0; i < currentFrame; i++) {
            FrameScore frame = frameScores.get(i);
            if (!frame.isScored()) {
                if (frame.isStrike()) {
                    // ストライクボーナス (次の2投分)
                    if (currentFrameScore.getShotCount() >= 2 || (currentFrameScore.getShotCount() == 1 && currentFrame == i + 1)) { // 次のフレームの2投、または現在のフレームの1投目
                        int bonus = 0;
                        if (currentFrame == i + 1) { // 連続ストライクの場合、現在のフレームの1投目
                            bonus = currentFrameScore.getShot1();
                            if (currentFrameScore.getShotCount() >= 2) { // 2投目もあればそれも加算
                                bonus += currentFrameScore.getShot2();
                            }
                        } else if (currentFrame == i + 2) { // 2フレーム後の投球の場合
                            bonus = currentFrameScore.getShot1() + currentFrameScore.getShot2();
                        }
                        frame.addBonus(bonus);
                        frame.markScored(); // ボーナスが確定したらスコア済みとする
                    }
                } else if (frame.isSpare()) {
                    // スペアボーナス (次の1投分)
                    if (currentFrame == i + 1 && currentFrameScore.getShotCount() >= 1) {
                        frame.addBonus(currentFrameScore.getShot1());
                        frame.markScored();
                    }
                }
            }
        }

        // 各フレームの合計スコアを計算し、累積スコアを更新
        totalScore = 0;
        for (FrameScore fs : frameScores) {
            if (fs.isScored()) { // スコアが確定しているフレームのみ加算
                totalScore += fs.getFrameTotal();
            }
        }
        Log.d("GameState", "Total Score: " + totalScore);
    }

    /**
     * 次の投球、または次のフレームへ移行します。
     */
    public void nextShot() {
        currentShot++;
        isWaitingForThrow = true; // 次の投球を待つ状態に
        Log.d("GameState", "Next shot: " + currentShot);
    }

    /**
     * 現在のフレームが終了したかどうかを判定します。
     * @return フレームが終了していればtrue
     */
    public boolean isFrameFinished() {
        FrameScore currentFrameScore = frameScores.get(currentFrame - 1);

        if (currentFrame == MAX_FRAMES) {
            // 10フレーム目
            if (currentFrameScore.isStrike() || currentFrameScore.isSpare()) {
                // ストライクまたはスペアの場合、3投目まで可能
                return currentShot >= 3;
            } else {
                // それ以外は2投で終了
                return currentShot >= 2;
            }
        } else {
            // 1～9フレーム目
            return currentFrameScore.isStrike() || currentFrameScore.isSpare() || currentShot >= 2;
        }
    }

    /**
     * 次のフレームへ移行します。ゲームが終了したかもチェックします。
     */
    public void nextFrame() {
        if (currentFrame < MAX_FRAMES) {
            currentFrame++;
            currentShot = 1;
            isWaitingForThrow = true;
            Log.d("GameState", "Next frame: " + currentFrame);
        } else {
            // 全てのフレームが終了したらゲームオーバー
            isGameRunning = false;
            isGameOver = true;
            Log.d("GameState", "Game Over!");
        }
    }

    // --- Getterメソッド ---
    public int getCurrentFrame() { return currentFrame; }
    public int getCurrentShot() { return currentShot; }
    public int getTotalScore() { return totalScore; }
    public boolean isWaitingForThrow() { return isWaitingForThrow; }
    public boolean isGameRunning() { return isGameRunning; }
    public boolean isGameOver() { return isGameOver; }
    public void setShotProcessed(boolean processed) { this.shotProcessed = processed; }
    public boolean isShotProcessed() { return shotProcessed; }

    // --- 内部クラス: FrameScore ---
    // 各フレームのスコアを管理する
    private static class FrameScore {
        private int shot1 = -1; // 1投目の倒したピンの数
        private int shot2 = -1; // 2投目の倒したピンの数
        private int shot3 = -1; // 10フレーム目の3投目
        private int bonus = 0;  // ストライク/スペアのボーナス
        private boolean scored = false; // スコアが確定したか

        public void addShot(int pins) {
            if (shot1 == -1) {
                shot1 = pins;
            } else if (shot2 == -1) {
                shot2 = pins;
            } else if (shot3 == -1) { // 10フレーム目のみ
                shot3 = pins;
            }
        }

        public int getShot1() { return shot1; }
        public int getShot2() { return shot2; }
        public int getShot3() { return shot3; }

        public boolean isStrike() {
            return shot1 == 10;
        }

        public boolean isSpare() {
            return shot1 != -1 && shot2 != -1 && (shot1 + shot2 == 10);
        }

        public int getFrameTotal() {
            int total = 0;
            if (shot1 != -1) total += shot1;
            if (shot2 != -1) total += shot2;
            if (shot3 != -1) total += shot3; // 10フレーム目のみ
            return total + bonus;
        }

        public void addBonus(int b) {
            this.bonus += b;
        }

        public boolean isScored() { return scored; }
        public void markScored() { this.scored = true; }
        public int getShotCount() {
            if (shot3 != -1) return 3;
            if (shot2 != -1) return 2;
            if (shot1 != -1) return 1;
            return 0;
        }
    }
}
