package edu.tamu.haptigo;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//This custom class was implemented to facilitate testing of the HaptiGo Nav. System
@SuppressLint("DrawAllocation")
public class SurfaceControlView extends View{
	private float xValue,yValue, 
	xMin,xMax,xRange,
	yMin,yMax,yRange;
	String xLabel,yLabel;

	List<ValuesChangeListener> listeners = new ArrayList<ValuesChangeListener>();


	public SurfaceControlView(Context context) {
		super(context);
		init();
	}
	public SurfaceControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	private void init(){
	}
	public void setXRange(float xMin, float xMax){
		this.xMax = xMax;
		this.xMin = xMin;
		this.xRange = xMax - xMin;
	}
	public void setYRange(float yMin, float yMax){
		this.yMin = yMin;
		this.yMax = yMax;
		this.yRange = yMax - yMin;
	}
	public void setXLabel(String xLabel){
		this.xLabel = xLabel;
	}
	public void setYLabel(String yLabel){
		this.yLabel = yLabel;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event){
		
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
		float w = this.getWidth();
		//float w2 = w/2;
		float h = this.getHeight();
		//float h2 = h/2;

		float xPos = event.getX();
		if(xPos<0)
			xPos = 0;
		else if(xPos > w)
			xPos = w;
		float yPos = event.getY();
		if(yPos<0)
			yPos = 0;
		if(yPos > h)
			yPos = h;

		setValues(
				xMin + (xPos/w)*xRange,
				yMin + (yPos/h)*yRange);
		}
		return true;
	}
	public void setValues(float xValue, float yValue){
		//float w = this.getWidth();
		//float w2 = w/2;
		//float h = this.getHeight();
		//float h2 = h/2;
		if(xValue < xMin)
			xValue = xMin;
		else if(xValue > xMax)
			xValue = xMax;
		if(yValue < yMin)
			yValue = yMin;
		else if(yValue > yMax)
			yValue = yMax;
		this.xValue = xValue;
		this.yValue = yValue;
		this.fireValuesChange();
		this.invalidate();
	}
	public float getXValue(){
		return xValue;
	}
	public float getYValue(){
		return yValue;
	}
	private void fireValuesChange(){
		for(ValuesChangeListener listener:listeners){
			listener.onValuesChanged(xValue, yValue);
		}
	}
	public void addValuesChangeListener(ValuesChangeListener listener){
		listeners.add(listener);
	}
	public interface ValuesChangeListener{
		public void onValuesChanged(float xValue, float yValue);
	}

	@Override
	public void onDraw(Canvas c){
		if(!isInEditMode()){
		float w = this.getWidth();
		float w2 = w/2;
		float h = this.getHeight();
		//float h2 = h/2;
		float r = 20;
		float xPos = ((xValue - xMin)/xRange)*w;
		float yPos = ((yValue - yMin)/yRange)*h;
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(3);
		paint.setStyle(Style.STROKE);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(12);
		float textSize = paint.getTextSize();
		c.drawColor(Color.GRAY);

		c.drawCircle(xPos, yPos, r, paint);

		c.drawLine(0, yPos, xPos-r, yPos, paint);
		c.drawLine(xPos, 0, xPos, yPos-r, paint);

		paint.setStrokeWidth(1);
		paint.setStyle(Style.FILL_AND_STROKE);

		if(xPos>w2){
			paint.setTextAlign(Align.RIGHT);
			c.drawText(xLabel, xPos-r*3, yPos-(textSize)/2, paint);
		}
		else{
			paint.setTextAlign(Align.LEFT);
			c.drawText(xLabel, xPos+r*3, yPos-(textSize)/2, paint);
		}


		if(xPos>w2){
			paint.setTextAlign(Align.RIGHT);
			c.drawText(yLabel, xPos-(textSize)/2, yPos/2, paint);
		}
		else{
			paint.setTextAlign(Align.LEFT);
			c.drawText(yLabel, xPos+(textSize)/2, yPos/2, paint);
		}

	}
	}
}
