/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easypermissions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to request and check System permissions for apps targeting Android M (API >= 23).
 */
public class EasyPermissions {

  private static final String TAG = "EasyPermissions";

  /**
   * 两个特殊的权限，需要特殊申请.
   * 开发者需要在{@link AfterPermissionGranted}中使用{@link EasyPermissions#SYSTEM_ALERT_WINDOW
   * EasyPermissions#WRITE_SETTINGS}
   * 才行
   */
  public static final int SYSTEM_ALERT_WINDOW = 6666;
  public static final int WRITE_SETTINGS = 8888;

  public interface PermissionCallbacks extends ActivityCompat.OnRequestPermissionsResultCallback {

    void onPermissionsGranted(int requestCode, List<String> perms);

    void onPermissionsDenied(int requestCode, List<String> perms);
  }

  /**
   * Check if the calling context has a set of permissions.
   *
   * @param context the calling context.
   * @param perms one ore more permissions, such as {@code android.Manifest.permission.CAMERA}.
   * @return true if all permissions are already granted, false if at least one permission
   * is not yet granted.
   */
  public static boolean hasPermissions(Context context, String... perms) {
    for (String perm : perms) {
      boolean hasPerm =
          (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED);

      if (!hasPerm && !hasSpecialPermission(context, perm)) {
        return false;
      }
    }

    return true;
  }

  /**
   * 特殊权限是否授权
   *
   * {@link Settings#ACTION_MANAGE_OVERLAY_PERMISSION},使用{@link Settings#canDrawOverlays(Context)}检测,
   * {@link Settings#ACTION_MANAGE_WRITE_SETTINGS},使用{@link Settings.System#canWrite(Context)}检测
   */
  private static boolean hasSpecialPermission(Context context, String perm) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// API>=23才有的 即Android 6.0
      if (Settings.ACTION_MANAGE_OVERLAY_PERMISSION.equals(perm)) {
        return Settings.canDrawOverlays(context);
      } else if (Settings.ACTION_MANAGE_WRITE_SETTINGS.equals(perm)) {
        return Settings.System.canWrite(context);
      }
    }

    return true;// 默认是有的
  }

  /**
   * Request a set of permissions, showing rationale if the system requests it.
   *
   * @param object Activity or Fragment requesting permissions. Should implement
   * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback}
   * or
   * {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
   * @param rationale a message explaining why the application needs this set of permissions, will
   * be displayed if the user rejects the request the first time.
   * @param requestCode request code to track this request, must be < 256.
   * @param perms a set of permissions to be requested.
   */
  public static void requestPermissions(final Object object, String rationale,
      final int requestCode, final String... perms) {
    requestPermissions(object, rationale, android.R.string.ok, android.R.string.cancel, requestCode,
        perms);
  }

  /**
   * Request a set of permissions, showing rationale if the system requests it.
   *
   * @param object Activity or Fragment requesting permissions. Should implement
   * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback}
   * or
   * {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
   * @param rationale a message explaining why the application needs this set of permissions, will
   * be displayed if the user rejects the request the first time.
   * @param positiveButton custom text for positive button
   * @param negativeButton custom text for negative button
   * @param requestCode request code to track this request, must be < 256.
   * @param perms a set of permissions to be requested.
   */
  public static void requestPermissions(final Object object, String rationale,
      @StringRes int positiveButton, @StringRes int negativeButton, final int requestCode,
      final String... perms) {

    checkCallingObjectSuitability(object);
    final PermissionCallbacks callbacks = (PermissionCallbacks) object;

    boolean shouldShowRationale = false;
    for (String perm : perms) {
      shouldShowRationale =
          shouldShowRationale || shouldShowRequestPermissionRationale(object, perm);
    }

    if (shouldShowRationale) {
      AlertDialog dialog = new AlertDialog.Builder(getActivity(object)).setMessage(rationale)
          .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              executePermissionsRequest(object, perms, requestCode);
            }
          })
          .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              // act as if the permissions were denied
              callbacks.onPermissionsDenied(requestCode, Arrays.asList(perms));
            }
          })
          .create();
      dialog.show();
    } else {
      executePermissionsRequest(object, perms, requestCode);
    }
  }

  /**
   * 请求特殊权限，需要在{@code object}的{@link Activity#onActivityResult(int, int, Intent)}调用{@link
   * EasyPermissions#onActivityResult(int, int, Intent, Object)},让{@link EasyPermissions}处理.
   *
   * 注意：
   * 1. 如果Activity或Fragment有其他{@link Activity#startActivityForResult(Intent, int)}时，requestCode不要
   * 和{@link #SYSTEM_ALERT_WINDOW}、{@link #WRITE_SETTINGS}相等
   * 2. 如果使用注解自动调用方法，要在注解中使用{@link #SYSTEM_ALERT_WINDOW}或{@link #WRITE_SETTINGS}
   *
   * @see Settings#ACTION_MANAGE_OVERLAY_PERMISSION
   * @see Settings#ACTION_MANAGE_WRITE_SETTINGS
   */
  public static void requestSpecialPermission(Object object, String perm) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {// API>=23才有的 6.0
      return;
    }

    checkSpecialCallingObjectSuitability(object, perm);

    // 需要开启Activity请求
    if (object instanceof Fragment) {// fragment
      Fragment f = (Fragment) object;

      if (Settings.ACTION_MANAGE_OVERLAY_PERMISSION.equals(perm)) {// System_alert_window
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + f.getActivity().getPackageName()));
        f.startActivityForResult(intent, SYSTEM_ALERT_WINDOW);
      } else if (Settings.ACTION_MANAGE_WRITE_SETTINGS.equals(perm)) {// write_setting
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + f.getActivity().getPackageName()));
        f.startActivityForResult(intent, WRITE_SETTINGS);
      }
    } else if (object instanceof Activity) {// activity
      Activity a = (Activity) object;

      if (Settings.ACTION_MANAGE_OVERLAY_PERMISSION.equals(perm)) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + a.getPackageName()));
        a.startActivityForResult(intent, SYSTEM_ALERT_WINDOW);
      } else if (Settings.ACTION_MANAGE_WRITE_SETTINGS.equals(perm)) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + a.getPackageName()));
        a.startActivityForResult(intent, WRITE_SETTINGS);
      }
    }
  }

  /**
   * Handle the result of a permission request, should be called from the calling Activity's
   * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int,
   * String[], int[])}
   * method.
   * <p/>
   * If any permissions were granted or denied, the Activity will receive the appropriate
   * callbacks through {@link PermissionCallbacks} and methods annotated with
   * {@link AfterPermissionGranted} will be run if appropriate.
   *
   * @param requestCode requestCode argument to permission result callback.
   * @param permissions permissions argument to permission result callback.
   * @param grantResults grantResults argument to permission result callback.
   * @param object the calling Activity or Fragment.
   * @throws IllegalArgumentException if the calling Activity does not implement
   * {@link PermissionCallbacks}.
   */
  public static void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults, Object object) {

    checkCallingObjectSuitability(object);
    PermissionCallbacks callbacks = (PermissionCallbacks) object;

    // Make a collection of granted and denied permissions from the request.
    ArrayList<String> granted = new ArrayList<>();
    ArrayList<String> denied = new ArrayList<>();
    for (int i = 0; i < permissions.length; i++) {
      String perm = permissions[i];
      if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
        granted.add(perm);
      } else {
        denied.add(perm);
      }
    }

    // Report granted permissions, if any.
    if (!granted.isEmpty()) {
      // Notify callbacks
      callbacks.onPermissionsGranted(requestCode, granted);
    }

    // Report denied permissions, if any.
    if (!denied.isEmpty()) {
      callbacks.onPermissionsDenied(requestCode, denied);
    }

    // If 100% successful, call annotated methods
    if (!granted.isEmpty() && denied.isEmpty()) {
      runAnnotatedMethods(object, requestCode);
    }
  }

  /**
   * 接受特殊权限回调
   * 申请特殊权限会打开Activity，用户处理了权限设置后，在{@link Activity#onActivityResult(int, int, Intent)}中调用{@link
   * EasyPermissions#onActivityResult(int, int, Intent, Object)},
   * 因为系统没有返回此权限是否授予，所以，需要{@link EasyPermissions}检查
   */
  public static void onActivityResult(int requestCode, int resultCode, Intent data, Object object) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return;
    }

    checkCallingObjectSuitability(object);

    PermissionCallbacks callbacks = (PermissionCallbacks) object;

    ArrayList<String> permissions = new ArrayList<>();
    switch (requestCode) {
      case SYSTEM_ALERT_WINDOW:
        permissions.add(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        break;
      case WRITE_SETTINGS:
        permissions.add(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        break;
    }

    if (permissions.size() < 1) {// 这种情况绝不会发生，╮(╯▽╰)╭
      return;
    }

    boolean hasPermission = hasSpecialPermission(getActivity(object), permissions.get(0));
    if (hasPermission) {
      callbacks.onPermissionsGranted(requestCode, permissions);
      runAnnotatedMethods(object, requestCode);// 自动调用注释方法
    } else {
      callbacks.onPermissionsDenied(requestCode, permissions);
    }
  }

  private static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
    if (object instanceof Activity) {
      return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
    } else if (object instanceof Fragment) {
      return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
    } else {
      return false;
    }
  }

  private static void executePermissionsRequest(Object object, String[] perms, int requestCode) {
    checkCallingObjectSuitability(object);

    if (object instanceof Activity) {
      ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
    } else if (object instanceof Fragment) {
      ((Fragment) object).requestPermissions(perms, requestCode);
    }
  }

  private static Activity getActivity(Object object) {
    if (object instanceof Activity) {
      return ((Activity) object);
    } else if (object instanceof Fragment) {
      return ((Fragment) object).getActivity();
    } else {
      return null;
    }
  }

  private static void runAnnotatedMethods(Object object, int requestCode) {
    Class clazz = object.getClass();
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(AfterPermissionGranted.class)) {
        // Check for annotated methods with matching request code.
        AfterPermissionGranted ann = method.getAnnotation(AfterPermissionGranted.class);
        if (ann.value() == requestCode) {
          // Method must be void so that we can invoke it
          if (method.getParameterTypes().length > 0) {
            throw new RuntimeException("Cannot execute non-void method " + method.getName());
          }

          try {
            // Make method accessible if private
            if (!method.isAccessible()) {
              method.setAccessible(true);
            }
            method.invoke(object);
          } catch (IllegalAccessException e) {
            Log.e(TAG, "runDefaultMethod:IllegalAccessException", e);
          } catch (InvocationTargetException e) {
            Log.e(TAG, "runDefaultMethod:InvocationTargetException", e);
          }
        }
      }
    }
  }

  private static void checkCallingObjectSuitability(Object object) {
    // Make sure Object is an Activity or Fragment
    if (!((object instanceof Fragment) || (object instanceof Activity))) {
      throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
    }

    // Make sure Object implements callbacks
    if (!(object instanceof PermissionCallbacks)) {
      throw new IllegalArgumentException("Caller must implement PermissionCallbacks.");
    }
  }

  private static void checkSpecialCallingObjectSuitability(Object object, String perm) {
    // Make sure Object is an Activity or Fragment
    if (!((object instanceof Fragment) || (object instanceof Activity))) {
      throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
    }

    // Make sure Permission is special permission
    if (!Settings.ACTION_MANAGE_OVERLAY_PERMISSION.equals(perm)
        && !Settings.ACTION_MANAGE_WRITE_SETTINGS.equals(perm)) {
      throw new IllegalArgumentException("permission must is a special permission");
    }
  }
}
