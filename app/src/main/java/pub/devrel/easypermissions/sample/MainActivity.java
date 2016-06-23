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

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends BaseActivity {

  private static final String TAG = "MainActivity";


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

//    ButterKnife.bind(this);

    // Button click listener that will request one permission.
    findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG,"请求Camera任务");
        cameraTask();
      }
    });

    // Button click listener that will request two permissions.
    findViewById(R.id.button_location).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.d(TAG,"请求定位任务");
        locationTask();
      }
    });


    /**
     * 百度权限比较麻烦，里面有个特殊权限 {@link android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS}
     */
    findViewById(R.id.button_baidu).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        baiduTask();
      }
    });

  }

  @Override
  protected void performCamera() {
    Log.d(TAG,"执行Camera任务");
  }

  @Override
  protected void performLocation() {
    Log.d(TAG,"执行定位任务");
  }

  @Override
  protected void performBaidu() {
    Log.d(TAG,"执行百度任务");
  }
}
