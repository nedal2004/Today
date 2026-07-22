# SUMMARY — إكمال مشروع "Today"

هذا الملخص يوثّق ما أُنجز في هذه الجلسة لإكمال مشروع "Today" (تطبيق مهام
offline-first مع Focus Widget) من نهاية الأسبوع ٣ حتى نهاية الأسبوع ٧،
والقرارات المتخذة، والمشاكل التي واجهتُها، وخطوات التحقق المطلوبة منك.

## ⚠️ ملاحظة أولى مهمة: لا يوجد ملف Blueprint في المستودع (تم استلامه لاحقاً في المحادثة)

قبل كتابة أي سطر، بحثتُ في كل الفروع (`Override` و
`claude/today-project-completion-hczgga`) وكامل تاريخ الـ commits — **لا يوجد
أي ملف Blueprint أو README أو خطة مكتوبة في هذا المستودع على الإطلاق**، فقط
كود المشروع. أعدت بناء خطة الأسابيع ٤-٧ من أدلة موجودة فعلاً في الكود
(تعليقات بـ `gradle/libs.versions.toml`) ولم أتوقف ولم أسأل، حسب التوجيه.

**بعد إنجاز كل ما هو موصوف أدناه، زوّدني المستخدم بنص الـ Blueprint الحقيقي
كاملاً.** قارنته بما بنيته وصحّحت الفروقات (تفصيل كامل في `DECISIONS.md` §٧):
أهمها إعادة تسمية `FocusWidget` → `TodayWidget`/`TodayWidgetReceiver`/
`WidgetActions.kt` (كما يحدده الـ Blueprint حرفياً)، إضافة زر "إضافة مهمة" من
الودجت (كان الجزء الناقص من تفاعل أسبوع ٦ الحقيقي)، وكتابة `README.md`
احترافي فيه Product Brief ومخططات UML (هذا هو تسليم أسبوع ٧ الحقيقي — وليس
الاختبارات/SavedStateHandle التي بنيتها كإضافة جودة قبل استلام الـ Blueprint).
كل ما هو موصوف بالأقسام التالية **محدَّث ليعكس الحالة النهائية بعد هذا
التصحيح.**

## ١. ما أُنجز، مرتّباً حسب الأسابيع

### أسبوع ١-٢ (كانا منجزَين مسبقاً، تم التحقق منهما فقط)
- طبقة Room كاملة: `Task`, `Bucket`, `Converters`, `TaskDao`, `AppDatabase` (singleton)
- `TaskRepository` كواجهة وحيدة فوق `TaskDao`
- Design System: `Color.kt`, `Spacing.kt`, `Type.kt`, `Theme.kt`
- `TaskListScreen` أساسي مع حوارات إضافة/تعديل/حذف

### أسبوع ٣ (كان شبه منجز، أُكمل في هذه الجلسة)
- **TopAppBar** يعرض اسم التطبيق
- تعريب/i18n كامل: كل نص كان مكتوباً inline بالعربية في `TaskListScreen.kt`
  انتقل إلى `values/strings.xml` (إنجليزي) و `values-ar/strings.xml` (عربي)،
  بما فيها تسميات تبويبات TODAY/TOMORROW/LATER عبر `Bucket.label()`
- مكوّن `EmptyState` مستقل أحادي المسؤولية بدل `Text` inline
- إصلاح حالتين حدّيتين متعلقتين بدوران الشاشة/process death (تفصيل في §٤)

### أسبوع ٤ — WorkManager: الترحيل اليومي
- `DailyRolloverWorker` (CoroutineWorker) يستدعي
  `TaskRepository.runDailyRollover` الموجود مسبقاً (ترقية TOMORROW→TODAY
  وأرشفة المهام المنجزة القديمة)
- `TodayWorkerFactory` يحقن `TaskRepository` يدوياً في الـ Worker بدون أي DI
  framework
- `TodayApp` أصبح `Configuration.Provider`، والتهيئة التلقائية الافتراضية
  لـ WorkManager عُطّلت في `AndroidManifest.xml` (شرط لازم عند استخدام
  Configuration.Provider مخصص)
- `RolloverScheduler` يجدول `PeriodicWorkRequest` كل ٢٤ ساعة بدءاً من أقرب
  منتصف ليل تالٍ

### أسبوع ٥ — Glance (بناء الودجت، عرض فقط حسب الـ Blueprint الحقيقي)
- فُعّلت تبعيات `glance-appwidget` و `glance-material3` (كانتا معلّقتين في
  Gradle بانتظار هذا الأسبوع بالضبط)
- `TodayWidget` (بعد التصحيح — كان اسمه `FocusWidget` قبل استلام الـ
  Blueprint): "وضع التركيز" — يعرض أهم مهمة الآن بخط كبير + عدّاد "+N more"
  للباقي، بدل جملة كاملة. الضغط على الودجت يفتح `MainActivity`. الألوان من
  `GlanceTheme.colors` (glance-material3) فقط، لا قيم hardcoded
- `TodayWidgetReceiver` + `res/xml/today_widget_info.xml`، مسجَّلان في
  Manifest
- `TaskListViewModel` أصبح يأخذ `onDataChanged: suspend () -> Unit` يُستدعى
  بعد كل تعديل، مربوطاً في `MainActivity` باستدعاء `TodayWidget().updateAll(...)`
  — يبقي الـ ViewModel خالياً من أي اعتماد مباشر على Android Context
- **إصلاح ضروري اكتُشف أثناء هذا الأسبوع**: `onComplete`/`onUndo` في
  الـ ViewModel كانا لا يضبطان `completedAt` إطلاقاً، ما يعني أن
  `archiveOldDone` (المُستخدم في أسبوع ٤) لن يحذف أي شيء أبداً. نُقل المنطق
  الصحيح إلى `TaskRepository.complete()/uncomplete()` ليُستخدم من الـ ViewModel
  والودجت معاً بدون تكرار

### أسبوع ٦ — تفاعل الودجت + Material You (الحقيقي، حسب الـ Blueprint)
- `WidgetActions.kt`: `CompleteFocusTaskAction` (`actionRunCallback`) ينجز
  المهمة المعروضة مباشرة من الودجت
- زر "إضافة" جديد على الودجت (في الحالتين: يوجد مهمة، أو القائمة فاضية) يفتح
  `MainActivity` مع `EXTRA_OPEN_ADD_DIALOG=true`؛ `MainActivity`/`TaskListScreen`
  يقرآن هذا الـ extra ليُفتح حوار الإضافة مباشرة — هذا كان الجزء الناقص من
  "actionRunCallback (شطب/إضافة من الـ widget)" بالـ Blueprint
- مزامنة widget↔app: مضمونة أصلاً عبر `onDataChanged` (أسبوع ٥) وعبر
  `DailyRolloverWorker` الذي يستدعي `TodayWidget().updateAll(...)` بعد كل
  ترحيل يومي
- Material You: `GlanceTheme.colors` من `glance-material3` توفّره تلقائياً
  (ألوان ديناميكية على أندرويد 12+، وثيم ثابت كـ fallback دونه) — لا كود
  إضافي كان مطلوباً، فقط تم التحقق والتوثيق

### أسبوع ٧ — UML + README احترافي (الحقيقي، حسب الـ Blueprint)
- `README.md` جديد: Product Brief كامل (Value Proposition، Target Customer،
  Opportunity، Feasibility)، مخطط معمارية، خريطة الأنماط، مخطط Class UML
  ومخطط Sequence UML لسيناريو "إنجاز مهمة مع Undo" (Mermaid)، بنية المشروع،
  أوامر البناء/الاختبار، وخارطة v1.1/v2
- **Screenshots/GIF لم تُلتقط**: تحتاج تشغيل فعلي على جهاز/محاكي غير متاح في
  هذه البيئة السحابية — مذكورة صراحة كمطلوب منك بعد فتح المشروع

### عمل جودة إضافي (🟡 لم يكن جزءاً رسمياً من أي أسبوع بالـ Blueprint، أُنجز كذلك)
- `TaskListViewModelTest` (JVM بحت) + `FakeTaskDao` + `TaskDaoTest`
  (instrumented، Room in-memory) — يغطيان منطق الـ ViewModel والترحيل اليومي
- `SavedStateHandle` لحفظ التبويب المختار عبر process death،
  و`rememberSaveable` لـ `editingTaskId` ونص حوار التعديل عبر دوران الشاشة
- مراجعة يدوية شاملة: توازن الأقواس، تطابق كل مفاتيح `R.string.*` بين الكود
  و`values`/`values-ar`، تطابق كل `package` مع مسار الملف (تم تكرار هذا
  التحقق بعد إعادة تسمية الودجت أيضاً)

## ٢. القرارات المعمارية والتفصيلية (التفاصيل الكاملة في `DECISIONS.md`)

1. **لا يوجد Blueprint** → أُعيد بناء خطة الأسابيع ٤-٧ من أدلة داخل الكود
   نفسه + وصف المهمة + القيود المعمارية المذكورة، ووُثّق ذلك.
2. **لغة strings.xml الافتراضية = إنجليزي**، `values-ar` = عربي (عرف Android
   القياسي، يحقق "دعم العربية والإنجليزية" بشكل متماثل).
3. **WorkManager**: `WorkerFactory` يدوي بدل أي DI framework، لأن القرار
   المعماري يمنع صراحة أي over-engineering، والمشروع لا يحتاج Hilt لعامل
   واحد فقط.
4. **Focus Widget "مركّز" على مهمة واحدة فقط** (وليس قائمة كاملة قابلة
   للتمرير) — تفادياً لتكرار منطق `TaskListScreen` بالكامل داخل Glance، وهذا
   ينسجم أصلاً مع اسم "Focus".
5. **لا اختبار مكتبة إضافية** (Turbine) — استُخدم `backgroundScope` +
   `UnconfinedTestDispatcher` من `kotlinx-coroutines-test` نفسها.
6. **`TaskRepository` بقيت class ملموسة (لا interface)** — إذ لا حاجة فعلية
   لتبديل تطبيقها، والاختبار تم عبر تزويد `FakeTaskDao` (واجهة `TaskDao`
   موجودة أصلاً بسبب Room)، فلا داعي لإضافة تجريد جديد.

## ٣. مشاكل واجهتها وكيف حُلّت

- **تعارض أسماء الحزم**: `namespace`/`applicationId` في Gradle هو
  `com.nedal.today`، لكن كل كود Kotlin موجود تحت حزمة `com.n.alian.today`.
  ظننته سهواً في البداية، لكن **الـ Blueprint الحقيقي يوثّق هذا كتغيير مقصود**
  ("الحزمة الحقيقية: com.n.alian.today ... applicationId بالـ Gradle لسه
  com.nedal.today — بيشتغل عادي")، فلا داعي لأي "إصلاح". يبقى فقط التعامل
  التقني الصحيح: صنف `R` المُولَّد فعلياً هو `com.nedal.today.R`، وكل مكان
  يستورد موارد (`BucketLabel.kt`, `TaskListScreen.kt`, `TodayWidget.kt`)
  يستورده صراحة من هناك.
- **`completedAt` لم يكن يُضبط أبداً** (موصوف أعلاه في أسبوع ٥) — اكتُشف أثناء
  ربط زر الإنجاز في الودجت بنفس منطق ViewModel، وأثّر مباشرة على صحة أرشفة
  أسبوع ٤، فتم إصلاحه بأقل تدخل ممكن (commit منفصل موسوم `fix:`).
- **لا يوجد Android SDK في هذه البيئة السحابية** (`ANDROID_HOME` فارغ،
  `./gradlew` بلا صلاحية تنفيذ ولم يُحاول تفعيله لأن السبب الجذري هو غياب
  الـ SDK وليس صلاحيات الملف). لذلك لم يكن ممكناً تشغيل build حقيقي أو
  الاختبارات. بدلاً من التوقف، تمت مراجعة كل ملف Kotlin يدوياً بدقة: توازن
  الأقواس، صحة الـ imports، تطابق تواقيع الدوال مع الاستدعاءات، وتطابق كل
  مفتاح `R.string.*` مستخدَم مع مفتاح معرَّف فعلياً في كلا ملفي strings.xml
  (تم تشغيل هذا التحقق آلياً عبر grep، والنتيجة تطابق تام).

## ٤. خطوات التحقق المطلوبة منك (في Android Studio)

1. **Sync + Build**: افتح المشروع، دع Gradle يعمل Sync (سيحمّل
   `glance-appwidget`/`glance-material3` و`kotlinx-coroutines-test` لأول
   مرة)، ثم Build → Make Project. هذا أول تحقق حقيقي من صحة الكود لأنني لم
   أستطع تشغيله هنا.
2. **الشاشة الرئيسية**: شغّل التطبيق، جرّب:
   - إضافة مهمة، تعديلها، حذفها
   - سحب مهمة لإنجازها (Swipe) ثم الضغط على "تراجع" في الـ Snackbar
   - التبديل بين تبويبات اليوم/غداً/لاحقاً
   - غيّر لغة الجهاز إلى العربية وتأكد من ظهور كل النصوص معرَّبة والاتجاه RTL
   - دوّر الشاشة أثناء فتح حوار تعديل مع نص مكتوب غير محفوظ — يجب أن يبقى
   - من إعدادات المطوّر: "Don't keep activities" ثم ارجع للتطبيق — يجب أن
     يبقى التبويب المختار كما هو (اختبار process death)
3. **الودجت (TodayWidget)**: من الشاشة الرئيسية للجهاز، اضغط مطولاً → ودجات
   → "Today" → أضف الودجت. تحقق أنه يعرض أهم مهمة + عدّاد "+N more" إن وُجد،
   واضغط "إنجاز" وتأكد أن الودجت يتحدّث فوراً وأن التطبيق (إن كان مفتوحاً)
   يعكس نفس التغيير. ثم اضغط زر "+ إضافة مهمة" وتأكد أن التطبيق يُفتح مباشرة
   على حوار إضافة مهمة جديدة.
4. **الترحيل اليومي**: هذا يصعب اختباره فورياً لأنه مجدول لمنتصف الليل
   التالي. يمكنك التحقق منطقياً عبر `adb shell am broadcast` لمحاكاة
   WorkManager، أو ببساطة عبر `TaskDaoTest` (اختبار instrumented) الذي
   يغطي بالضبط `promoteTomorrowToToday` و `archiveOldDone`.
5. **الاختبارات**:
   - `./gradlew testDebugUnitTest` → يجب أن ينجح `TaskListViewModelTest`
     (JVM بحت، سريع)
   - `./gradlew connectedDebugAndroidTest` (يحتاج جهاز/محاكي) → يجب أن ينجح
     `TaskDaoTest`

## ٥. اقتراحات لما بعد v1

- شاشة "المهام المنجزة اليوم" تستخدم `TaskDao.observeDoneToday` الموجود
  مسبقاً وغير مستخدَم حالياً في أي شاشة
- سحب لليسار (`enableDismissFromEndToStart`) لحذف سريع، بدل الاكتفاء بالسحب
  لليمين للإنجاز فقط
- إشعار تذكير اختياري (WorkManager + Notification) لمهام TODAY لم تُنجز
  بحلول ساعة معينة
- ودجت بحجم أكبر (2×2 أو أكثر) يعرض 2-3 مهام بدل مهمة واحدة فقط، كخيار
  للمستخدم وليس بديلاً عن الحجم الحالي
- دعم ترتيب المهام يدوياً (drag to reorder) بالاستفادة من `sortOrder`
  الموجود مسبقاً في `Task` لكنه غير مُستخدَم فعلياً بعد
