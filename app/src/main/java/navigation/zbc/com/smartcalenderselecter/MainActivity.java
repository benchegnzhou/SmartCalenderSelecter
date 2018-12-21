package navigation.zbc.com.smartcalenderselecter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import navigation.zbc.com.smartcalenderselecter.customview.CalenderSelectDialog;
import navigation.zbc.com.smartcalenderselecter.util.GraphicUtils;

public class MainActivity extends AppCompatActivity {
    private String selectTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view1 = findViewById(R.id.tv_style1);
        InitScreenMessage();
        final TextView tv_data1 = findViewById(R.id.tv_data1);

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date(System.currentTimeMillis()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateToday = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, +21);
        final String dateThreeWeeksLater = sdf.format(calendar.getTime());


        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CalenderSelectDialog(MainActivity.this, "请选择搬运日期")  //创建dialog并初始化标题
                        .setStartTime(dateToday)            //设置起始时间
                        .setEndTime(dateThreeWeeksLater)    //设置结束时间
                        .setDefaultime(TextUtils.isEmpty(tv_data1.getText().toString().trim()) ? dateToday : tv_data1.getText().toString().trim() + " 00:00:00")  //设置默认时间，如果再次修改时间将上次的时间回传,控件将默认选中当前设置的时间
                        .SetOnSelectCallBack(new CalenderSelectDialog.OnClickCallBack() {  //设置选中回调
                            @Override
                            public void onCallClick(String selectTime) {
                                MainActivity.this.selectTime = selectTime;
                                tv_data1.setText(selectTime);
                            }
                        })
                        .show();  //调用显示

            }
        });
    }

    /**
     * 屏幕信息初始化
     */
    private void InitScreenMessage() {
        MApplication.screenWidth = GraphicUtils.getScreenWidth(this);
        MApplication.screenHeight = GraphicUtils.getScreenHeight(this);
        MApplication.density = GraphicUtils.getDensity(this);
    }
}
