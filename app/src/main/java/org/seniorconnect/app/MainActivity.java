package org.seniorconnect.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import org.seniorconnect.app.dialing.DialingActivity;

public final class MainActivity extends Activity {
    private static final int RECORD_AUDIO_REQUEST = 41;
    private static final String MODEL_FILE_NAME = "gemma.task";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private TextView status;
    private TextView transcript;
    private Button speakButton;
    private boolean conversationActive;
    private Object localModel;
    private Class<?> localModelClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private void showHome() {
        stopConversationIfNeeded();
        setContentView(R.layout.activity_main);
        findViewById(R.id.action_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DialingActivity.class));
            }
        });
        findViewById(R.id.action_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, YouTubeActivity.class));
            }
        });
        findViewById(R.id.action_speak).setOnClickListener(view -> showSpeakScreen());
        findViewById(R.id.action_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });
    }

    private void showSpeakScreen() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(28, 30, 28, 28);
        content.setBackgroundColor(getColor(R.color.screen_background));

        TextView title = label(R.string.speak_title, 30);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView subtitle = label(R.string.speak_subtitle, 17);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle, params(0, 4));

        status = label(modelFile().exists() ? R.string.speak_ready_to_talk : R.string.speak_model_missing, 19);
        status.setGravity(Gravity.CENTER);
        status.setPadding(16, 18, 16, 18);
        status.setBackgroundColor(getColor(R.color.status_background));
        content.addView(status, params(0, 18));

        transcript = label(R.string.speak_transcript_empty, 18);
        transcript.setGravity(Gravity.CENTER);
        transcript.setPadding(12, 18, 12, 18);
        content.addView(transcript, new LinearLayout.LayoutParams(-1, 0, 1));

        speakButton = actionButton(R.string.speak_stop, true);
        speakButton.setOnClickListener(view -> {
            if (conversationActive) stopConversation(); else startListening();
        });
        content.addView(speakButton);

        Button homeButton = actionButton(R.string.speak_home, false);
        homeButton.setOnClickListener(view -> showHome());
        content.addView(homeButton);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);

        textToSpeech = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS && textToSpeech != null) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle params) { status.setText(R.string.speak_ready); }
            public void onBeginningOfSpeech() { status.setText(R.string.speak_hearing); }
            public void onRmsChanged(float rmsdB) { }
            public void onBufferReceived(byte[] buffer) { }
            public void onEndOfSpeech() { status.setText(R.string.speak_thinking); }
            public void onError(int error) {
                if (conversationActive) handler.postDelayed(() -> { if (conversationActive) startListening(); }, 900);
            }
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches == null || matches.isEmpty()) {
                    if (conversationActive) handler.postDelayed(() -> { if (conversationActive) startListening(); }, 900);
                    return;
                }
                askLocalGemma(matches.get(0));
            }
            public void onPartialResults(Bundle partialResults) { }
            public void onEvent(int eventType, Bundle params) { }
        });

        if (modelFile().exists()) handler.postDelayed(this::startListening, 700);
    }

    private TextView label(int text, float size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(getColor(R.color.text_primary));
        view.setTextSize(size);
        return view;
    }

    private LinearLayout.LayoutParams params(int width, int marginTop) {
        LinearLayout.LayoutParams result = new LinearLayout.LayoutParams(width == 0 ? -1 : width, -2);
        result.setMargins(0, marginTop, 0, 0);
        return result;
    }

    private Button actionButton(int text, boolean primary) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(primary ? 21 : 17);
        button.setAllCaps(false);
        button.setMinHeight(primary ? 76 : 54);
        button.setTextColor(primary ? getColor(R.color.button_text) : getColor(R.color.text_primary));
        if (primary) button.setBackgroundColor(getColor(R.color.action_blue));
        return button;
    }

    private File modelFile() {
        return new File(getFilesDir(), MODEL_FILE_NAME);
    }

    private void startListening() {
        if (!modelFile().exists()) {
            conversationActive = false;
            speakButton.setText(R.string.speak_start);
            status.setText(R.string.speak_model_missing);
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST);
            return;
        }
        conversationActive = true;
        speakButton.setText(R.string.speak_stop);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.startListening(intent);
    }

    private void stopConversation() {
        conversationActive = false;
        handler.removeCallbacksAndMessages(null);
        if (speechRecognizer != null) speechRecognizer.cancel();
        if (textToSpeech != null) textToSpeech.stop();
        if (speakButton != null) speakButton.setText(R.string.speak_start);
        if (status != null) status.setText(R.string.speak_stopped);
    }

    private void stopConversationIfNeeded() {
        if (conversationActive || speechRecognizer != null || textToSpeech != null) {
            conversationActive = false;
            handler.removeCallbacksAndMessages(null);
            if (speechRecognizer != null) speechRecognizer.cancel();
            if (textToSpeech != null) textToSpeech.stop();
        }
    }

    private void askLocalGemma(String question) {
        transcript.setText(getString(R.string.speak_you_said, question));
        status.setText(R.string.speak_thinking);
        new Thread(() -> {
            try {
                String answer = generateWithGemma(question);
                runOnUiThread(() -> {
                    transcript.setText(getString(R.string.speak_answer, question, answer));
                    status.setText(R.string.speak_done);
                    if (textToSpeech != null) {
                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            public void onStart(String utteranceId) { }
                            public void onDone(String utteranceId) { handler.post(() -> { if (conversationActive) startListening(); }); }
                            public void onError(String utteranceId) { handler.post(() -> { if (conversationActive) startListening(); }); }
                        });
                        textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "gemma-answer");
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> status.setText(R.string.speak_model_error));
            }
        }).start();
    }

    private String generateWithGemma(String question) throws Exception {
        if (localModel == null) {
            localModelClass = Class.forName("com.google.mediapipe.tasks.genai.llminference.LlmInference");
            Class<?> optionsClass = Class.forName("com.google.mediapipe.tasks.genai.llminference.LlmInference$LlmInferenceOptions");
            Object builder = optionsClass.getMethod("builder").invoke(null);
            builder.getClass().getMethod("setModelPath", String.class).invoke(builder, modelFile().getAbsolutePath());
            builder.getClass().getMethod("setMaxTokens", int.class).invoke(builder, 128);
            Object options = builder.getClass().getMethod("build").invoke(builder);
            Method create = localModelClass.getMethod("createFromOptions", android.content.Context.class, optionsClass);
            localModel = create.invoke(null, this, options);
        }
        String prompt = "You are SeniorConnect. Answer older adults clearly, kindly, and briefly. " +
                "Use one idea at a time. If unsure, say so.\nUser: " + question + "\nAssistant:";
        return (String) localModelClass.getMethod("generateResponse", String.class).invoke(localModel, prompt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else if (status != null) {
            status.setText(R.string.speak_permission);
        }
    }

    @Override
    protected void onDestroy() {
        conversationActive = false;
        handler.removeCallbacksAndMessages(null);
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (textToSpeech != null) textToSpeech.shutdown();
        if (localModel != null && localModelClass != null) {
            try { localModelClass.getMethod("close").invoke(localModel); } catch (Exception ignored) { }
        }
        super.onDestroy();
    }
}
