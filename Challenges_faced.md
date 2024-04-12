# Challenges Faced 💪

## Screen Recording related 📷 
#### 🧐 How to record screen?
Learnt about `MediaProjection`, `VirtualDisplay`, `MediaRecorder` APIs in Android.

#### 🧐 How to record just the internal audio along with video?
Learnt
- `AudioRecord`
- `MediaCodec`, `MediaMuxer` and how to use it.
- How to record internal in parallel to `MediaRecorder` which records screen and mic
  
#### 🧐 How to mix the two files?
Learnt 
- `MediaExtractor` and how to use it with `MediaMuxer` to mix two files in parallel

## Mobile app related
### 🧐 👑 Creating multi module architecture 
- Created plugins for `core`, `domain` and `feature` modules to avoid code redundancy 
- Packaged each feature in its own module
