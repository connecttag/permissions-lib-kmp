# ConnectTag Permissions Library (permissions-lib-kmp)

[![JitPack](https://jitpack.io/v/connecttag/permissions-lib-kmp.svg)](https://jitpack.io/#connecttag/permissions-lib-kmp)

مكتبة إدارة وتنسيق أذونات وصلاحيات التطبيقات عبر المنصات (Kotlin Multiplatform) مع دعم سياسات الإشعارات، ترقية دقة الموقع الجغرافي، وحفظ الحالة التفاعلية:
- **Android** (API 24+)
- **Desktop JVM** (Windows, macOS, Linux)
- **iOS** (Arm64, SimulatorArm64)

---

## 📦 التثبيت والإضافة للمشروع (Installation via JitPack)

### 1. إضافة مستودع JitPack في `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. إضافة التبعية في `build.gradle.kts`:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // For Kotlin Multiplatform:
            implementation("com.github.connecttag.permissions-lib-kmp:permissions-lib-kmp:1.0.0")
        }
    }
}
```

الحزمة الأساسية للمكتبة: `org.connecttag.lib.permissions.*`

---

## 🌟 الميزات الأساسية

1. **`PlatformPermissionService` & `CoordinatedPlatformPermissionService`:**
   - تنسيق طلب الصلاحيات ومنع التضارب وحالات السباق (Race conditions) مع دعم الإلغاء والمهلة الزمنية.
2. **`ReactivePlatformPermissionStore`:**
   - تخزين تفاعلي لحالات الصلاحيات عبر `StateFlow` لتحديث واجهات المستخدم فور تغيير حالة الإذن في النظام.
3. **`NotificationPermissionPolicy`:**
   - تقييم ديناميكي لسياسات طلب إذن الإشعارات حسب الشاشة والتوقيت وإصدار النظام.
4. **`PreciseLocationUpgrade`:**
   - دعم الترقية المؤقتة لدقة الموقع الجغرافي الكاملة (Full Accuracy) على نظام iOS عبر `CLLocationManager`.
5. **`PermissionUtils` & `AndroidPermissionStatusResolver`:**
   - أدوات مساعدة متكاملة لربط وفحص الصلاحيات مباشرة مع نظام Android ومكتبات AndroidX.
