# B&M Supermarket – supermart

## Android demo app (`android/`)

نسخة عرض (Demo) لتطبيق أندرويد واحد بصلاحيات:

- **المالك / الأدمن**: لوحة تحكم، كاشير (POS) شغال أوفلاين، الطلبات الأونلاين، المخزون، التقارير، الفواتير والمرتجعات، العملاء، الموظفين والصلاحيات، الوردية والخزنة، الإعدادات.
- **الكاشير**: الكاشير + الطلبات + الوردية فقط.
- **مسؤول المخزن**: لوحة التحكم + المخزون فقط.
- **العميل**: تسجيل برقم الموبايل، تصفح الأقسام والعروض، بحث، سلة، إتمام الطلب، تتبع الطلب، نقاط ولاء.

كل البيانات تجريبية ومحفوظة على الموبايل (مفيش سيرفر). الطلب اللي العميل يعمله بيظهر عند الأدمن في "الطلبات" ولما الأدمن يغيّر حالته بتتغير عند العميل.

| الحساب | المستخدم | كلمة السر |
|---|---|---|
| المالك | `admin` | `1234` |
| كاشير | `cashier` | `1234` |
| مسؤول مخزن | `store` | `1234` |
| عميل | `01000000000` | `1234` |

رقم المدير السري للخصومات والمرتجعات وكود OTP في العرض: `1234`.

### Build

The APK is built by GitHub Actions (`.github/workflows/android-apk.yml`) on every push that touches `android/`, and published as a GitHub Release asset `BM-Supermarket-demo.apk`.

Locally (needs Android SDK 34): `cd android && gradle assembleRelease`.

The UI is a single offline HTML/JS app at `android/app/src/main/assets/www/index.html` loaded in a native WebView (`MainActivity.java`).

### Online accounts (Firebase Firestore)

Customer and staff accounts can sync through a free Firebase Firestore database so they work across phones.
Put the project's `projectId` and web `apiKey` in `android/app/src/main/assets/www/config.js`. With it empty, accounts stay on the device.
Passwords are stored as SHA-256 hashes. The database must be in Firestore *test mode* (demo only; test-mode rules expire after 30 days).
