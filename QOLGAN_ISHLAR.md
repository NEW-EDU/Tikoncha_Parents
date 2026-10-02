# Qolgan ishlar — Parent (KMP)

Holat sanasi: **2026-10-02**. Ishni davom ettiradigan dasturchi (va uning AI agenti) uchun.

To'liq ro'yxat, v2.2 qoidalari, backend bilan bog'liqlik va sinov ssenariylari Student reposida:
`NEW-EDU/New-Edu` → `docs/QOLGAN_ISHLAR.md` va `CLAUDE.md`. Bu faylda faqat Parent'ga tegishlisi.

## Holat

- Branch `policy-v2.1`; oxirgi reliz v2.0.0 (versionCode 25), 2026-09-25, commit `3933512`.
- Relizdan keyin commit qilingan: `1aca0e7` (refresh token JSON body'da), `556f67d` (kategoriyalar Google Play ID'larida).
- **Policy to'g'riligi v2.2** (2026-09-30). `git log` da ko'rinmasa — hali commit qilinmagan, loyiha egasidan so'rang.
  Belgisi: `domain/policy/TimeRuleMatcher.kt` da `occurrenceDayOffset` bor.

## v2.2 da Parent'da nima o'zgargan

- `TimeRuleMatcher` / `PolicyActiveness`: tungi oyna boshlangan kunga tegishli; "dan tashqari" faqat ro'yxatdagi kunlarda.
- `CreatePolicyUseCase`: `PolicyConditions.isValid()`, `PolicyLimits.isValid()`, `PolicyDraft.withSharedDays()` —
  vaqt + limit bo'lsa kunlar bitta. `SavePolicyDraftUseCase` va `UpdatePolicyUseCase` shu tekshiruvdan o'tkazadi.
- Tahrirlagich: boshlanish == tugash bo'lganda jadval 24/7 ga aylanib qolmaydi; limit kunlari vaqt kunlariga ergashadi.
- Sayt nishoni: yo'l (`/path`) qabul qilinmaydi (`TargetsEditorState`).
- Himoya ekrani: "jadval yetib bordi" qatori (`policies_up_to_date`, `policies_synced_at`) — backend migratsiyasi va deploy'dan keyin ishlaydi.
- Matnlar uch tilda: `composeResources/values*/strings.xml`.

## Qolgan ishlar

| # | Ish |
|---|---|
| 1 | v2.2 ni telefonda sinash (Student faylidagi ssenariylar 1, 2, 4, 9) |
| 2 | ALLOW (oq ro'yxat) jadvalida Shorts/Reels tanlash hech narsa qilmaydi — yashirish yoki tushuntirish |
| 3 | Jadval kartochkalarida ALLOW/DENY turi va limit kunlarini ko'rsatish |
| 4 | MOB-01: AI-chat WebView tokenni begona saytga yuborishi mumkin |
| 5 | MOB-04: WebSocket tokeni URL'da va release logda |
| 6 | `.gitignore`: `release/`, `build-log.txt`, `build.log`, `*.bak`, `iosApp/full_log.txt` |
| 7 | `iosApp/iosApp.xcodeproj/project.pbxproj` da commit qilinmagan o'zgarish bor — kimniki ekanini aniqlang |

## Buyruqlar

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew -q :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosSimulatorArm64 :composeApp:testDebugUnitTest
```

## Tegmaslik kerak

- Release imzosi (signing) sozlamalari.
- `ResourceProvider` ishlatilmaydi — matn faqat Compose'da.
- Prod server 2026-10-01 da buzilgani aniqlangan; yangi serverga ko'chguncha prod'ga qarshi sinab bo'lmaydi.
