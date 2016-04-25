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
package pub.devrel.easypermissions.sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.List;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

  private static final String TAG = "MainActivity";

  private static final int RC_CAMERA_PERM = 123;
  private static final int RC_LOCATION_CONTACTS_PERM = 124;
  private static final int RC_WRITE_SETTING = 125;
  private static final int RC_ALERT_WINDOW = 126;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    // Button click listener that will request one permission.
    findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        cameraTask();
      }
    });

    // Button click listener that will request two permissions.
    findViewById(R.id.button_location_and_wifi).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        locationAndContactsTask();
      }
    });

    findViewById(R.id.button_setting).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        writeSettingTask();
      }
    });
  }

  @AfterPermissionGranted(RC_CAMERA_PERM) public void cameraTask() {
    if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
      // Have permission, do the thing!
      Toast.makeText(this, "TODO: Camera things", Toast.LENGTH_LONG).show();
    } else {
      // Ask for one permission
      EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera), RC_CAMERA_PERM,
          Manifest.permission.CAMERA);
    }
  }

  @AfterPermissionGranted(RC_LOCATION_CONTACTS_PERM) public void locationAndContactsTask() {
    String[] perms =
        { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS };
    if (EasyPermissions.hasPermissions(this, perms)) {
      // Have permissions, do the thing!
      Toast.makeText(this, "TODO: Location and Contacts things", Toast.LENGTH_LONG).show();
    } else {
      // Ask for both permissions
      EasyPermissions.requestPermissions(this, getString(R.string.rationale_location_contacts),
          RC_LOCATION_CONTACTS_PERM, perms);
    }
  }

  @AfterPermissionGranted(EasyPermissions.WRITE_SETTINGS) public void writeSettingTask() {

    if (EasyPermissions.hasPermissions(this, Settings.ACTION_MANAGE_WRITE_SETTINGS)) {
      // Have permissions, do the thing!
      Toast.makeText(this, "TODO: Setting things", Toast.LENGTH_LONG).show();
    } else {
      // Ask for both permissions
      Log.d(TAG, "申请权限Setting");
      EasyPermissions.requestSpecialPermission(this, Settings.ACTION_MANAGE_WRITE_SETTINGS);
    }
  }

  @OnClick(R.id.bt_alertwindow) @AfterPermissionGranted(EasyPermissions.SYSTEM_ALERT_WINDOW)
  public void alertWindow() {
    if (EasyPermissions.hasPermissions(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) {
      Toast.makeText(this, "TODO: AlertVersion things", Toast.LENGTH_LONG).show();
    } else {
      Log.d(TAG, "申请AlertWindow权限");
      EasyPermissions.requestSpecialPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
    }
  }

  @OnClick(R.id.bt_has_setting) void hasSettingPermission() {
    boolean isHas = EasyPermissions.hasPermissions(this, Settings.ACTION_MANAGE_WRITE_SETTINGS);
    Log.d(TAG, "setting:" + isHas);
    Toast.makeText(this, isHas + "", Toast.LENGTH_SHORT).show();
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // EasyPermissions handles the request result.
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override public void onPermissionsGranted(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
  }

  @Override public void onPermissionsDenied(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(TAG, "Setting:" + requestCode + ":" + resultCode + ":" + data);
    EasyPermissions.onActivityResult(requestCode, resultCode, data, this);
  }
}
