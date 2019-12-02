package in.mrasif.app.syncadapterdemo.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import static android.content.Context.ACCOUNT_SERVICE;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    public static final int SYNC_INTERVAL=60*180;
    public static final int SYNC_FLEXTIME=SYNC_INTERVAL/3;
    public static final String AUTHORITY="in.mrasif.app.syncadapterdemo.sync.provider";
    public static final String ACCOUNT = "dummyaccount";
    public static final String ACCOUNT_TYPE = "sync.mrasif.in";
    ContentResolver contentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        contentResolver = context.getContentResolver();
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        contentResolver = context.getContentResolver();

    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        /*
         * Put the data transfer code here.
         */
        Log.d(TAG, "onPerformSync: Sync started");

    }

    public static void configurePeriodicSync(Context context, int interval, int flextime){
        Account account=getSyncAccount(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            SyncRequest request=new SyncRequest.Builder()
                    .syncPeriodic(interval,flextime)
                    .setSyncAdapter(account,AUTHORITY)
                    .setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        }
        else {
            ContentResolver.addPeriodicSync(account,AUTHORITY,new Bundle(),interval);
        }
    }

    public static void syncImmediately(Context context){
        Bundle bundle=new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED,true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL,true);
        ContentResolver.requestSync(getSyncAccount(context),AUTHORITY,bundle);
    }

    public static Account getSyncAccount(Context context){
        AccountManager accountManager=(AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        Account newAccount=new Account(ACCOUNT,ACCOUNT_TYPE);
        if (null!=accountManager.getPassword(newAccount)){
            Bundle bundle=new Bundle();
            if (!accountManager.addAccountExplicitly(newAccount,null,null)){
                return null;
            }
            else {
                Log.e(TAG, "getSyncAccount: Failed to create account.");
            }
            onAccountCreated(newAccount,context);
        }
        return newAccount;
    }

    public static void onAccountCreated(Account account, Context context) {
        SyncAdapter.configurePeriodicSync(context,SYNC_INTERVAL,SYNC_FLEXTIME);
        SyncAdapter.syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context){
        getSyncAccount(context);
    }

}
