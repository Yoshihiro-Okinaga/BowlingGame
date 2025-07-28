package com.BowlingGame;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // GameViewをコンテンツビューとして設定
        // この時点でGameViewのインスタンスが生成され、描画が開始されます
        setContentView(new GameView(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // アプリが一時停止されたときにゲームループを停止する処理が必要な場合に追加
        // 例: if (gameView != null) gameView.getThread().setRunning(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // アプリが再開されたときにゲームループを再開する処理が必要な場合に追加
        // 例: if (gameView != null) gameView.getThread().setRunning(true);
    }
}
