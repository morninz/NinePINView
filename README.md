NinePINView是一个图像PIN码view，效果如下：

![](https://github.com/morninz/NinePINView/raw/master/sample_1.png)
![](https://github.com/morninz/NinePINView/raw/master/sample_2.png)
![](https://github.com/morninz/NinePINView/raw/master/sample_4.png)
-------------
## 属性配置
* 可以配置9个中心点的颜色和大小
* 可以配置9个圆圈的颜色、大小和粗细
* 可以配置连接线的颜色和粗细
* 可以配置错误PIN码圆圈和箭头的颜色
```xml	
<?xml version="1.0" encoding="utf-8"?>
<resources>
  	<declare-styleable name="NinePINView">
    	<!-- Define the size of 9 center points. -->
        <attr name="pointSize" format="dimension" />
        <!-- Define the radius of circle around point that finger has gone across. -->
        <attr name="circleRadius" format="dimension" />
        <!-- Define the width of line which connect the points that finger has gone across. -->
        <attr name="lineWidth" format="dimension" />
        <!-- Define the width of circle around point that finger has gone across. -->
        <attr name="circleWidth" format="dimension" />
        <!-- Define color of nine circles -->
        <attr name="circleColor" format="color" />
        <!-- Define color of nine points -->
        <attr name="pointColor" format="color" />
        <!-- Define color of the connection line-->
        <attr name="lineColor" format="color" />
        <!-- Define color of the wrong path -->
        <attr name="wrongColor" format="color" />
    </declare-styleable>
</resources>
```
## 使用方法
### 布局xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    android:background="#ffffff" >
    <TextView
        android:id="@+id/result"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        android:gravity="center"
        android:text="Draw a graphic as PIN"
        android:textColor="#000000" />
    <com.morninz.ninepinview.widget.NinePINView
        xmlns:nine="http://schemas.android.com/apk/res-auto"
        android:id="@+id/nine_pin_view"
        android:layout_width="300dp"
        android:layout_height="300dp"
        android:layout_below="@+id/result"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="30dp"
        android:background="#FF99CC"
        nine:circleRadius="30dp"
        nine:circleWidth="4dp"
        nine:lineWidth="4dp"
        nine:pointColor="#FFFFFF"
        nine:pointSize="6dp" />
</RelativeLayout>
```
### Activity
* NinePINView有两种模式：`MODE_STUDY`和`MODE_WORK`，分别表示初次设置PIN码和检验PIN码是否正确，
调用`setMode`方法设置mode。
* 调用`setCorrectPIN`设置正确的PIN码。
* 调用`setOnDrawListener`设置绘制动作的回调方法。
```java	
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
```
