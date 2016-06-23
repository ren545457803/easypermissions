package pub.devrel.easypermissions.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pub.devrel.easypermissions.sample.permission.PermissionFragment;

public class MainFragment extends PermissionFragment {

    private static final String TAG = "MainFragment";

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Create view
        View v =  inflater.inflate(R.layout.fragment_main, container);

        // Button click listener
        v.findViewById(R.id.button_jpush).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"请求JPush任务");
                jpushTask();
            }
        });

        v.findViewById(R.id.button_2_contacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"请求读取联系人任务");
                contactsTask();
            }
        });

        return v;
    }

    @Override
    protected void performContacts() {
        Log.d(TAG,"执行Contacts任务");
    }

    @Override
    protected void performJPush() {
        Log.d(TAG,"执行JPsuh任务");
    }
}
