package io.phonk.runner.apprunner.api.media.midi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ChordParser {

    private static final Pattern CHORD_NAME_PATTERN = Pattern.compile("^([A-G][#b]?)([a-z0-9]+)?$");
    private static final List<Integer> MAJOR_SCALE_SEMITONE_INTERVALS = Arrays.asList(2, 2, 1, 2, 2, 2, 1);

    public static List<MidiNote> findChord(final String rootName, final String degree, final String mode, final int chordSize) {
        final int rotationNumber;
        switch (mode) {
            case "I":
            case "ionian":
            case "major":
                rotationNumber = 0;
                break;
            case "II":
            case "dorian":
                rotationNumber = -1;
                break;
            case "III":
            case "phrygian":
                rotationNumber = -2;
                break;
            case "IV":
            case "lydian":
                rotationNumber = -3;
                break;
            case "V":
            case "mixolydian":
                rotationNumber = -4;
                break;
            case "VI":
            case "aeolian":
            case "minor":
                rotationNumber = -5;
                break;
            case "VII":
            case "locrian":
                rotationNumber = -6;
                break;
            default:
                throw new RuntimeException(mode + " is un unkonwn mode");
        }
        final int degreeIndex;
        switch (degree) {
            case "I":
                degreeIndex = 0;
                break;
            case "II":
                degreeIndex = 1;
                break;
            case "III":
                degreeIndex = 2;
                break;
            case "IV":
                degreeIndex = 3;
                break;
            case "V":
                degreeIndex = 4;
                break;
            case "VI":
                degreeIndex = 5;
                break;
            case "VII":
                degreeIndex = 6;
                break;
            default:
                throw new RuntimeException(degree + " is un unkonwn degree");
        }
        final List<Integer> scaleSemitoneIntervals = new ArrayList<>(MAJOR_SCALE_SEMITONE_INTERVALS);
        Collections.rotate(scaleSemitoneIntervals, rotationNumber);

        int pitch = Note.getPitch(rootName);
        final List<Integer> pitchesInScale = new ArrayList<>();
        for (final int interval : scaleSemitoneIntervals) {
            pitchesInScale.add(pitch);
            pitch += interval;
        }
        final List<MidiNote> chord = new ArrayList<>();
        for (int i = 0; i < chordSize; i++) {
            chord.add(new MidiNote(pitchesInScale.get((degreeIndex + 2 * i) % 7)));
        }
        return chord;
    }

    public static List<MidiNote> resolve(final String chord, final int octave) {
        final Matcher m = CHORD_NAME_PATTERN.matcher(chord);
        if (!m.matches()) {
            throw new RuntimeException(chord + " is not a valid chord");
        }

        // First resolve the pitch. This is the root of the chord.
        final String note = m.group(1);
        final int rootPitch = Note.getPitch(note + octave);

        final String name = m.group(2);
        if (name == null) {
            // By default a chord is a major chord, so if it is missing a name then it is assumed to mean `maj`.
            return majorChord(rootPitch);
        }

        switch (name) {
            case "maj":     return majorChord(rootPitch);
            case "m":
            case "min":     return minorChord(rootPitch);
            case "aug":     return augmentedChord(rootPitch);
            case "dim":     return diminishedChord(rootPitch);
            case "dim7":    return diminished7thChord(rootPitch);
            case "maj7b5":  return majorSeventhFlatFiveChord(rootPitch);
            case "m7":
            case "min7":    return minorSeventhChord(rootPitch);
            case "minmaj7": return minorMajorSeventhChord(rootPitch);
            case "7":
            case "dom7":    return dominantSeventhChord(rootPitch);
            case "maj7":    return majorSeventhChord(rootPitch);
            case "aug7":    return augmentedSeventhChord(rootPitch);
            case "maj7s5":  return majorSeventhSharpFiveChord(rootPitch);
            case "6":
            case "maj6":    return majorSixthChord(rootPitch);
            case "min6":    return minorSixthChord(rootPitch);
            case "sus2":    return suspendedTwoChord(rootPitch);
            case "sus4":    return suspendedFourthChord(rootPitch);
        }
        throw new RuntimeException(chord + " is not a valid chord");
    }

    //Triads
    //------
    //
    //Three note chords. These consists of the root, a third and a fifth.
    //The third and fifth intervals are shifted slightly to vary the chord's sound.

    //**Major**: Cmaj or C (root, major 3rd, perfect 5th)
    private static List<MidiNote> majorChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)));
    }

    //**Minor**: Cmin (root, minor 3rd, perfect 5th)
    private static List<MidiNote> minorChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(minorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)));
    }

    //**Augmented**: Caug (root, major 3rd, augmented 5th)
    private static List<MidiNote> augmentedChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorThird(rootPitch)), new MidiNote(augmentedFifth(rootPitch)));
    }

    //**Diminished**: Cdim (root, minor 3rd, diminished 5th)
    private static List<MidiNote> diminishedChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(minorThird(rootPitch)), new MidiNote(diminishedFifth(rootPitch)));
    }

    //**Suspended 2**: Csus2 (root, major 2nd, perfect 5th)
    private static List<MidiNote> suspendedTwoChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorSecond(rootPitch)), new MidiNote(perfectFifth(rootPitch)));
    }

    //**Suspended 4**: Csus4 (root, perfect 4th, perfect 5th)
    private static List<MidiNote> suspendedFourthChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(perfectFourth(rootPitch)), new MidiNote(perfectFifth(rootPitch)));
    }

    //Sixths
    //------
    //
    //Triads with an added fourth note that is a sixth interval
    //above the root.

    //**Major Sixth**: Cmaj6 (root, major 3rd, perfect 5th, major 6th)
    private static List<MidiNote> majorSixthChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)), new MidiNote(majorSixth(rootPitch)));
    }

    //**Minor Sixth**: Cmin6 (root, minor 3rd, perfect 5th, major 6th)
    private static List<MidiNote> minorSixthChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(minorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)), new MidiNote(majorSixth(rootPitch)));
    }

    //Sevenths
    //--------
    //
    //Triads with an added fourth note that is a seventh interval
    //above the root.

    //**Diminished Seventh**: Cdim7 (root, minor 3rd, diminished 5th, diminished 7th)
    private static List<MidiNote> diminished7thChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(minorThird(rootPitch)), new MidiNote(diminishedFifth(rootPitch)), new MidiNote(diminishedSeventh(rootPitch)));
    }

    //**Major Seventh Flat Five**: Cmaj7b5 (root, minor 3rd, diminished 5th, minor 7th)
    private static List<MidiNote> majorSeventhFlatFiveChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(minorThird(rootPitch)), new MidiNote(diminishedFifth(rootPitch)), new MidiNote(minorSeventh(rootPitch)));
    }

    //**Minor Seventh**: Cmin7 (root, minor 3rd, perfect 5th, minor 7th)
    private static List<MidiNote> minorSeventhChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(minorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)), new MidiNote(minorSeventh(rootPitch)));
    }

    //**Minor Major Seventh**: Cminmaj7 (root, minor 3rd, perfect 5th, major 7th)
    private static List<MidiNote> minorMajorSeventhChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(minorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)), new MidiNote(majorSeventh(rootPitch)));
    }

    //**Dominant Seventh**: Cdom7 (root, major 3rd, perfect 5th, minor 7th)
    private static List<MidiNote> dominantSeventhChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)), new MidiNote(minorSeventh(rootPitch)));
    }

    //**Major Seventh**: Cmaj7 (root, major 3rd, perfect 5th, major 7th)
    private static List<MidiNote> majorSeventhChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorThird(rootPitch)), new MidiNote(perfectFifth(rootPitch)), new MidiNote(majorSeventh(rootPitch)));
    }

    //**Augmented Seventh**: Caug7 (root, major 3rd, augmented 5th, minor 7th)
    private static List<MidiNote> augmentedSeventhChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorThird(rootPitch)), new MidiNote(augmentedFifth(rootPitch)), new MidiNote(minorSeventh(rootPitch)));
    }

    //**Augmented Major Seventh**: Cmaj7s5 (root, major 3rd, augmented 5th, major 7th)
    private static List<MidiNote> majorSeventhSharpFiveChord(final int rootPitch) {
        return Arrays.asList(new MidiNote(rootPitch), new MidiNote(majorThird(rootPitch)), new MidiNote(augmentedFifth(rootPitch)), new MidiNote(majorSeventh(rootPitch)));
    }

    // Intervals
    private static int majorSecond(final int pitch) {
        return pitch + 2;
    }

    private static int majorThird(final int pitch) {
        return pitch + 4;
    }

    private static int minorThird(final int pitch) {
        return pitch + 3;
    }

    private static int perfectFourth(final int pitch) {
        return pitch + 5;
    }

    private static int perfectFifth(final int pitch) {
        return pitch + 7;
    }

    private static int augmentedFifth(final int pitch) {
        return pitch + 8;
    }

    private static int diminishedFifth(final int pitch) {
        return pitch + 6;
    }

    private static int diminishedSeventh(final int pitch) {
        return pitch + 9;
    }

    private static int majorSixth(final int pitch) {
        return pitch + 9;
    }

    private static int minorSeventh(final int pitch) {
        return pitch + 10;
    }

    private static int majorSeventh(final int pitch) {
        return pitch + 11;
    }
}
