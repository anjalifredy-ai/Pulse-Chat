# Pulse Chat

**Original production-oriented Android messaging & calling app**  
Kotlin · Jetpack Compose · Material 3 · Firebase · WebRTC · Hilt · Coroutines

> Not affiliated with WhatsApp / Meta. Original name, branding, UI and architecture.

## Features (implemented / architecture ready)

- Phone number authentication (Firebase Auth OTP)
- Profile setup (name, about, photo)
- Real-time 1:1 & group-ready conversations (Firestore listeners)
- Message send / delivery model + optimistic UI
- Bottom navigation: Chats · Status · Calls · Settings
- WebRTC client for voice & video (offer/answer/ICE)
- Incoming call full-screen UI + FCM high-priority call notifications
- Call foreground service
- Notification channels (messages, calls, status)
- Dark / light / system theme
- Offline Firestore persistence
- Secure storage patterns, no secrets in source
- GitHub Actions workflow that builds and uploads a **downloadable debug APK**

## Repository

https://github.com/anjalifredy-ai/Pulse-Chat

## Setup (required for full functionality)

1. Create a Firebase project at https://console.firebase.google.com
2. Enable **Authentication → Phone**
3. Create Firestore database (production mode + security rules)
4. Enable Storage
5. Enable Cloud Messaging
6. Download `google-services.json` and replace the placeholder at `app/google-services.json`
7. (Optional) Add TURN credentials via backend / secrets — never hardcode them

### Suggested Firestore security rules (start)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    match /conversations/{convId} {
      allow read, write: if request.auth != null
        && request.auth.uid in resource.data.participants;
      match /messages/{msgId} {
        allow read, write: if request.auth != null
          && request.auth.uid in get(/databases/$(database)/documents/conversations/$(convId)).data.participants;
      }
    }
    match /calls/{callId} {
      allow read, write: if request.auth != null
        && (request.auth.uid == resource.data.callerId || request.auth.uid == resource.data.calleeId);
    }
  }
}
```

## Build APK (GitHub Actions)

- Workflow: `.github/workflows/build-apk.yml`
- Triggers on push to `main` and manual `workflow_dispatch`
- Artifact name: **PulseChat-debug-apk**
- Download from the Actions run summary

## Local build

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/`

## Architecture

```
app/
  data/          models + repositories (Firestore, Auth, Storage)
  di/            Hilt modules
  service/       FCM + Call foreground service
  ui/            Compose screens (auth, home, chat, call, settings…)
  webrtc/        WebRtcClient (real PeerConnection)
```

## Next increments (already scaffolded)

- Full media messages (image/video/voice/document) with upload progress
- Group create / admin roles / invite links
- Status / stories with expiration
- Contact sync & discovery
- Read receipts, typing indicators, reactions
- End-to-end encryption layer (Signal Protocol / established library)
- Release signing via GitHub Secrets

---

Built as an original Pulse product.
