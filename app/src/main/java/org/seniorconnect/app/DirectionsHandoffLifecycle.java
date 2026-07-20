package org.seniorconnect.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * Deterministic, in-memory lifecycle for the confirmed Google Maps handoff.
 * It intentionally persists neither the selected location nor the receipt.
 */
public final class DirectionsHandoffLifecycle {
    public static final String TOOL = "open_google_maps_directions";
    public static final String TARGET_ID = "app_google_maps";
    public static final String CHANNEL = "google_maps_navigation";
    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final long RECEIPT_TTL_MILLIS = 120_000L;

    public Proposal propose(double latitude, double longitude) {
        return new Proposal(latitude, longitude);
    }

    /** Every proposal receives a deterministic policy result before a confirmation can be shown. */
    public PolicyDecision evaluate(Proposal proposal) {
        if (proposal == null || !isValidCoordinate(proposal.latitude, proposal.longitude)) {
            return PolicyDecision.DENY;
        }
        return PolicyDecision.CONFIRM;
    }

    /** Creates a single-use receipt only after the person selects Directions for this exact proposal. */
    public ConfirmationReceipt confirm(Proposal proposal, PolicyDecision decision) {
        if (decision != PolicyDecision.CONFIRM) return null;
        long now = SystemClock.elapsedRealtime();
        return new ConfirmationReceipt(
                "receipt_" + UUID.randomUUID().toString().replace("-", ""),
                proposal.hash(),
                now,
                now + RECEIPT_TTL_MILLIS);
    }

    /** Invokes only a valid, unexpired receipt and reports no more than Android actually proves. */
    public ToolResult invoke(Proposal proposal, ConfirmationReceipt receipt, IntentLauncher launcher) {
        if (proposal == null || receipt == null || launcher == null
                || receipt.consumed || SystemClock.elapsedRealtime() > receipt.expiresAtElapsedMillis
                || !receipt.proposalHash.equals(proposal.hash())) {
            return ToolResult.failed("RECEIPT_INVALID");
        }
        receipt.consumed = true;
        try {
            launcher.launch(createIntent(proposal));
            // startActivity accepted the targeted handoff; it does not prove navigation started or arrived.
            return ToolResult.opened();
        } catch (ActivityNotFoundException | SecurityException error) {
            return ToolResult.failed("GOOGLE_MAPS_UNAVAILABLE");
        }
    }

    private static Intent createIntent(Proposal proposal) {
        String destination = String.format(Locale.US, "%.6f,%.6f", proposal.latitude, proposal.longitude);
        Uri directions = Uri.parse("google.navigation:q=" + Uri.encode(destination) + "&mode=d");
        return new Intent(Intent.ACTION_VIEW, directions).setPackage(GOOGLE_MAPS_PACKAGE);
    }

    private static boolean isValidCoordinate(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    public enum PolicyDecision { ALLOW, CONFIRM, CLARIFY, DENY }

    public static final class Proposal {
        public final String tool = TOOL;
        public final String targetId = TARGET_ID;
        public final String channel = CHANNEL;
        final double latitude;
        final double longitude;

        Proposal(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        String hash() {
            String value = TOOL + "|" + TARGET_ID + "|"
                    + String.format(Locale.US, "%.6f,%.6f", latitude, longitude);
            try {
                byte[] bytes = MessageDigest.getInstance("SHA-256")
                        .digest(value.getBytes(StandardCharsets.UTF_8));
                StringBuilder hash = new StringBuilder(bytes.length * 2);
                for (byte item : bytes) hash.append(String.format(Locale.US, "%02x", item));
                return hash.toString();
            } catch (NoSuchAlgorithmException error) {
                throw new IllegalStateException("SHA-256 must be available", error);
            }
        }
    }

    public static final class ConfirmationReceipt {
        public final String schemaVersion = "1.0";
        public final String receiptId;
        public final String proposalHash;
        public final String tool = TOOL;
        public final String targetId = TARGET_ID;
        public final String channel = CHANNEL;
        public final long issuedAtElapsedMillis;
        public final long expiresAtElapsedMillis;
        private boolean consumed;

        ConfirmationReceipt(String receiptId, String proposalHash, long issuedAtElapsedMillis,
                            long expiresAtElapsedMillis) {
            this.receiptId = receiptId;
            this.proposalHash = proposalHash;
            this.issuedAtElapsedMillis = issuedAtElapsedMillis;
            this.expiresAtElapsedMillis = expiresAtElapsedMillis;
        }
    }

    public static final class ToolResult {
        public final String schemaVersion = "1.0";
        public final String tool = TOOL;
        public final String targetId = TARGET_ID;
        public final String status;
        public final String resultCode;
        public final String errorCode;

        private ToolResult(String status, String resultCode, String errorCode) {
            this.status = status;
            this.resultCode = resultCode;
            this.errorCode = errorCode;
        }

        static ToolResult opened() { return new ToolResult("success", "DIRECTIONS_HANDOFF_OPENED", null); }
        static ToolResult failed(String errorCode) { return new ToolResult("failed", "FAILED", errorCode); }
    }

    public interface IntentLauncher {
        void launch(Intent intent);
    }
}
