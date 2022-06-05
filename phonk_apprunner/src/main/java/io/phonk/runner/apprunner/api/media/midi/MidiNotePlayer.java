package io.phonk.runner.apprunner.api.media.midi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MidiNotePlayer {

    final Map<MidiNote, Integer> playedNotesCount = new ConcurrentHashMap<>();
    private final PMidiController pMidiController;

    public MidiNotePlayer(final PMidiController pMidiController) {
        this.pMidiController = pMidiController;
    }

    public void playNote(final MidiNote noteToPlay) {
        Integer currentCount = playedNotesCount.get(noteToPlay);
        if (currentCount == null) {
            currentCount = 0;
        }
        currentCount++;
        playedNotesCount.put(noteToPlay, currentCount);
        // if not is already played, stop it before playing again to avoid some strange bugs in synths
        if (currentCount > 1) {
            pMidiController.noteOff(noteToPlay.getChannel(), noteToPlay.getPitch(), noteToPlay.getVelocity());
        }
        pMidiController.noteOn(noteToPlay.getChannel(), noteToPlay.getPitch(), noteToPlay.getVelocity());
    }

    public void stopPlayingNote(final MidiNote noteToStop) {
        Integer currentCount = playedNotesCount.get(noteToStop);
        if (currentCount != null && currentCount > 0) {
            currentCount--;
            playedNotesCount.put(noteToStop, currentCount);
            if (currentCount == 0) {
                pMidiController.noteOff(noteToStop.getChannel(), noteToStop.getPitch(), noteToStop.getVelocity());
            }
        }
    }
}
