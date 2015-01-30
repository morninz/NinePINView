package com.morninz.ninepinview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.morninz.ninepinview.widget.NinePINView;
import com.morninz.ninepinview.widget.NinePINView.Mode;
import com.morninz.ninepinview.widget.NinePINView.OnDrawListener;

public class DemoActivity extends Activity {

	TextView mTextViewResult;
	NinePINView mNinePINView;
	String mCorrectPin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_activity);
		mTextViewResult = (TextView) findViewById(R.id.result);
		mNinePINView = (NinePINView) findViewById(R.id.nine_pin_view);
		mNinePINView.setMode(Mode.MODE_STUDY);
		mNinePINView.setOnDrawListener(new OnDrawListener() {

			@Override
			public void onDrawStart(NinePINView ninePINView) {
				mTextViewResult.setText("");
			}

			@Override
			public void onDrawComplete(NinePINView ninePINView, boolean correct) {
				String drawnPIN = ninePINView.getDrawnPIN();
				Mode mode = ninePINView.getMode();
				if (mode == Mode.MODE_STUDY) {
					mTextViewResult.setText("Study Complete！" + drawnPIN);
					mCorrectPin = drawnPIN;
					ninePINView.setCorrectPIN(mCorrectPin);
					ninePINView.setMode(Mode.MODE_WORK);
				} else if (mode == Mode.MODE_WORK) {
					if (correct) {
						mTextViewResult.setText("Draw Correct！" + drawnPIN);
					} else {
						mTextViewResult.setText("Draw Wrong！" + drawnPIN);
					}
				}
			}
		});
	}
}
