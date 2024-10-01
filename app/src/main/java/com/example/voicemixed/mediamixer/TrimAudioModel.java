package com.example.voicemixed.mediamixer;

public class TrimAudioModel {
    private String filePath;
    private int startOffset; // inSecond
    private int endOffset; // inSecond
    private float volume;
    private int delayOffsets; // inMillisecond

    public TrimAudioModel(String filePath, int startOffset, int endOffset, float volume, int delayOffsets) {
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

    public int getEndOffset() {
        return endOffset;
    }

    public float getVolume() {
        return volume;
    }

    public int getDelayOffsets() {
        return delayOffsets;
    }
}
