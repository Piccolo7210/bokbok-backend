# BokBok Flutter App - Development Summary

## Overview

This document summarizes the progress made on the BokBok Flutter chat application. The app is a full-featured chat application with real-time messaging, media sharing, voice notes, and voice/video calls.

---

## Completed Segments

### Segment 5 - Chat List Screen

**Created files:**
- `lib/features/chat/presentation/bloc/chat_list/chat_list_bloc.dart` - BLoC for chat list management
- `lib/features/chat/presentation/bloc/chat_list/chat_list_event.dart` - Events for chat list
- `lib/features/chat/presentation/bloc/chat_list/chat_list_state.dart` - States for chat list
- `lib/features/chat/presentation/widgets/online_indicator.dart` - Online status indicator
- `lib/features/chat/presentation/widgets/avatar_widget.dart` - User avatar widget
- `lib/features/chat/presentation/widgets/conversation_tile.dart` - Chat list item widget
- `lib/features/chat/presentation/screens/chat_list_screen.dart` - Main chat list screen

**Updated files:**
- `lib/main.dart` - Added ChatListBloc provider
- `lib/core/router/app_router.dart` - Added /chats route

---

### Segment 6 - Chat Screen

**Created files:**
- `lib/features/chat/presentation/bloc/chat/chat_bloc.dart` - BLoC for chat messaging
- `lib/features/chat/presentation/bloc/chat/chat_event.dart` - Events for chat
- `lib/features/chat/presentation/bloc/chat/chat_state.dart` - States for chat
- `lib/features/chat/presentation/widgets/message_bubble.dart` - Message bubble widget
- `lib/features/chat/presentation/widgets/message_status_icon.dart` - Message status icons
- `lib/features/chat/presentation/widgets/date_separator.dart` - Date separator widget
- `lib/features/chat/presentation/widgets/typing_indicator.dart` - Typing indicator
- `lib/features/chat/presentation/widgets/chat_input_bar.dart` - Message input bar
- `lib/features/chat/presentation/screens/chat_screen.dart` - Main chat screen

**Updated files:**
- `lib/main.dart` - Added ChatBloc provider

---

### Segment 7 - Media Sharing

**Created files:**
- `lib/features/media/presentation/bloc/media_bloc.dart` - BLoC for media handling
- `lib/features/media/presentation/bloc/media_event.dart` - Events for media
- `lib/features/media/presentation/bloc/media_state.dart` - States for media
- `lib/features/media/presentation/widgets/media_picker_sheet.dart` - Media picker bottom sheet
- `lib/features/media/presentation/widgets/upload_progress_indicator.dart` - Upload progress
- `lib/features/media/presentation/screens/image_viewer_screen.dart` - Full-screen image viewer
- `lib/features/media/presentation/screens/video_player_screen.dart` - Video player screen

**Updated files:**
- `lib/features/chat/presentation/widgets/message_bubble.dart` - Added media type support
- `pubspec.yaml` - Added video_player dependency

---

### Segment 8 - Voice Notes

**Created files:**
- `lib/features/media/presentation/bloc/voice_note/voice_note_bloc.dart` - BLoC for voice notes
- `lib/features/media/presentation/bloc/voice_note/voice_note_event.dart` - Events for voice notes
- `lib/features/media/presentation/bloc/voice_note/voice_note_state.dart` - States for voice notes
- `lib/features/media/presentation/widgets/voice_note_recorder.dart` - Voice note recorder widget
- `lib/features/media/presentation/widgets/voice_note_player.dart` - Voice note player widget

**Updated files:**
- `lib/features/chat/presentation/widgets/chat_input_bar.dart` - Added voice note recording
- `android/app/src/main/AndroidManifest.xml` - Added RECORD_AUDIO permission

---

### Segment 9 - Voice & Video Calls

**Created files:**
- `lib/services/webrtc/webrtc_service.dart` - WebRTC service (simplified/placeholder)
- `lib/features/call/presentation/bloc/call_bloc.dart` - BLoC for calls (events, states combined)
- `lib/features/call/presentation/bloc/call_event.dart` - Events for calls
- `lib/features/call/presentation/bloc/call_state.dart` - States for calls
- `lib/features/call/presentation/screens/call_screen.dart` - Call screen
- `lib/features/call/presentation/widgets/call_controls.dart` - Call control buttons
- `lib/features/call/presentation/widgets/incoming_call_overlay.dart` - Incoming call overlay

**Updated files:**
- `pubspec.yaml` - Added flutter_webrtc dependency
- `android/app/src/main/AndroidManifest.xml` - Added CAMERA, RECORD_AUDIO, etc. permissions

---

### Segment 10 - Profile & Settings

**Created files:**
- `lib/features/user/presentation/bloc/search/search_bloc.dart` - BLoC for user search
- `lib/features/user/presentation/bloc/search/search_event.dart` - Events for search
- `lib/features/user/presentation/bloc/search/search_state.dart` - States for search
- `lib/features/user/presentation/bloc/profile/profile_bloc.dart` - BLoC for profile
- `lib/features/user/presentation/bloc/profile/profile_event.dart` - Events for profile
- `lib/features/user/presentation/bloc/profile/profile_state.dart` - States for profile
- `lib/features/user/presentation/widgets/user_tile.dart` - User list item widget
- `lib/features/user/presentation/screens/search_screen.dart` - User search screen
- `lib/features/user/presentation/screens/profile_screen.dart` - User profile screen
- `lib/features/user/presentation/screens/settings_screen.dart` - Settings screen

**Updated files:**
- `lib/core/router/app_router.dart` - Added /search, /settings, /newChat routes
- `lib/main.dart` - Added ProfileBloc provider

---

## Bug Fixes Applied

1. **Package version issues** - `stomp_dart_client` upgraded to 3.0.1

2. **Record package compatibility** - Upgraded from 5.2.0 to 6.0.0 to fix `record_linux` compatibility

3. **UserTile field names** - Fixed incorrect field names (subtitle → body1, phoneNumber → phone)

4. **ChatScreen conversation creation** - Fixed to create conversation when starting new chat

5. **ProfileScreen type cast bug** - Fixed type cast error where `ProfileLogout` was cast as `ProfileUpdating`

---

## Known Issues

### Backend Issue: 403 Forbidden on Conversation Creation

**Issue Description:**

When attempting to create a new conversation via the REST API, the application receives a 403 Forbidden response. This prevents users from starting new chats.

**Endpoint:** `POST https://bokbok-backend.onrender.com/api/chat/conversations`

**Error Response:** `403 Forbidden`

**Root Cause:** The backend appears to have the REST API endpoint for creating conversations blocked or disabled. However, the search functionality (GET requests) works correctly.

**Impact:**
- Users cannot initiate new conversations from the chat list
- Existing conversations can be loaded and messaged
- User search functionality works

**Potential Solutions:**

1. **Fix the backend** - Enable POST requests to `/api/chat/conversations` on the server
2. **Use WebSocket instead** - Implement conversation creation via STOMP WebSocket protocol
3. **Alternative endpoint** - Check if there's an alternative API endpoint for creating conversations

**Related Files:**
- `lib/features/chat/data/datasources/chat_remote_datasource.dart` - Contains getOrCreateConversation method
- `lib/core/network/dio_client.dart` - HTTP client with authentication

---

## Project Structure

```
bokbok/
├── lib/
│   ├── main.dart
│   ├── core/
│   │   ├── router/
│   │   │   └── app_router.dart
│   │   ├── network/
│   │   │   ├── dio_client.dart
│   │   │   └── stomp_client.dart
│   │   └── config/
│   │       └── api_config.dart
│   ├── features/
│   │   ├── auth/
│   │   │   └── (login, register)
│   │   ├── chat/
│   │   │   ├── data/
│   │   │   │   └── datasources/
│   │   │   └── presentation/
│   │   │       ├── bloc/
│   │   │       ├── screens/
│   │   │       └── widgets/
│   │   ├── user/
│   │   │   └── presentation/
│   │   ├── media/
│   │   │   └── presentation/
│   │   └── call/
│   │       └── presentation/
│   └── services/
│       └── webrtc/
├── android/
│   └── app/src/main/AndroidManifest.xml
└── pubspec.yaml
```

---

## Dependencies Added

- `flutter_bloc` - State management
- `dio` - HTTP client
- `stomp_dart_client` - WebSocket STOMP client
- `video_player` - Video playback
- `record` - Audio recording
- `flutter_webrtc` - WebRTC for calls
- `image_picker` - Media selection
- `cached_network_image` - Image caching
- `intl` - Date formatting
- `uuid` - Unique ID generation

---

## Next Steps

1. **Resolve 403 Forbidden issue** - Fix backend or implement WebSocket-based conversation creation
2. **Complete WebRTC implementation** - Current service is a placeholder, needs full WebRTC signaling
3. **Add push notifications** - Segment 11 (Firebase Cloud Messaging)
4. **Testing** - Comprehensive testing of all features

---

*Last Updated: March 2026*
