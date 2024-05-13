# Challenges Faced 💪

### #1 Screen Recording related 📷
- **#1.1 How to record screen?**
  Learnt about `MediaProjection`, `VirtualDisplay`, `MediaRecorder` APIs in Android.
-  **#1.2 How to record just the internal audio along with video?**
   Learnt about
    - `AudioRecord`
    - `MediaCodec`, `MediaMuxer` and how to use it.
    - How to record internal in parallel to `MediaRecorder` which records screen and mic
- **#1.3 How to mix the two files?**
  Learnt
    - `MediaExtractor` and how to use it with `MediaMuxer` to mix two files in parallel

### #2 [IMP] 👑 Creating multi module architecture
- Created plugins for `core`, `domain` and `feature` modules to avoid code redundancy
- Packaged each feature in its own module

### #3 Create notification controls for ScreenRecorder
- It was required to enable user to stop recording even if the app is destroyed.
- Learnt about
    - How to display notification.
    - `RemoteView`
    - How to display custom views on Notification? And handle click behaviours.
    - How to update notification view upon any action.
    - **👑 [IMP]** **How to make sure that the Notification controls and HomeScreen's controls are in sync.** For eg: If the user clicks "Pause" on HomeScreen, the Notification should be updated to show only "Resume" button and vice versa. Solution: Designed `ScreenRecorderEventRepository` that acts as a communication channel between Service and UI. ScreenRecorderService also updates notification UI when required. The NotificationPanel sends events to Service using PendingIntents.