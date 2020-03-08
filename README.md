# GpsGeofencingTracking-Experiment
Test and experiment gps, geofencing tracking in background

References:

https://developer.android.com/training/location/receive-location-updates#inform-user-background-location-requirement

https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime

[geofencing] https://developer.android.com/training/location/geofencing

https://medium.com/@christopherney/notification-listener-service-vulnerability-8d0c586f88d5

https://github.com/android/location-samples

Android code search

1. Permission Controller:
https://cs.android.com/android/platform/superproject/+/master:packages/apps/PermissionController/src/com/android/packageinstaller/permission/service/RuntimePermissionsUpgradeController.java
https://cs.android.com/android/platform/superproject/+/master:packages/apps/PermissionController/res/values/strings.xml;l=587

2.Google Awareness
https://developers.google.com/awareness/android-api/get-a-key
NOTE: Error APIExeception 17 ContextManager API is not available on this device là do awareness api key nó ko connect vào service để verify
nên kiểm tra lai network.

3. NOTE: Không hiểu sao Activity Recognition Transition API ActivityRecognitionClient không work khi request update trong background service, nên sẽ dùng AWARENESS API thay thế
Xem trong comment của link sau:
https://blog.mindorks.com/activity-recognition-in-android-still-walking-running-driving-and-much-more
Codelab Activity Recognition Transition API 
https://codelabs.developers.google.com/codelabs/activity-recognition-transition/index.html#0
The Google Awareness API is also designed to manage system resources on its own, so using it shouldn't cause additional battery drain or require more processing power. Google says the Awareness API monitors its battery and data usage itself, so ideally developers won't have to worry about modifying any part of their apps, aside from adding calls

I.Android < 8
Trigger screen on/off -> Dựa vào event này sẽ đi gọi module kích hoạt track user activity(Move or Idle).
Module kích hoạt track activity
1. Nếu request activity updated đang ON thì skip xem như ko làm gì, tức nó đang going để nó work tiếp flow
2. Nếu OFF request update activity thì đi get last location and save lại(Update location ngay khi có thể) , sau đó start request update activity -> request này sẽ tự động stop itself


Todo List
x1. custome dùng alarm manager cho android 7- workmanager cho android 8+ khi schedule -> work manager dang work ok tren android 7- nen co the ko can dung alarm manager
v2. xem detect activity khong work với android 10, các android 5,6,7,8,9 đều work sau khi move vài ba phút là detect được -> android 10 need to ask permission at runtime
3. xem sao crash report nó ko send to firebase sau khi dùng firebase crashlytic sđk mới beta version
v4. Xem kết quả detect activity trên android 5,7,8 và location tracking background trên android 5,7 có tốt ko. xem log theo ngày.
5. Core traking jobintentservice create-destroy lien tuc thi co van de gi ko?

NOTE List
1. Với android 10 cần ask permission at run time cho Manifest.permission.ACTIVITY_RECOGNITION
2. Nhớ kiểm tra nếu turn off location service thì navigate user tới settings để bật lên
3. Check location service có turn on ko? đây là 1 thông tin sẽ đc update lên server cho device status. vì khi ko lấy dc location cần
báo cho user biết là device đã bị cố tình turn off location service. (họ sẽ phải turn on lại khi sài app nào cần location)

S >F>V ok
S>V>F > s? -> khi user đi tới lui trong bán kính 200m trong vòng 1h thì sẽ dừng track, vì trước sau gì user cung sẽ still
F>S>V ok
F>V>S  ok
V>F>S ok
V>S>F > s? user có thể still

S > F -> track interval 2 mins 3 times để xem nếu vẫn trong khu vực around stay thì cancel
F > F -> khi user on foot around trong 30' và vẫn tiếp tục on foot mà ko ngồi nghỉ ngơi phut nào !!!


