package lk.matale.saferide;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Matale SafeRide - school transport tracking prototype.
 *
 * Features requested by the project:
 * 1) Driver and Parent login
 * 2) Registration form
 * 3) Live map demonstration
 * 4) Start / end trip
 * 5) Child pickup / drop-off confirmation
 * 6) Emergency button
 * 7) Delay notifications
 *
 * The UI is built programmatically to keep the project dependency-free and easy to open in Android Studio.
 */
public class MainActivity extends Activity {

    private static final int GREEN = Color.rgb(23, 129, 104);
    private static final int GREEN_DARK = Color.rgb(15, 87, 72);
    private static final int MINT = Color.rgb(232, 247, 242);
    private static final int NAVY = Color.rgb(35, 52, 62);
    private static final int GREY = Color.rgb(103, 117, 124);
    private static final int LIGHT = Color.rgb(247, 250, 249);
    private static final int ORANGE = Color.rgb(245, 166, 35);
    private static final int RED = Color.rgb(218, 72, 72);
    private static final int BORDER = Color.rgb(219, 229, 225);

    private final SchoolTransportState tripState = new SchoolTransportState();
    private SharedPreferences prefs;
    private LinearLayout root;
    private SimulatedMapView currentMap;
    private String selectedRole = "Driver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(GREEN_DARK);
        prefs = getSharedPreferences("matale_saferide", MODE_PRIVATE);
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();
        showLoginScreen();
    }

    // -------------------------------------------------------------------------
    // LOGIN / REGISTRATION
    // -------------------------------------------------------------------------

    private void showLoginScreen() {
        ScrollView scroll = page();
        root = content(scroll);
        setContentView(scroll);

        TextView badge = text("MATALE • SCHOOL TRANSPORT", 12, GREEN_DARK, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(rounded(MINT, 30, MINT));
        root.addView(badge, lpMatch(dp(38)));

        addSpace(22);
        TextView logo = text("SR", 30, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(rounded(GREEN, 28, GREEN));
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(70), dp(70));
        logoLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logo, logoLp);

        addSpace(16);
        TextView title = text("Matale SafeRide", 29, NAVY, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView subtitle = text("Safe journeys. Calm parents. Focused drivers.", 15, GREY, false);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        addSpace(28);
        root.addView(sectionLabel("LOGIN AS"));
        LinearLayout roleRow = new LinearLayout(this);
        roleRow.setOrientation(LinearLayout.HORIZONTAL);
        Button driver = pillButton("Driver", true);
        Button parent = pillButton("Parent", false);
        roleRow.addView(driver, weightLp());
        addHorizontalSpace(roleRow, 10);
        roleRow.addView(parent, weightLp());
        root.addView(roleRow);

        EditText email = input("Email", "driver@demo.lk", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password = input("Password", "1234", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(email);
        root.addView(password);

        driver.setOnClickListener(v -> {
            selectedRole = "Driver";
            styleRoleButtons(driver, parent, true);
            email.setText("driver@demo.lk");
        });
        parent.setOnClickListener(v -> {
            selectedRole = "Parent";
            styleRoleButtons(driver, parent, false);
            email.setText("parent@demo.lk");
        });

        Button login = primaryButton("Login securely");
        root.addView(login, lpMatch(dp(54)));
        login.setOnClickListener(v -> {
            if (email.getText().toString().trim().isEmpty() || password.getText().toString().isEmpty()) {
                toast("Please enter email and password.");
                return;
            }
            // Demo credentials are intentionally simple for a viva. Registered users are also accepted.
            if (selectedRole.equals("Driver")) showDriverDashboard();
            else showParentDashboard();
        });

        Button register = textButton("New user? Create an account");
        root.addView(register, lpMatch(dp(50)));
        register.setOnClickListener(v -> showRegisterScreen());

        addSpace(18);
        TextView demo = text("Demo: driver@demo.lk / 1234   •   parent@demo.lk / 1234", 12, GREY, false);
        demo.setGravity(Gravity.CENTER);
        root.addView(demo);
    }

    private void showRegisterScreen() {
        ScrollView scroll = page();
        root = content(scroll);
        setContentView(scroll);

        topBar("Create account", "Register as a driver or parent");
        root.addView(sectionLabel("ACCOUNT TYPE"));
        LinearLayout roleRow = new LinearLayout(this);
        roleRow.setOrientation(LinearLayout.HORIZONTAL);
        Button driver = pillButton("Driver", true);
        Button parent = pillButton("Parent", false);
        roleRow.addView(driver, weightLp());
        addHorizontalSpace(roleRow, 10);
        roleRow.addView(parent, weightLp());
        root.addView(roleRow);

        final String[] role = {"Driver"};
        driver.setOnClickListener(v -> { role[0] = "Driver"; styleRoleButtons(driver, parent, true); });
        parent.setOnClickListener(v -> { role[0] = "Parent"; styleRoleButtons(driver, parent, false); });

        EditText name = input("Full name", "", InputType.TYPE_CLASS_TEXT);
        EditText phone = input("Mobile number", "", InputType.TYPE_CLASS_PHONE);
        EditText email = input("Email", "", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText vehicleOrChild = input("Vehicle number / Child name", "", InputType.TYPE_CLASS_TEXT);
        EditText password = input("Password", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(name); root.addView(phone); root.addView(email); root.addView(vehicleOrChild); root.addView(password);

        Button create = primaryButton("Create account");
        root.addView(create, lpMatch(dp(54)));
        create.setOnClickListener(v -> {
            if (name.getText().toString().trim().isEmpty() || email.getText().toString().trim().isEmpty() || password.getText().toString().isEmpty()) {
                toast("Name, email and password are required.");
                return;
            }
            prefs.edit()
                    .putString("registered_name", name.getText().toString().trim())
                    .putString("registered_email", email.getText().toString().trim())
                    .putString("registered_role", role[0])
                    .apply();
            new AlertDialog.Builder(this)
                    .setTitle("Registration successful")
                    .setMessage("Your " + role[0].toLowerCase(Locale.ROOT) + " account is ready for this prototype.")
                    .setPositiveButton("Go to login", (d, w) -> showLoginScreen())
                    .show();
        });
        root.addView(textButton("Back to login"), lpMatch(dp(48)));
        ((Button) root.getChildAt(root.getChildCount()-1)).setOnClickListener(v -> showLoginScreen());
    }

    // -------------------------------------------------------------------------
    // DRIVER EXPERIENCE
    // -------------------------------------------------------------------------

    private void showDriverDashboard() {
        ScrollView scroll = page();
        root = content(scroll);
        setContentView(scroll);

        dashboardHeader("Good morning, Driver", "Van WP CAB-4581 • Matale Route 03");
        root.addView(statusHero());
        addSpace(14);

        currentMap = mapCard("LIVE ROUTE", tripState.isTripActive() ? "Live • GPS simulation running" : "Ready • route preview");
        currentMap.setTripActive(tripState.isTripActive());

        addSpace(14);
        root.addView(sectionLabel("TRIP CONTROLS"));
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        Button start = actionButton("Start trip", GREEN);
        Button end = actionButton("End trip", NAVY);
        controls.addView(start, weightLp());
        addHorizontalSpace(controls, 10);
        controls.addView(end, weightLp());
        root.addView(controls);

        start.setOnClickListener(v -> {
            tripState.startTrip();
            prefs.edit().putBoolean("trip_active", true).apply();
            sendLocalNotification("Trip started", "School van WP CAB-4581 has started the Matale route.");
            toast("Trip started. Live route is now active.");
            showDriverDashboard();
        });
        end.setOnClickListener(v -> {
            tripState.endTrip();
            prefs.edit().putBoolean("trip_active", false).apply();
            if (currentMap != null) currentMap.setTripActive(false);
            sendLocalNotification("Trip ended", "The school transport trip has been completed.");
            toast("Trip ended safely.");
            showDriverDashboard();
        });

        addSpace(18);
        root.addView(sectionLabel("CHILDREN ON THIS ROUTE"));
        for (int i = 0; i < tripState.getChildren().size(); i++) {
            root.addView(childDriverCard(i));
        }

        addSpace(8);
        LinearLayout alerts = new LinearLayout(this);
        alerts.setOrientation(LinearLayout.HORIZONTAL);
        Button delay = actionButton("Send delay", ORANGE);
        Button emergency = actionButton("EMERGENCY", RED);
        alerts.addView(delay, weightLp());
        addHorizontalSpace(alerts, 10);
        alerts.addView(emergency, weightLp());
        root.addView(alerts);
        delay.setOnClickListener(v -> showDelayDialog());
        emergency.setOnClickListener(v -> showEmergencyDialog());

        addSpace(12);
        root.addView(textButton("Logout"), lpMatch(dp(46)));
        ((Button) root.getChildAt(root.getChildCount()-1)).setOnClickListener(v -> showLoginScreen());
    }

    private View statusHero() {
        LinearLayout card = card();
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView dot = text(tripState.isTripActive() ? "LIVE" : "READY", 12, tripState.isTripActive() ? GREEN : GREY, true);
        dot.setBackground(rounded(tripState.isTripActive() ? MINT : Color.rgb(239,242,241), 24, BORDER));
        dot.setGravity(Gravity.CENTER);
        top.addView(dot, new LinearLayout.LayoutParams(dp(72), dp(34)));
        TextView time = text(nowTime(), 13, GREY, false);
        time.setGravity(Gravity.RIGHT);
        top.addView(time, weightLp());
        card.addView(top);

        TextView route = text("Matale Town → Central School", 20, NAVY, true);
        card.addView(route);
        TextView detail = text("4 children • Approx. 8.4 km • Morning service", 13, GREY, false);
        card.addView(detail);
        return card;
    }

    private View childDriverCard(final int index) {
        SchoolTransportState.Child child = tripState.getChildren().get(index);
        LinearLayout card = card();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = text(initials(child.getName()), 14, GREEN_DARK, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(rounded(MINT, 40, MINT));
        row.addView(avatar, new LinearLayout.LayoutParams(dp(46), dp(46)));
        addHorizontalSpace(row, 12);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(text(child.getName(), 15, NAVY, true));
        info.addView(text(child.getStop(), 12, GREY, false));
        row.addView(info, weightLp());

        TextView status = text(statusLabel(child.getStatus()), 11, statusColor(child.getStatus()), true);
        row.addView(status);
        card.addView(row);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button pickup = miniButton("Picked up");
        Button drop = miniButton("Dropped off");
        actions.addView(pickup, weightLp());
        addHorizontalSpace(actions, 8);
        actions.addView(drop, weightLp());
        card.addView(actions);

        pickup.setEnabled(child.getStatus() == SchoolTransportState.ChildStatus.WAITING);
        drop.setEnabled(child.getStatus() == SchoolTransportState.ChildStatus.PICKED_UP);
        pickup.setOnClickListener(v -> {
            tripState.pickupChild(index);
            sendLocalNotification("Child picked up", child.getName() + " boarded safely at " + child.getStop() + ".");
            toast(child.getName() + " marked as picked up.");
            showDriverDashboard();
        });
        drop.setOnClickListener(v -> {
            tripState.dropOffChild(index);
            sendLocalNotification("Child dropped off", child.getName() + " was safely dropped off.");
            toast(child.getName() + " marked as dropped off.");
            showDriverDashboard();
        });
        return card;
    }

    private void showDelayDialog() {
        final String[] options = {"10 min • traffic", "20 min • heavy traffic", "30 min • road delay", "Vehicle breakdown"};
        new AlertDialog.Builder(this)
                .setTitle("Send delay notification")
                .setItems(options, (dialog, which) -> {
                    String message = "Delay update: " + options[which] + ". Driver sent this at " + nowTime() + ".";
                    tripState.setDelayMessage(message);
                    prefs.edit().putString("delay_message", message).apply();
                    sendLocalNotification("School transport delay", message);
                    toast("Delay notification sent to parents.");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEmergencyDialog() {
        final String[] options = {"Vehicle breakdown", "Road accident", "Medical emergency", "Unsafe road / obstruction"};
        new AlertDialog.Builder(this)
                .setTitle("Emergency alert")
                .setMessage("Select the incident. The prototype will alert all linked parents immediately.")
                .setItems(options, (dialog, which) -> {
                    String message = "EMERGENCY: " + options[which] + " near Matale route. Driver alert at " + nowTime() + ".";
                    tripState.setEmergencyMessage(message);
                    prefs.edit().putString("emergency_message", message).apply();
                    sendLocalNotification("Emergency - Matale SafeRide", message);
                    new AlertDialog.Builder(this)
                            .setTitle("Emergency alert sent")
                            .setMessage("All linked parent accounts have been notified in this prototype.")
                            .setPositiveButton("OK", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // -------------------------------------------------------------------------
    // PARENT EXPERIENCE
    // -------------------------------------------------------------------------

    private void showParentDashboard() {
        // Restore the latest locally stored driver messages so the viva can switch roles.
        String delay = prefs.getString("delay_message", tripState.getDelayMessage());
        String emergency = prefs.getString("emergency_message", tripState.getEmergencyMessage());
        boolean tripActive = prefs.getBoolean("trip_active", tripState.isTripActive());

        ScrollView scroll = page();
        root = content(scroll);
        setContentView(scroll);

        dashboardHeader("Hello, Parent", "Tracking Nethmi • Matale Route 03");

        if (emergency != null && !emergency.isEmpty()) {
            root.addView(alertCard("EMERGENCY UPDATE", emergency, RED));
        }
        if (delay != null && !delay.isEmpty()) {
            root.addView(alertCard("DELAY NOTICE", delay, ORANGE));
        }

        LinearLayout child = card();
        child.addView(text("Nethmi Perera", 21, NAVY, true));
        child.addView(text("Pallepola Junction → Central School", 13, GREY, false));
        addSpaceTo(child, 8);
        TextView status = text(tripActive ? "Vehicle is on the way" : "Waiting for next trip", 14, tripActive ? GREEN : GREY, true);
        child.addView(status);
        root.addView(child);

        addSpace(14);
        currentMap = mapCard("LIVE VEHICLE LOCATION", tripActive ? "Updated moments ago • demo GPS" : "Trip currently inactive");
        currentMap.setTripActive(tripActive);

        addSpace(14);
        LinearLayout eta = card();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(text("Estimated arrival", 12, GREY, false));
        left.addView(text(tripActive ? "07:28 AM" : "--:--", 25, NAVY, true));
        row.addView(left, weightLp());
        TextView minutes = text(tripActive ? "8 min" : "Offline", 16, tripActive ? GREEN : GREY, true);
        minutes.setGravity(Gravity.CENTER);
        minutes.setBackground(rounded(tripActive ? MINT : Color.rgb(239,242,241), 28, BORDER));
        row.addView(minutes, new LinearLayout.LayoutParams(dp(86), dp(44)));
        eta.addView(row);
        root.addView(eta);

        addSpace(14);
        root.addView(sectionLabel("SAFETY STATUS"));
        root.addView(parentStatusRow("Driver", "Sunil Jayawardena", "Verified"));
        root.addView(parentStatusRow("Vehicle", "WP CAB-4581", "Active"));
        root.addView(parentStatusRow("Child", "Nethmi Perera", "Linked"));

        addSpace(10);
        TextView info = text("Pickup and drop-off confirmations will appear as notifications when the driver marks each event.", 13, GREY, false);
        info.setPadding(dp(12), dp(8), dp(12), dp(8));
        root.addView(info);

        root.addView(textButton("Logout"), lpMatch(dp(48)));
        ((Button) root.getChildAt(root.getChildCount()-1)).setOnClickListener(v -> showLoginScreen());
    }

    private View parentStatusRow(String label, String value, String state) {
        LinearLayout card = cardCompact();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(text(label, 11, GREY, false));
        info.addView(text(value, 15, NAVY, true));
        row.addView(info, weightLp());
        TextView badge = text(state, 11, GREEN, true);
        badge.setBackground(rounded(MINT, 24, MINT));
        badge.setGravity(Gravity.CENTER);
        row.addView(badge, new LinearLayout.LayoutParams(dp(75), dp(32)));
        card.addView(row);
        return card;
    }

    // -------------------------------------------------------------------------
    // REUSABLE UI COMPONENTS
    // -------------------------------------------------------------------------

    private SimulatedMapView mapCard(String title, String subtitle) {
        LinearLayout wrapper = card();
        wrapper.addView(text(title, 12, GREEN_DARK, true));
        wrapper.addView(text(subtitle, 13, GREY, false));
        SimulatedMapView map = new SimulatedMapView(this);
        LinearLayout.LayoutParams mapLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(245));
        mapLp.topMargin = dp(12);
        wrapper.addView(map, mapLp);
        root.addView(wrapper);
        return map;
    }

    private View alertCard(String title, String message, int color) {
        LinearLayout card = card();
        card.setBackground(rounded(lighten(color), 18, color));
        card.addView(text(title, 12, color, true));
        card.addView(text(message, 13, NAVY, false));
        return card;
    }

    private void dashboardHeader(String title, String subtitle) {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("SR", 16, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(rounded(GREEN, 22, GREEN));
        top.addView(logo, new LinearLayout.LayoutParams(dp(46), dp(46)));
        addHorizontalSpace(top, 12);
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(text(title, 21, NAVY, true));
        labels.addView(text(subtitle, 12, GREY, false));
        top.addView(labels, weightLp());
        root.addView(top);
        addSpace(20);
    }

    private void topBar(String title, String subtitle) {
        TextView back = text("‹ Back", 14, GREEN, true);
        back.setPadding(0, 0, 0, dp(14));
        back.setOnClickListener(v -> showLoginScreen());
        root.addView(back);
        root.addView(text(title, 27, NAVY, true));
        root.addView(text(subtitle, 14, GREY, false));
        addSpace(20);
    }

    private ScrollView page() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(LIGHT);
        return scroll;
    }

    private LinearLayout content(ScrollView scroll) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(22), dp(24), dp(22), dp(34));
        scroll.addView(c, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return c;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(rounded(Color.WHITE, 18, BORDER));
        LinearLayout.LayoutParams lp = lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout cardCompact() {
        LinearLayout card = card();
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        return card;
    }

    private TextView sectionLabel(String s) {
        TextView t = text(s, 11, GREY, true);
        t.setLetterSpacing(.08f);
        t.setPadding(0, dp(5), 0, dp(9));
        return t;
    }

    private EditText input(String hint, String text, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(text);
        e.setTextColor(NAVY);
        e.setHintTextColor(Color.rgb(135, 146, 150));
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setInputType(inputType);
        e.setPadding(dp(16), 0, dp(16), 0);
        e.setBackground(rounded(Color.WHITE, 14, BORDER));
        LinearLayout.LayoutParams lp = lpMatch(dp(54));
        lp.bottomMargin = dp(12);
        e.setLayoutParams(lp);
        return e;
    }

    private Button primaryButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(rounded(GREEN, 15, GREEN));
        return b;
    }

    private Button actionButton(String s, int color) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(rounded(color, 14, color));
        LinearLayout.LayoutParams lp = weightLp();
        lp.height = dp(50);
        b.setLayoutParams(lp);
        return b;
    }

    private Button miniButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(GREEN_DARK);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setBackground(rounded(MINT, 12, BORDER));
        LinearLayout.LayoutParams lp = weightLp();
        lp.height = dp(42);
        lp.topMargin = dp(12);
        b.setLayoutParams(lp);
        return b;
    }

    private Button textButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(GREEN_DARK);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }

    private Button pillButton(String s, boolean selected) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(selected ? Color.WHITE : GREY);
        b.setBackground(rounded(selected ? GREEN : Color.WHITE, 30, selected ? GREEN : BORDER));
        LinearLayout.LayoutParams lp = weightLp();
        lp.height = dp(46);
        lp.bottomMargin = dp(14);
        b.setLayoutParams(lp);
        return b;
    }

    private void styleRoleButtons(Button driver, Button parent, boolean driverSelected) {
        driver.setTextColor(driverSelected ? Color.WHITE : GREY);
        driver.setBackground(rounded(driverSelected ? GREEN : Color.WHITE, 30, driverSelected ? GREEN : BORDER));
        parent.setTextColor(!driverSelected ? Color.WHITE : GREY);
        parent.setBackground(rounded(!driverSelected ? GREEN : Color.WHITE, 30, !driverSelected ? GREEN : BORDER));
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setLineSpacing(0, 1.12f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable rounded(int fill, int radiusDp, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(dp(1), stroke);
        return g;
    }

    private LinearLayout.LayoutParams lpMatch(int height) {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    private LinearLayout.LayoutParams weightLp() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private void addSpace(int heightDp) {
        View v = new View(this);
        root.addView(v, new LinearLayout.LayoutParams(1, dp(heightDp)));
    }

    private void addSpaceTo(LinearLayout parent, int heightDp) {
        parent.addView(new View(this), new LinearLayout.LayoutParams(1, dp(heightDp)));
    }

    private void addHorizontalSpace(LinearLayout parent, int widthDp) {
        parent.addView(new View(this), new LinearLayout.LayoutParams(dp(widthDp), 1));
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        return (parts[0].substring(0,1) + parts[parts.length-1].substring(0,1)).toUpperCase(Locale.ROOT);
    }

    private String statusLabel(SchoolTransportState.ChildStatus status) {
        if (status == SchoolTransportState.ChildStatus.PICKED_UP) return "PICKED UP";
        if (status == SchoolTransportState.ChildStatus.DROPPED_OFF) return "DROPPED";
        return "WAITING";
    }

    private int statusColor(SchoolTransportState.ChildStatus status) {
        if (status == SchoolTransportState.ChildStatus.PICKED_UP) return ORANGE;
        if (status == SchoolTransportState.ChildStatus.DROPPED_OFF) return GREEN;
        return GREY;
    }

    private String nowTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    private int lighten(int color) {
        int r = Math.min(255, (Color.red(color) + 255 * 4) / 5);
        int g = Math.min(255, (Color.green(color) + 255 * 4) / 5);
        int b = Math.min(255, (Color.blue(color) + 255 * 4) / 5);
        return Color.rgb(r,g,b);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // -------------------------------------------------------------------------
    // LOCAL ANDROID NOTIFICATIONS
    // -------------------------------------------------------------------------

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "saferide_alerts", "School Transport Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Trip, pickup, delay and emergency updates");
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }
    }

    private void sendLocalNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, "saferide_alerts")
                : new Notification.Builder(this);
        builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        manager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }
}

