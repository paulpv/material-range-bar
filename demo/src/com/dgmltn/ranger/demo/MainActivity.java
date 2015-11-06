
package com.dgmltn.ranger.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dgmltn.ranger.demo.colorpicker.ColorPickerDialog;
import com.dgmltn.ranger.demo.colorpicker.Utils;
import com.dgmltn.ranger.internal.AbsRangeBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements
	ColorPickerDialog.OnColorSelectedListener {

	@Bind(R.id.leftIndex)
	EditText vLeftIndex;

	@Bind(R.id.rightIndex)
	EditText vRightIndex;

	@Bind(R.id.rangebar1)
	AbsRangeBar vRangeBar;


	// Sets variables to save the colors of each attribute
	private int mBarColor;

	private int mConnectingLineColor;

	private int mPinColor;
	private int mTextColor;

	private int mTickColor;

	private int mSelectorColor;


	private AbsRangeBar.ValueFormatter mIntValueFormatter = new AbsRangeBar.ValueFormatter() {
		public int mStart = 10;
		public int mInterval = 2;

		@Override
		public String getLabel(int index) {
			return Integer.toString(mStart + index * mInterval);
		}
	};

	private AbsRangeBar.ValueFormatter mFloatValueFormatter = new AbsRangeBar.ValueFormatter() {
		public float mStart = 0f;
		public float mInterval = 1f;

		@Override
		public String getLabel(int index) {
			return Float.toString(mStart + index * mInterval);
		}
	};

	private AbsRangeBar.ValueFormatter mCharValueFormatter = new AbsRangeBar.ValueFormatter() {
		public char mStart = 'A';

		@Override
		public String getLabel(int index) {
			return Character.toString((char)(mStart + index));
		}
	};

	// Saves the state upon rotating the screen/restarting the activity
	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt("BAR_COLOR", mBarColor);
		bundle.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Removes title bar and sets content view
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		vRangeBar.setValueFormatter(mIntValueFormatter);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.setStatusBarColor(getResources().getColor(R.color.primary_dark));

		// Gets the buttons references for the buttons
		final TextView barColor = (TextView) findViewById(R.id.barColor);
		final TextView connectingLineColor = (TextView) findViewById(R.id.connectingLineColor);
		final TextView pinColor = (TextView) findViewById(R.id.pinColor);
		final TextView pinTextColor = (TextView) findViewById(R.id.textColor);
		final TextView tickColor = (TextView) findViewById(R.id.tickColor);
		final TextView selectorColor = (TextView) findViewById(R.id.selectorColor);

		// Setting Index Values -------------------------------

		// Sets the display values of the indices
		vRangeBar.setOnRangeBarChangeListener(new AbsRangeBar.OnRangeBarChangeListener() {
			@Override
            public void onRangeChangeListener(AbsRangeBar rangeBar, int firstIndex, int secondIndex) {
                vLeftIndex.setText(Integer.toString(firstIndex));
                vRightIndex.setText(Integer.toString(secondIndex));
			}
		});

		// Setting Number Attributes -------------------------------

		// Sets tickCount
		final TextView tickCount = (TextView) findViewById(R.id.tickCount);
		SeekBar tickCountSeek = (SeekBar) findViewById(R.id.tickCountSeek);
		tickCountSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar tickCountSeek, int progress, boolean fromUser) {
				int count = progress + 2;
				try {
                    vRangeBar.setTickCount(count);
                } catch (IllegalArgumentException e) {
				}
				tickCount.setText("Tick Count = " + count);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		// Sets barWeight
		final TextView barWeight = (TextView) findViewById(R.id.barWeight);
		SeekBar barWeightSeek = (SeekBar) findViewById(R.id.barWeightSeek);
		barWeightSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar barWeightSeek, int progress, boolean fromUser) {
				vRangeBar.setBarWeight(progress);
				barWeight.setText("barWeight = " + progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		// Sets connectingLineWeight
		final TextView connectingLineWeight = (TextView) findViewById(R.id.connectingLineWeight);
		SeekBar connectingLineWeightSeek = (SeekBar) findViewById(R.id.connectingLineWeightSeek);
		connectingLineWeightSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar connectingLineWeightSeek, int progress,
				boolean fromUser) {
				vRangeBar.setConnectingLineWeight(progress);
				connectingLineWeight.setText("connectingLineWeight = " + progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		// Sets thumbRadius
		final TextView thumbRadius = (TextView) findViewById(R.id.thumbRadius);
		SeekBar thumbRadiusSeek = (SeekBar) findViewById(R.id.thumbRadiusSeek);
		thumbRadiusSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar thumbRadiusSeek, int progress, boolean fromUser) {
				if (progress == 0) {
					vRangeBar.setPinRadius(-1);
					thumbRadius.setText("Pin Radius = 30");
                } else {
					vRangeBar.setPinRadius(progress);
					thumbRadius.setText("Pin Radius = " + progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		// Setting Color Attributes---------------------------------

        if (false) {
            vRangeBar.setConnectingLineInverted(true);

            vRangeBar.setFirstConnectingLineColor(Color.BLUE);
            vRangeBar.setFirstPinColor(Color.BLUE);
            vRangeBar.setFirstSelectorColor(Color.BLUE);
            vRangeBar.setFirstPinTextColor(Color.WHITE);

            vRangeBar.setSecondConnectingLineColor(Color.RED);
            vRangeBar.setSecondPinColor(Color.RED);
            vRangeBar.setSecondSelectorColor(Color.RED);
            vRangeBar.setSecondPinTextColor(Color.WHITE);

            vRangeBar.setTemporaryPins(false);
        }

		// Sets barColor
		barColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				initColorPicker(Component.BAR_COLOR, mBarColor, mBarColor);
			}
		});

		// Sets connectingLineColor
		connectingLineColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				initColorPicker(Component.CONNECTING_LINE_COLOR, mConnectingLineColor,
					mConnectingLineColor);
			}
		});

		// Sets pinColor
		pinColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				initColorPicker(Component.PIN_COLOR, mPinColor, mPinColor);
			}
		});
		// Sets pinTextColor
		pinTextColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				initColorPicker(Component.TEXT_COLOR, mTextColor, mTextColor);
			}
		});
		// Sets tickColor
		tickColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				initColorPicker(Component.TICK_COLOR, mTickColor, mTickColor);
			}
		});
		// Sets selectorColor
		selectorColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				initColorPicker(Component.SELECTOR_COLOR, mSelectorColor, mSelectorColor);
			}
		});

	}

	@OnClick(R.id.enableRange)
	public void buttonEnableRange(View v) {
		vRangeBar.setRangeBarEnabled(!vRangeBar.isRangeBar());
		vLeftIndex.setEnabled(vRangeBar.isRangeBar());
	}

	@OnClick(R.id.disable)
	public void buttonDisble(View v) {
		vRangeBar.setEnabled(!vRangeBar.isEnabled());
	}

	@OnClick(R.id.buttonSetByIndex)
	public void buttonSetByIndex(View v) {
		String leftIndex = vLeftIndex.getText().toString();
		String rightIndex = vRightIndex.getText().toString();

		if (vRangeBar.isRangeBar()) {
			if (!leftIndex.isEmpty() && !rightIndex.isEmpty()) {
                vRangeBar.setFirstPinIndex(Integer.parseInt(leftIndex));
                vRangeBar.setSecondPinIndex(Integer.parseInt(rightIndex));
			}
        } else {
            if (!leftIndex.isEmpty()) {
                vRangeBar.setFirstPinIndex(Integer.parseInt(leftIndex));
			}
		}
	}

	@OnClick(R.id.buttonUseIntLabels)
	public void buttonUseIntLabels(View v) {
		vRangeBar.setValueFormatter(mIntValueFormatter);
	}

	@OnClick(R.id.buttonUseFloatLabels)
	public void buttonUseFloatLabels(View v) {
		vRangeBar.setValueFormatter(mFloatValueFormatter);
	}

	@OnClick(R.id.buttonUseCharLabels)
	public void buttonUseCharLabels(View v) {
		vRangeBar.setValueFormatter(mCharValueFormatter);
	}

	/**
	 * Sets the changed color using the ColorPickerDialog.
	 *
	 * @param component Component specifying which input is being used
	 * @param newColor  Integer specifying the new color to be selected.
	 */

	@Override
	public void onColorSelected(int newColor, Component component) {
		Log.d("Color selected", " new color = " + newColor + ",compoment = " + component);
		String hexColor = String.format("#%06X", (0xFFFFFF & newColor));

		switch (component) {
		case BAR_COLOR:
			mBarColor = newColor;
			vRangeBar.setBarColor(newColor);
			final TextView barColorText = (TextView) findViewById(R.id.barColor);
			barColorText.setText("barColor = " + hexColor);
			barColorText.setTextColor(newColor);
			break;
		case TEXT_COLOR:
			mTextColor = newColor;
			vRangeBar.setPinTextColor(newColor);
			final TextView textColorText = (TextView) findViewById(R.id.textColor);
			textColorText.setText("textColor = " + hexColor);
			textColorText.setTextColor(newColor);
			break;

		case CONNECTING_LINE_COLOR:
			mConnectingLineColor = newColor;
			vRangeBar.setConnectingLineColor(newColor);
			final TextView connectingLineColorText = (TextView) findViewById(
				R.id.connectingLineColor);
			connectingLineColorText.setText("connectingLineColor = " + hexColor);
			connectingLineColorText.setTextColor(newColor);
			break;

		case PIN_COLOR:
			mPinColor = newColor;
			vRangeBar.setPinColor(newColor);
			final TextView pinColorText = (TextView) findViewById(R.id.pinColor);
			pinColorText.setText("pinColor = " + hexColor);
			pinColorText.setTextColor(newColor);
			break;
		case TICK_COLOR:
			mTickColor = newColor;
			vRangeBar.setTickColor(newColor);
			final TextView tickColorText = (TextView) findViewById(R.id.tickColor);
			tickColorText.setText("tickColor = " + hexColor);
			tickColorText.setTextColor(newColor);
			break;
		case SELECTOR_COLOR:
			mSelectorColor = newColor;
			vRangeBar.setSelectorColor(newColor);
			final TextView selectorColorText = (TextView) findViewById(R.id.selectorColor);
			selectorColorText.setText("selectorColor = " + hexColor);
			selectorColorText.setTextColor(newColor);
			break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Initiates the colorPicker from within a button function.
	 *
	 * @param component    Component specifying which input is being used
	 * @param initialColor Integer specifying the initial color choice. *
	 * @param defaultColor Integer specifying the default color choice.
	 */
	private void initColorPicker(Component component, int initialColor, int defaultColor) {
		ColorPickerDialog colorPicker = ColorPickerDialog
			.newInstance(R.string.colorPickerTitle, Utils.ColorUtils.colorChoice(this),
				initialColor, 4, ColorPickerDialog.SIZE_SMALL, component);
		colorPicker.setOnColorSelectedListener(this);
		colorPicker.show(getFragmentManager(), "color");
	}
}
