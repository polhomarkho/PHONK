package io.phonk.runner.apprunner.api.widgets.midi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.phonk.runner.apidoc.annotation.PhonkClass;
import io.phonk.runner.apidoc.annotation.PhonkMethod;
import io.phonk.runner.apprunner.api.common.ReturnObject;
import io.phonk.runner.apprunner.api.media.midi.ChordParser;
import io.phonk.runner.apprunner.api.media.midi.MidiNote;
import io.phonk.runner.apprunner.api.media.midi.PMidiController;
import io.phonk.runner.apprunner.api.widgets.PButton;

@RequiresApi(api = Build.VERSION_CODES.M)
@PhonkClass
public class NoteButton extends AbstractMidiWidget {

    private static final String GREEN_COLOR = "#00FF00";
    private static final String GREY_COLOR = "#474747";

    private final PButton pButton;
    private final PMidiController pMidiController;
    private final List<MidiNote> midiNotes = new ArrayList<>();

    public NoteButton(final PMidiController pMidiController,
                      final Object x,
                      final Object y,
                      final Object w,
                      final Object h) {
        super(pMidiController.getAppRunner());
        this.pMidiController = pMidiController;
        pButton = getAppRunner().pUi
                .addButton("", x, y, w, h)
                .onPress(this::playNotes)
                .onRelease(this::stopPlayingNotes);
        final Map<String, String> props = new HashMap<>();
        props.put("background", GREY_COLOR);
        props.put("backgroundPressed", GREEN_COLOR);
        pButton.setProps(props);
    }

    @PhonkMethod()
    public NoteButton withNotePitches(final int... notePitches) {
        midiNotes.clear();
        for (final int notePitch : notePitches) {
            midiNotes.add(new MidiNote(notePitch));
        }
        return this;
    }

    @PhonkMethod()
    public NoteButton withNoteNames(final String... noteNames) {
        midiNotes.clear();
        for (final String noteName : noteNames) {
            addNoteName(noteName);
        }
        return this;
    }

    @PhonkMethod()
    public NoteButton addNoteName(final String noteName) {
        midiNotes.add(new MidiNote(noteName));
        return this;
    }

    @SafeVarargs
    @PhonkMethod()
    public final NoteButton withNotes(final Map<String, Object>... notes) {
        midiNotes.clear();
        for (final Map<String, Object> note : notes) {
            addNote(note);
        }
        return this;
    }

    @PhonkMethod()
    public NoteButton addNote(final Map<String, Object> note) {
        final MidiNote midiNote = new MidiNote(
                numberToInt(note.get("channel")),
                (String) note.get("name"),
                numberToInt(note.get("velocity"), PMidiController.MAX_VELOCITY_VALUE));
        midiNotes.add(midiNote);
        return this;
    }

    @PhonkMethod()
    public NoteButton withChord(final Map<String, Object> chord) {
        midiNotes.clear();
        final int octave = numberToInt(chord.get("octave"));
        final int channel = numberToInt(chord.get("channel"));
        final int velocity = numberToInt(chord.get("velocity"), PMidiController.MAX_VELOCITY_VALUE);
        final String name = (String) chord.get("name");
        if (pButton.getText() == "") {
            withText(name);
        }
        for (final MidiNote midiNote : ChordParser.resolve(name, octave)) {
            midiNotes.add(new MidiNote(channel, midiNote.getPitch(), velocity));
        }
        return this;
    }

    public NoteButton withDegreeAndMode(final Map<String, Object> degreeAndMode) {
        midiNotes.clear();
        final String rootName = (String) degreeAndMode.get("rootName");
        final String degree = (String) degreeAndMode.get("degree");
        final String mode = (String) degreeAndMode.get("mode");
        final int chordSize = numberToInt(degreeAndMode.get("chordSize"), 3);
        final int channel = numberToInt(degreeAndMode.get("channel"));
        final int velocity = numberToInt(degreeAndMode.get("velocity"), PMidiController.MAX_VELOCITY_VALUE);

        if (pButton.getText() == "") {
            withText(degree + " (" + rootName + " " + mode + ")");
        }
        for (final MidiNote midiNote : ChordParser.findChord(rootName, degree, mode, chordSize)) {
            midiNotes.add(new MidiNote(channel, midiNote.getPitch(), velocity));
        }
        return this;
    }

    @PhonkMethod()
    public NoteButton withText(final String text) {
        pButton.setProps(Collections.singletonMap("text", text));
        return this;
    }

    @PhonkMethod()
    public NoteButton withBackgroundColorWhenPlayed(final String backgroundColorWhenPlayed) {
        pButton.setProps(Collections.singletonMap("backgroundPressed", backgroundColorWhenPlayed));
        return this;
    }

    @PhonkMethod()
    public NoteButton withBackgroundColor(final String backgroundColor) {
        pButton.setProps(Collections.singletonMap("background", backgroundColor));
        return this;
    }

    @PhonkMethod()
    public PButton button() {
        return pButton;
    }

    private void playNotes(final ReturnObject r) {
        for (final MidiNote midiNote : midiNotes) {
            pMidiController.player.playNote(midiNote);
        }
    }

    private void stopPlayingNotes(final ReturnObject r) {
        for (final MidiNote midiNote : midiNotes) {
            pMidiController.player.stopPlayingNote(midiNote);
        }
    }

    @Override
    public void __stop() {

    }
}
