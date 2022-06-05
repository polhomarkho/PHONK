package io.phonk.runner.apprunner.api.widgets.midi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import io.phonk.runner.apidoc.annotation.PhonkClass;
import io.phonk.runner.apidoc.annotation.PhonkMethod;
import io.phonk.runner.apprunner.api.ProtoBase;
import io.phonk.runner.apprunner.api.media.midi.PMidiController;

@RequiresApi(api = Build.VERSION_CODES.M)
@PhonkClass
public class MidiUiFactory extends ProtoBase {

    final PMidiController pMidiController;

    public MidiUiFactory(final PMidiController pMidiController) {
        super(pMidiController.getAppRunner());
        this.pMidiController = pMidiController;
    }

    @PhonkMethod()
    public NoteButton addNoteButton(final Object x, final Object y, final Object w, final Object h) {
        return new NoteButton(pMidiController, x, y, w, h);
    }

    @PhonkMethod()
    public PitchBendSlider addPitchBendSlider(final Object x, final Object y, final Object w, final Object h) {
        return new PitchBendSlider(pMidiController, x, y, w, h);
    }

    @PhonkMethod()
    public CcSlider addCcSlider(final Object x, final Object y, final Object w, final Object h, final int cc) {
        return new CcSlider(pMidiController, x, y, w, h, cc);
    }

    @PhonkMethod()
    public CcSlider addModulationWheelSlider(final Object x, final Object y, final Object w, final Object h) {
        return new ModulationWheelSlider(pMidiController, x, y, w, h);
    }

    @Override
    public void __stop() {

    }
}

