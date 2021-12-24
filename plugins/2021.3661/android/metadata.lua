local metadata =
{
   plugin =
   {
      format = 'jar',
      manifest =
      {
         usesPermissions =
         {
             "android.permission.INTERNET",
             "com.samsung.android.iap.permission.BILLING",

         },
         applicationChildElements ={[[
           <activity
             android:name="com.samsung.android.sdk.iap.lib.activity.DialogActivity"
             android:theme="@style/Theme.Empty"
             android:configChanges="orientation|screenSize"/>

           <activity
             android:name="com.samsung.android.sdk.iap.lib.activity.CheckPackageActivity"
             android:theme="@style/Theme.Empty"
             android:configChanges="orientation|screenSize"/>

           <activity
             android:name="com.samsung.android.sdk.iap.lib.activity.AccountActivity"
             android:theme="@style/Theme.Transparent"
             android:configChanges="orientation|screenSize"/>

           <activity
             android:name="com.samsung.android.sdk.iap.lib.activity.PaymentActivity"
             android:theme="@style/Theme.Empty"
             android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboard|keyboardHidden|locale|uiMode|fontScale|density"/>
         ]]},

      },
   },
   coronaManifest = {
       dependencies = {
           ["shared.android.support.v7.appcompat"] = "com.coronalabs"
       }
   }

}

return metadata
