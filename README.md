# جرد الأرشيف - تطبيق Android

تطبيق Android بسيط يفتح صفحة `archive_inventory.html` داخل WebView محلي، لذلك لا يحتاج إلى استضافة الصفحة أو اتصال دائم بالإنترنت.

## البناء
- Android Studio: افتح هذا المجلد كمشروع Gradle ثم نفّذ `assembleDebug`.
- أو ارفع المشروع إلى GitHub ثم شغّل GitHub Actions من تبويب Actions (أو ادفع إلى فرع main/master). نزّل artifact باسم `archive-inventory-android-apk`.
- ملف APK الناتج: `app/build/outputs/apk/debug/app-debug.apk`.

## التوافق والبيانات
- الحد الأدنى Android 6.0 (API 23)، ويشمل Android 10.
- تخزّن الصفحة البيانات في Local Storage داخل WebView على الجهاز نفسه.
- استيراد CSV متاح محلياً. استيراد Excel يعتمد على مكتبة SheetJS التي تُحمّل من الإنترنت عند الحاجة.
- تصدير CSV يفتح نافذة حفظ ملفات Android.

ملاحظة: هذه النسخة تخزّن البيانات على الهاتف فقط ولا تزامنها تلقائياً مع أجهزة أخرى أو خادم.
