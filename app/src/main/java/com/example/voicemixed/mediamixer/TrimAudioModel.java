package com.example.voicemixed.mediamixer;

public class TrimAudioModel {
    private String filePath;
    private final int startOffset; // inSecond
    private final float endOffset; // inSecond
    private final float volume;
    private final float delayOffsets; // inMillisecond

    public TrimAudioModel(String filePath, int startOffset, float endOffset, float volume, float delayOffsets) {
        this.filePath = filePath;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.volume = volume;
        this.delayOffsets = delayOffsets;
    }


    public String getFilePath() {
        return filePath;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public float getEndOffset() {
        return endOffset;
    }

    public float getVolume() {
        return volume;
    }

    public float getDelayOffsets() {
        return delayOffsets;
    }
}
