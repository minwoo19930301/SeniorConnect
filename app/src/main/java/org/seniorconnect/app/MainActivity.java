package org.seniorconnect.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.seniorconnect.app.dialing.DialingActivity;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.action_call).setOnClickListener(
                view -> startActivity(new Intent(this, DialingActivity.class))
        );
    }
}
