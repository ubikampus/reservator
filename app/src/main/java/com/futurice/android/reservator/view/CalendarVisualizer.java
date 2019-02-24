package com.futurice.android.reservator.view;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Canvas.VertexMode;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class CalendarVisualizer extends HorizontalScrollView implements ReservatorVisualizer,
        OnTouchListener {
    TimeSpan touchedTimeSpan;
    Reservation touchedReservation;
    DateTime touchedTime;
    Shader reservationShader, leftEdgeShader, rightEdgeShader, tentativeShader;
    int textColor, weekTextColor, gridColor, reservationTextColor;
    String textFont, reservationTextFont;
    int weekStartDay = Calendar.MONDAY;
    String dayLabels[], weekLabels[];
    private Paint markerPaint, textPaint, weekTextPaint, gridPaint, tentativePaint;
    private int dayStartTime; // minutes from midnight
    private int dayEndTime;
    private DateTime firstDayToShow;
    private int daysToShow = 1;
    private int dayWidth = 250;
    private int timeLabelWidth = 70; //margin to left of time
    private Reservation[] reservations;
    private SimpleDateFormat dayLabelFormatter, weekLabelFormatter;
    private Paint fadingEdgePaint;
    private RectF calendarAreaRect, timeLabelRect, headerRect;
    private FrameLayout contentFrame;
    private TimeSpan tentativeTimeSpan;
    private Room room;
    private Context context;

    public CalendarVisualizer(Context context, int dayStartTime, int dayEndTime, int daysToShow,
        int textColor, int weekTextColor, int gridColor, int reservationTextColor,
        String textFont, String reservationTextFont) {
        super(context, null);

        this.context = context;

        this.dayStartTime = dayStartTime;
        this.dayEndTime = dayEndTime;
        this.daysToShow = daysToShow;
        timeLabelWidth = context.getResources().getDimensionPixelSize(R.dimen.timeLabelWidth);

        firstDayToShow = new DateTime().stripTime();
        //forces scroll view to have scrollable content area
        contentFrame = new FrameLayout(getContext());
        contentFrame.setClickable(true);
        contentFrame.setOnTouchListener(this);
        this.addView(contentFrame, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);


        this.textColor = textColor;
        this.weekTextColor = weekTextColor;
        this.gridColor = gridColor;
        this.reservationTextColor = getResources().getColor(reservationTextColor);
        this.textFont = textFont;
        this.reservationTextFont = reservationTextFont;

        this.textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);


        this.weekTextPaint = new Paint();
        weekTextPaint.setColor(weekTextColor);
        weekTextPaint.setAntiAlias(true);

        this.gridPaint = new Paint();
        gridPaint.setColor(gridColor);

        this.markerPaint = new Paint();
        this.reservationShader = new LinearGradient(0, 0, 1, 1, getResources().getColor(R.color.CalendarMarkerReservedColor), getResources().getColor(R.color.CalendarMarkerReservedColor), TileMode.REPEAT);
        markerPaint.setShader(reservationShader);

        this.tentativePaint = new Paint();
        this.tentativeShader = new LinearGradient(0, 0, 1, 1, getResources().getColor(R.color.CalendarMarkerTentativeColor), getResources().getColor(R.color.CalendarMarkerTentativeColor), TileMode.REPEAT);
        this.tentativePaint.setShader(tentativeShader);

        this.fadingEdgePaint = new Paint();
        this.leftEdgeShader = new LinearGradient(0, 0, 16, 0, Color.argb(128, 128, 128, 128), Color.argb(0, 0, 0, 0), TileMode.CLAMP);
        this.rightEdgeShader = new LinearGradient(0, 0, 16, 0, Color.argb(0, 0, 0, 0), Color.argb(128, 128, 128, 128), TileMode.CLAMP);

        setHorizontalFadingEdgeEnabled(false);
        this.setBackgroundColor(Color.TRANSPARENT);

        dayLabelFormatter = new SimpleDateFormat(
                getResources().getString(R.string.dateLabelFormat), Locale.getDefault());
        String weekLabelFormat = getResources().getString(R.string.weekLabelFormat);
        weekLabelFormatter = new SimpleDateFormat(weekLabelFormat, Locale.getDefault());

    }

    public Typeface getTypeFaceFromFont(Context ctx, String asset) {
        try {
            return Typeface.createFromAsset(ctx.getAssets(), asset);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized void setRoom(Room room) {
        this.room = room;
    }
    @Override
    public synchronized void setReservations(List<Reservation> reservationList) {
        long start = System.currentTimeMillis();
        this.reservations = new Reservation[reservationList.size()];
        reservationList.toArray(this.reservations);
        Arrays.sort(this.reservations);
        generateDayHeaderLabels();
        contentFrame.setPadding(Math.max(getWidth(), daysToShow * dayWidth + timeLabelWidth), 0, 0, 0);
        Log.d("Performance", "Set reservations done in " + (System.currentTimeMillis() - start) + "ms");
    }

    private void generateDayHeaderLabels() {
        if (reservations.length > 0) {
            dayLabels = new String[Math.max(
                    getDaysFromStart(reservations[reservations.length - 1]
                            .getEndTime()), daysToShow)];
            weekLabels = new String[dayLabels.length];
        } else {
            dayLabels = new String[daysToShow];
            weekLabels = new String[daysToShow];
        }

        DateTime day = getFirstDayToShow();
        for (int i = 0; i < dayLabels.length; i++) {
            dayLabels[i] = dayLabelFormatter.format(day.getTime());
            weekLabels[i] = day.get(Calendar.DAY_OF_WEEK) == weekStartDay ? weekLabelFormatter.format(day.getTime()) : null;
            day = day.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void drawTimeLabels(Canvas c, RectF area) {
        float width = area.width();
        float height = area.height();
        Align originalAlign = textPaint.getTextAlign();


        c.save();
        c.translate(getScrollX(), 0);
        //c.clipRect(area); no clipRect used. the first label goes few pixels above the top
        c.translate(area.left, area.top);
        textPaint.setTextAlign(Align.RIGHT);
        float normalTextSize = context.getResources().getDimensionPixelSize(R.dimen.timeLabelTextSize); //textPaint
        // .getTextSize();
        float smallTextSize = normalTextSize * 0.65f;
        textPaint.setTextSize(smallTextSize);
        float padding = width / 8;
        float x = width - padding;
        final String minuteStr = "00";
        float minutesWidth = textPaint.measureText(minuteStr); //minutes are drawn separately with smaller font
        for (int minutes = dayStartTime; minutes < dayEndTime; minutes += 60) {
            float timeY = getProportionalY(0, minutes) * height;
            textPaint.setTextSize(smallTextSize);
            c.drawText(minuteStr, x, timeY + smallTextSize, textPaint);
            textPaint.setTextSize(normalTextSize);
            String hoursStr = Integer.toString(minutes / 60);
            c.drawText(hoursStr, x - minutesWidth, timeY + smallTextSize, textPaint);
            c.drawLine(x + (width - x) / 3, timeY, width, timeY, gridPaint);
        }
        textPaint.setTextAlign(originalAlign);
        c.restore();
    }

    private void drawDayHeaders(Canvas c, RectF area) {
        c.save();
        c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
        c.translate(area.left, area.top);
        float textSize = area.height() / 3;
        textPaint.setTextSize(textSize);
        weekTextPaint.setTextSize(textSize * 0.7f);
        float dayLabelY = area.height() - textSize / 2;
        float weekLabelY = dayLabelY - textSize;
        for (int i = 0; i < dayLabels.length; i++) {
            float x = i * dayWidth + textSize / 2;
            if (weekLabels[i] != null) {
                c.drawText(weekLabels[i], x, weekLabelY, weekTextPaint);
            }
            c.drawText(dayLabels[i], x, dayLabelY, textPaint);
        }
        c.restore();
    }

    private void drawCalendarReservations(Canvas c, RectF area) {
        c.save();
        c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
        c.translate(area.left, area.top);
        int height = (int) area.height();
        int paddingLeft = 1;
        if (reservations.length > 0) {
            float[] points = new float[reservations.length * 8];
            short[] indices = new short[reservations.length * 6];
            for (int i = 0; i < reservations.length; i++) {
                int j = 8 * i;
                //order of points is top-left, top-right, bottom-left, bottom-right
                points[j] = getXForTime(reservations[i].getStartTime());
                points[j + 1] = getProportionalY(reservations[i].getStartTime()) * height;

                if (daysToShow == 1 ) {
                    points[j + 2] = getWidth();
                    points[j] += paddingLeft;
                }
                else
                    points[j + 2] = getXForTime(reservations[i].getStartTime()) + dayWidth;

                points[j + 3] = points[j + 1];
                points[j + 4] = points[j];
                points[j + 5] = getProportionalEndY(reservations[i].getEndTime()) * height;
                points[j + 6] = points[j + 2];
                points[j + 7] = points[j + 5];
                j += 8;
                //top-left * 2, top-right, bottom-left, bottom-right * 2
                // *2 makes reservation connecting triangles zero area
                int p = 6 * i;
                short vi = (short) (4 * i); //each reservation needs 4 vertices
                indices[p] = vi;
                indices[p + 1] = vi;
                indices[p + 2] = (short) (vi + 1);
                indices[p + 3] = (short) (vi + 2);
                indices[p + 4] = (short) (vi + 3);
                indices[p + 5] = (short) (vi + 3);
            }
            c.drawVertices(VertexMode.TRIANGLE_STRIP, points.length, points, 0,
                    points, 0, null, 0, indices, 0, indices.length, markerPaint);

            Paint linePaint = new Paint();
            // linePaint.setARGB(200, 255, 255, 255);
            linePaint.setColor(Color.WHITE);

            // Draw the separator line only if the next reservation is following this one immediately.
            for (int i = 0; i < reservations.length; i++) {
                if ((i + 1) < reservations.length && !room.isFreeAt(reservations[i].getEndTime())) {
                    //reservations[i].getEndTime().getTimeInMillis() == reservations[i + 1].getStartTime().getTimeInMillis()) {
                    int tempStopX = getXForTime(reservations[i].getStartTime()) + dayWidth;
                    if (daysToShow == 1)
                        tempStopX = getWidth();

                    c.drawLine(getXForTime(reservations[i].getStartTime()),
                            getProportionalEndY(reservations[i].getEndTime()) * height,
                            tempStopX,
                            getProportionalEndY(reservations[i].getEndTime()) * height,
                            linePaint);
                }
            }

        }
        c.restore();
    }

    private void drawTentativeArea(Canvas c, RectF area, TimeSpan timeSpan) {
        c.save();
        c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
        c.translate(area.left, area.top);
        int height = (int) area.height();

        float[] points = new float[8];
        short[] indices = new short[6];

        int j = 0;
        int paddingLeft = 1;
        //order of points is top-left, top-right, bottom-left, bottom-right
        points[j] = getXForTime(timeSpan.getStart());
        points[j + 1] = getProportionalY(timeSpan.getStart()) * height;

        if (daysToShow == 1 ) {
            points[j + 2] = getWidth();
            points[j] += paddingLeft;
        }
        else
            points[j + 2] = getXForTime(timeSpan.getStart()) + dayWidth;

        points[j + 3] = points[j + 1];
        points[j + 4] = points[j];
        points[j + 5] = getProportionalEndY(timeSpan.getEnd()) * height;
        points[j + 6] = points[j + 2];
        points[j + 7] = points[j + 5];
        j += 8;

        //top-left * 2, top-right, bottom-left, bottom-right * 2
        // *2 makes reservation connecting triangles zero area
        int p = 0;
        short vi = (short) (0); //each reservation needs 4 vertices
        indices[p] = vi;
        indices[p + 1] = vi;
        indices[p + 2] = (short) (vi + 1);
        indices[p + 3] = (short) (vi + 2);
        indices[p + 4] = (short) (vi + 3);
        indices[p + 5] = (short) (vi + 3);

        c.drawVertices(VertexMode.TRIANGLE_STRIP, points.length, points, 0,
                    points, 0, null, 0, indices, 0, indices.length, tentativePaint);

        Paint linePaint = new Paint();
        // linePaint.setARGB(200, 255, 255, 255);
        linePaint.setColor(Color.WHITE);

        c.restore();
    }

    private void drawCalendarLines(Canvas c, RectF area) {
        float height = area.height();
        c.save();
        c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
        c.translate(area.left, area.top);


        for (int i = 0; i < dayLabels.length; i++) {
                c.drawLine(i * dayWidth, 0, i * dayWidth, height, gridPaint);
        }
        for (int minutes = dayStartTime; minutes < dayEndTime; minutes += 60) {
            float y = getProportionalY(0, minutes) * height;
            c.drawLine(0, y, getWidth(), y, gridPaint);
        }

        c.restore();
    }

    private void drawReservationSubjects(Canvas c, RectF area) {
        textPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen
            .reservationSubjectTextSize));
        float textHeight = textPaint.getTextSize();
        int paddingX = context.getResources().getDimensionPixelSize(R.dimen
            .reservationSubjectPaddingX);
        int paddingY = context.getResources().getDimensionPixelSize(R.dimen
            .reservationSubjectPaddingY);
        float height = area.height();
        c.save();
        c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
        c.translate(area.left, area.top);

        textPaint.setColor(reservationTextColor);
        textPaint.setTypeface(getTypeFaceFromFont(getContext(), reservationTextFont));
        TextPaint textPaintForEllipsize = new TextPaint(textPaint);
        float previousTextY = 0;
        for (Reservation r : reservations) {
            float highlightEndY = getProportionalEndY(r.getEndTime()) * height;
            float textY = getProportionalY(r.getStartTime()) * height + textHeight + paddingY;

            if ((highlightEndY - textY) < ((textHeight/2)-4))
                continue;

            float textWidth = textPaint.measureText(r.getSubject());
            int tempAvail = 250;
            if (daysToShow == 1)
                tempAvail = getWidth();

            String subject = (String) TextUtils.ellipsize(r.getSubject(), textPaintForEllipsize, tempAvail,
                    TextUtils.TruncateAt.END);
            if (previousTextY + textHeight + paddingY < textY) {
                c.drawText(subject, getXForTime(r.getStartTime()) + paddingX,
                    getProportionalY(r.getStartTime()) * height + textHeight + paddingY, textPaint);
            } else {
                Log.d("CalendarVizualizer", "not enough distance, not drawing");
            }
            previousTextY = textY;
        }
        textPaint.setColor(textColor);
        textPaint.setTypeface(getTypeFaceFromFont(getContext(), textFont));
        c.restore();
    }

    private void drawFadingEdges(Canvas c, RectF area) {
        c.save();
        //c.clipRect(area.left + getScrollX(), area.top,  area.right + getScrollX(), area.bottom);
        c.translate(area.left, area.top);
        if (getParent() instanceof View) {
            c.translate(getScrollX(), 0);
            if (getScrollX() > 0) {
                fadingEdgePaint.setShader(leftEdgeShader);
                c.drawRect(0, 0, 16, area.height(), fadingEdgePaint);
            }
            if (getScrollX() + getWidth() < contentFrame.getWidth()) {
                c.translate(area.width() - 16, 0);
                fadingEdgePaint.setShader(rightEdgeShader);
                c.drawRect(0, 0, 16, area.height(), fadingEdgePaint);
            }
        }
        c.restore();
    }

    private void drawCurrentTimeIndicators(Canvas c, RectF area) {
        c.save();
        c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
        c.translate(area.left, area.top);
        int height = (int) area.height();

        DateTime now = new DateTime();

        int startX = getXForTime(now);
        int endX = startX + dayWidth;
        int currentY = (int) (getProportionalY(now) * height);

        Paint fillPaint = new Paint();
        fillPaint.setARGB(128, 192, 192, 192); // #C0C0C0 = semialpha grey

        // the rectangle
        if (daysToShow ==1)
            c.drawRect(startX, 0, getWidth(), currentY, fillPaint);
        else
            c.drawRect(startX, 0, endX, currentY, fillPaint);

        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(3);
        linePaint.setColor(getResources().getColor(R.color.CurrentTimeLineColor));

        // the red line
        if (daysToShow ==1)
            c.drawLine(startX, currentY, getWidth(), currentY, linePaint);
        else
            c.drawLine(startX, currentY, endX, currentY, linePaint);

        c.restore();
    }

    @Override
    protected void onDraw(Canvas c) {
        long start = System.currentTimeMillis();
        int headerHeight = Math.min(getHeight(), getWidth()) / 30;
        timeLabelRect = new RectF(0, headerHeight, timeLabelWidth, getHeight());
        calendarAreaRect = new RectF(timeLabelWidth, headerHeight, getWidth(), getHeight());
        //headerRect = new RectF(timeLabelWidth, 0, getWidth(), headerHeight);

        //drawDayHeaders(c, headerRect);
        drawCalendarLines(c, calendarAreaRect);
        drawCalendarReservations(c, calendarAreaRect);
        drawReservationSubjects(c, calendarAreaRect);
        drawFadingEdges(c, calendarAreaRect);
        drawTimeLabels(c, timeLabelRect);
        drawCurrentTimeIndicators(c, calendarAreaRect);

        if (this.tentativeTimeSpan != null)
            drawTentativeArea(c, calendarAreaRect, this.tentativeTimeSpan);

        //Log.d("Performance", "Drew CalendarVisualizer in " + (System.currentTimeMillis() - start) + "ms");
    }

    private int getDaysFromStart(DateTime day) {
        return (int) (day.getTimeInMillis() - getFirstDayToShow().getTimeInMillis()) / (60 * 60 * 24 * 1000);
        //return day.subtract(getFirstDayToShow(), Calendar.DAY_OF_YEAR);
    }

    private float getProportionalEndY(DateTime time) {
        int hours = time.get(Calendar.HOUR_OF_DAY);
        return getProportionalY(hours == 0 ? 24 : hours, time.get(Calendar.MINUTE));
    }

    private float getProportionalY(DateTime time) {
        return getProportionalY(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
    }

    private float getProportionalY(int hours, int minutes) {
        return (minutes + hours * 60 - dayStartTime) / (float) (dayEndTime - dayStartTime);
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            touchedTime = getTimeForCoordinates(e.getX(), e.getY());
            touchedReservation = getReservationForTime(touchedTime);
            if (touchedReservation != null) {
                //touched a reservation
                touchedTimeSpan = touchedReservation.getTimeSpan();
                return false;
            }
            DateTime start;
            Reservation before = findReservationBefore(touchedTime);
            if (before == null || touchedTime.stripTime().after(before.getEndTime())) {
                start = touchedTime.setTime(dayStartTime / 60, dayStartTime % 60, 0);
            } else {
                start = before.getEndTime();
            }
            DateTime end;
            Reservation after = findReservationAfter(touchedTime);
            if (after == null || after.getStartTime().stripTime().after(touchedTime)) {
                end = touchedTime.setTime(dayEndTime / 60, dayEndTime % 60, 0);
            } else {
                end = after.getStartTime();
            }
            touchedTimeSpan = new TimeSpan(start, end);
            Log.d("CalendarVisualize", "Calendar visualizer touched time: "
                    + touchedTime.toGMTString() + "\n timespan: "
                    + touchedTimeSpan.getStart().toGMTString() + "-"
                    + touchedTimeSpan.getEnd().toGMTString());
            invalidate();
            v.performClick();
        }
        return false; // do not interfere with onClick logic
    }

    private Reservation getReservationForTime(DateTime time) {
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i].getStartTime().before(time)) {
                if (reservations[i].getEndTime().after(time)) {
                    return reservations[i];
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private Reservation findReservationBefore(DateTime time) {
        Reservation latest = null;
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i].getEndTime().before(time)) {
                latest = reservations[i];
            } else {
                return latest;
            }
        }
        return null;
    }

    private Reservation findReservationAfter(DateTime time) {
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i].getStartTime().after(time)) {
                return reservations[i];
            }
        }
        return null;
    }

    private DateTime getTimeForCoordinates(float x, float y) {
        int minutes = dayStartTime + (int) ((y - calendarAreaRect.top) / calendarAreaRect.height() * (dayEndTime - dayStartTime));
        DateTime absoluteDays = firstDayToShow.add(Calendar.DAY_OF_YEAR, (int) ((x - calendarAreaRect.left) / dayWidth))
                .setTime(minutes / 60, minutes % 60, 0);
        return absoluteDays;
    }

    public DateTime getSelectedTime() {
        return touchedTime;
    }

    public TimeSpan getSelectedTimeSpan() {
        return touchedTimeSpan;
    }

    public Reservation getSelectedReservation() {
        return touchedReservation;
    }

    private DateTime getFirstDayToShow() {
        if (daysToShow == 1)
            return new DateTime().stripTime();

        if (reservations.length > 0
                && reservations[0].getStartTime().before(firstDayToShow)) {
            return reservations[0].getStartTime();
        } else {
            return firstDayToShow; // TODO some logic here now it's today by default
        }
    }

    public int getXForTime(DateTime day) {
        return getDaysFromStart(day) * dayWidth;
    }

    public void setTentativeTimeSpan(TimeSpan span) {
        this.tentativeTimeSpan = span;
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        //ScrollView does not produce onClick events, so bind the contentFrame's onClick to fake this ones onClick..
        contentFrame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                l.onClick(CalendarVisualizer.this);
            }
        });
    }

}
