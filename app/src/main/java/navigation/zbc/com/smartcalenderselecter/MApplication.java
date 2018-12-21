package navigation.zbc.com.smartcalenderselecter;

import android.app.Application;
import android.content.Context;

public class MApplication extends Application {

    //屏幕的宽高信息
    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static float density = 0;

    public static Context sAppContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
    }


}
