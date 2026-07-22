# قرارات التنفيذ (Implementation Decisions)

هذا الملف يوثّق كل القرارات المعمارية والتفصيلية التي اتُّخذت أثناء إكمال مشروع
"Today"، خصوصاً في الحالات التي كانت فيها الخطة (Blueprint) غامضة أو غير موجودة،
حسب التوجيه: "اتخذ القرار الأبسط المنسجم مع القرارات المعمارية، وسجّله هنا،
لا تتوقف ولا تسأل".

## 0. ملاحظة مهمة: ملف الـ Blueprint غير موجود في المستودع

تم البحث في كل الفروع (`Override`, `claude/today-project-completion-hczgga`)
وكامل تاريخ الـ commits عن أي ملف باسم يحتوي "blueprint" أو "plan" أو حتى
`README.md` — **لا يوجد أي منها في المستودع على الإطلاق**. لا يوجد سوى كود
المشروع نفسه.

بما أن التعليمات صريحة بعدم التوقف أو السؤال، تم إعادة بناء خطة الأسابيع ٤-٧
اعتماداً على:
- تعليقات صريحة موجودة فعلياً في `gradle/libs.versions.toml` و
  `app/build.gradle.kts` تربط كل تقنية بأسبوع محدد:
  - `Room (أسبوع ١)`
  - `WorkManager (أسبوع ٤)`
  - `Glance (أسبوع ٥)` معلّق حالياً بانتظار التفعيل
- وصف المشروع في المهمة: "تطبيق مهام offline-first مع Focus Widget"
- القرارات المعمارية الملزمة المذكورة في المهمة (MVVM+Repository، Room
  كمصدر حقيقة وحيد، KSP، UiState واحد، Bucket enum، Undo بسيط، لا
  LocalDataSource، Design System موحد، لا over-engineering)
- الحالة الفعلية للكود المُنجز (أسابيع ١-٣ تقريباً كما وُصفت في المهمة، وتم
  التحقق من ذلك بقراءة الكود فعلياً)

### إعادة بناء الخطة الكاملة (٧ أسابيع)

| الأسبوع | المحتوى | الحالة قبل هذه الجلسة |
|---|---|---|
| ١ | طبقة Room (Task, Bucket, TaskDao, Converters, AppDatabase) + Repository | ✅ منجز |
| ٢ | Design System (Theme/Color/Spacing/Type) + TaskListScreen أساسي + Add/Edit/Delete dialogs | ✅ منجز |
| ٣ | ViewModel + UiState + Complete/Undo + Snackbar + Checkbox + Swipe-to-complete + Empty State | ✅ منجز جزئياً — كان ناقصاً: TopAppBar، تعريب/i18n للتبويبات، مكوّن EmptyState مستقل، ثبات حالة التعديل عند دوران الشاشة |
| ٤ | **WorkManager**: ترحيل يومي (TOMORROW→TODAY) وأرشفة المهام المُنجزة القديمة، مبني على `runDailyRollover` الموجود مسبقاً في Repository | ❌ لم يُنفَّذ (تم في هذه الجلسة) |
| ٥ | **Glance**: Focus Widget يعرض المهمة التالية وعدد المهام المتبقية اليوم مع زر إنجاز سريع | ❌ لم يُنفَّذ (تم في هذه الجلسة) |
| ٦ | اختبارات: ViewModel (JVM، بمستودع وهمي) + DAO (instrumented، Room in-memory) | ❌ لم يُنفَّذ (تم في هذه الجلسة) |
| ٧ | تلميع نهائي: SavedStateHandle لثبات التبويب المختار عند process death، مراجعة إمكانية الوصول، حالات حدّية، مراجعة كود يدوية شاملة | ❌ لم يُنفَّذ (تم في هذه الجلسة) |

## 1. اللغة الافتراضية لـ strings.xml

القرار: `values/strings.xml` (الافتراضي/fallback) = **إنجليزي**،
`values-ar/strings.xml` = **عربي**. هذا هو العرف القياسي في Android (المسار
الافتراضي هو locale المرجعي وليس بالضرورة العربية)، ويحقق "دعم العربية
والإنجليزية" المطلوب في المعايير بشكل متماثل. كل النصوص العربية التي كانت
مكتوبة inline في `TaskListScreen.kt` نُقلت حرفياً إلى `values-ar` وتُرجمت
للإنجليزية في `values`.

## 2. جدولة الترحيل اليومي (WorkManager)

بما لا يوجد DI framework (ممنوع صراحة over-engineering)، تم استخدام
`Configuration.Provider` على `TodayApp` مع `WorkerFactory` بسيط يدوي
(`TodayWorkerFactory`) لحقن `TaskRepository` في `DailyRolloverWorker` بدون
Hilt/Koin. تم تعطيل التهيئة التلقائية الافتراضية لـ WorkManager عبر
`<provider tools:node="remove">` في AndroidManifest لأن `Configuration.Provider`
يتطلب ذلك.

فاصل التكرار: `PeriodicWorkRequest` كل 24 ساعة مع `initialDelay` محسوبة لأقرب
منتصف ليل تالي (بدل Exact Alarm، لتفادي تعقيد صلاحيات `SCHEDULE_EXACT_ALARM`
غير الضروري لهذه الميزة — هامش تأخير WorkManager الطبيعي مقبول لعملية أرشفة
خلفية غير حرجة التوقيت).

## 3. تصميم Focus Widget (Glance)

القرار الأبسط المتوافق مع "Focus Widget": widget واحد صغير يعرض:
- عدد المهام المتبقية اليوم (بكت TODAY وغير منجزة)
- عنوان أول مهمة نشطة (بحسب sortOrder)
- زر "إنجاز" (ActionCallback) يُنجز تلك المهمة مباشرة من الـ widget
- الضغط على الـ widget نفسه يفتح `MainActivity`

لم يتم بناء widget يعرض قائمة كاملة قابلة للتمرير (LazyColumn داخل Glance) حتى
لا يتكرر منطق TaskListScreen بالكامل داخل Glance — هذا كان سيُعتبر
over-engineering لتطبيق "Focus" (التركيز على مهمة واحدة تالية، وليس عرض كل
القائمة).

تحديث الـ widget بعد أي تعديل (إضافة/إنجاز/تراجع/حذف) يتم عبر lambda بسيطة
`onDataChanged: () -> Unit` تُمرَّر لـ `TaskListViewModel` من طبقة العرض
(بدل حقن Context مباشرة داخل ViewModel، حفاظاً على قابلية اختبار الـ ViewModel
بمعزل عن Android framework).

## 4. الاختبارات (أسبوع ٦)

- اختبار `TaskListViewModel` في `test/` (JVM بحت) باستخدام `FakeTaskRepository`
  يدوي (in-memory map) و `kotlinx-coroutines-test` — بدون Turbine أو أي مكتبة
  اختبار إضافية، تجنباً لإضافة تبعية غير ضرورية؛ جُمعت انبعاثات الـ Flow يدوياً
  عبر `launch` + قائمة.
- اختبار `TaskDao` في `androidTest/` باستخدام Room in-memory database (النمط
  القياسي لاختبار Room) — لم يمكن تشغيله فعلياً في هذه البيئة السحابية (لا
  يوجد Android SDK/emulator)، لكن تمت مراجعته يدوياً بدقة.

## 5. ثبات الحالة عند دوران الشاشة و process death

- `editingTask` كان يُخزَّن ككائن `Task` كامل عبر `remember` عادي (يُفقد عند
  process death). تم تغييره إلى تخزين `editingTaskId: Int?` عبر
  `rememberSaveable`، ثم اشتقاق `editingTask` من `uiState.tasks` — أبسط حل، لا
  حاجة لجعل `Task` قابلاً لـ Parcelable/Serializable.
- `selectedBucket` في الـ ViewModel أصبح يُحفظ عبر `SavedStateHandle` بدل
  `MutableStateFlow` عادي، ليبقى التبويب المختار كما هو بعد إعادة إنشاء
  العملية (process death)، اتساقاً مع معيار "عالج... دوران الشاشة، Process
  death" في المهمة.

## 6. لا تعديل على الميزات المنجزة إلا للضرورة

كل تعديل على كود موجود (مثل `TaskListViewModel`, `TaskListScreen`,
`TodayApp`, `AndroidManifest`) اقتصر على ما تطلبه دمج الميزات الجديدة
(i18n، WorkManager، Glance، SavedStateHandle) وذُكر سببه في رسالة الـ commit
الخاصة به.

## 7. تحديث: استُلم ملف الـ Blueprint الحقيقي بعد إنجاز §0-6

بعد إتمام العمل أعلاه، زوّدني المستخدم بنص الـ Blueprint الفعلي كاملاً. هذا
يصحّح افتراضات §0 ويستبدلها حيث تعارضت. الفروقات وكيف عُولجت:

- **تسمية الحزمة `com.n.alian.today` مقابل `applicationId`/`namespace`
  `com.nedal.today`**: الـ Blueprint نفسه يوثّق هذا كتغيير مقصود عن الخطة
  الأصلية ("الحزمة الحقيقية: com.n.alian.today ... applicationId بالـ Gradle
  لسه com.nedal.today — بيشتغل عادي")، وليس عيباً كما ظننت في §0. لا حاجة لأي
  تعديل — الاستيراد الصحيح لـ `R` من `com.nedal.today.R` (كما فعلت في §0)
  يبقى صحيحاً وضرورياً بغض النظر عن كون الفرق مقصوداً أو لا.
- **تسمية الودجت**: الـ Blueprint يحدد صراحة
  `widget/TodayWidget.kt` + `TodayWidgetReceiver.kt` + `WidgetActions.kt`،
  وليس "Focus Widget" كتسمية صنف (المفهوم/الهوية اسمها "Focus mode"، لكن
  الصنف البرمجي `TodayWidget`). أُعيدت التسمية بالكامل (commit
  `refactor(widget): rename to TodayWidget...`).
- **تقسيم الأسبوعين ٦ و٧ يختلف عمّا افترضته في §0**:
  - **الحقيقي أسبوع ٦** = تفاعل الودجت (`actionRunCallback` لإنجاز **و**إضافة
    مهمة من الودجت) + مزامنة widget↔app + Material You. كنت قد نفّذت زر
    "إنجاز" من الودجت ضمن ما سميته "أسبوع ٥" — بقي فقط زر "إضافة" من الودجت
    (يفتح `MainActivity` مع `EXTRA_OPEN_ADD_DIALOG` ليُفتح حوار الإضافة
    مباشرة) و توثيق أن `GlanceTheme.colors` (من `glance-material3`) توفّر
    Material You تلقائياً بدون كود إضافي — كلاهما أُنجز الآن.
  - **الحقيقي أسبوع ٧** = إصلاح أخطاء + مخططات UML (Mermaid) + README
    احترافي فيه Product Brief + screenshots/GIF، **وليس** اختبارات أو
    SavedStateHandle كما افترضت.
- **الاختبارات (`TaskListViewModelTest`, `TaskDaoTest`) و`SavedStateHandle`
  لثبات التبويب عبر process death**: هذه لم تكن جزءاً رسمياً من أي أسبوع
  بالـ Blueprint الحقيقي، لكنها تبقى — عمل جودة إضافي (🟡 إضافة) ينسجم مع
  معايير الجودة المطلوبة أصلاً في المهمة الأولى (عالج دوران الشاشة/process
  death)، ولا تعارض أي قرار معماري ملزم. لم أتراجع عنها.
- **بنية `domain/usecase/RolloverUseCase.kt`** المذكورة بقسم "بنية المشروع"
  بالـ Blueprint: لم تُضَف. الكود الفعلي الذي يقدّمه الـ Blueprint نفسه بقسم
  "طبقة البيانات الكاملة" (٧.٦) يضع `runDailyRollover` مباشرة داخل
  `TaskRepository` بلا أي use-case منفصل — وهذا هو الموجود فعلاً بالمشروع
  منذ أسبوع ١ (قبل هذه الجلسة). التناقض داخلي بالـ Blueprint نفسه (قسم البنية
  التخطيطي أوسع من الكود الفعلي المُعطى)، والقرار الأبسط المطابق للكود
  الموجود هو عدم إضافة طبقة `domain` غير مستخدَمة أصلاً — ينسجم مع "لا
  over-engineering".
