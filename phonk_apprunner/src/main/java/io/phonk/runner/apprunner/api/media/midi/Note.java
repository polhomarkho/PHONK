package io.phonk.runner.apprunner.api.media.midi;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Note {
    private final static Map<String, Integer> NOTE_NAME_MIDI_PITCH_MAP;
    static {
        NOTE_NAME_MIDI_PITCH_MAP = new HashMap<>();
        final String[][] noteNames = {
                {"C", "B#"},
                {"Db", "C#"},
                {"D"},
                {"Eb", "D#"},
                {"E", "Fb"},
                {"F", "E#"},
                {"Gb", "F#"},
                {"G"},
                {"Ab", "G#"},
                {"A"},
                {"Bb", "A#"},
                {"B", "Cb"}
        };
        int octave = -1;
        int pitch = 0;
        do {
            for (int i = 0; i < noteNames.length; i++) {
                for (final String noteName : noteNames[i]) {
                    pitch = (octave + 1) * 12 + i;
                    if (pitch <= 127) {
                        NOTE_NAME_MIDI_PITCH_MAP.put(noteName + octave, pitch);
                    }
                }
            }
            octave++;
        } while (pitch <= 127);
    }

    public static int getPitch(final String noteName) {
        final Integer pitch = NOTE_NAME_MIDI_PITCH_MAP.get(noteName);
        if (pitch == null) {
            throw new RuntimeException(noteName + " is not a valid note");
        }
        return pitch;
    }

    @NonNull
    public static String getName(final int pitch) {
        for (Map.Entry<String, Integer> namePitch : NOTE_NAME_MIDI_PITCH_MAP.entrySet()) {
            if (pitch == namePitch.getValue()) {
                return namePitch.getKey();
            }
        }
        throw new RuntimeException(pitch + " is not a valid pitch");
    }
}
