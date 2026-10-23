package sa.urtv.screensaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Remote-friendly settings: every row is a button that opens a list. */
public class SettingsActivity extends Activity {
    private static final int DIALOG_THEME = android.R.style.Theme_Material_Dialog_Alert;

    private Prefs prefs;
    private LinearLayout list;

    /** Arabic locale for this screen, so every dialog title, list and button is laid out right-to-left. */
    @Override
    protected void attachBaseContext(android.content.Context base) {
        android.content.res.Configuration c = new android.content.res.Configuration(base.getResources().getConfiguration());
        c.setLocale(new java.util.Locale("ar"));
        c.setLayoutDirection(new java.util.Locale("ar"));
        super.attachBaseContext(base.createConfigurationContext(c));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = new Prefs(this);
        UpdateJob.schedule(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackground(new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xFF0A2016, 0xFF04120C, 0xFF020806}));
        scroll.setVerticalScrollBarEnabled(false);
        android.widget.FrameLayout frame = new android.widget.FrameLayout(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        list.setPadding(dp(8), dp(36), dp(8), dp(48));
        int width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(64), dp(860));
        frame.addView(list, new android.widget.FrameLayout.LayoutParams(width, -2, Gravity.CENTER_HORIZONTAL));
        scroll.addView(frame);
        setContentView(scroll);
        render();
    }

    @Override
    protected void onResume() {   // back in front (from the TV's settings, or opened again over itself): show what is set now
        super.onResume();
        render();
    }

    private void render() {
        list.removeAllViews();
        TextView title = new TextView(this);
        title.setText("تلفزيونك");
        title.setTextColor(0xFFF6EFE0);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        title.setTypeface(font("ThmanyahDisplay-Bold"));
        list.addView(title);
        TextView sub = new TextView(this);
        sub.setText("إعدادات شاشة التوقف");
        sub.setTextColor(0xFFC6BBA4);
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        sub.setTypeface(font("Thmanyah-Regular"));
        sub.setPadding(0, 0, 0, dp(18));
        list.addView(sub);

        row("معاينة الشاشة", "شاهد الشاشة الآن", R.drawable.ic_play, new Runnable() { public void run() {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class)); } }).requestFocus();

        header("الموقع");
        row("المدينة", prefs.city()[0], R.drawable.ic_place, new Runnable() { public void run() { pickCity(); } });

        header("المباريات");
        row("الفرق المتابعة (" + arabicNum(prefs.teams().size()) + "/" + arabicNum(Prefs.MAX_TEAMS) + ")", teamsLabel(),
            R.drawable.ic_trophy, new Runnable() { public void run() { manageTeams(); } });

        row("الدوريات كاملة", leaguesLabel(), R.drawable.ic_trophy,
            new Runnable() { public void run() { pickLeagues(); } });

        row("رياضات أخرى", labels(Prefs.EVENTS, prefs.list("events"), "بدون"), R.drawable.ic_trophy,
            new Runnable() { public void run() { pickMany("events", "رياضات أخرى", Prefs.EVENTS, "بدون اختيار = بدون"); } });

        header("العرض");
        row("اللغة", Prefs.languageLabel(prefs.language()), R.drawable.ic_language,
            new Runnable() { public void run() { pickLanguage(); } });
        row("الأرقام", Prefs.digitsLabel(prefs.digits()), R.drawable.ic_language,
            new Runnable() { public void run() { pickDigits(); } });
        row("تنبيه قبل الأذان", prefs.adhanBar() ? "يعمل · شريط في أعلى الشاشة آخر ١٠ دقائق، ثم حتى الإقامة" : "متوقف", R.drawable.ic_eye,
            new Runnable() { public void run() { prefs.setAdhanBar(!prefs.adhanBar()); render(); } });
        row("المناسبات الوطنية", Prefs.nationalLabel(prefs.national()), R.drawable.ic_eye,
            new Runnable() { public void run() { pickNational(); } });
        row("الخانة اليسرى", Prefs.slotLabel(prefs.slot()), R.drawable.ic_eye,
            new Runnable() { public void run() { pickSlot(); } });
        if ("countdown".equals(prefs.slot()) || !prefs.countdownDate().isEmpty())
            row("العدّ التنازلي", countdownLabel(), R.drawable.ic_eye,
                new Runnable() { public void run() { manageCountdown(); } });

        header("الأسواق (أعلى اليسار)");
        row("الأسواق (" + arabicNum(prefs.markets().size()) + "/" + arabicNum(Prefs.MAX_MARKETS) + ")", marketsLabel(),
            R.drawable.ic_trophy, new Runnable() { public void run() { pickMarkets(); } });
        row("عرض الأسواق", Prefs.mkStyleLabel(prefs.mkStyle()), R.drawable.ic_eye,
            new Runnable() { public void run() { pickMkStyle(); } });

        header("بجانب الساعة");
        row("المحتوى", Prefs.panelLabel(prefs.panel()), R.drawable.ic_eye, new Runnable() { public void run() { pickPanel(-1); } });
        row("شكل البطاقة", Prefs.cardStyleLabel(prefs.cardStyle()), R.drawable.ic_eye,
            new Runnable() { public void run() { pickCardStyle(); } });

        header("الشاشة الرئيسية (تلفزيونك)");
        // up to three cards left of the clock; each place takes any card, or «بدون» (the ones after it move up)
        for (int k = 0; k < Prefs.HOME_CARDS; k++) {
            final int place = k;
            row(HOME_CARD_NAMES[k], Prefs.panelLabel(prefs.homePanelAt(k), "بدون"), R.drawable.ic_eye,
                new Runnable() { public void run() { pickPanel(place); } });
        }
        row("التحية والطقس", prefs.greeting() ? "تعمل" : "متوقفة", R.drawable.ic_eye,
            new Runnable() { public void run() { prefs.setGreeting(!prefs.greeting()); render(); } });
        row("ترتيب التطبيقات المفضلة", "قائمة ترتّب فيها المفضلة بالأسهم", R.drawable.ic_star,
            new Runnable() { public void run() { openDockSettings(); } });

        if ("shows".equals(prefs.panel()) || prefs.homePanels().contains("shows")) {   // only matter for the shows card
        header("جديد المسلسلات");
        row("التصنيفات", labels(Prefs.GENRES, prefs.list("genres"), "الكل"),
            R.drawable.ic_category, new Runnable() { public void run() { pickMany("genres", "التصنيفات", Prefs.GENRES); } });
        row("اللغات", labels(Prefs.LANGS, prefs.list("langs"), "الكل"),
            R.drawable.ic_language, new Runnable() { public void run() { pickMany("langs", "اللغات", Prefs.LANGS); } });
        row("مسلسلات مختارة", showsLabel(), R.drawable.ic_star, new Runnable() { public void run() { manageShows(); } });
        row("أقل تقييم", "0".equals(prefs.minRating()) ? "بدون حد" : prefs.minRating(),
            R.drawable.ic_star, new Runnable() { public void run() { pickRating(); } });
        }

        if ("stocks".equals(prefs.panel()) || prefs.homePanels().contains("stocks")) {   // only matters for the stocks card
        header("الأسهم");
        row("قائمة الأسهم (" + arabicNum(prefs.stocks().size()) + "/" + arabicNum(Prefs.MAX_STOCKS) + ")", stocksLabel(),
            R.drawable.ic_star, new Runnable() { public void run() { askStocks(); } });
        }

        header("التوقيت");
        row("تبديل الصورة كل", minutesLabel(Integer.parseInt(prefs.photoMinutes())), R.drawable.ic_sync,
            new Runnable() { public void run() { pickPhotoMinutes(); } });
        row("صوري (" + arabicNum(Own.count(this)) + "/" + arabicNum(Own.MAX) + ")", ownLabel(), R.drawable.ic_eye,
            new Runnable() { public void run() { pickOwn(); } });
        row("بدء شاشة التوقف بعد", idleLabel(), R.drawable.ic_screensaver,
            new Runnable() { public void run() { pickIdle(); } });

        header("التطبيق");
        row("التحديث التلقائي للصفحة", prefs.autoUpdate() ? "يعمل · عند بدء الشاشة، كل ٦ ساعات" : "متوقف", R.drawable.ic_sync, new Runnable() { public void run() {
            prefs.setAutoUpdate(!prefs.autoUpdate()); render(); } });
        View last = row("تحديث الشاشة الآن", "ينزّل أحدث نسخة من الشاشة", R.drawable.ic_sync, new Runnable() { public void run() { updateNow(); } });
        note(last, Updater.versionLines(this));
    }

    // ---------- pickers ----------

    private void pickCity() {
        final Cities.Region[] regions = Cities.REGIONS;
        String[] labels = new String[regions.length + 1];
        labels[0] = "بحث عن مدينة أو قرية…";
        for (int i = 0; i < regions.length; i++) labels[i + 1] = regions[i].name + "  (" + regions[i].cities.length + ")";
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("المنطقة")
            .setItems(labels, (d, i) -> { if (i == 0) searchCity(); else pickCityIn(regions[i - 1]); })
            .show();
    }

    private void pickCityIn(final Cities.Region r) {
        String[] names = new String[r.cities.length];
        int sel = -1;
        for (int i = 0; i < names.length; i++) {
            names[i] = r.cities[i][0];
            if (names[i].equals(prefs.city()[0])) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle(r.name)
            .setSingleChoiceItems(names, sel, (d, i) -> {
                prefs.setCity(r.cities[i][0], r.cities[i][1], r.cities[i][2]);
                d.dismiss();
                render();
            }).show();
    }

    // any place in Saudi Arabia, from Open-Meteo's GeoNames search
    private void searchCity() {
        ask("اسم المدينة أو القرية", q -> fetch("https://geocoding-api.open-meteo.com/v1/search?count=20&language=ar&countryCode=SA&name="
            + Uri.encode(q), json -> {
                JSONArray a = new JSONObject(json).optJSONArray("results");
                if (a == null || a.length() == 0) { toast("لا توجد نتائج"); return; }
                final int n = a.length();
                final String[] names = new String[n], lats = new String[n], lons = new String[n], zones = new String[n], rows = new String[n];
                for (int i = 0; i < n; i++) {
                    JSONObject c = a.getJSONObject(i);
                    names[i] = c.optString("name");
                    lats[i] = String.valueOf(c.optDouble("latitude"));
                    lons[i] = String.valueOf(c.optDouble("longitude"));
                    zones[i] = c.optString("timezone", "");   // the place's own; empty: the TV's (Prefs.cityTz)
                    String region = c.optString("admin1", "");
                    rows[i] = region.isEmpty() ? names[i] : names[i] + "  ·  " + region;
                }
                new AlertDialog.Builder(this, DIALOG_THEME).setTitle("اختر المكان")
                    .setItems(rows, (d, i) -> { prefs.setCity(names[i], lats[i], lons[i], zones[i]); render(); }).show();
            }));
    }

    /** Followed teams: untick to remove, or add one (presets, league browsing, search). Up to 20. */
    private void manageTeams() {
        final List<String> cur = prefs.teams();
        String[] labels = new String[cur.size()];
        final boolean[] keep = new boolean[cur.size()];
        for (int i = 0; i < cur.size(); i++) { labels[i] = cur.get(i).split("\\|", 2)[1]; keep[i] = true; }
        AlertDialog.Builder b = new AlertDialog.Builder(this, DIALOG_THEME)
            .setTitle(cur.isEmpty() ? "لا توجد فرق متابعة" : "الفرق المتابعة (ألغِ التحديد للحذف)")
            .setPositiveButton("حفظ", (d, w) -> {
                List<String> v = new ArrayList<>();
                for (int i = 0; i < cur.size(); i++) if (keep[i]) v.add(cur.get(i));
                prefs.setList("teams", v);
                render();
            })
            .setNegativeButton("إلغاء", null);
        if (cur.size() < Prefs.MAX_TEAMS) b.setNeutralButton("إضافة فريق", (d, w) -> pickTeam(0));
        if (!cur.isEmpty()) b.setMultiChoiceItems(labels, keep, (d, i, checked) -> keep[i] = checked);
        b.show();
    }

    private void addTeam(String id, String name) {
        if (prefs.addTeam(id, name)) toast("تمت متابعة " + name);
        else toast(prefs.teams().size() >= Prefs.MAX_TEAMS ? "الحد الأعلى ٢٠ فريقاً" : "متابَع مسبقاً");
        render();
    }

    private void pickTeam(final int slot) {
        final List<String[]> opts = new ArrayList<>();
        for (String[] t : Prefs.TEAM_PRESETS) opts.add(t);
        String[] labels = new String[opts.size() + 2];
        for (int i = 0; i < opts.size(); i++) labels[i] = opts.get(i)[1];
        labels[opts.size()] = "تصفح الدوريات…";
        labels[opts.size() + 1] = "بحث عن أي فريق أو منتخب…";
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("إضافة فريق")
            .setItems(labels, (d, i) -> {
                if (i < opts.size()) addTeam(opts.get(i)[0], opts.get(i)[1]);
                else if (i == opts.size()) pickLeague(slot);
                else searchTeam(slot);
            }).show();
    }

    /** Our bundled list first (every club of these leagues, offline); TheSportsDB only if that is missing. */
    private void pickLeague(final int slot) {
        final String[] labels = new String[Prefs.LEAGUES.length + 1];
        for (int i = 0; i < Prefs.LEAGUES.length; i++) labels[i] = Prefs.LEAGUES[i][1];
        labels[Prefs.LEAGUES.length] = "المنتخبات";
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("الدوري")
            .setItems(labels, (d, i) -> {
                String league = i < Prefs.LEAGUES.length ? Prefs.LEAGUES[i][0] : Teams.NATIONAL;
                List<TeamSearch.Team> clubs = Teams.inLeague(this, league);
                if (!clubs.isEmpty()) showOurs(clubs, labels[i], false);
                else if (i < Prefs.LEAGUES.length)
                    fetch(SPORTS + "search_all_teams.php?l=" + Uri.encode(league), json -> showTeams(slot, json, labels[i], league));
                else toast("تعذّر تحميل القائمة، جرّب البحث عن الفريق");
            }).show();
    }

    private void searchTeam(final int slot) {
        ask("اسم الفريق بالعربية أو الإنجليزية (مثل الهلال أو Real Madrid)", q -> {
            List<TeamSearch.Team> found = Teams.search(this, q);
            if (!found.isEmpty()) showOurs(found.subList(0, Math.min(found.size(), 30)), "اختر الفريق", true);
            else if (!Arabic.hasArabic(q))   // a club outside our list: TheSportsDB's search, in English only
                fetch(SPORTS + "searchteams.php?t=" + Uri.encode(q), json -> showTeams(slot, json, "اختر الفريق", null));
            else toast("لا توجد نتائج، جرّب اسم الفريق بالإنجليزية");
        });
    }

    /** Clubs from our list: their Arabic names are already there, so following one needs no network. */
    private void showOurs(final List<TeamSearch.Team> clubs, String title, boolean withLeague) {
        String[] rows = new String[clubs.size()];
        for (int i = 0; i < rows.length; i++) {
            TeamSearch.Team t = clubs.get(i);
            rows[i] = Teams.label(this, t);
            if (withLeague && t.leagues.length > 0)
                rows[i] += "  ·  " + (Teams.NATIONAL.equals(t.leagues[0]) ? "منتخب" : leagueAr(t.leagues[0]));
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle(title)
            .setItems(rows, (d, i) -> {
                TeamSearch.Team t = clubs.get(i);
                addTeam(t.id, t.ar.isEmpty() ? Arabic.translit(t.en) : t.ar);
            }).show();
    }

    private static final String SPORTS = "https://www.thesportsdb.com/api/v1/json/123/";

    /** TheSportsDB's answer, when our list has nothing. league != null: keep only clubs that really play in it. */
    private void showTeams(final int slot, String json, String title, String league) throws Exception {
        JSONArray all = new JSONObject(json).optJSONArray("teams");
        JSONArray a = new JSONArray();
        for (int i = 0; all != null && i < all.length(); i++) {
            JSONObject t = all.getJSONObject(i);
            if (!"Soccer".equalsIgnoreCase(t.optString("strSport", "Soccer"))) continue;
            boolean inLeague = league == null;
            for (int k = 1; !inLeague && k <= 7; k++)
                inLeague = league.equalsIgnoreCase(t.optString(k == 1 ? "strLeague" : "strLeague" + k));
            if (inLeague) a.put(t);
        }
        if (a.length() == 0) {
            toast(league == null ? "لا توجد نتائج" : "تعذّر تحميل هذا الدوري، جرّب البحث عن الفريق");
            return;
        }
        final int n = Math.min(a.length(), 60);
        final String[] ids = new String[n], names = new String[n], rows = new String[n];
        final TeamSearch.Team[] ours = new TeamSearch.Team[n];
        for (int i = 0; i < n; i++) {
            JSONObject t = a.getJSONObject(i);
            ids[i] = t.optString("idTeam");
            names[i] = t.optString("strTeam");
            ours[i] = Teams.byId(this, ids[i]);
            rows[i] = (ours[i] != null ? Teams.label(this, ours[i]) : Arabic.team(this, names[i]))
                + "  ·  " + leagueAr(t.optString("strLeague", ""));
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle(title)
            .setItems(rows, (d, i) -> {
                if (ours[i] != null && !ours[i].ar.isEmpty()) addTeam(ids[i], ours[i].ar);
                else inArabic(names[i], Arabic.TEAM, ar -> addTeam(ids[i], ar));
            })
            .show();
    }

    private void pickMany(final String key, String title, final String[][] options) {
        pickMany(key, title, options, "بدون اختيار = الكل");
    }

    private void pickMany(final String key, String title, final String[][] options, String hint) {
        List<String> cur = prefs.list(key);
        String[] labels = new String[options.length];
        final boolean[] on = new boolean[options.length];
        for (int i = 0; i < options.length; i++) { labels[i] = options[i][1]; on[i] = cur.contains(options[i][0]); }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle(title + " (" + hint + ")")
            .setMultiChoiceItems(labels, on, (d, i, checked) -> on[i] = checked)
            .setPositiveButton("حفظ", (d, w) -> {
                List<String> v = new ArrayList<>();
                for (int i = 0; i < options.length; i++) if (on[i]) v.add(options[i][0]);
                prefs.setList(key, v);
                render();
            })
            .setNegativeButton("إلغاء", null).show();
    }

    /** Whole leagues: every game of every club in them is shown, up to Prefs.MAX_LEAGUES leagues. */
    private String leaguesLabel() {
        List<String> out = new ArrayList<>();
        for (String en : prefs.leagues()) out.add(leagueAr(en));
        return out.isEmpty() ? "بدون" : Prefs.join(out, "، ");
    }

    private void pickLeagues() {
        final List<String> cur = prefs.leagues();
        final String[] labels = new String[Prefs.LEAGUES.length];
        final boolean[] on = new boolean[Prefs.LEAGUES.length];
        for (int i = 0; i < labels.length; i++) { labels[i] = Prefs.LEAGUES[i][1]; on[i] = cur.contains(Prefs.LEAGUES[i][0]); }
        new AlertDialog.Builder(this, DIALOG_THEME)
            .setTitle("الدوريات كاملة (حتى " + arabicNum(Prefs.MAX_LEAGUES) + "، بدون اختيار = بدون)")
            .setMultiChoiceItems(labels, on, (d, i, checked) -> {
                int n = 0;
                for (boolean b : on) if (b) n++;
                if (checked && n > Prefs.MAX_LEAGUES) {
                    on[i] = false;
                    ((AlertDialog) d).getListView().setItemChecked(i, false);
                    toast("حتى " + arabicNum(Prefs.MAX_LEAGUES) + " دوريات");
                }
            })
            .setPositiveButton("حفظ", (d, w) -> {
                List<String> v = new ArrayList<>();
                for (int i = 0; i < on.length; i++) if (on[i]) v.add(Prefs.LEAGUES[i][0]);
                prefs.setList("leagues", v);
                render();
            })
            .setNegativeButton("إلغاء", null).show();
    }

    private void manageShows() {
        final List<String> cur = prefs.shows();
        String[] labels = new String[cur.size()];
        final boolean[] keep = new boolean[cur.size()];
        for (int i = 0; i < cur.size(); i++) { labels[i] = cur.get(i).split("\\|", 2)[1]; keep[i] = true; }
        AlertDialog.Builder b = new AlertDialog.Builder(this, DIALOG_THEME)
            .setTitle(cur.isEmpty() ? "لا توجد مسلسلات مختارة" : "المسلسلات المختارة (ألغِ التحديد للحذف)")
            .setPositiveButton("حفظ", (d, w) -> {
                List<String> v = new ArrayList<>();
                for (int i = 0; i < cur.size(); i++) if (keep[i]) v.add(cur.get(i));
                prefs.setList("shows", v);
                render();
            })
            .setNeutralButton("إضافة مسلسل", (d, w) -> searchShow())
            .setNegativeButton("إلغاء", null);
        if (!cur.isEmpty()) b.setMultiChoiceItems(labels, keep, (d, i, checked) -> keep[i] = checked);
        b.show();
    }

    private void searchShow() {
        ask("اسم المسلسل", q -> fetch("https://api.tvmaze.com/search/shows?q=" + Uri.encode(q), json -> {
            JSONArray a = new JSONArray(json);
            if (a.length() == 0) { toast("لا توجد نتائج"); return; }
            final int n = Math.min(a.length(), 15);
            final String[] ids = new String[n], names = new String[n], rows = new String[n];
            for (int i = 0; i < n; i++) {
                JSONObject s = a.getJSONObject(i).getJSONObject("show");
                ids[i] = String.valueOf(s.optInt("id"));
                names[i] = s.optString("name").replace("|", " ").replace(";", " ");
                String year = s.optString("premiered", "");
                rows[i] = names[i] + (year.length() >= 4 ? "  ·  " + year.substring(0, 4) : "")
                    + "  ·  " + labelOf(Prefs.LANGS, s.optString("language", ""));
            }
            new AlertDialog.Builder(this, DIALOG_THEME).setTitle("اختر المسلسل")
                .setItems(rows, (d, i) -> {
                    for (String s : prefs.shows()) if (s.startsWith(ids[i] + "|")) { toast("مضاف مسبقاً"); return; }
                    List<String> v = prefs.shows();
                    v.add(ids[i] + "|" + names[i]);
                    prefs.setList("shows", v);
                    toast("تمت إضافة " + names[i]);
                    render();
                }).show();
        }));
    }

    /** «ترتيب المفضلة» lives in the launcher (it owns the row). */
    private void openDockSettings() {
        try {
            startActivity(new Intent().setClassName("sa.urtv.home", "sa.urtv.home.DockActivity"));
        } catch (RuntimeException e) {
            toast("ثبّت الواجهة الرئيسية «تلفزيونك» أولاً");
        }
    }

    private static final String[] HOME_CARD_NAMES = {"البطاقة الأولى", "البطاقة الثانية", "البطاقة الثالثة"};

    /** The card beside the screensaver's clock (place -1), or place 0 to 2 of the cards on the launcher's home screen. */
    private void pickPanel(final int place) {
        final boolean home = place >= 0;
        String current = home ? prefs.homePanelAt(place) : prefs.panel();
        // the home places can also be left empty; «لا شيء» is that choice there, so it is «بدون» in the list
        String[] labels = new String[Prefs.PANELS.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = home && "none".equals(Prefs.PANELS[i][0]) ? "بدون" : Prefs.PANELS[i][1];
            if (Prefs.PANELS[i][0].equals(current)) sel = i;
        }
        String[] values = new String[labels.length], about = new String[labels.length];
        for (int i = 0; i < labels.length; i++) { values[i] = Prefs.PANELS[i][0]; about[i] = Prefs.about(Prefs.PANEL_ABOUT, values[i]); }
        pickWithPreview(home ? HOME_CARD_NAMES[place] : "بجانب الساعة", values, labels, about, sel,
            v -> prefs.previewUrl(this, v, prefs.cardStyle()),
            v -> { if (home) prefs.setHomePanelAt(place, v); else prefs.setPanel(v); });
    }

    private interface Url { String of(String value); }
    private interface Pick { void on(String value); }

    /**
     * A picker with the list on one side, one plain line under each name, and on the other side the focused choice drawn live by the
     * page itself. The WebView is made when the picker opens and destroyed when it closes; it keeps the TV's text size out of it
     * (setTextZoom(100) in ScreenView) like every other one of ours.
     */
    private void pickWithPreview(String title, final String[] values, final String[] labels, final String[] about, int selected,
                                 final Url urlFor, final Pick onPick) {
        final int previewW = 400, previewH = 260;   // dp: about the size of the card on the home screen
        final LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(16), dp(8), dp(16), dp(8));
        final android.widget.ListView lv = new android.widget.ListView(this);
        lv.setDividerHeight(0);
        lv.setSelector(new android.graphics.drawable.ColorDrawable(0x33D6AF6B));
        lv.setAdapter(new android.widget.BaseAdapter() {
            @Override public int getCount() { return values.length; }
            @Override public Object getItem(int i) { return values[i]; }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View old, android.view.ViewGroup parent) {
                LinearLayout r = new LinearLayout(SettingsActivity.this);
                r.setOrientation(LinearLayout.VERTICAL);
                r.setPadding(dp(12), dp(8), dp(12), dp(8));
                TextView n = new TextView(SettingsActivity.this);
                n.setText(labels[i]);
                n.setTextColor(0xFFF6EFE0);
                n.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                n.setTypeface(font("Thmanyah-Medium"));
                r.addView(n);
                TextView a = new TextView(SettingsActivity.this);
                a.setText(about[i]);
                a.setTextColor(0xFFC6BBA4);
                a.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                a.setTypeface(font("Thmanyah-Regular"));
                r.addView(a);
                return r;
            }
        });
        box.addView(lv, new LinearLayout.LayoutParams(0, dp(previewH + 60), 1f));
        final android.widget.FrameLayout stage = new android.widget.FrameLayout(this);
        stage.setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            new int[]{0xFF14382A, 0xFF07150E}));   // the home screen's dark wallpaper, so glass and dark cards show as they will
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(previewW), dp(previewH));
        sp.setMarginStart(dp(16));
        sp.gravity = Gravity.CENTER_VERTICAL;
        box.addView(stage, sp);

        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final android.webkit.WebView[] web = new android.webkit.WebView[1];
        final int[] shown = {-1};
        final Runnable update = new Runnable() { public void run() {
            int i = lv.getSelectedItemPosition();
            if (i < 0) i = Math.max(0, selected);   // nothing selected yet (touch mode): the current choice
            if (i == shown[0]) return;
            shown[0] = i;
            String u = urlFor.of(values[i]);
            if (web[0] == null) {
                web[0] = ScreenView.createPreview(SettingsActivity.this, u);
                stage.addView(web[0], new android.widget.FrameLayout.LayoutParams(-1, -1));
            } else {   // the page reads its settings once, when it starts: a new #fragment alone would not start it again
                web[0].loadUrl(u);
                web[0].reload();
            }
        } };
        lv.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                handler.removeCallbacks(update);
                handler.postDelayed(update, 200);   // a long press of the arrow key goes past choices without drawing each
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) { }
        });
        final AlertDialog dialog = new AlertDialog.Builder(this, DIALOG_THEME).setTitle(title).setView(box)
            .setNegativeButton("إلغاء", null).create();
        lv.setOnItemClickListener((p, v, pos, id) -> { onPick.on(values[pos]); dialog.dismiss(); render(); });
        dialog.setOnShowListener(d -> {
            dialog.getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(48), dp(940)), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            lv.requestFocus();
            lv.setSelection(Math.max(0, selected));
            handler.postDelayed(update, 50);
        });
        dialog.setOnDismissListener(d -> {   // nothing of the preview outlives the picker
            handler.removeCallbacks(update);
            if (web[0] != null) { ScreenView.release(web[0]); web[0] = null; }
        });
        dialog.show();
    }

    private void pickRating() {
        String[] labels = Prefs.MIN_RATINGS.clone();
        labels[0] = "بدون حد";
        int sel = 0;
        for (int i = 0; i < Prefs.MIN_RATINGS.length; i++) if (Prefs.MIN_RATINGS[i].equals(prefs.minRating())) sel = i;
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("أقل تقييم")
            .setSingleChoiceItems(labels, sel, (d, i) -> { prefs.setMinRating(Prefs.MIN_RATINGS[i]); d.dismiss(); render(); })
            .show();
    }

    private String marketsLabel() {
        List<String> out = new ArrayList<>();
        for (String k : prefs.markets()) out.add(labelOf(Prefs.MARKETS, k));
        return Prefs.join(out, "، ");
    }

    /** Up to MAX_MARKETS; the order they are shown in is the order of the list. */
    private void pickMarkets() {
        List<String> cur = prefs.markets();
        String[] labels = new String[Prefs.MARKETS.length];
        final boolean[] on = new boolean[Prefs.MARKETS.length];
        for (int i = 0; i < labels.length; i++) { labels[i] = Prefs.MARKETS[i][1]; on[i] = cur.contains(Prefs.MARKETS[i][0]); }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("الأسواق (حتى " + arabicNum(Prefs.MAX_MARKETS) + ")")
            .setMultiChoiceItems(labels, on, (d, i, checked) -> on[i] = checked)
            .setPositiveButton("حفظ", (d, w) -> {
                List<String> v = new ArrayList<>();
                for (int i = 0; i < on.length; i++) if (on[i]) v.add(Prefs.MARKETS[i][0]);
                if (v.isEmpty() || v.size() > Prefs.MAX_MARKETS) {
                    toast("اختر من سوق إلى " + arabicNum(Prefs.MAX_MARKETS) + " أسواق");
                    return;
                }
                prefs.setMarkets(v);
                render();
            })
            .setNegativeButton("إلغاء", null).show();
    }

    private String stocksLabel() { return Prefs.join(prefs.stocks(), "، "); }

    /** Codes (2222 = أرامكو) and US tickers (AAPL), separated by spaces or commas. */
    private void askStocks() {
        final EditText in = new EditText(this);
        in.setHint("مثال: 2222 1120 AAPL NVDA");
        in.setText(Prefs.join(prefs.stocks(), " "));
        in.setSingleLine(true);
        in.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("قائمة الأسهم (رمز تداول من ٤ أرقام أو رمز أمريكي، حتى " + arabicNum(Prefs.MAX_STOCKS) + ")")
            .setView(in)
            .setPositiveButton("حفظ", (d, w) -> {
                List<String> v = new ArrayList<>();
                for (String s : in.getText().toString().split("[\\s,،]+")) {
                    s = s.trim().toUpperCase(java.util.Locale.ROOT);
                    if (s.isEmpty()) continue;
                    if (!s.matches(Prefs.STOCK_PATTERN)) { toast("رمز غير صالح: " + s); return; }
                    if (!v.contains(s)) v.add(s);
                }
                if (v.isEmpty() || v.size() > Prefs.MAX_STOCKS) {
                    toast("أدخل من رمز إلى " + arabicNum(Prefs.MAX_STOCKS) + " رموز");
                    return;
                }
                prefs.setStocks(v);
                render();
            })
            .setNegativeButton("إلغاء", null).show();
        in.requestFocus();
    }

    private void pickMkStyle() {
        String[] labels = new String[Prefs.MK_STYLES.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Prefs.MK_STYLES[i][1];
            if (Prefs.MK_STYLES[i][0].equals(prefs.mkStyle())) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("عرض الأسواق")
            .setSingleChoiceItems(labels, sel, (d, i) -> { prefs.setMkStyle(Prefs.MK_STYLES[i][0]); d.dismiss(); render(); })
            .show();
    }

    private void pickCardStyle() {
        String[] labels = new String[Prefs.CARD_STYLES.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Prefs.CARD_STYLES[i][1];
            if (Prefs.CARD_STYLES[i][0].equals(prefs.cardStyle())) sel = i;
        }
        String[] values = new String[labels.length], about = new String[labels.length];
        for (int i = 0; i < labels.length; i++) { values[i] = Prefs.CARD_STYLES[i][0]; about[i] = Prefs.about(Prefs.CARD_STYLE_ABOUT, values[i]); }
        // the preview is the card you have now, in the style under the cursor (the prayer times when there is no card)
        final String card = "none".equals(prefs.panel()) ? "prayers" : prefs.panel();
        pickWithPreview("شكل البطاقة", values, labels, about, sel, v -> prefs.previewUrl(this, card, v), v -> prefs.setCardStyle(v));
    }

    private void pickSlot() {
        String[] labels = new String[Prefs.SLOTS.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Prefs.SLOTS[i][1];
            if (Prefs.SLOTS[i][0].equals(prefs.slot())) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("الخانة اليسرى")
            .setSingleChoiceItems(labels, sel, (d, i) -> { prefs.setSlot(Prefs.SLOTS[i][0]); d.dismiss(); render(); })
            .show();
    }

    private String countdownLabel() {
        if (prefs.countdownDate().isEmpty()) return "لم يُحدَّد";
        String name = prefs.countdownName();
        return arabicDigits(prefs.countdownDate()) + (name.isEmpty() ? "" : " · " + name);
    }

    /** The date is set from the computer (optimize/saver.sh countdown); here it can be cleared. */
    private void manageCountdown() {
        if (prefs.countdownDate().isEmpty()) {
            new AlertDialog.Builder(this, DIALOG_THEME).setTitle("العدّ التنازلي")
                .setMessage("لم يُحدَّد تاريخ. يحدده المساعد على الكمبيوتر.")
                .setPositiveButton("حسنًا", null).show();
            return;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("العدّ التنازلي")
            .setMessage(countdownLabel())
            .setPositiveButton("مسح", (d, w) -> { prefs.setCountdown("", ""); render(); })
            .setNegativeButton("إلغاء", null).show();
    }

    private void pickLanguage() {
        String[] labels = new String[Prefs.UI_LANGS.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Prefs.UI_LANGS[i][1];
            if (Prefs.UI_LANGS[i][0].equals(prefs.language())) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("اللغة")
            .setSingleChoiceItems(labels, sel, (d, i) -> { prefs.setLanguage(Prefs.UI_LANGS[i][0]); d.dismiss(); render(); })
            .show();
    }

    private void pickDigits() {
        String[] labels = new String[Prefs.DIGITS.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Prefs.DIGITS[i][1];
            if (Prefs.DIGITS[i][0].equals(prefs.digits())) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("الأرقام")
            .setSingleChoiceItems(labels, sel, (d, i) -> { prefs.setDigits(Prefs.DIGITS[i][0]); d.dismiss(); render(); })
            .show();
    }

    private void pickNational() {
        String[] labels = new String[Prefs.NATIONAL.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Prefs.NATIONAL[i][1];
            if (Prefs.NATIONAL[i][0].equals(prefs.national())) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("المناسبات الوطنية")
            .setSingleChoiceItems(labels, sel, (d, i) -> { prefs.setNational(Prefs.NATIONAL[i][0]); d.dismiss(); render(); })
            .show();
    }

    private void pickPhotoMinutes() {
        String[] labels = new String[Prefs.PHOTO_MINUTES.length];
        int sel = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = minutesLabel(Integer.parseInt(Prefs.PHOTO_MINUTES[i]));
            if (Prefs.PHOTO_MINUTES[i].equals(prefs.photoMinutes())) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("تبديل الصورة كل")
            .setSingleChoiceItems(labels, sel, (d, i) -> { prefs.setPhotoMinutes(Prefs.PHOTO_MINUTES[i]); d.dismiss(); render(); })
            .show();
    }

    private String ownLabel() {
        if (Own.count(this) == 0) return "لا توجد صور · يضيفها مساعدك الذكي";
        return Own.ONLY.equals(Own.mode(this)) ? "صوري فقط" : "تُضاف إلى صور التطبيق";
    }

    /** «صوري»: photos only arrive through the owner's assistant; here the owner picks how they show, or removes them all. */
    private void pickOwn() {
        if (Own.count(this) == 0) {
            toast("اطلب من مساعدك الذكي إضافة صورك (حتى ١٠)");
            return;
        }
        String[] items = {"تُضاف إلى صور التطبيق", "صوري فقط", "حذف كل صوري"};
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("صوري")
            .setSingleChoiceItems(items, Own.ONLY.equals(Own.mode(this)) ? 1 : 0, (d, i) -> {
                d.dismiss();
                if (i == 2) { confirmClearOwn(); return; }
                prefs.setOwnMode(i == 1 ? Own.ONLY : Own.MIX);
                prefs.bumpOwnRev();
                render();
            })
            .show();
    }

    private void confirmClearOwn() {
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("حذف كل صوري؟")
            .setPositiveButton("حذف", (d, i) -> { Own.clear(this); render(); })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    /** The TV's own idle delay. Writing it needs WRITE_SETTINGS, which TVs rarely grant: then its own screen opens. */
    private void pickIdle() {
        if (!Prefs.canWriteSettings(this)) {
            toast("غيّرها من إعدادات التلفاز، أو بأمر من مساعدك");
            openDreamSettings();
            return;
        }
        String[] labels = new String[Prefs.IDLE_MINUTES.length];
        int sel = -1;
        long now = Prefs.idleMs(this);
        for (int i = 0; i < labels.length; i++) {
            labels[i] = minutesLabel(Integer.parseInt(Prefs.IDLE_MINUTES[i]));
            if (Long.parseLong(Prefs.IDLE_MINUTES[i]) * 60000 == now) sel = i;
        }
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("بدء شاشة التوقف بعد")
            .setSingleChoiceItems(labels, sel, (d, i) -> {
                try {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                        Integer.parseInt(Prefs.IDLE_MINUTES[i]) * 60000);
                } catch (SecurityException e) {
                    toast("غيّرها من إعدادات التلفاز، أو بأمر من مساعدك");
                }
                d.dismiss();
                render();
            })
            .show();
    }

    private String idleLabel() {
        long ms = Prefs.idleMs(this);
        if (ms <= 0) return "يحدده التلفاز";
        if (ms % 60000 != 0) return arabicNum((int) (ms / 1000)) + " ثانية";
        return minutesLabel((int) (ms / 60000));
    }

    /** دقيقة واحدة · دقيقتان · ٥ دقائق · ١٥ دقيقة */
    private static String minutesLabel(int n) {
        if (n == 1) return "دقيقة واحدة";
        if (n == 2) return "دقيقتان";
        return arabicNum(n) + (n <= 10 ? " دقائق" : " دقيقة");
    }

    private void updateNow() {
        toast("جارٍ التحقق…");
        new Thread(() -> {
            final String msg = Updater.check(getApplicationContext(), true);
            runOnUiThread(() -> { toast(msg); if (!isFinishing()) render(); });
        }).start();
    }

    private void openDreamSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_DREAM_SETTINGS));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    // ---------- labels ----------

    /** Wikidata's Arabic name (or a transliteration) fetched off the UI thread. */
    private void inArabic(final String en, final java.util.regex.Pattern kind, final Text then) {
        toast("جارٍ جلب الاسم بالعربية…");
        new Thread(() -> {
            String k = kind == Arabic.TEAM ? Arabic.known(getApplicationContext(), en) : null;
            final String ar = k != null ? k : Arabic.lookup(en, kind);
            runOnUiThread(() -> { if (!isFinishing()) then.got(ar); });
        }).start();
    }

    private static String leagueAr(String en) {
        for (String[] l : Prefs.LEAGUES) if (l[0].equalsIgnoreCase(en)) return l[1];
        return Arabic.translit(en);
    }

    private static String labelOf(String[][] options, String key) {
        for (String[] o : options) if (o[0].equalsIgnoreCase(key)) return o[1];
        return Arabic.translit(key);
    }

    private String teamsLabel() {
        List<String> names = new ArrayList<>();
        for (String t : prefs.teams()) names.add(t.split("\\|", 2)[1]);
        return names.isEmpty() ? "لا شيء" : Prefs.join(names, "، ");
    }

    private static String arabicDigits(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) b.append(c >= '0' && c <= '9' ? (char) ('٠' + (c - '0')) : c);
        return b.toString();
    }

    private static String arabicNum(int n) {
        StringBuilder b = new StringBuilder();
        for (char c : String.valueOf(n).toCharArray()) b.append((char) ('٠' + (c - '0')));
        return b.toString();
    }

    private String showsLabel() {
        List<String> v = prefs.shows();
        if (v.isEmpty()) return "لا شيء";
        List<String> names = new ArrayList<>();
        for (String s : v) names.add(s.split("\\|", 2)[1]);
        return Prefs.join(names, "، ");
    }

    private static String labels(String[][] options, List<String> picked, String none) {
        List<String> out = new ArrayList<>();
        for (String[] o : options) if (picked.contains(o[0])) out.add(o[1]);
        return out.isEmpty() ? none : Prefs.join(out, "، ");
    }

    // ---------- small UI helpers ----------

    private final java.util.Map<String, Typeface> fonts = new java.util.HashMap<>();

    /** Thmanyah, shared with the launcher's assets; falls back to the system font. */
    private Typeface font(String name) {
        Typeface tf = fonts.get(name);
        if (tf == null) {
            try { tf = Typeface.createFromAsset(getAssets(), "fonts/" + name + ".otf"); }
            catch (RuntimeException e) { tf = Typeface.DEFAULT; }
            fonts.put(name, tf);
        }
        return tf;
    }

    private void header(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(0xFFD6AF6B);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setTypeface(font("Thmanyah-Medium"));
        t.setLetterSpacing(0.05f);
        t.setPadding(dp(6), dp(22), dp(6), dp(8));
        list.addView(t);
    }

    /** Plain lines at the end, in no button: nothing to focus, so the last row scrolls them into view. */
    private void note(View last, String... lines) {
        TextView t = new TextView(this);
        t.setText(android.text.TextUtils.join("\n", lines));
        t.setTextColor(0xFFC6BBA4);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setTypeface(font("Thmanyah-Regular"));
        t.setLineSpacing(dp(4), 1f);
        t.setGravity(Gravity.START);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setPadding(dp(6), dp(18), dp(6), dp(24));
        list.addView(t);
        final View.OnFocusChangeListener was = last.getOnFocusChangeListener();
        last.setOnFocusChangeListener((v, has) -> {
            if (was != null) was.onFocusChange(v, has);
            if (!has) return;
            for (android.view.ViewParent p = list.getParent(); p != null; p = p.getParent())   // list sits in a frame in the scroll view
                if (p instanceof ScrollView) { final ScrollView sv = (ScrollView) p; v.post(() -> sv.fullScroll(View.FOCUS_DOWN)); break; }
        });
    }

    /** A card row: icon, label and its current value on a second line, chevron; gold when focused. */
    private View row(String label, String value, int icon, final Runnable onClick) {
        final LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setPadding(dp(18), dp(12), dp(18), dp(12));
        card.setFocusable(true);
        card.setClickable(true);
        final android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadius(dp(14));
        bg.setColor(0x14FFFFFF);
        bg.setStroke(dp(2), 0x1FD6AF6B);
        card.setBackground(bg);

        final android.widget.ImageView ic = new android.widget.ImageView(this);
        final android.graphics.drawable.Drawable d = getDrawable(icon).mutate();
        d.setTint(0xFFD6AF6B);
        ic.setImageDrawable(d);
        card.addView(ic, new LinearLayout.LayoutParams(dp(24), dp(24)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        final TextView name = new TextView(this);
        name.setText(label);
        name.setTextColor(0xFFF6EFE0);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        name.setTypeface(font("Thmanyah-Medium"));
        name.setSingleLine(true);
        texts.addView(name);
        final TextView val = new TextView(this);
        if (value != null) {
            val.setText(value);
            val.setTextColor(0xFFC6BBA4);
            val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            val.setTypeface(font("Thmanyah-Regular"));
            val.setSingleLine(true);
            val.setEllipsize(android.text.TextUtils.TruncateAt.END);
            texts.addView(val);
        }
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1f);
        tp.setMarginStart(dp(16));
        card.addView(texts, tp);

        final TextView chevron = new TextView(this);
        chevron.setText("›");   // shown mirrored (‹) in the RTL row
        chevron.setTextColor(0x99C6BBA4);
        chevron.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        card.addView(chevron);

        card.setOnClickListener(v -> onClick.run());
        card.setOnFocusChangeListener((v, has) -> {
            // No scaling: a scaled rounded card overlapped its neighbours and blurred the text.
            bg.setColor(has ? 0x33D6AF6B : 0x14FFFFFF);
            bg.setStroke(dp(2), has ? 0xFFD6AF6B : 0x1FD6AF6B);
            chevron.setTextColor(has ? 0xFFD6AF6B : 0x99C6BBA4);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(8);
        list.addView(card, lp);
        return card;
    }

    interface Text { void got(String s); }
    interface Json { void got(String json) throws Exception; }

    private void ask(String hint, final Text then) {
        final EditText in = new EditText(this);
        in.setHint(hint);
        in.setSingleLine(true);
        in.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("بحث").setView(in)
            .setPositiveButton("بحث", (d, w) -> {
                String q = in.getText().toString().trim();
                if (!q.isEmpty()) then.got(q);
            })
            .setNegativeButton("إلغاء", null).show();
        in.requestFocus();
    }

    /** GET on a background thread, result handled on the UI thread. */
    private void fetch(final String url, final Json then) {
        toast("جارٍ البحث…");
        new Thread(() -> {
            String body = null;
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(url).openConnection();
                con.setConnectTimeout(12000);
                con.setReadTimeout(15000);
                InputStream in = con.getInputStream();
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
                in.close();
                body = b.toString("UTF-8");
            } catch (Exception ignored) {
            } finally {
                if (con != null) con.disconnect();
            }
            final String result = body;
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (result == null) { toast("لا يوجد اتصال"); return; }
                try { then.got(result); } catch (Exception e) { toast("تعذرت قراءة النتائج"); }
            });
        }).start();
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
