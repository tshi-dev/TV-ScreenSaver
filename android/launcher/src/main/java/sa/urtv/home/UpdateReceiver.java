package sa.urtv.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

/**
 * The answer from Android's installer for the self-update: either the system needs the user to
 * confirm (its own screen), or the install went through — after which this app starts again.
 */
public class UpdateReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, Integer.MIN_VALUE);
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirm != null) {
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(confirm);
                } catch (RuntimeException e) {
                    SelfUpdater.failed(status, null);
                }
            }
        } else if (status != PackageInstaller.STATUS_SUCCESS) {
            SelfUpdater.failed(status, intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
        }
    }
}
