package com.example.voicemixed.mediamixer;

public class TrimAudioModel {
    private String filePath;
    private int startOffset;
    private int endOffset;
    private float volume;

    public TrimAudioModel(String filePath, int startOffset, int endOffset, float volume) {
        this.filePath = filePath;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.volume = volume;
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
}
