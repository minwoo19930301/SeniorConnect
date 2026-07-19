package org.seniorconnect.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Active in this phase: YouTube TV-mode + Maps nearby places.
        // Call and Speak remain behavior-free (see AGENTS.md).
        findViewById(R.id.action_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, YouTubeActivity.class));
            }
        });
        findViewById(R.id.action_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });
    }
}
