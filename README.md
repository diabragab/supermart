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

### Production accounts (Firebase Authentication + Firestore)

With `projectId` and `apiKey` set in `android/app/src/main/assets/www/config.js` the app runs in production mode:

- Customers register with name, mobile number, address, optional email and password; they log in with mobile + password.
- Staff log in with a username created by the owner (المزيد → الموظفين). The first owner is created once from the login screen ("إعداد حساب المالك").
- Under the hood every account is a Firebase Email/Password user (`<phone>@customers.bm-market.app` / `<username>@staff.bm-market.app`), so it works on the free Spark plan. Profile and role live in Firestore `users/{uid}`; `meta/owner` locks the owner role.
- Online orders in Firestore `orders/{id}` (items as a JSON string). Customers create and cancel (while new) their own orders; staff list all orders and move them along; the app polls every 30 s.
- Loyalty points: only staff change `users.pts`. On delivery the staff device commits `st=3, ptsGiven=true` plus a `pts` increment of `floor(total/10) + bonus` (wheel prize, max 100) in one batch.
- Security rules: `firebase/firestore.rules` (role-based; all other collections closed).
- Staff who logged in online once can log in offline on the same phone; the app re-authenticates when the internet returns.
- Step-by-step console setup in Arabic: `firebase/SETUP-ar.md`.

With `config.js` empty the app runs the old offline demo (demo accounts, data on the device only).
