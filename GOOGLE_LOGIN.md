# Google Login setup (Pulse Chat)

## 1. Firebase Console (mobile se)

1. Open https://console.firebase.google.com → project **viewtube-v2**
2. **Authentication** → **Sign-in method**
3. **Google** → Enable → Support email select → **Save**
4. **Email/Password** → Enable → **Save**

## 2. Add SHA-1 (REQUIRED for Google — Error 10 fix)

1. Firebase → Project Settings (gear) → Your apps → **com.pulsechat.app**
2. Scroll to **SHA certificate fingerprints** → **Add fingerprint**
3. Paste this SHA-1 (for GitHub Actions / our debug APK):

```
18:99:03:75:C9:72:67:11:BD:2C:F7:31:F6:92:00:9D:8F:04:9E:D9
```

4. Also add SHA-256 if asked:

```
18:C7:D1:C7:CB:74:4C:67:99:94:0F:01:37:72:C1:A5:43:D8:27:70:26:56:C5:96:47:7E:50:63:D5:E8:F1:DA
```

5. **Download** new `google-services.json` and replace `app/google-services.json` in the repo (or keep current if package is already `com.pulsechat.app`).

## 3. Wait 5–10 minutes

Google OAuth can take a few minutes after adding SHA-1.

## 4. Install latest APK from Actions and try "Continue with Google"

Email + password login works without SHA-1 anytime.
