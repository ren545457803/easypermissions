package pub.devrel.easypermissions.sample.permission;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.sample.R;

import static pub.devrel.easypermissions.sample.permission.PermissionUtil.*;

/**
 * Created by 任先生 on 2016-04-26.
 */
public class PermissionFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
  private static final String TAG = "PermissionFragment";

  private static final String NOT_RELOAD_ERROR = "申请了此权限，子类要重载此执行方法";

  @Override public void onPermissionsGranted(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsGranted:使用注解接受权限被赋予后的处理");
  }

  @Override public void onPermissionsDenied(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsDenied:被拒了");
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // 让EasyPermissions处理(危险权限).
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // 让EasyPermissions处理（两个特殊权限）
    EasyPermissions.onActivityResult(requestCode, resultCode, data, this);
  }

  /**
   * 执行电话相关权限操作时，先调用此方法，当赋予权限后，会自动调用相应的perform方法，子类要重载此方法。
   */
  @AfterPermissionGranted(RC_CALL_PERM) public void callPhoneTask() {
    if (hasPermissions(getActivity(), CALL_PERMISSION)) {
      performCallPhone();
    } else {
      performRequestPermission(R.string.perm_call_phone, RC_CALL_PERM, CALL_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_CAMERA_PERM) public void cameraTask() {
    if (hasPermissions(getActivity(), CAMERA_PERMISSION)) {
      performCamera();
    } else {
      performRequestPermission(R.string.perm_camera, RC_CAMERA_PERM, CAMERA_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_CONTACTS_PERM) public void contactsTask() {
    if (hasPermissions(getActivity(), CONTACTS_PERMISSION)) {
      performContacts();
    } else {
      performRequestPermission(R.string.perm_contacts, RC_CONTACTS_PERM, CONTACTS_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_LOCATION_PERM) public void locationTask() {
    if (hasPermissions(getActivity(), LOCATION_PERMISSION)) {
      performLocation();
    } else {
      performRequestPermission(R.string.perm_location, RC_LOCATION_PERM, LOCATION_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_UMENG_PERM) public void umengTask() {
    if (hasPermissions(getActivity(), UMENG_PERMISSION)) {
      performUmeng();
    } else {
      performRequestPermission(R.string.perm_umeng, RC_UMENG_PERM, UMENG_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_BAIDU_PERM) public void baiduTask() {
    if (hasPermissions(getActivity(), BAIDU_PERMISSION)) {
      performBaidu();
    } else {
      performRequestPermission(R.string.perm_baidu, RC_BAIDU_PERM, BAIDU_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_JPUSH_PERM) public void jpushTask() {
    if (hasPermissions(getActivity(), JPUSH_PERMISSION)) {
      performJPush();
    } else {
      performRequestPermission(R.string.perm_jpush, RC_JPUSH_PERM, JPUSH_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_QQ_PERM) public void qqTask() {
    if (hasPermissions(getActivity(), QQ_PERMISSION)) {
      performQQ();
    } else {
      performRequestPermission(R.string.perm_qq, RC_QQ_PERM, QQ_PERMISSION);
    }
  }

  @AfterPermissionGranted(RC_AUTOUPDATE_PERM) public void autoUpdateTask() {
    if (hasPermissions(getActivity(), AUTOUPDATE_PERMISSION)) {
      performAutoUpdate();
    } else {
      performRequestPermission(R.string.perm_autoupdate, RC_AUTOUPDATE_PERM, AUTOUPDATE_PERMISSION);
    }
  }

  /**
   * 子类要重载此方法
   */
  protected void performCallPhone() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  /**
   * 子类要重载此方法
   */
  protected void performCamera() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  /**
   * 子类要重载此方法
   */
  protected void performContacts() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  /**
   * 子类要重载此方法
   */
  protected void performLocation() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  /**
   * 子类要重载此方法
   */
  protected void performUmeng() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  /**
   * 子类要重载此方法
   */
  protected void performBaidu() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  /**
   * 子类要重载此方法
   */
  protected void performJPush() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  /**
   * 子类要重载此方法
   */
  protected void performQQ() {
    throw new RuntimeException(NOT_RELOAD_ERROR);
  }

  protected void performAutoUpdate(){
    throw new RuntimeException(NOT_RELOAD_ERROR);
  };
  /**
   * 执行请求权限
   *
   * @param rationaleId 提示语
   * @param perms 所需要权限
   */
  private void performRequestPermission(@StringRes int rationaleId, int flag, String... perms) {
    Log.d(TAG, "Fragment请求权限：" + Arrays.toString(perms));
    requestPermission(this, getString(rationaleId), flag, perms);
    Toast.makeText(getActivity(), R.string.perm_sorry, Toast.LENGTH_SHORT).show();
  }
}