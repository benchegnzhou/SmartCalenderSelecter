package navigation.zbc.com.smartcalenderselecter.customview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import navigation.zbc.com.smartcalenderselecter.R;
import navigation.zbc.com.smartcalenderselecter.util.LogUtil;
import navigation.zbc.com.smartcalenderselecter.util.Util;

import static navigation.zbc.com.smartcalenderselecter.util.Util.stringToLong;


/**
 * Created by benchengzhou on 2018/1/30  10:42 .
 * 作者邮箱： mappstore@163.com
 * 功能描述： 时间日历选择 ，选择格式2018年10月12日
 * 类    名： CalenderSelectDialog
 * 备    注：
 * <p>
 * 使用说明： 1.创建对象
 * <p>
 * 使用示例
 * new CalenderSelectDialog(this, "请选择搬运日期")
 * .setStartTime(dateToday)
 * .setEndTime(dateThreeWeeksLater)
 * .setDefaultime(TextUtils.isEmpty(tvSelectDate.getText().toString().trim())?dateToday:tvSelectDate.getText().toString().trim()+" 00:00:00")
 * .SetOnSelectCallBack(new CalenderSelectDialog.OnClickCallBack() {
 *
 * @Override public void onCallClick(String selectTime) {
 * tvSelectDate.setText(selectTime.substring(0, 10));
 * outDate = selectTime;
 * }
 * })
 * .show();
 */

public class CalenderSelectDialog extends Dialog {


    private OnClickCallBack mOnClickCallBack;
    private final Context mContext;
    public static final String DEFAULT_TITLE = "选择时间";
    private Disposable disposable;

    private TextView tvCancle;
    private TextView tvConfirm;
    private WheelView wvYear;
    private WheelView wvMonth;
    private WheelView wvDay;
    private TextView tvTitle;
    private String mTitle;
    private int currentYear = -1;
    private int currentMonth = -1;
    private int currentDay = -1;


    String startTime;
    String endTime;
    String defaultTime;


    ArrayList<String> yearList = new ArrayList<>();
    ArrayList<String> monthList = new ArrayList<>();
    ArrayList<String> dayList = new ArrayList<>();


    /**
     * 创建的时候直接的将项目的信息传入
     *
     * @param context
     * @param title
     */
    public CalenderSelectDialog(Context context, String title) {
        super(context, R.style.TimeSelectDialog);
        mContext = context;
        this.mTitle = title;
        if (TextUtils.isEmpty(startTime)) {
            startTime = (Util.getYearNow() - 5) + Util.getDateStr().substring(5);//往前推5年
        }
        if (TextUtils.isEmpty(endTime)) {
            startTime = (Util.getYearNow() + 10) + Util.getDateStr().substring(5); //往后推10年
        }
    }


    /**
     * 时间设置
     * 需在dialog显示之前调用
     *
     * @param startTime   格式必须是2018-10-10 20:20:10
     * @param endTime
     * @param defaultTime
     */
    public CalenderSelectDialog setDataTime(String startTime, String endTime, String defaultTime) {

        setStartTime(startTime);
        setEndTime(endTime);
        setDefaultime(defaultTime);
        return this;
    }

    /**
     * 限定开始时间的设置
     * 需在dialog显示之前调用
     *
     * @param startTime
     */
    public CalenderSelectDialog setStartTime(String startTime) {
        try {  //格式化检查
            stringToLong(startTime, "yyyy-MM-dd HH:mm:ss");
            this.startTime = startTime;
        } catch (ParseException e) {
            throw new IllegalArgumentException("the startTime time is invalude!");
        }
        return this;
    }

    /**
     * 限定结束时间的设置
     * 需在dialog显示之前调用
     *
     * @param endTime
     */
    public CalenderSelectDialog setEndTime(String endTime) {
        try {  //格式化检查
            stringToLong(endTime, "yyyy-MM-dd HH:mm:ss");
            this.endTime = endTime;
        } catch (ParseException e) {
            throw new IllegalArgumentException("the endTime time is invalude!");
        }
        return this;
    }

    /**
     * 设置默认时间
     * 需在dialog显示之前调用
     *
     * @param defaultTime
     */
    public CalenderSelectDialog setDefaultime(String defaultTime) {


        if (!TextUtils.isEmpty(defaultTime)) {
            try {
                long defaultTimeL = stringToLong(defaultTime, "yyyy-MM-dd HH:mm:ss");
                long startTimeL = stringToLong(startTime, "yyyy-MM-dd HH:mm:ss");
                long endTimeL = stringToLong(endTime, "yyyy-MM-dd HH:mm:ss");
                if (defaultTimeL > startTimeL && defaultTimeL < endTimeL) {
                    this.defaultTime = defaultTime;
                } else {
                    this.defaultTime = startTime;
                    LogUtil.e("the default time must betowm startTime and endTime!");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("the default time is invalude!");
            }
        }
        initDef();
        return this;
    }


    /**
     * 初始化参数年、月、日参数
     */
    private void initDef() {
        currentYear = Integer.parseInt(defaultTime.substring(0, 4));
        currentMonth = Integer.parseInt(defaultTime.substring(5, 7));
        currentDay = Integer.parseInt(defaultTime.substring(8, 10));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_data_select);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        wvYear = (WheelView) findViewById(R.id.wheelview_year);
        wvMonth = (WheelView) findViewById(R.id.wheelview_month);
        wvDay = (WheelView) findViewById(R.id.wheelview_day);


        tvCancle = (TextView) findViewById(R.id.tv_cancel);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);

        //dialog所在的屏幕
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();//获取屏幕中的属性
        attributes.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;//设置屏幕中的控件的位置
        window.setAttributes(attributes);//设置新的属性，将原有的属性效果覆盖
        //不能通过window设置去除标题栏和边框操作，根据源码提示需要通过styles.xml设置
        initListener();
        initData();
    }

    private void initListener() {
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (disposable != null) {
                    disposable.dispose();
                }
            }
        });
        tvCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mOnClickCallBack != null) {
                    mOnClickCallBack.onCallClick(Util.getTimeStr(currentYear, currentMonth, currentDay));
                    dismiss();
                }
            }
        });
    }


    /**
     * 纠偏默认显示时间
     * 如果默认显示时间没有在范围内
     * 时间将会重置，默认优先显示今天，
     * 如果今天也不再范围内默认显示startTime
     */
    private void initDefaultTime() {
        if (!TextUtils.isEmpty(defaultTime)) {
            try {
                long defaultTimeL = stringToLong(defaultTime, "yyyy-MM-dd HH:mm:ss");
                long startTimeL = stringToLong(startTime, "yyyy-MM-dd HH:mm:ss");
                long endTimeL = stringToLong(endTime, "yyyy-MM-dd HH:mm:ss");
                if (defaultTimeL > startTimeL && defaultTimeL < endTimeL) {
                    return;
                } else {
                    defaultTime = startTime;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 显示时间控件，如果默认显示时间没有在范围内
     * 时间将会重置，默认优先显示今天，
     * 如果今天也不再范围内默认显示startTime
     */
    public void displayTime() {
        //执行玩这个方法，defaultTime就有默认值了
        initDefaultTime();
        initDef();
    }


    private void initData() {
        tvTitle.setText(mTitle);
        initWheelviewData();
        seletTime();
    }


    /**
     * 初始化年月日
     */
   /* private void initCurrentData() {
        currentYear = Util.getYearNow();
        currentMonth = Util.getMonthNow();
        currentDay = Util.getDayNow();
    }
*/

    /**
     * 回调监听设置
     *
     * @param onClickCallBack
     * @return
     */
    public CalenderSelectDialog SetOnSelectCallBack(OnClickCallBack onClickCallBack) {
        mOnClickCallBack = onClickCallBack;
        return this;
    }


    /**
     * 确定
     */
    public interface OnClickCallBack {
        /**
         * @param selectTime 当前选中的时间
         */
        void onCallClick(String selectTime);
    }

    /**
     * 让后面的文字显示颜色给定的颜色，默认色是酒红色
     *
     * @param string
     * @param color
     * @return
     */
    private CharSequence showTextWithColor(String string, int color) {
        SpannableString ss = new SpannableString(string);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(0xFFFD5056);   //红色前景色
        ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(color);  //默认色

        int end = string.indexOf(":") + 1;

        ss.setSpan(colorSpan, 0, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(colorSpan2, end, string.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

        return ss;
    }


    /**
     * 时间选择器
     */
    private void seletTime() {
        //初始化数据
        initWheelviewData();


        //        设置偏移量
        wvYear.setOffset(1);
        //        设置数据内容
        wvYear.setItems(yearList);
        for (int i = 0; i < yearList.size(); i++) {
            if (currentYear == Integer.parseInt(yearList.get(i))) {
                wvYear.setSeletion(i);
                break;
            }
        }

        // 滚动监听
        wvYear.setOnWheelViewListener(new WheelView.OnWheelViewListener() {

            @Override
            public void onSelected(int selectedIndex, String item) {
                currentYear = Integer.parseInt(item);
                /**
                 * 必须先初始化月在初始化日
                 */
                wvMonthReloadAndScrollCurrent();
                wvDayReloadAndScrollCurrent();

            }
        });


        //        设置偏移量
        wvMonth.setOffset(1);
        //        设置数据内容
        wvMonth.setItems(monthList);
        ScrollMonthCurrent();
        //        wvMonth.setSeletion(0);
        //        滚动监听
        wvMonth.setOnWheelViewListener(new WheelView.OnWheelViewListener() {


            @Override
            public void onSelected(int selectedIndex, String item) {
                int currentSelectedMonthInde = selectedIndex;
                currentMonth = Integer.parseInt(item);

                wvDayReloadAndScrollCurrent();
            }
        });


        //    设置偏移量
        wvDay.setOffset(1);

        //    设置数据内容
        wvDay.setItems(dayList);
        ScrollDayCurrent();
        //   wvDay.setSeletion(0);
        //   滚动监听
        wvDay.setOnWheelViewListener(new WheelView.OnWheelViewListener() {


            @Override
            public void onSelected(int selectedIndex, String item) {
                int currentSelectedMonthInde = selectedIndex;
                currentDay = Integer.parseInt(item);
            }
        });
    }


    /**
     * 根据当前选中的年份和月份以及设置的startTime和endTime准备数据、填充、最终滚动到当前位置
     */
    private void wvDayReloadAndScrollCurrent() {
        loadDay();
        wvDay.setItems(dayList);
        ScrollDayCurrent();
    }

    /**
     * 日滚动到当前位置
     */
    private void ScrollDayCurrent() {
        for (int i = 0; i < dayList.size(); i++) {
            if (currentDay == Integer.parseInt(dayList.get(i))) {
                wvDay.setSeletion(i);
                break;
            }
        }
    }

    /**
     * 根据当前选中的年份以及设置的startTime和endTime准备数据、填充、最终滚动到当前位置
     */
    private void wvMonthReloadAndScrollCurrent() {
        loadMonth();
        wvMonth.setItems(monthList);
        ScrollMonthCurrent();
    }

    /**
     * 月份滚动显示当前月份位置
     */
    private void ScrollMonthCurrent() {

        for (int i = 0; i < monthList.size(); i++) {
            if (currentMonth == Integer.parseInt(monthList.get(i))) {
                final int currentPosition = i;
                Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                        emitter.onNext(currentPosition);
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {
                                CalenderSelectDialog.this.disposable = disposable;
                            }

                            @Override
                            public void onNext(Integer s) {
                                wvMonth.setSeletion(s);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                break;
            }
        }
    }


    /**
     * wheelview数据初始化
     */
    private void initWheelviewData() {
        loadYearData();
        loadMonth();
        loadDay();
    }

    /**
     * 加载年数据
     */
    private void loadYearData() {
        int yearStartTemp = Integer.parseInt(startTime.substring(0, 4));
        int yearEndTepmp = Integer.parseInt(endTime.substring(0, 4));
        yearList.clear();
        for (int i = yearStartTemp; i < yearEndTepmp + 1; i++) {
            yearList.add(String.valueOf(i));
        }
        Integer.parseInt(startTime.substring(0, 4));
        /**
         * 默认选中年份初始化话
         */
        if (currentYear == -1) {
            currentYear = Integer.parseInt(defaultTime.substring(0, 4));
        }
    }


    /**
     * 加载月数
     */
    private void loadMonth() {
        int monthStartTemp = 1;
        int monthEndTepmp = 12;

        /**
         * 开始时间命中
         */
        if (Integer.parseInt(startTime.substring(0, 4)) == currentYear) {
            monthStartTemp = Integer.parseInt(startTime.substring(5, 7));
        }

        /**
         * 结束时间命中
         */
        if (Integer.parseInt(endTime.substring(0, 4)) == currentYear) {
            monthEndTepmp = Integer.parseInt(endTime.substring(5, 7));
        }

        monthList.clear();
        for (int i = monthStartTemp; i < monthEndTepmp + 1; i++) {
            monthList.add(String.valueOf(i));
        }
        if (currentMonth == -1) {
            currentMonth = Integer.parseInt(defaultTime.substring(5, 7));
        }
        if (currentMonth > monthStartTemp && currentMonth < monthEndTepmp) {
            //在起止时间段中包含了当前月，年月动日不动
        } else if (currentMonth < monthStartTemp) {
            currentMonth = monthStartTemp;
        } else if (currentMonth > monthEndTepmp) {
            currentMonth = monthEndTepmp;
        }
    }


    /**
     * 加载日数据
     */
    private void loadDay() {
        int dayStartTemp = 1;
        int dayEndTepmp = getLastDay();
        /**
         * 年月同时命中
         */
        if (Integer.parseInt(startTime.substring(0, 4)) == currentYear && currentMonth == Integer.parseInt(startTime.substring(5, 7))) {  //同一个月份才限制
            dayStartTemp = Integer.parseInt(startTime.substring(8, 10));
        }

        /**
         * 年月同时命中
         */
        if (Integer.parseInt(endTime.substring(0, 4)) == currentYear && currentMonth == Integer.parseInt(endTime.substring(5, 7))) { //同一个月份才限制
            dayEndTepmp = Integer.parseInt(endTime.substring(8, 10));
        }
        dayList.clear();
        for (int i = dayStartTemp; i < dayEndTepmp + 1; i++) {
            dayList.add(String.valueOf(i));
        }
        if (currentDay == -1) {
            currentDay = Integer.parseInt(defaultTime.substring(8, 10));
        }
        if (currentDay > dayStartTemp && currentDay < dayEndTepmp) {
            //在起止时间段中包含了当前月，年月动日不动
        } else if (currentDay < dayStartTemp) {
            currentDay = dayStartTemp;
        } else if (currentDay > dayEndTepmp) {
            currentDay = dayEndTepmp;
        }
    }


    /**
     * 获取最后一天
     *
     * @return
     */
    private int getLastDay() {
        Date theLastDayofMonth = Util.getTheLastDayofMonth(currentYear, currentMonth);
        SimpleDateFormat format = new SimpleDateFormat("dd", Locale.CHINA);

        return Integer.parseInt(format.format(theLastDayofMonth));
    }

    /**
     * 获取选择的年份
     *
     * @param selectDate
     * @return
     */
    public String getSelectYear(String selectDate) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(selectDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String year = "";
        String format = simpleDateFormat1.format(parse);
        year = TextUtils.isEmpty(format) ? "" : format;
        return year;
    }

    /**
     * 获取选择的月份
     *
     * @param selectDate
     * @return
     */
    public String getSelectMonth(String selectDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MM");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(selectDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String month = "";
        String format = simpleDateFormat1.format(parse);
        month = TextUtils.isEmpty(format) ? "" : format;
        return month;
    }

    /**
     * 获取选择的日期
     *
     * @param selectDate
     * @return
     */
    public String getSelectDay(String selectDate) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(selectDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String day = "";
        String format = simpleDateFormat1.format(parse);
        day = TextUtils.isEmpty(format) ? "" : format;
        return day;
    }

    /**
     * 获取现在的时间
     *
     * @return
     */
    public String getToday() {
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        String format = sdf.format(date);
        return format;
    }

}