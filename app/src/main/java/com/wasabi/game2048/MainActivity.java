package com.wasabi.game2048;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private static final String PREFS = "classic_2048_prefs";
    private static final String KEY_BEST = "best_score";

    private GameBoardView boardView;
    private TextView scoreText;
    private TextView bestText;
    private TextView statusText;
    private SharedPreferences prefs;
    private ToneGenerator tones;
    private int bestScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_BEST, 0);
        tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 42);
        buildUi();
        boardView.newGame();
    }

    private void buildUi() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(pad, pad + getStatusBarHeight(), pad, pad);
        root.setBackgroundResource(R.drawable.bg_app_gradient);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(88)));

        TextView title = new TextView(this);
        title.setText("2048");
        title.setTextColor(color(0xFF2D235C));
        title.setTextSize(48);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(title, new LinearLayout.LayoutParams(0, -1, 1));

        LinearLayout scores = new LinearLayout(this);
        scores.setOrientation(LinearLayout.HORIZONTAL);
        top.addView(scores, new LinearLayout.LayoutParams(-2, dp(64)));

        scoreText = scoreBox("分数\n0");
        scores.addView(scoreText, new LinearLayout.LayoutParams(dp(82), -1));

        bestText = scoreBox("最佳\n0");
        LinearLayout.LayoutParams bestParams = new LinearLayout.LayoutParams(dp(82), -1);
        bestParams.leftMargin = dp(8);
        scores.addView(bestText, bestParams);

        TextView tip = new TextView(this);
        tip.setText("滑动方块，相同数字会合并。到达 2048 即获胜。");
        tip.setTextColor(color(0xFF544C7A));
        tip.setTextSize(16);
        tip.setLineSpacing(dp(2), 1.1f);
        root.addView(tip, new LinearLayout.LayoutParams(-1, -2));

        statusText = new TextView(this);
        statusText.setText("准备开始");
        statusText.setTextColor(color(0xFFFF7A59));
        statusText.setTextSize(16);
        statusText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, dp(42));
        statusParams.topMargin = dp(8);
        root.addView(statusText, statusParams);

        boardView = new GameBoardView(this);
        boardView.setGameListener(new GameBoardView.GameListener() {
            @Override
            public void onScoreChanged(int score) {
                updateScore(score);
            }

            @Override
            public void onStatusChanged(String status) {
                statusText.setText(status);
                statusText.setScaleX(0.96f);
                statusText.setScaleY(0.96f);
                statusText.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
            }

            @Override
            public void onFeedback(int type) {
                playFeedback(type);
            }
        });
        LinearLayout.LayoutParams boardParams = new LinearLayout.LayoutParams(-1, 0, 1);
        boardParams.topMargin = dp(10);
        boardParams.bottomMargin = dp(18);
        root.addView(boardView, boardParams);

        Button restart = new Button(this);
        restart.setText("重新开始");
        restart.setTextSize(18);
        restart.setTextColor(Color.WHITE);
        restart.setAllCaps(false);
        restart.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        restart.setBackgroundResource(R.drawable.bg_restart_button);
        restart.setElevation(dp(4));
        restart.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            }
            return false;
        });
        restart.setOnClickListener(v -> {
            playFeedback(GameBoardView.FEEDBACK_NEW_GAME);
            boardView.newGame();
        });
        root.addView(restart, new LinearLayout.LayoutParams(-1, dp(56)));

        setContentView(root);
    }

    private TextView scoreBox(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(14);
        view.setTextColor(Color.WHITE);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setBackgroundResource(R.drawable.bg_score_box);
        view.setIncludeFontPadding(false);
        view.setElevation(dp(1));
        return view;
    }

    private void updateScore(int score) {
        if (score > bestScore) {
            bestScore = score;
            prefs.edit().putInt(KEY_BEST, bestScore).apply();
        }
        scoreText.setText("分数\n" + score);
        bestText.setText("最佳\n" + bestScore);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int getStatusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24);
    }

    private int color(int value) {
        return value;
    }

    private void playFeedback(int type) {
        if (tones == null) {
            return;
        }
        switch (type) {
            case GameBoardView.FEEDBACK_MERGE:
                tones.startTone(ToneGenerator.TONE_PROP_ACK, 55);
                break;
            case GameBoardView.FEEDBACK_WIN:
                tones.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
                break;
            case GameBoardView.FEEDBACK_GAME_OVER:
                tones.startTone(ToneGenerator.TONE_PROP_NACK, 140);
                break;
            case GameBoardView.FEEDBACK_INVALID:
                tones.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 70);
                break;
            case GameBoardView.FEEDBACK_NEW_GAME:
                tones.startTone(ToneGenerator.TONE_PROP_BEEP, 60);
                break;
            default:
                tones.startTone(ToneGenerator.TONE_PROP_BEEP, 35);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tones != null) {
            tones.release();
            tones = null;
        }
    }

    public static class GameBoardView extends View {
        static final int FEEDBACK_MOVE = 1;
        static final int FEEDBACK_MERGE = 2;
        static final int FEEDBACK_WIN = 3;
        static final int FEEDBACK_GAME_OVER = 4;
        static final int FEEDBACK_INVALID = 5;
        static final int FEEDBACK_NEW_GAME = 6;

        interface GameListener {
            void onScoreChanged(int score);
            void onStatusChanged(String status);
            void onFeedback(int type);
        }

        private static final int SIZE = 4;
        private static final long TILE_ANIMATION_MS = 210L;
        private static final long SHAKE_ANIMATION_MS = 240L;
        private final int[][] board = new int[SIZE][SIZE];
        private final boolean[][] spawned = new boolean[SIZE][SIZE];
        private final boolean[][] mergedEffect = new boolean[SIZE][SIZE];
        private final boolean[] mergeLineEffect = new boolean[SIZE];
        private final Random random = new Random();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private GameListener listener;
        private int score = 0;
        private boolean won = false;
        private boolean gameOver = false;
        private boolean moveMergedThisTurn = false;
        private long tileAnimationStart = 0L;
        private long shakeStart = 0L;
        private float downX;
        private float downY;

        public GameBoardView(Context context) {
            super(context);
            setFocusable(true);
            setClickable(true);
        }

        void setGameListener(GameListener listener) {
            this.listener = listener;
        }

        void newGame() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    board[r][c] = 0;
                }
            }
            score = 0;
            won = false;
            gameOver = false;
            clearEffects();
            addRandomTile(true);
            addRandomTile(true);
            startTileAnimation();
            notifyScore();
            notifyStatus("滑起来，冲到 2048");
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int side = Math.min(getWidth(), getHeight());
            float left = (getWidth() - side) / 2f;
            float top = (getHeight() - side) / 2f;
            float gap = dp(10);
            float boardRadius = dp(18);
            float tileSize = (side - gap * 5) / 4f;

            long now = System.currentTimeMillis();
            float shakeOffset = getShakeOffset(now);
            if (shakeOffset != 0f) {
                canvas.save();
                canvas.translate(shakeOffset, 0f);
            }

            paint.setColor(colorForBoard());
            rect.set(left, top, left + side, top + side);
            canvas.drawRoundRect(rect, boardRadius, boardRadius, paint);

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    float x = left + gap + c * (tileSize + gap);
                    float y = top + gap + r * (tileSize + gap);
                    drawTile(canvas, board[r][c], x, y, tileSize);
                }
            }
            if (shakeOffset != 0f) {
                canvas.restore();
            }
            if (isAnimating(now)) {
                postInvalidateDelayed(16);
            }
        }

        private void drawTile(Canvas canvas, int value, float x, float y, float size) {
            float scale = effectScale(value, x, y);
            float inset = (size - size * scale) / 2f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(tileColor(value));
            rect.set(x + inset, y + inset, x + size - inset, y + size - inset);
            canvas.drawRoundRect(rect, dp(12), dp(12), paint);

            if (value == 0) {
                return;
            }
            paint.setColor(value <= 4 ? color(0xFF776E65) : color(0xFFF9F6F2));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(textSizeFor(value, size));
            Paint.FontMetrics metrics = paint.getFontMetrics();
            float baseline = y + size / 2f - (metrics.ascent + metrics.descent) / 2f;
            canvas.drawText(String.valueOf(value), x + size / 2f, baseline, paint);
        }

        private float effectScale(int value, float x, float y) {
            if (value == 0) {
                return 1f;
            }
            long elapsed = System.currentTimeMillis() - tileAnimationStart;
            if (elapsed < 0 || elapsed > TILE_ANIMATION_MS) {
                return 1f;
            }
            float progress = Math.min(1f, elapsed / (float) TILE_ANIMATION_MS);
            int[] cell = cellForPoint(x, y);
            if (cell == null) {
                return 1f;
            }
            if (mergedEffect[cell[0]][cell[1]]) {
                return 1f + 0.18f * (float) Math.sin(progress * Math.PI);
            }
            if (spawned[cell[0]][cell[1]]) {
                float eased = 1f - (float) Math.pow(1f - progress, 3);
                return 0.48f + 0.52f * eased;
            }
            return 1f;
        }

        private int[] cellForPoint(float x, float y) {
            int side = Math.min(getWidth(), getHeight());
            float left = (getWidth() - side) / 2f;
            float top = (getHeight() - side) / 2f;
            float gap = dp(10);
            float tileSize = (side - gap * 5) / 4f;
            int c = (int) ((x - left - gap) / (tileSize + gap));
            int r = (int) ((y - top - gap) / (tileSize + gap));
            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) {
                return null;
            }
            return new int[]{r, c};
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                float threshold = dp(36);
                if (Math.max(Math.abs(dx), Math.abs(dy)) < threshold) {
                    return true;
                }
                if (Math.abs(dx) > Math.abs(dy)) {
                    move(dx > 0 ? Direction.RIGHT : Direction.LEFT);
                } else {
                    move(dy > 0 ? Direction.DOWN : Direction.UP);
                }
                return true;
            }
            return true;
        }

        private void move(Direction direction) {
            if (gameOver) {
                return;
            }
            clearEffects();
            moveMergedThisTurn = false;
            boolean moved = false;
            for (int i = 0; i < SIZE; i++) {
                int[] line = readLine(direction, i);
                int[] merged = mergeLine(line);
                if (!sameLine(line, merged)) {
                    moved = true;
                    writeLine(direction, i, merged);
                }
            }
            if (!moved) {
                notifyStatus("这个方向走不动，换个方向试试");
                shakeStart = System.currentTimeMillis();
                notifyFeedback(FEEDBACK_INVALID);
                invalidate();
                return;
            }
            addRandomTile(true);
            startTileAnimation();
            if (!won && hasValue(2048)) {
                won = true;
                notifyStatus("你合成了 2048，漂亮！");
                notifyFeedback(FEEDBACK_WIN);
            } else if (!canMove()) {
                gameOver = true;
                notifyStatus("游戏结束，点重新开始再来一局");
                notifyFeedback(FEEDBACK_GAME_OVER);
            } else {
                notifyStatus("继续滑动，节奏很好");
                notifyFeedback(moveMergedThisTurn ? FEEDBACK_MERGE : FEEDBACK_MOVE);
            }
            notifyScore();
            invalidate();
        }

        private int[] readLine(Direction direction, int index) {
            int[] line = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                switch (direction) {
                    case LEFT:
                        line[i] = board[index][i];
                        break;
                    case RIGHT:
                        line[i] = board[index][SIZE - 1 - i];
                        break;
                    case UP:
                        line[i] = board[i][index];
                        break;
                    case DOWN:
                        line[i] = board[SIZE - 1 - i][index];
                        break;
                }
            }
            return line;
        }

        private void writeLine(Direction direction, int index, int[] line) {
            for (int i = 0; i < SIZE; i++) {
                switch (direction) {
                    case LEFT:
                        board[index][i] = line[i];
                        mergedEffect[index][i] = mergeLineEffect[i];
                        break;
                    case RIGHT:
                        board[index][SIZE - 1 - i] = line[i];
                        mergedEffect[index][SIZE - 1 - i] = mergeLineEffect[i];
                        break;
                    case UP:
                        board[i][index] = line[i];
                        mergedEffect[i][index] = mergeLineEffect[i];
                        break;
                    case DOWN:
                        board[SIZE - 1 - i][index] = line[i];
                        mergedEffect[SIZE - 1 - i][index] = mergeLineEffect[i];
                        break;
                }
            }
        }

        private int[] mergeLine(int[] line) {
            for (int i = 0; i < SIZE; i++) {
                mergeLineEffect[i] = false;
            }
            List<Integer> values = new ArrayList<>();
            for (int value : line) {
                if (value != 0) {
                    values.add(value);
                }
            }
            List<Integer> merged = new ArrayList<>();
            List<Boolean> effects = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                int value = values.get(i);
                if (i + 1 < values.size() && value == values.get(i + 1)) {
                    int result = value * 2;
                    merged.add(result);
                    effects.add(true);
                    score += result;
                    moveMergedThisTurn = true;
                    i++;
                } else {
                    merged.add(value);
                    effects.add(false);
                }
            }
            while (merged.size() < SIZE) {
                merged.add(0);
                effects.add(false);
            }
            int[] result = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                result[i] = merged.get(i);
                mergeLineEffect[i] = effects.get(i);
            }
            return result;
        }

        private boolean sameLine(int[] a, int[] b) {
            for (int i = 0; i < SIZE; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }

        private void addRandomTile(boolean animate) {
            List<int[]> empty = new ArrayList<>();
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == 0) {
                        empty.add(new int[]{r, c});
                    }
                }
            }
            if (empty.isEmpty()) {
                return;
            }
            int[] cell = empty.get(random.nextInt(empty.size()));
            board[cell[0]][cell[1]] = random.nextFloat() < 0.9f ? 2 : 4;
            spawned[cell[0]][cell[1]] = animate;
        }

        private boolean canMove() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == 0) {
                        return true;
                    }
                    if (c + 1 < SIZE && board[r][c] == board[r][c + 1]) {
                        return true;
                    }
                    if (r + 1 < SIZE && board[r][c] == board[r + 1][c]) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasValue(int target) {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == target) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void notifyScore() {
            if (listener != null) {
                listener.onScoreChanged(score);
            }
        }

        private void notifyStatus(String status) {
            if (listener != null) {
                listener.onStatusChanged(status);
            }
        }

        private void notifyFeedback(int type) {
            if (listener != null) {
                listener.onFeedback(type);
            }
        }

        private void clearEffects() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    spawned[r][c] = false;
                    mergedEffect[r][c] = false;
                }
            }
        }

        private void startTileAnimation() {
            tileAnimationStart = System.currentTimeMillis();
        }

        private boolean isAnimating(long now) {
            return now - tileAnimationStart <= TILE_ANIMATION_MS
                    || now - shakeStart <= SHAKE_ANIMATION_MS;
        }

        private float getShakeOffset(long now) {
            long elapsed = now - shakeStart;
            if (elapsed < 0 || elapsed > SHAKE_ANIMATION_MS) {
                return 0f;
            }
            float progress = elapsed / (float) SHAKE_ANIMATION_MS;
            float wave = (float) Math.sin(progress * Math.PI * 6f);
            return wave * dp(7) * (1f - progress);
        }

        private int tileColor(int value) {
            switch (value) {
                case 0:
                    return color(0x33FFFFFF);
                case 2:
                    return color(0xFFE9F7FF);
                case 4:
                    return color(0xFFD8F3FF);
                case 8:
                    return color(0xFFFFC56E);
                case 16:
                    return color(0xFFFF9F66);
                case 32:
                    return color(0xFFFF6B8B);
                case 64:
                    return color(0xFFFF4D6D);
                case 128:
                    return color(0xFF9B8CFF);
                case 256:
                    return color(0xFF7C6BFF);
                case 512:
                    return color(0xFF43D9B8);
                case 1024:
                    return color(0xFF20C997);
                case 2048:
                    return color(0xFFFFD43B);
                default:
                    return color(0xFF2D235C);
            }
        }

        private float textSizeFor(int value, float tileSize) {
            if (value < 100) {
                return tileSize * 0.42f;
            }
            if (value < 1000) {
                return tileSize * 0.34f;
            }
            return tileSize * 0.27f;
        }

        private int dp(int value) {
            return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
        }

        private int color(int value) {
            return value;
        }

        private int colorForBoard() {
            return color(0x802D235C);
        }

        private enum Direction {
            LEFT,
            RIGHT,
            UP,
            DOWN
        }
    }
}
