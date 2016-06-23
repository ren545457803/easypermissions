package pub.devrel.easypermissions.sample;

import android.os.Bundle;

public class FragmentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,new MainFragment()).commit();
    }
}
