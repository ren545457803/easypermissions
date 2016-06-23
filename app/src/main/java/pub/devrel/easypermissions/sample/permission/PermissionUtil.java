package pub.devrel.easypermissions.sample.permission;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by 任先生 on 2016-04-25.
 * 安卓权限申请问题
 *
 * 6.0 权限图:
 * *-- normal(安装时直接赋予APP)
 * *--System----
 * *            *--dangerous(需要运行时动态申请)
 * *
 * ---安卓权限------
 * *
 * *                    *--SYSTEM_ALERT_WINDOW
 * *--Special(只有两个)--需要开启Activity申请
 * *--WRITE_SETTINGS
 *
 * <href src="http://www.tuicool.com/articles/2Aviya">权限详情</>
 */
public class PermissionUtil {
  private static final String TAG = "PermissionUtil";

  public static final int RC_CALL_PERM = 200;
  public static final int RC_CAMERA_PERM = RC_CALL_PERM + 1;
  public static final int RC_CONTACTS_PERM = RC_CAMERA_PERM + 1;
  public static final int RC_LOCATION_PERM = RC_CONTACTS_PERM + 1;
  public static final int RC_UMENG_PERM = RC_LOCATION_PERM + 1;
  public static final int RC_BAIDU_PERM = RC_UMENG_PERM + 1;
  public static final int RC_JPUSH_PERM = RC_BAIDU_PERM + 1;
  public static final int RC_QQ_PERM = RC_JPUSH_PERM + 1;
  public static final int RC_AUTOUPDATE_PERM = RC_QQ_PERM + 1;
  /**
   * 这两个比较特殊{@link EasyPermissions#WRITE_SETTINGS}
   */
  public static final int RC_WRITE_SETTING_PERM = EasyPermissions.WRITE_SETTINGS;
  /**
   * {@link EasyPermissions#SYSTEM_ALERT_WINDOW}
   */
  public static final int RC_SYS_ALERT_WINDOW_PERM = EasyPermissions.SYSTEM_ALERT_WINDOW;

  // 两个特殊权限
  public static final String WRITE_SETTING_PERMISSION = Settings.ACTION_MANAGE_WRITE_SETTINGS;
  public static final String SYSTEM_ALERT_WINDOW_PERMISSION =
      Settings.ACTION_MANAGE_OVERLAY_PERMISSION;

  public static final String[] CALL_PERMISSION = { CALL_PHONE };
  public static final String[] CAMERA_PERMISSION = { CAMERA };
  public static final String[] CONTACTS_PERMISSION = { READ_CONTACTS };
  public static final String[] LOCATION_PERMISSION =
      { ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION };


  // 自动升级
  public static String[] AUTOUPDATE_PERMISSION;
  /**
   * READ_PHONE_STATE权限，API 16才有的
   */
  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      AUTOUPDATE_PERMISSION = new String[] { READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE };
    } else {
      AUTOUPDATE_PERMISSION = new String[] { WRITE_EXTERNAL_STORAGE };
    }
  }

  // 友盟
  public static final String[] UMENG_PERMISSION = { READ_PHONE_STATE };
  // 百度地图
  public static final String[] BAIDU_PERMISSION = {
      READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION,
      WRITE_SETTING_PERMISSION
  };
  // 极光
  public static String[] JPUSH_PERMISSION;
  /**
   * READ_PHONE_STATE权限，API 16才有的
   */
  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      JPUSH_PERMISSION = new String[] {
              READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, WRITE_SETTING_PERMISSION,
              SYSTEM_ALERT_WINDOW_PERMISSION
      };
    } else {
      JPUSH_PERMISSION = new String[] {
              READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE, WRITE_SETTING_PERMISSION,
              SYSTEM_ALERT_WINDOW_PERMISSION
      };
    }
  }

  // QQ语音
  public static final String[] QQ_PERMISSION =
      { CAMERA, ACCESS_COARSE_LOCATION, CALL_PHONE, READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE };


  /**
   * 是否有权限
   *
   * @param perms 权限数组
   */
  public static boolean hasPermissions(Context context, String... perms) {
    return EasyPermissions.hasPermissions(context, perms);
  }

  /**
   * 请求权限。调用此方法前，最好调用{@link #hasPermissions(Context, String...)},没有权限时，再申请
   */
  public static void requestPermission(final Object object, String rationale, final int requestCode,
      String... perms) {
    if (perms == null || perms.length < 0) {
      throw new IllegalArgumentException("PermissionUtil#requestPermission,必须要填写权限");
    }

    // 1. 已有的权限，不必再次请求
    perms = EasyPermissions.excludeHadPermissions(object, perms);
    Log.d(TAG, "过滤完已有权限:" + Arrays.toString(perms));
    if (perms.length < 0) {
      throw new IllegalArgumentException(
          "PermissionUtil#requestPermission,已有所需权限，请先调用#hasPermissions可好");
    }

    // 2. 过滤掉特殊权限
    String[] dangerPermissions = excludeSpecialPermissions(perms);
    Log.d(TAG, "过滤完特殊权限：" + Arrays.toString(dangerPermissions));

    if (dangerPermissions.length > 0) {// 3. 如果有dangerous权限
      Log.d(TAG, "执行请求危险权限:" + Arrays.toString(dangerPermissions));
      EasyPermissions.requestPermissions(object, rationale, requestCode, dangerPermissions);
    } else {// 4. 如果没有危险权限，就意味着有特殊权限喽
      for (String perm : perms) {
        Log.d(TAG, "得到特殊权限：" + perm);
        EasyPermissions.requestSpecialPermission(object, perm);
      }
      /*String specialPermission = getSpecialPermission(perms);
      LogA.d(TAG, "得到特殊权限：" + specialPermission);
      if (!TextUtils.isEmpty(specialPermission)) {
        EasyPermissions.requestSpecialPermission(object, specialPermission);
      }*/
    }
  }

  /**
   * 过滤特殊权限
   */
  private static String[] excludeSpecialPermissions(String... perms) {
    String[] dangerPermissions = {};// 危险权限

    List<String> permsList = new ArrayList(Arrays.asList(perms));

    Iterator<String> iterator = permsList.iterator();
    while (iterator.hasNext()) {
      String perm = iterator.next();
      if (Settings.ACTION_MANAGE_OVERLAY_PERMISSION.equals(perm)
          || Settings.ACTION_MANAGE_WRITE_SETTINGS.equals(perm)) {// 如果是特殊权限，则删除
        iterator.remove();
      }
    }

    return permsList.toArray(dangerPermissions);
  }

  /**
   * 得到特殊权限
   */
  private static String getSpecialPermission(String... perms) {
    for (String p : perms) {
      if (Settings.ACTION_MANAGE_OVERLAY_PERMISSION.equals(p)
          || Settings.ACTION_MANAGE_WRITE_SETTINGS.equals(p)) {
        return p;
      }
    }
    return "";
  }
}
