# Cloudinary free setup (mobile OK — no PC, no Blaze)

Firebase Storage paise maangta hai → hum **Cloudinary free** use karte hain.

## 1. Account (phone Chrome)

1. Open https://cloudinary.com
2. **Sign up free** (Google se bhi ho sakta hai)
3. Dashboard pe **Cloud name** dikhega — copy karo

## 2. Unsigned upload preset

1. Dashboard → **Settings** (gear) → **Upload**
2. **Upload presets** → **Add upload preset**
3. Settings:
   - **Signing mode**: **Unsigned**
   - **Preset name**: `pulse_chat_unsigned` (ya koi bhi naam)
   - Folder (optional): `pulse_chat`
4. **Save**

## 3. App mein values daalo

File:

`app/src/main/java/com/pulsechat/app/data/media/CloudinaryUploader.kt`

```kotlin
const val CLOUD_NAME = "yahan_apna_cloud_name"
const val UPLOAD_PRESET = "pulse_chat_unsigned"
```

GitHub mobile / website se is file ko edit karke save karo.

## 4. Firebase mein kya chahiye (Storage NAHI)

| Service | Need? |
|--------|--------|
| Authentication → Phone | Yes |
| Firestore | Yes |
| **Storage** | **No — skip** |
| Cloud Messaging | Yes |

Package name: `com.pulsechat.app`

## Free limits (Cloudinary)

- Enough for development + small user base
- Images, video, audio upload supported via `auto/upload`
