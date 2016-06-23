# EasyPermissions

基于https://github.com/googlesamples/easypermissions
添加了两个特殊权限的申请SYSTEM_ALERT_WINDOW和WRITE_SETTINGS
EasyPermissions is a wrapper library to simplify basic system permissions logic when targeting
Android M or higher.

## Installation

Clone我的项目吧，还不会使用gradle依赖，会了以后再改。

## Usage

### 项目快速集成权限申请模块

1. 项目依赖easypermissions模块
2. 复制包permission到你的项目
3. 所有的Activity和Fragment(需要申请权限)继承相应的PermissionActivity和PermissionFragment

#### 修改相应代码满足自己项目的需要
例子，需要申请定位权限：

 * 在`PermissionUtil`中声明此权限`public static final String[] LOCATION_PERMISSION = { ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION }`和
对应申请此权限使用的CODE`public static final int RC_LOCATION_PERM = RC_CONTACTS_PERM + 1`

 * 在`PermissionActivity`中实现此权限申请的逻辑代码
 ```java
 
 @AfterPermissionGranted(RC_LOCATION_PERM)
     public void locationTask() {
         if (hasPermissions(this, LOCATION_PERMISSION)) {
             performLocation();
         } else {
             performRequestPermission(R.string.perm_location, RC_LOCATION_PERM, LOCATION_PERMISSION);
         }
     }
    
    //子类要重载此方法
    protected void performLocation() {
            throw new RuntimeException(NOT_RELOAD_ERROR);
         }
 ```

 * 在业务Activity,如`MainActivity`中申请并使用此权限。
使用`locationTask()`申请，然后，覆写`performLocation()`,并在此方法中使用定位相关的权限。


### Basic

To begin using EasyPermissions, have your Activity (or Fragment) implement the 
`EasyPermissions.PermissionCallbacks` and override the following methods:

```java
public class MainActivity extends AppCompatActivity
    implements EasyPermissions.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Forward results to EasyPermissions
        EasyPermissions.onActivityResult(requestCode, resultCode, data, this);
      }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }
}
```

### Request Permissions

#### System Permissions(normal and dangerous)

The example below shows how to request permissions for a method that requires both
`CAMERA` and `CHANGE_WIFI_STATE` permissions. There are a few things to note:

  * Using `EasyPermissions#hasPermissions(...)` to check if the app already has the
    required permissions. This method can take any number of permissions as its final
    argument.
  * Requesting permissions with `EasyPermissions#requestPermissions`. This method
    will request the system permissions and show the rationale string provided if
    necessary. The request code provided should be unique to this request, and the method
    can take any number of permissions as its final argument.
  * Use of the `AfterPermissionGranted` annotation. This is optional, but provided for
    convenience. If all of the permissions in a given request are granted, any methods
    annotated with the proper request code will be executed. This is to simplify the common
    flow of needing to run the requesting method after all of its permissions have been granted.
    This can also be achieved by adding logic on the `onPermissionsGranted` callback.

```java
    @AfterPermissionGranted(RC_CAMERA_AND_WIFI)
    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.CHANGE_WIFI_STATE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_wifi_rationale),
                    RC_CAMERA_AND_WIFI, perms);
        }
    }
```

#### Special Permission

SYSTEM_ALERT_WINDOW和WRITE_SETTINGS两个权限。
  * 使用`EasyPermissions#hasPermissions(...)`查看是否有此权限
  * 请求权限使用`EasyPermission#requestSpecialPermission(...)`请求
  * Use of the `AfterPermissionGranted` annotation. This is optional, but provided for
      convenience. If all of the permissions in a given request are granted, any methods
      annotated with the proper request code will be executed. This is to simplify the common
      flow of needing to run the requesting method after all of its permissions have been granted.
      This can also be achieved by adding logic on the `onPermissionsGranted` callback.

Note:如果使用`AfterPermissionGranted` annotation,要使用EasyPermission#SYSTEM_ALERT_WINDOW或EasyPermission#WRITE_SETTINGS

```java
   @AfterPermissionGranted(EasyPermissions.WRITE_SETTINGS)
    public void writeSettingTask() {
       if (EasyPermissions.hasPermissions(this, Settings.ACTION_MANAGE_WRITE_SETTINGS)) {
         // Already have permission, do the thing
         // ...
       } else {
         // Do not have permissions, request them now
         EasyPermissions.requestSpecialPermission(this, Settings.ACTION_MANAGE_WRITE_SETTINGS);
       }
     }
```
