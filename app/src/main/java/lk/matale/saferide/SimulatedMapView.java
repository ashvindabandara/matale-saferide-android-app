package lk.matale.saferide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * A lightweight, API-key-free live-map simulation for viva demonstrations.
 * It draws a Matale-style route and animates a school-van marker while a trip is active.
 *
 * Production replacement: Google Maps SDK / OpenStreetMap + real driver GPS updates.
 */
public class SimulatedMapView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean tripActive = false;
    private float progress = 0.18f;

    private final Runnable animator = new Runnable() {
        @Override public void run() {
            if (tripActive) {
                progress += 0.006f;
                if (progress > 0.96f) progress = 0.08f;
                invalidate();
                handler.postDelayed(this, 120);
            }
        }
    };

    public SimulatedMapView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setTripActive(boolean active) {
        this.tripActive = active;
        handler.removeCallbacks(animator);
        if (active) handler.post(animator);
        invalidate();
    }

    public void resetRoute() {
        progress = 0.18f;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(animator);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        // Soft map background.
        canvas.drawColor(Color.rgb(238, 247, 244));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(218, 237, 227));
        canvas.drawOval(new RectF(w * .04f, h * .06f, w * .43f, h * .44f), paint);
        canvas.drawOval(new RectF(w * .63f, h * .50f, w * .98f, h * .91f), paint);

        // Secondary roads.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(8));
        paint.setColor(Color.WHITE);
        canvas.drawLine(w * .08f, h * .72f, w * .93f, h * .26f, paint);
        canvas.drawLine(w * .10f, h * .26f, w * .88f, h * .76f, paint);
        canvas.drawLine(w * .34f, h * .04f, w * .42f, h * .95f, paint);

        // Main tracked school route.
        Path route = routePath(w, h);
        paint.setStrokeWidth(dp(11));
        paint.setColor(Color.rgb(45, 183, 141));
        canvas.drawPath(route, paint);
        paint.setStrokeWidth(dp(3));
        paint.setColor(Color.rgb(20, 120, 95));
        canvas.drawPath(route, paint);

        // Route endpoints.
        drawPin(canvas, w * .12f, h * .72f, "Start", Color.rgb(28, 99, 83));
        drawPin(canvas, w * .86f, h * .20f, "School", Color.rgb(235, 90, 90));

        // Place labels make the map feel local and easy to explain in a viva.
        drawLabel(canvas, w * .08f, h * .18f, "Pallepola");
        drawLabel(canvas, w * .56f, h * .64f, "Matale Town");
        drawLabel(canvas, w * .68f, h * .36f, "Ukuwela Rd");

        // Animated vehicle marker follows a simplified interpolation of the route.
        float[] p = pointForProgress(w, h, progress);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(p[0], p[1], dp(17), paint);
        paint.setColor(Color.rgb(255, 180, 48));
        canvas.drawRoundRect(new RectF(p[0]-dp(11), p[1]-dp(8), p[0]+dp(11), p[1]+dp(8)), dp(5), dp(5), paint);
        paint.setColor(Color.rgb(45, 64, 74));
        canvas.drawCircle(p[0]-dp(6), p[1]+dp(9), dp(3), paint);
        canvas.drawCircle(p[0]+dp(6), p[1]+dp(9), dp(3), paint);
    }

    private Path routePath(int w, int h) {
        Path route = new Path();
        route.moveTo(w * .12f, h * .72f);
        route.cubicTo(w * .24f, h * .58f, w * .34f, h * .70f, w * .43f, h * .53f);
        route.cubicTo(w * .52f, h * .37f, w * .64f, h * .45f, w * .72f, h * .31f);
        route.cubicTo(w * .77f, h * .25f, w * .81f, h * .23f, w * .86f, h * .20f);
        return route;
    }

    private float[] pointForProgress(int w, int h, float t) {
        // Three connected linear segments are sufficient for a smooth-looking viva demo.
        float[][] pts = {
                {w*.12f,h*.72f},{w*.43f,h*.53f},{w*.72f,h*.31f},{w*.86f,h*.20f}
        };
        float scaled = Math.max(0, Math.min(.999f, t)) * 3f;
        int seg = Math.min(2, (int) scaled);
        float local = scaled - seg;
        return new float[]{
                pts[seg][0] + (pts[seg+1][0]-pts[seg][0]) * local,
                pts[seg][1] + (pts[seg+1][1]-pts[seg][1]) * local
        };
    }

    private void drawPin(Canvas canvas, float x, float y, String text, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, y, dp(8), paint);
        drawLabel(canvas, x + dp(10), y - dp(8), text);
    }

    private void drawLabel(Canvas canvas, float x, float y, String text) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(56, 74, 80));
        paint.setTextSize(dp(11));
        paint.setFakeBoldText(true);
        canvas.drawText(text, x, y, paint);
        paint.setFakeBoldText(false);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}

