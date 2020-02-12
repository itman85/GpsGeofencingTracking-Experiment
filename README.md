# GpsGeofencingTracking-Experiment
Test and experiment gps, geofencing tracking in background

References:

https://developer.android.com/training/location/receive-location-updates#inform-user-background-location-requirement

https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime

Android code search

1. Permission Controller:
https://cs.android.com/android/platform/superproject/+/master:packages/apps/PermissionController/src/com/android/packageinstaller/permission/service/RuntimePermissionsUpgradeController.java
https://cs.android.com/android/platform/superproject/+/master:packages/apps/PermissionController/res/values/strings.xml;l=587

2.Google Awareness
https://developers.google.com/awareness/android-api/get-a-key
NOTE: Error APIExeception 17 ContextManager API is not available on this device là do awareness api key nó ko connect vào service để verify
nên kiểm tra lai network.

3. NOTE: Không hiểu sao ActivityRecognitionClient không work khi request update trong background service, nên sẽ dùng awareness api thay thế
Xem trong comment của link sau:
https://blog.mindorks.com/activity-recognition-in-android-still-walking-running-driving-and-much-more

I.Android < 8
Trigger screen on/off -> Dựa vào event này sẽ đi gọi module kích hoạt track user activity(Move or Idle).
Module kích hoạt track activity
1. Nếu request activity updated đang ON thì skip xem như ko làm gì, tức nó đang going để nó work tiếp flow
2. Nếu OFF request update activity thì đi get last location and save lại(Update location ngay khi có thể) , sau đó start request update activity -> request này sẽ tự động stop itself


Todo List
1. custome dùng alarm manager cho android 7- workmanager cho android 8+ khi schedule
v2. xem detect activity khong work với android 10, các android 5,6,7,8,9 đều work sau khi move vài ba phút là detect được -> android 10 need to ask permission at runtime
3. xem sao crash report nó ko send to firebase sau khi dùng firebase crashlytic sđk mới beta version
4. Xem kết quả detect activity trên android 5,7,8 và location tracking background trên android 5,7 có tốt ko. xem log theo ngày.
